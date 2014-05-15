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

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop._3DMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.IteratorQueries.EventIterator;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;
import fr.inria.soctrace.tools.ocelotl.microdesc.config.DistributionConfig;

public class EventDistribution extends _3DMicroDescription {

	class OcelotlThread extends Thread {

		List<EventProducer>	eventProducers;
		int					threadNumber;
		int					thread;
		int					size;

		public OcelotlThread(final int threadNumber, final int thread, final int size) {
			super();
			this.threadNumber = threadNumber;
			this.thread = thread;
			this.size = size;

			start();
		}
		
		private void matrixWrite(final long slice, final EventProducer ep, String type) {
			synchronized (matrix) {
				matrix.get((int) slice).get(ep).put(type, matrix.get((int) slice).get(ep).get(type) + 1);
			}

		}

		private void matrixUpdate(final Event event, final EventProducer ep) {
			synchronized (matrix) {
				if (!matrix.get(0).get(ep).containsKey(event.getType().getName())) {
					System.out.println("Adding " + event.getType().getName() + " state");
					// addKey(state.getStateType());
					for (int incr = 0; incr < matrix.size(); incr++)
						for (final EventProducer epset : matrix.get(incr).keySet())
							matrixPushType(incr, epset, event.getType().getName());
				}
				final long slice = timeSliceManager.getTimeSlice(event.getTimestamp());
					matrixWrite(slice, ep, event.getType().getName());
			}
		}

		@Override
		public void run() {
			while (true) {
				final List<Event> events = getEvents(size);
				if (events.size() == 0)
					break;
				for (final Event event : events) {
					//final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
					matrixUpdate(event, event.getEventProducer());
				}
			}
		}
	}

	EventIterator	it;

	public EventDistribution() throws SoCTraceException {
		super();
	}

	public EventDistribution(final OcelotlParameters parameters) throws SoCTraceException {
		super(parameters);
	}

	@Override
	protected void computeSubMatrix(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		dm = new DeltaManager();
		dm.start();
		it = ocelotlQueries.getEventIterator(eventProducers);
		// eventsNumber = fullEvents.size();
		// dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " +
		// fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < ((DistributionConfig) getOcelotlParameters().getTraceTypeConfig()).getThreadNumber(); t++)
			threadlist.add(new OcelotlThread(((DistributionConfig) getOcelotlParameters().getTraceTypeConfig()).getThreadNumber(), t, ((DistributionConfig) getOcelotlParameters().getTraceTypeConfig()).getEventsPerThread()));
		for (final Thread thread : threadlist)
			thread.join();
		ocelotlQueries.closeIterator();
		dm.end("VECTORS COMPUTATION : " + getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	private List<Event> getEvents(final int size) {
		final List<Event> events = new ArrayList<Event>();
		synchronized (it) {
			for (int i = 0; i < size; i++) {
				if (it.getNext() == null)
					return events;
				events.add(it.getEvent());
				eventsNumber++;
			}
		}
		return events;
	}

	@Override
	public void initQueries() {
		try {
			ocelotlQueries = new OcelotlQueries(parameters);
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setOcelotlParameters(final OcelotlParameters parameters) throws SoCTraceException, InterruptedException {
		this.parameters = parameters;
		ocelotlQueries = new OcelotlQueries(parameters);
		count = 0;
		epit = 0;
		timeSliceManager = new TimeSliceManager(getOcelotlParameters().getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		initVectors();
		computeMatrix();
	}

}
