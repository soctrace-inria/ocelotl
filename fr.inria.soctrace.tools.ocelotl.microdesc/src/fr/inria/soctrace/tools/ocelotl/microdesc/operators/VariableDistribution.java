/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
 * overview by using aggregation techniques
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.microdesc.operators;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.events.IVariable;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.Microscopic3DDescription;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceVariableManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;
import fr.inria.soctrace.tools.ocelotl.microdesc.genericevents.GenericVariable;

public class VariableDistribution extends Microscopic3DDescription {

	private static final Logger logger = LoggerFactory
			.getLogger(VariableDistribution.class);

	class OcelotlThread extends Thread {

		List<EventProducer> localActiveEventProducers;
		int threadNumber;
		int thread;
		int size;
		IProgressMonitor monitor;

		public OcelotlThread(final int threadNumber, final int thread,
				final int size, IProgressMonitor monitor) {
			super();
			this.threadNumber = threadNumber;
			this.thread = thread;
			this.size = size;
			this.monitor = monitor;
			localActiveEventProducers = new ArrayList<EventProducer>();

			start();
		}

		private void matrixUpdate(final IVariable variable,
				final EventProducer ep, final Map<Long, Double> distrib) {
			// Mutex
			synchronized (getMatrix()) {
				if (!getMatrix().get(0).get(ep).containsKey(variable.getType())) {
					logger.debug("Adding " + variable.getType() + " variable");

					for (int incr = 0; incr < getMatrix().size(); incr++)
						for (final EventProducer epset : getMatrix().get(incr)
								.keySet())
							matrixPushType(incr, epset, variable.getType());
				}
				for (final long it : distrib.keySet())
					matrixWrite(it, ep, variable.getType(), distrib);
			}
		}

		@Override
		public void run() {
			EventProducer currentEP = null;
			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (events.size() == 0)
					break;
				if (monitor.isCanceled())
					return;

				IVariable variable;
				for (final Event event : events) {
					variable = new GenericVariable(event,
							(TimeSliceVariableManager) timeSliceManager);
					final Map<Long, Double> distrib = variable
							.getTimeSlicesDistribution();
					EventProducer eventEP = event.getEventProducer();
					
					if(aggregatedProducers.containsKey(event.getEventProducer()))
						eventEP = aggregatedProducers.get(event.getEventProducer());
					
					matrixUpdate(variable, eventEP, distrib);

					if (currentEP != eventEP) {
						currentEP = eventEP;
						// If the event producer is not in the active producers
						// list
						if (!localActiveEventProducers.contains(eventEP)) {
							// Add it
							localActiveEventProducers.add(eventEP);
						}
					}
					if (monitor.isCanceled())
						return;
				}
				monitor.worked(events.size());
			}
			// Merge local active event producers to the global one
			synchronized (activeProducers) {
				for (EventProducer ep : localActiveEventProducers) {
					if (!activeProducers.contains(ep))
						activeProducers.add(ep);
				}
			}
		}
	}

	public VariableDistribution() {
		super();
	}

	@Override
	public void computeSubMatrix(List<EventProducer> eventProducers,
			List<IntervalDesc> time, IProgressMonitor monitor)
			throws SoCTraceException, InterruptedException, OcelotlException {
		dm = new DeltaManagerOcelotl();
		dm.start();
		monitor.subTask("Querying Database...");
		eventIterator = ocelotlQueries.getVariableIterator(eventProducers,
				time, monitor);
		if (monitor.isCanceled()) {
			ocelotlQueries.closeIterator();
			return;
		}

		setTimeSliceManager(new TimeSliceVariableManager(getOcelotlParameters()
				.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber()));
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		monitor.subTask("Loading Data From Database...");
		for (int t = 0; t < getOcelotlParameters().getNumberOfThreads(); t++)
			threadlist.add(new OcelotlThread(getOcelotlParameters()
					.getNumberOfThreads(), t, getOcelotlParameters()
					.getEventsPerThread(), monitor));
		for (final Thread thread : threadlist)
			thread.join();
		ocelotlQueries.closeIterator();
		dm.end("VECTORS COMPUTATION: "
				+ getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	@Override
	public void rebuildMatrix(String[] values, EventProducer ep,
			int sliceMultiple) {

		String evType = values[2];

		// If the event type is filtered out
		if (!typeNames.contains(evType))
			return;
		
		EventProducer eventEP = ep;
		
		if(aggregatedProducers.containsKey(ep))
			eventEP = aggregatedProducers.get(ep);

		// If the event producer is flag as inactive
		if (!getActiveProducers().contains(eventEP)) {
			// Remove it
			getActiveProducers().add(eventEP);
		}
		
		int slice = Integer.parseInt(values[0]);
		double value = Double.parseDouble(values[3]);

		// If the number of time slice is a multiple of the cached time
		// slice number
		if (sliceMultiple > 1) {
			// Compute the correct slice number
			slice = slice / sliceMultiple;

			// And add the value to the one already in the matrix
			if (matrix.get(slice).get(eventEP).get(evType) != null)
				value = matrix.get(slice).get(eventEP).get(evType) + (value / sliceMultiple);
		}

		matrix.get(slice).get(eventEP).put(evType, value);
	}
	
	@Override
	public void rebuildMatrixFromDirtyCache(String[] values, EventProducer ep,
			int slice, double factor) {

		String evType = values[2];

		// If the event type is filtered out
		if (!typeNames.contains(evType))
			return;
		
		EventProducer eventEP = ep;
		
		if(aggregatedProducers.containsKey(ep))
			eventEP = aggregatedProducers.get(ep);
		
		// If the event producer is flag as inactive
		if (!getActiveProducers().contains(eventEP)) {
			// Remove it
			getActiveProducers().add(eventEP);
		}

		// Compute a value proportional to the time ratio spent in the slice
		double value = Double.parseDouble(values[3]) * factor;

		// Add the value to the one potentially already in the matrix
		if (matrix.get(slice).get(eventEP).get(evType) != null)
			value = matrix.get(slice).get(eventEP).get(evType) + value / parameters.getTimeSliceFactor();

		matrix.get(slice).get(eventEP).put(evType, value);
	}
	
	@Override
	public void rebuildDirty(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		buildNormalMatrix(monitor);
	}

}
