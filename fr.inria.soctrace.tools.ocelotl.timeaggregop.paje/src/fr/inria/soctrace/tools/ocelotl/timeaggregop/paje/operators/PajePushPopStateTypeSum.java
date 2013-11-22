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

package fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.generic.query.EventProxy;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.CubicMatrix;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.state.IState;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.config.PajeConfig;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.query.PajeQuery;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.query.PajeReducedEvent2;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.query.PajeReducedEvent2Cache;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.state.PajeState;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeExternalConstants;

public class PajePushPopStateTypeSum extends CubicMatrix {

	class OcelotlThread extends Thread {

		List<EventProducer>						eventProducers;
		Map<Integer, List<EventProxy>>			eventProxyList;
		Map<Integer, List<PajeReducedEvent2>>	eventList;
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
				this.eventList = (Map<Integer, List<PajeReducedEvent2>>) eventList;
			this.threadNumber = threadNumber;
			this.thread = thread;

			start();
		}

		private void cacheRun() throws SoCTraceException {
			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
				final ArrayList<PajeReducedEvent2> stack = new ArrayList<PajeReducedEvent2>();

				PajeReducedEvent2Cache cache;
				cache = new PajeReducedEvent2Cache(genericQuery.getOcelotlParameters());
				final EventProducer ep = eventProducers.get(t);
				final IState state = null;
				final List<EventProxy> events = eventProxyList.get(ep.getId());
				for (int i = 1; i < events.size(); i++) {
					final PajeReducedEvent2 tmp1 = cache.getEventMultiPageEPCache(events.get(i - 1));
					final PajeReducedEvent2 tmp2 = cache.getEventMultiPageEPCache(events.get(i));
					final PajeReducedEvent2 current = null;
					compute(tmp1, tmp2, current, stack, state, ep);
				}
				computeEnd(stack, state, ep);

				final int c = getCount();
				if (c % EPCOUNT == 0)
					total(c);
			}

		}

		void compute(final PajeReducedEvent2 tmp1, final PajeReducedEvent2 tmp2, PajeReducedEvent2 current, final ArrayList<PajeReducedEvent2> stack, IState state, final EventProducer ep) {
			if (tmp2.TYPE.contains(PajeExternalConstants.PajePushState)) {
				if (!tmp1.TYPE.contains(PajeExternalConstants.PajePopState))
					stack.add(tmp1);
				else if (current != null)
					stack.add(current);
			} else if (tmp2.TYPE.contains(PajeExternalConstants.PajePopState)) {
				current = null;
				if (!stack.isEmpty()) {
					current = stack.get(stack.size() - 1);
					stack.remove(stack.size() - 1);
				}
				if (tmp1.TYPE.contains(PajeExternalConstants.PajePushState)) {
					state = new PajeState(tmp1, tmp2, timeSliceManager);
					matrixUpdate(state, ep);
				} else if (current != null) {
					state = new PajeState(current, tmp2, timeSliceManager);
					matrixUpdate(state, ep);
				}
			} else if (tmp2.TYPE.contains(PajeExternalConstants.PajeSetState)) {
				state = new PajeState(tmp1, tmp2, timeSliceManager);
				matrixUpdate(state, ep);
				for (final PajeReducedEvent2 stacked : stack) {
					state = new PajeState(stacked, tmp2, timeSliceManager);
					matrixUpdate(state, ep);
				}
				stack.clear();
			}
		}

		void computeEnd(final ArrayList<PajeReducedEvent2> stack, IState state, final EventProducer ep) {
			if (!stack.isEmpty())
				for (final PajeReducedEvent2 stacked : stack) {
					state = new PajeState(stacked, genericQuery.getOcelotlParameters().getTimeRegion().getTimeStampEnd(), timeSliceManager);
					matrixUpdate(state, ep);
				}
			stack.clear();
		}

		private void matrixUpdate(final IState state, final EventProducer ep) {
			synchronized (matrix) {
				if (!((PajeConfig) genericQuery.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
					final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
					if (!matrix.get(0).get(ep.getName()).containsKey(state.getStateType())) {
						System.out.println("Adding " + state.getStateType() + " state");
						addKey(state.getStateType());
						for (int incr = 0; incr < matrix.size(); incr++)
							for (final String epstring : matrix.get(incr).keySet())
								matrixPushType(incr, epstring, state, distrib);
					}
					for (final long it : distrib.keySet())
						matrixWrite(it, ep, state, distrib);
				}
			}
		}

		private void noCacheRun() {
			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
				final EventProducer ep = eventProducers.get(t);
				final ArrayList<PajeReducedEvent2> stack = new ArrayList<PajeReducedEvent2>();
				final List<PajeReducedEvent2> events = eventList.get(ep.getId());
				final IState state = null;
				for (int i = 1; i < events.size(); i++) {
					final PajeReducedEvent2 tmp1 = events.get(i - 1);
					final PajeReducedEvent2 tmp2 = events.get(i);
					final PajeReducedEvent2 current = null;
					compute(tmp1, tmp2, current, stack, state, ep);
				}
				computeEnd(stack, state, ep);
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

	public final static String	descriptor	= "State Type Sum (PushPop)";
	public final static String	traceType	= PajeConstants.PajeFormatName;

	public PajePushPopStateTypeSum() throws SoCTraceException {
		super();
	}

	public PajePushPopStateTypeSum(final OcelotlParameters parameters) throws SoCTraceException {
		super(parameters);
	}

	@Override
	protected void computeSubMatrixCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		dm = new DeltaManager();
		dm.start();
		final List<EventProxy> fullEvents = genericQuery.getEventsProxy(eventProducers);
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
		for (int t = 0; t < Math.min(genericQuery.getOcelotlParameters().getThread(), eventProducers.size()); t++)
			threadlist.add(new OcelotlThread(eventProducers, eventList, genericQuery.getOcelotlParameters().getThread(), t, true));
		for (final Thread thread : threadlist)
			thread.join();
		dm.end("VECTORS COMPUTATION : " + genericQuery.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	@Override
	protected void computeSubMatrixNonCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		dm = new DeltaManager();
		dm.start();
		final List<PajeReducedEvent2> fullEvents = ((PajeQuery) genericQuery).getReducedEvents2(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<PajeReducedEvent2>> eventList = new HashMap<Integer, List<PajeReducedEvent2>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<PajeReducedEvent2>());
		for (final PajeReducedEvent2 e : fullEvents)
			eventList.get(e.EP).add(e);
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < Math.min(genericQuery.getOcelotlParameters().getThread(), eventProducers.size()); t++)
			threadlist.add(new OcelotlThread(eventProducers, eventList, genericQuery.getOcelotlParameters().getThread(), t, false));
		for (final Thread thread : threadlist)
			thread.join();
		dm.end("VECTORS COMPUTATION : " + genericQuery.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	@Override
	public String descriptor() {
		return descriptor;
	}

	@Override
	public void setOcelotlParameters(final OcelotlParameters parameters) throws SoCTraceException, InterruptedException {
		this.parameters = parameters;
		genericQuery = new PajeQuery(parameters);
		genericQuery.checkTimeStamps();
		count = 0;
		epit = 0;
		timeSliceManager = new TimeSliceManager(genericQuery.getOcelotlParameters().getTimeRegion(), genericQuery.getOcelotlParameters().getTimeSlicesNumber());
		initVectors();
		computeMatrix();
	}

	@Override
	public String traceType() {
		return traceType;
	}

}
