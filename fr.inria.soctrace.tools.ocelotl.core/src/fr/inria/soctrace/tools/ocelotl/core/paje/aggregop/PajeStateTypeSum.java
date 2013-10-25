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
import fr.inria.soctrace.tools.ocelotl.core.iaggregop.CubicMatrix;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeStateSum.OcelotlThread;
import fr.inria.soctrace.tools.ocelotl.core.paje.config.PajeConfig;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeEventProxy;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeReducedEvent1;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeReducedEvent1Cache;
import fr.inria.soctrace.tools.ocelotl.core.paje.state.PajeState;
import fr.inria.soctrace.tools.ocelotl.core.ts.IState;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;

public class PajeStateTypeSum extends CubicMatrix {
	
	class OcelotlThread extends Thread {

		List<EventProducer>						eventProducers;
		Map<Integer, List<PajeEventProxy>>		eventProxyList;
		Map<Integer, List<PajeReducedEvent1>>	eventList;
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
				this.eventList = (Map<Integer, List<PajeReducedEvent1>>) eventList;
			this.threadNumber = threadNumber;
			this.thread = thread;

			start();
		}
		
		private void matrixUpdate(IState state, EventProducer ep, Map<Long, Long> distrib){
			synchronized(matrix){
		if (!matrix.get(0).get(ep.getName()).containsKey(state.getStateType())) {
			System.out.println("Adding " + state.getStateType() + " state");
			for (int incr = 0; incr < matrix.size(); incr++)
				for (final String epstring : matrix.get(incr).keySet())
					matrixPushType(incr, epstring, state, distrib);
		}
			for (final long it : distrib.keySet())
				matrixWrite(it, ep, state, distrib);
		}
	}

		private void cacheRun() throws SoCTraceException {
			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
				PajeReducedEvent1Cache cache;
				cache = new PajeReducedEvent1Cache(query.getOcelotlParameters());
				final EventProducer ep = eventProducers.get(t);
				IState state;
				final List<PajeEventProxy> events = eventProxyList.get(ep.getId());
				for (int i = 0; i < events.size() - 1; i++) {
					state = new PajeState(cache.getEventMultiPageEPCache(events.get(i)), cache.getEventMultiPageEPCache(events.get(i + 1)), timeSliceManager);
					if (!((PajeConfig) query.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
						final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
						matrixUpdate(state, ep, distrib);
						}
				}
				final int c = getCount();
				if (c % EPCOUNT == 0)
					total(c);
			}

		}

		private void noCacheRun() {
			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
				final EventProducer ep = eventProducers.get(t);
				final List<PajeReducedEvent1> events = eventList.get(ep.getId());
				IState state;
				for (int i = 0; i < events.size() - 1; i++) {
					state = new PajeState(events.get(i), events.get(i + 1), timeSliceManager);
					if (!((PajeConfig) query.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
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


	public final static String	descriptor	= "State Type Sum";
	public final static String	traceType	= PajeConstants.PajeFormatName;

	public PajeStateTypeSum() throws SoCTraceException {
		super();
	}

	public PajeStateTypeSum(final OcelotlParameters parameters) throws SoCTraceException {
		super(parameters);
	}

	protected void computeSubMatrixCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		final List<PajeEventProxy> fullEvents = query.getEventsProxy(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<PajeEventProxy>> eventList = new HashMap<Integer, List<PajeEventProxy>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<PajeEventProxy>());
		for (final PajeEventProxy e : fullEvents)
			eventList.get(e.EP).add(e);
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < Math.min(query.getOcelotlParameters().getThread(), eventProducers.size()); t++)
			threadlist.add(new OcelotlThread(eventProducers, eventList, query.getOcelotlParameters().getThread(), t, true));
		for (final Thread thread : threadlist)
			thread.join();
		dm.end("VECTORS COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	protected void computeSubMatrixNonCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		final List<PajeReducedEvent1> fullEvents = query.getReducedEvents1(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<PajeReducedEvent1>> eventList = new HashMap<Integer, List<PajeReducedEvent1>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<PajeReducedEvent1>());
		for (final PajeReducedEvent1 e : fullEvents)
			eventList.get(e.EP).add(e);
			final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
			for (int t = 0; t < Math.min(query.getOcelotlParameters().getThread(), eventProducers.size()); t++)
				threadlist.add(new OcelotlThread(eventProducers, eventList, query.getOcelotlParameters().getThread(), t, false));
			for (final Thread thread : threadlist)
				thread.join();
		dm.end("VECTORS COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
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
