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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.micromodel.Microscopic3DModel;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceStateManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public class EventDistribution extends Microscopic3DModel {

	private static final Logger logger = LoggerFactory.getLogger(EventDistribution.class);
	private TimeSliceStateManager timeSliceManager;
	
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
			synchronized (getMatrix()) {
				getMatrix().get((int) slice)
						.get(ep)
						.put(type,
								getMatrix().get((int) slice).get(ep).get(type) + 1);
			}
		}

		private void matrixUpdate(final Event event, final EventProducer ep) {
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
				final long slice = timeSliceManager.getTimeSlice(event
						.getTimestamp());
				matrixWrite(slice, ep, event.getType().getName());
			}
		}

		@Override
		public void run() {
			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (monitor.isCanceled())
					return;

				if (events.size() == 0)
					break;
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

	public EventDistribution() throws SoCTraceException, OcelotlException {
		super();
	}

	@Override
	public void computeSubMatrix(final List<EventProducer> eventProducers,
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

}
