/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.ITimeAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop._2DSpaceTimeMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceStateManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public class EventDistributionSpaceTime extends _2DSpaceTimeMicroDescription {

	private static final Logger logger = LoggerFactory.getLogger(EventDistributionSpaceTime.class);
	
	class OcelotlThread extends Thread {

		List<EventProducer> eventProducers;
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
			
			start();
		}

		private void matrixWrite(final long slice, final EventProducer ep,
				String type) {
			synchronized (matrix) {
				matrix.get((int) slice)
						.get(ep)
						.put(type,
								matrix.get((int) slice).get(ep).get(type) + 1);
			}
		}

		private void matrixUpdate(final Event event, final EventProducer ep) {
			synchronized (matrix) {
				// If the event type is not in the matrix yet
				if (!matrix.get(0).get(ep)
						.containsKey(event.getType().getName())) {
					logger.debug("Adding " + event.getType().getName()
							+ " event");
					
					// Add the type for each slice and ep and init to zero
					for (int incr = 0; incr < matrix.size(); incr++)
						for (final EventProducer epset : matrix.get(incr)
								.keySet())
							matrixPushType(incr, epset, event.getType()
									.getName());
				}
				
				// Get the time slice number of the event
				final long slice = timeSliceManager.getTimeSlice(event
						.getTimestamp());
				matrixWrite(slice, ep, event.getType().getName());
			}
		}

		@Override
		public void run() {
			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (events.size() == 0)
					break;
				if (monitor.isCanceled())
					return;
				
				for (final Event event : events) {
					// final Map<Long, Long> distrib =
					// state.getTimeSlicesDistribution();
					matrixUpdate(event, event.getEventProducer());
					if (monitor.isCanceled())
						return;
				}
			}
		}
	}

	private TimeSliceStateManager timeSliceManager;

	public EventDistributionSpaceTime() throws SoCTraceException {
		super();
	}

	public EventDistributionSpaceTime(final OcelotlParameters parameters, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		super(parameters, monitor);
	}

	@Override
	protected void computeSubMatrix(final List<EventProducer> eventProducers,
			List<IntervalDesc> time, IProgressMonitor monitor)
			throws SoCTraceException, InterruptedException, OcelotlException {
		dm = new DeltaManagerOcelotl();
		dm.start();
		monitor.subTask("Query events");
		eventIterator = ocelotlQueries.getEventIterator(eventProducers, time,
				monitor);
		if (monitor.isCanceled()) {
			ocelotlQueries.closeIterator();
			return;
		}

		timeSliceManager = new TimeSliceStateManager(getOcelotlParameters()
				.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		monitor.subTask("Fill the matrix");
		for (int t = 0; t < getOcelotlParameters().getThreadNumber(); t++)
			threadlist.add(new OcelotlThread(getOcelotlParameters()
					.getThreadNumber(), t, getOcelotlParameters()
					.getEventsPerThread(), monitor));
		for (final Thread thread : threadlist)
			thread.join();
		ocelotlQueries.closeIterator();
		dm.end("VECTORS COMPUTATION: "
				+ getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}
	
	@Override
	public ITimeAggregationOperator copy() {
		EventDistributionSpaceTime aNewDist = null;
		try {
			aNewDist = new EventDistributionSpaceTime();

			aNewDist.parameters = new OcelotlParameters(this.parameters);
			aNewDist.matrix = new ArrayList<HashMap<EventProducer, HashMap<String, Double>>>();
			int i;

			for (i = 0; i < matrix.size(); i++) {
				aNewDist.matrix
						.add(new HashMap<EventProducer, HashMap<String, Double>>());
				for (EventProducer ep : parameters.getAllEventProducers())
					aNewDist.matrix.get((int) i).put(ep,
							new HashMap<String, Double>());
			}

			for (i = 0; i < matrix.size(); i++) {
				for (EventProducer anEP : parameters.getAllEventProducers()) {
					for (String state : matrix.get(i).get(anEP).keySet())
						aNewDist.matrix.get(i).get(anEP)
								.put(state, matrix.get(i).get(anEP).get(state));
				}
			}
		} catch (SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return aNewDist;
	}
}
