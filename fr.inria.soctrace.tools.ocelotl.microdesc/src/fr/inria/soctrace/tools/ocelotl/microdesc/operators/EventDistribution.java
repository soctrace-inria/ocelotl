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
import java.io.IOException;
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
import fr.inria.soctrace.tools.ocelotl.core.microdesc.Microscopic3DDescription;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public class EventDistribution extends Microscopic3DDescription {

	private static final Logger logger = LoggerFactory.getLogger(EventDistribution.class);
	
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

		private void matrixWrite(final long slice, final EventProducer ep,
				String type) {
			synchronized (getMatrix()) {
				getMatrix().get((int) slice)
						.get(ep)
						.put(type,
								getMatrix().get((int) slice).get(ep).get(type) + 1);
			}
		}

		private void matrixUpdate(final Event event, final EventProducer ep) {
			// Mutex
			synchronized (getMatrix()) {
				// If the event type is not in the matrix yet
				if (!getMatrix().get(0).get(ep)
						.containsKey(event.getType().getName())) {
					logger.debug("Adding " + event.getType().getName()
							+ " event");
					
					// Add the type for each slice and ep and init to zero
					for (int incr = 0; incr < getMatrix().size(); incr++)
						for (final EventProducer epset : getMatrix().get(incr)
								.keySet())
							matrixPushType(incr, epset, event.getType()
									.getName());
				}
				
				// Get the time slice number of the event
				final long slice = getTimeSliceManager().getTimeSlice(event
						.getTimestamp());
				matrixWrite(slice, ep, event.getType().getName());
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

				for (final Event event : events) {
					// final Map<Long, Long> distrib =
					// state.getTimeSlicesDistribution();
					EventProducer eventEP = event.getEventProducer();
					
					if(aggregatedProducers.containsKey(event.getEventProducer()))
						eventEP = aggregatedProducers.get(event.getEventProducer());
					
					matrixUpdate(event, eventEP);
					
					if (currentEP != eventEP) {
						currentEP = eventEP;
						// If the event producer is not in the active producers list
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

	public EventDistribution() throws SoCTraceException, OcelotlException {
		super();
	}

	@Override
	public void computeSubMatrix(final List<EventProducer> eventProducers,
			List<IntervalDesc> time, IProgressMonitor monitor)
			throws SoCTraceException, InterruptedException, OcelotlException {
		dm = new DeltaManagerOcelotl();
		dm.start();
		monitorMessageDatabaseQuery(monitor);
		eventIterator = ocelotlQueries.getEventIterator(eventProducers, time,
				monitor);
		if (monitor.isCanceled()) {
			ocelotlQueries.closeIterator();
			return;
		}

		setTimeSliceManager(new TimeSliceManager(getOcelotlParameters()
				.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber()));
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		monitorMessageDatabaseReading(monitor);
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
	public void rebuildDirty(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException, SoCTraceException,
			InterruptedException, OcelotlException {
		buildNormalMatrix(monitor);
	}
	
}
