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

package fr.inria.soctrace.tools.ocelotl.core.paje.aggregop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop.Matrix;
import fr.inria.soctrace.tools.ocelotl.core.paje.config.PajeConfig;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeEventProxy;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeReducedEvent;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeReducedEventCache;
import fr.inria.soctrace.tools.ocelotl.core.paje.state.PajeState;
import fr.inria.soctrace.tools.ocelotl.core.ts.IState;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;

public class PajeStateSum extends Matrix {

	class OcelotlThread extends Thread {

		List<EventProducer>						eventProducers;
		Map<Integer, List<PajeEventProxy>>		eventProxyList;
		Map<Integer, List<PajeReducedEvent>>	eventList;
		int										threadNumber;
		int										thread;
		boolean									cached;

		@SuppressWarnings("unchecked")
		public OcelotlThread(final List<EventProducer> eventProducers, final Object eventList, final int threadNumber, final int thread, final boolean cached) {
			super();
			this.eventProducers = eventProducers;
			this.cached = cached;
			if (cached)
				eventProxyList = (Map<Integer, List<PajeEventProxy>>) eventList;
			else
				this.eventList = (Map<Integer, List<PajeReducedEvent>>) eventList;
			this.threadNumber = threadNumber;
			this.thread = thread;

			start();
		}

		private void cacheRun() throws SoCTraceException {
			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
				PajeReducedEventCache cache;
				cache = new PajeReducedEventCache(query.getOcelotlParameters());
				final EventProducer ep = eventProducers.get(t);
				IState state;
				final List<PajeEventProxy> events = eventProxyList.get(ep.getId());
				for (int i = 0; i < events.size() - 1; i++) {
					state = new PajeState(cache.getEventMultiPageEPCache(events.get(i)), cache.getEventMultiPageEPCache(events.get(i + 1)), timeSliceManager);
					if (!((PajeConfig) query.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
						final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
						for (final long it : distrib.keySet())
							matrixWrite(it, ep, distrib);
					}
				}
				cache.close();
				final int c = getCount();
				if (c % EPCOUNT == 0)
					total(c);
			}

		}

		private void noCacheRun() {
			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
				final EventProducer ep = eventProducers.get(t);
				final List<PajeReducedEvent> events = eventList.get(ep.getId());
				IState state;
				for (int i = 0; i < events.size() - 1; i++) {
					state = new PajeState(events.get(i), events.get(i + 1), timeSliceManager);
					if (!((PajeConfig) query.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
						final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
						for (final long it : distrib.keySet())
							matrixWrite(it, ep, distrib);
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

	public final static String	descriptor	= "State Sum";

	public final static String	traceType	= PajeConstants.PajeFormatName;

	public PajeStateSum() throws SoCTraceException {
		super();
	}

	public PajeStateSum(final OcelotlParameters parameters) throws SoCTraceException {
		super(parameters);
		System.out.println(descriptor);
	}

	@Override
	protected void computeSubMatrixCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		dm = new DeltaManager();
		dm.start();
		final List<PajeEventProxy> fullEvents = query.getEventsProxy(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		final Map<Integer, List<PajeEventProxy>> eventList = new HashMap<Integer, List<PajeEventProxy>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<PajeEventProxy>());
		for (final PajeEventProxy e : fullEvents)
			eventList.get(e.EP).add(e);
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < query.getOcelotlParameters().getThread(); t++)
			threadlist.add(new OcelotlThread(eventProducers, eventList, query.getOcelotlParameters().getThread(), t, true));
		for (final Thread thread : threadlist)
			thread.join();
		dm.end("VECTOR COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	@Override
	protected void computeSubMatrixNonCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		dm = new DeltaManager();
		dm.start();
		final List<PajeReducedEvent> fullEvents = query.getReducedEvents(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<PajeReducedEvent>> eventList = new HashMap<Integer, List<PajeReducedEvent>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<PajeReducedEvent>());
		for (final PajeReducedEvent e : fullEvents)
			eventList.get(e.EP).add(e);
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < query.getOcelotlParameters().getThread(); t++)
			threadlist.add(new OcelotlThread(eventProducers, eventList, query.getOcelotlParameters().getThread(), t, false));
		for (final Thread thread : threadlist)
			thread.join();
		dm.end("VECTOR COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	@Override
	public String descriptor() {
		return descriptor;
	}

	@Override
	public String traceType() {
		return traceType;
	}
}
