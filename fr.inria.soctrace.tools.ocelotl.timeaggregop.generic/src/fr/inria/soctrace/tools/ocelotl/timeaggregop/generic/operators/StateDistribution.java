/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
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

package fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop._3DCacheMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.queries.eventproxy.EventProxy;
import fr.inria.soctrace.tools.ocelotl.core.state.IState;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.queries.EventCache;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.state.GenericState;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.config.StateDistributionConfig;


public class StateDistribution extends _3DCacheMicroDescription {

	class OcelotlThread extends Thread {

		List<EventProducer>						eventProducers;
		Map<Integer, List<EventProxy>>			eventProxyList;
		Map<Integer, List<Event>>	eventList;
		int										threadNumber;
		int										thread;
		boolean									cached;

		@SuppressWarnings("unchecked")
		public OcelotlThread(final List<EventProducer> eventProducers, final Object eventList, final int threadNumber, final int thread, final boolean cached) {
			super();
			this.eventProducers = eventProducers;
			this.cached = cached;
			if (cached)
				eventProxyList = (Map<Integer, List<EventProxy>>) eventList;
			else
				this.eventList = (Map<Integer, List<Event>>) eventList;
			this.threadNumber = threadNumber;
			this.thread = thread;

			start();
		}

		private void cacheRun() throws SoCTraceException {
			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
				EventCache cache;
				cache = new EventCache(getOcelotlParameters());
				final EventProducer ep = eventProducers.get(t);
				IState state;
				final List<EventProxy> events = eventProxyList.get(ep.getId());
				for (int i = 0; i < events.size() - 1; i++) {
					state = new GenericState(cache.getEventMultiPageEPCache(events.get(i)), timeSliceManager);
					if (!((StateDistributionConfig) getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
						final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
						matrixUpdate(state, ep, distrib);
					}
				}
				cache.close();
				final int c = getCount();
				if (c % EPCOUNT == 0)
					total(c);
			}

		}

		private void matrixUpdate(final IState state, final EventProducer ep, final Map<Long, Long> distrib) {
			synchronized (matrix) {
				if (!matrix.get(0).get(ep.getName()).containsKey(state.getStateType())) {
					System.out.println("Adding " + state.getStateType() + " state");
					// addKey(state.getStateType());
					for (int incr = 0; incr < matrix.size(); incr++)
						for (final String epstring : matrix.get(incr).keySet())
							matrixPushType(incr, epstring, state.getStateType(), distrib);
				}
				for (final long it : distrib.keySet())
					matrixWrite(it, ep, state.getStateType(), distrib);
			}
		}

		private void noCacheRun() {
			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
				final EventProducer ep = eventProducers.get(t);
				final List<Event> events = eventList.get(ep.getId());
				IState state;
				for (int i = 0; i < events.size() - 1; i++) {
					state = new GenericState(events.get(i), timeSliceManager);
					if (!((StateDistributionConfig) getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
						final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
						matrixUpdate(state, ep, distrib);
					}
				}
				final int c = getCount();
				if (c % EPCOUNT == 0)
					total(c);
			}

		}

		@Override
		public void run() {
			if (cached)
				try {
					cacheRun();
				} catch (final SoCTraceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else
				noCacheRun();
		}
	}

	public StateDistribution() throws SoCTraceException {
		super();
	}

	public StateDistribution(final OcelotlParameters parameters) throws SoCTraceException {
		super(parameters);
	}

	@Override
	protected void computeSubMatrixCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		dm = new DeltaManager();
		dm.start();
		final List<EventProxy> fullEvents = ((OcelotlQueries) ocelotlQueries).getEventProxies(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<EventProxy>> eventList = new HashMap<Integer, List<EventProxy>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<EventProxy>());
		for (final EventProxy e : fullEvents)
			eventList.get(e.EP).add(e);
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < Math.min(getOcelotlParameters().getThread(), eventProducers.size()); t++)
			threadlist.add(new OcelotlThread(eventProducers, eventList, getOcelotlParameters().getThread(), t, true));
		for (final Thread thread : threadlist)
			thread.join();
		dm.end("VECTORS COMPUTATION : " + getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	@Override
	protected void computeSubMatrixNonCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		dm = new DeltaManager();
		dm.start();
		final List<Event> fullEvents = ((OcelotlQueries) ocelotlQueries).getEvents(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<Event>> eventList = new HashMap<Integer, List<Event>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<Event>());
		for (final Event e : fullEvents)
			eventList.get(e.getEventProducer().getName()).add(e);
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < Math.min(getOcelotlParameters().getThread(), eventProducers.size()); t++)
			threadlist.add(new OcelotlThread(eventProducers, eventList, getOcelotlParameters().getThread(), t, false));
		for (final Thread thread : threadlist)
			thread.join();
		dm.end("VECTORS COMPUTATION : " + getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	@Override
	public void initQueries() {
		try {
			ocelotlQueries = new OcelotlQueries(parameters);
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ocelotlQueries.checkTimeStamps();
	}

	@Override
	public void setOcelotlParameters(final OcelotlParameters parameters) throws SoCTraceException, InterruptedException {
		this.parameters = parameters;
		ocelotlQueries = new OcelotlQueries(parameters);
		ocelotlQueries.checkTimeStamps();
		count = 0;
		epit = 0;
		timeSliceManager = new TimeSliceManager(getOcelotlParameters().getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		initVectors();
		computeMatrix();
	}

}
