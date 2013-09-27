/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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

package fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.query.EventProxy;
import fr.inria.soctrace.tools.ocelotl.core.query.OcelotlEventCache;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.query.ReducedEvent;
import fr.inria.soctrace.tools.ocelotl.core.ts.IState;
import fr.inria.soctrace.tools.ocelotl.core.ts.PajeState;
import fr.inria.soctrace.tools.ocelotl.core.ts.State;

public class ActivityTimeMatrix extends TimeSliceMatrix {

	int	count	= 0;

	public ActivityTimeMatrix(final Query query) throws SoCTraceException {
		super(query);
		System.out.println("Activity Time Matrix");
	}

	protected void computeSubMatrixNonCached(final List<EventProducer> eventProducers) throws SoCTraceException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		final List<ReducedEvent> fullEvents = query.getEvents(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<ReducedEvent>> eventList = new HashMap<Integer, List<ReducedEvent>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<ReducedEvent>());
		for (final ReducedEvent e : fullEvents)
			eventList.get(e.EP).add(e);
		for (final EventProducer ep : eventProducers) {
			final List<ReducedEvent> events = eventList.get(ep.getId());
			IState state;
			for (int i = 0; i < events.size() - 1; i++) {
				state = (new PajeState(events.get(i), events.get(i + 1), timeSliceManager));
				if (!query.getOcelotlParameters().getSleepingStates().contains(state.getStateType())) {
					final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
					for (final long it : distrib.keySet())
						matrix.get((int) it).put(ep.getName(), matrix.get((int) it).get(ep.getName()) + distrib.get(it));
				}
			}
		}
		dm.end("VECTORS COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	protected void computeSubMatrixCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		final List<EventProxy> fullEvents = query.getEventsProxy(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		final Map<Integer, List<EventProxy>> eventList = new HashMap<Integer, List<EventProxy>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<EventProxy>());
		for (final EventProxy e : fullEvents)
			eventList.get(e.EP).add(e);
		List<OcelotlCachedThread> threadlist = new ArrayList<OcelotlCachedThread>();
		for (int t = 0; t < 8; t++) {
			threadlist.add(new OcelotlCachedThread(eventProducers, eventList, 8, t));
		}
		for (Thread thread : threadlist)
			thread.join();

		dm.end("VECTORS COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	@Deprecated
	protected void computeSubMatrixCachedOld(final List<EventProducer> eventProducers) throws SoCTraceException {
		int count = 0;
		DeltaManager dm = new DeltaManager();
		dm.start();
		OcelotlEventCache cache = new OcelotlEventCache(query.getOcelotlParameters());
		final List<EventProxy> fullEvents = query.getEventsProxy(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		DeltaManager dm2 = new DeltaManager();
		final Map<Integer, List<EventProxy>> eventList = new HashMap<Integer, List<EventProxy>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<EventProxy>());
		for (final EventProxy e : fullEvents)
			eventList.get(e.EP).add(e);
		for (final EventProducer ep : eventProducers) {
			dm2.start();
			IState state;
			final List<EventProxy> events = eventList.get(ep.getId());
			for (int i = 0; i < events.size() - 1; i++) {
				state = (new PajeState(cache.getEventMultiPageEPCache(events.get(i)), cache.getEventMultiPageEPCache(events.get(i + 1)), timeSliceManager));
				if (!query.getOcelotlParameters().getSleepingStates().contains(state.getStateType())) {
					final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
					for (final long it : distrib.keySet())
						matrix.get((int) it).put(ep.getName(), matrix.get((int) it).get(ep.getName()) + distrib.get(it));
				}
			}
			dm2.end("EP VECTORS (" + count++ + "): " + ep.getName());
		}
		dm.end("VECTORS COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	protected void computeSubMatrix(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (query.getOcelotlParameters().isCache()) {
			try {
				computeSubMatrixCached(eventProducers);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			computeSubMatrixNonCached(eventProducers);
		}
	}

	synchronized void matrixWrite(long it, EventProducer ep, Map<Long, Long> distrib) {
		matrix.get((int) it).put(ep.getName(), matrix.get((int) it).get(ep.getName()) + distrib.get(it));
	}

	synchronized int getCount() {
		count++;
		return count - 1;
	}

	class OcelotlCachedThread extends Thread {

		List<EventProducer>				eventProducers;
		Map<Integer, List<EventProxy>>	eventList;
		int								threadNumber;
		int								thread;

		@Override
		public void run() {
			for (int t = thread; t < eventProducers.size(); t = t + threadNumber) {
				DeltaManager dm2 = new DeltaManager();
				dm2.start();
				EventProducer ep = eventProducers.get(t);
				OcelotlEventCache cache;
				try {
					cache = new OcelotlEventCache(query.getOcelotlParameters());

					IState state;
					final List<EventProxy> events = eventList.get(ep.getId());
					for (int i = 0; i < events.size() - 1; i++) {
						state = (new PajeState(cache.getEventMultiPageEPCache(events.get(i)), cache.getEventMultiPageEPCache(events.get(i + 1)), timeSliceManager));
						if (!query.getOcelotlParameters().getSleepingStates().contains(state.getStateType())) {
							final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
							for (final long it : distrib.keySet())
								matrixWrite(it, ep, distrib);
						}
					}
				} catch (SoCTraceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dm2.end("THREAD " + thread + " - EP VECTORS (" + t + "): " + ep.getName());
			}

		}

		public OcelotlCachedThread(List<EventProducer> eventProducers, Map<Integer, List<EventProxy>> eventList, int threadNumber, int thread) {
			super();
			this.eventProducers = eventProducers;
			this.eventList = eventList;
			this.threadNumber = threadNumber;
			this.thread = thread;
			start();
		}

	}

	class OcelotlThread extends Thread {

		List<EventProducer>				eventProducers;
		Map<Integer, List<EventProxy>>	eventList;
		int								threadNumber;
		int								thread;

		@Override
		public void run() {
			for (int t = thread; t < eventProducers.size(); t = t + threadNumber) {
				DeltaManager dm2 = new DeltaManager();
				dm2.start();
				EventProducer ep = eventProducers.get(t);
				OcelotlEventCache cache;
				try {
					cache = new OcelotlEventCache(query.getOcelotlParameters());

					IState state;
					final List<EventProxy> events = eventList.get(ep.getId());
					for (int i = 0; i < events.size() - 1; i++) {
						state = (new PajeState(cache.getEventMultiPageEPCache(events.get(i)), cache.getEventMultiPageEPCache(events.get(i + 1)), timeSliceManager));
						if (!query.getOcelotlParameters().getSleepingStates().contains(state.getStateType())) {
							final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
							for (final long it : distrib.keySet())
								matrixWrite(it, ep, distrib);
						}
					}
				} catch (SoCTraceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dm2.end("THREAD " + thread + " - EP VECTORS (" + t + "): " + ep.getName());
			}

		}

		public OcelotlThread(List<EventProducer> eventProducers, Map<Integer, List<EventProxy>> eventList, int threadNumber, int thread) {
			super();
			this.eventProducers = eventProducers;
			this.eventList = eventList;
			this.threadNumber = threadNumber;
			this.thread = thread;
			start();
		}

	}
}
