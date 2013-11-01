package fr.inria.soctrace.tools.ocelotl.core.generic.timeaggregop;

///* ===========================================================
// * Ocelotl Visualization Tool
// * =====================================================================
// * 
// * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
// * under an aggregated representation form.
// *
// * (C) Copyright 2013 INRIA
// *
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     Damien Dosimont <damien.dosimont@imag.fr>
// *     Generoso Pagano <generoso.pagano@inria.fr>
// */
//
//package fr.inria.soctrace.tools.ocelotl.core.generic.timeaggregop;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import fr.inria.soctrace.lib.model.EventProducer;
//import fr.inria.soctrace.lib.model.utils.SoCTraceException;
//import fr.inria.soctrace.lib.utils.DeltaManager;
//import fr.inria.soctrace.tools.ocelotl.core.OcelotlParameters;
//import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.CubicMatrix;
//import fr.inria.soctrace.tools.ocelotl.core.paje.config.PajeConfig;
//import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeReducedEvent2;
//import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeReducedEvent2Cache;
//import fr.inria.soctrace.tools.ocelotl.core.paje.state.PajeState;
//import fr.inria.soctrace.tools.ocelotl.core.timeslice.IState;
//import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;
//import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeExternalConstants;
//
//public class GenericEventDistribution extends CubicMatrix {
//	
//	class OcelotlThread extends Thread {
//
//		List<EventProducer>						eventProducers;
//		Map<Integer, List<EventProxy>>		eventProxyList;
//		Map<Integer, List<PajeReducedEvent2>>	eventList;
//		int										threadNumber;
//		int										thread;
//		boolean									cached;
//
//		@SuppressWarnings("unchecked")
//		public OcelotlThread(final List<EventProducer> eventProducers, final Object eventList, final int threadNumber, final int thread, final boolean cached) {
//			super();
//			this.eventProducers = eventProducers;
//			this.cached = cached;
//			if (cached)
//				eventProxyList = (Map<Integer, List<EventProxy>>) eventList;
//			else
//				this.eventList = (Map<Integer, List<PajeReducedEvent2>>) eventList;
//			this.threadNumber = threadNumber;
//			this.thread = thread;
//
//			start();
//		}
//		
//		private void matrixUpdate(String type, EventProducer ep, Map<Long, Long> distrib){
//			synchronized(matrix){
//		if (!matrix.get(0).get(ep.getName()).containsKey(type)) {
//			System.out.println("Adding " + type + " state");
//			for (int incr = 0; incr < matrix.size(); incr++)
//				for (final String epstring : matrix.get(incr).keySet())
//					matrixPushType(incr, epstring, type);
//		}
//			for (final long it : distrib.keySet())
//				matrixWrite(it, ep, type);
//		}
//	}
//		
//	public void matrixWrite(final long it, final EventProducer ep, String type) {
//			matrix.get((int) it).get(ep.getName()).put(type, matrix.get((int) it).get(ep.getName()).get(type) + 1);
//	}
//	
//	public void matrixPushType(int incr, String epstring, String type) {
//			matrix.get(incr).get(epstring).put(type, 0L);
//	}
//
//		private void cacheRun() throws SoCTraceException {
//			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
//				ArrayList<GenericReducedEvent> stack = new ArrayList<GenericReducedEvent>();
//				GenericReducedEventCache cache;
//				cache = new GenericReducedEventCache(genericQuery.getOcelotlParameters());
//				final EventProducer ep = eventProducers.get(t);
//				IState state=null;
//				final List<EventProxy> events = eventProxyList.get(ep.getId());
//				for (int i = 1; i < events.size(); i++) {
//					GenericReducedEvent tmp1 = cache.getEventMultiPageEPCache(events.get(i-1));
//					
//					
//				final int c = getCount();
//				if (c % EPCOUNT == 0)
//					total(c);
//			}
//
//		}
//
//		private void noCacheRun() {
//			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
//				final EventProducer ep = eventProducers.get(t);
//				ArrayList<PajeReducedEvent2> stack = new ArrayList<PajeReducedEvent2>();
//				final List<PajeReducedEvent2> events = eventList.get(ep.getId());
//				IState state=null;
//					for (int i = 1; i < events.size(); i++) {
//						PajeReducedEvent2 tmp1 = events.get(i-1);
//						PajeReducedEvent2 tmp2 = events.get(i);
//						compute(tmp1, tmp2, stack, state, ep);
//					}
//						computeEnd(stack, state, ep);
//				final int c = getCount();
//				if (c % EPCOUNT == 0)
//					total(c);
//			}
//
//		}
//			void compute(PajeReducedEvent2 tmp1, PajeReducedEvent2 tmp2, ArrayList<PajeReducedEvent2> stack, IState state, EventProducer ep){
//				if (tmp2.TYPE.contains(PajeExternalConstants.PajePushState)){
//					if (!tmp1.TYPE.contains(PajeExternalConstants.PajePopState))
//						stack.add(tmp1);
//				}
//				else if (tmp2.TYPE.contains(PajeExternalConstants.PajePopState)){
//					if (tmp1.TYPE.contains(PajeExternalConstants.PajePushState)){
//						state = new PajeState(tmp1, tmp2, timeSliceManager);
//					if (!((PajeConfig) genericQuery.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
//						final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
//						matrixUpdate(state, ep, distrib);
//					}
//					}
//					else{
//						state = new PajeState(stack.get(stack.size()-1), tmp2, timeSliceManager);
//						stack.remove(stack.size()-1);
//						if (!((PajeConfig) genericQuery.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
//							final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
//							matrixUpdate(state, ep, distrib);
//						}
//					}
//				}
//				else if (tmp2.TYPE.contains(PajeExternalConstants.PajeSetState)){
//					state = new PajeState(tmp1, tmp2, timeSliceManager);
//					if (!((PajeConfig) genericQuery.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
//						final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
//						matrixUpdate(state, ep, distrib);
//					}
//					for (PajeReducedEvent2 stacked:stack){
//						state = new PajeState(stacked, tmp2, timeSliceManager);
//						if (!((PajeConfig) genericQuery.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
//							final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
//							matrixUpdate(state, ep, distrib);
//						}
//					}
//					stack.clear();
//				}
//			}
//			
//			void computeEnd(ArrayList<PajeReducedEvent2> stack, IState state, EventProducer ep){
//			if (!stack.isEmpty()){
//				for (PajeReducedEvent2 stacked:stack){
//					state = new PajeState(stacked, genericQuery.getOcelotlParameters().getTimeRegion().getTimeStampEnd(), timeSliceManager);
//					if (!((PajeConfig) genericQuery.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
//						final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
//						matrixUpdate(state, ep, distrib);
//					}
//				}
//			}
//			}
//				
//		
//		@Override
//		public void run() {
//			if (cached)
//				try {
//					cacheRun();
//				} catch (final SoCTraceException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			else
//				noCacheRun();
//		}
//	}
//
//
//	public final static String	descriptor	= "State Type Sum (PushPop)";
//	public final static String	traceType	= PajeConstants.PajeFormatName;
//
//	public GenericEventDistribution() throws SoCTraceException {
//		super();
//	}
//
//	public GenericEventDistribution(final OcelotlParameters parameters) throws SoCTraceException {
//		super(parameters);
//	}
//
//	protected void computeSubMatrixCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
//		DeltaManager dm = new DeltaManager();
//		dm.start();
//		final List<EventProxy> fullEvents = genericQuery.getEventsProxy(eventProducers);
//		eventsNumber = fullEvents.size();
//		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
//		dm = new DeltaManager();
//		dm.start();
//		final Map<Integer, List<EventProxy>> eventList = new HashMap<Integer, List<EventProxy>>();
//		for (final EventProducer ep : eventProducers)
//			eventList.put(ep.getId(), new ArrayList<EventProxy>());
//		for (final EventProxy e : fullEvents)
//			eventList.get(e.EP).add(e);
//		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
//		for (int t = 0; t < genericQuery.getOcelotlParameters().getThread(); t++)
//			threadlist.add(new OcelotlThread(eventProducers, eventList, genericQuery.getOcelotlParameters().getThread(), t, true));
//		for (final Thread thread : threadlist)
//			thread.join();
//		dm.end("VECTORS COMPUTATION : " + genericQuery.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
//	}
//
//	protected void computeSubMatrixNonCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
//		DeltaManager dm = new DeltaManager();
//		dm.start();
//		final List<GenericReducedEvent> fullEvents = genericQuery.getReducedEvents(eventProducers);
//		eventsNumber = fullEvents.size();
//		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
//		dm = new DeltaManager();
//		dm.start();
//		final Map<Integer, List<GenericReducedEvent>> eventList = new HashMap<Integer, List<GenericReducedEvent>>();
//		for (final EventProducer ep : eventProducers)
//			eventList.put(ep.getId(), new ArrayList<GenericReducedEvent>());
//		for (final GenericReducedEvent e : fullEvents)
//			eventList.get(e.EP).add(e);
//			final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
//			for (int t = 0; t < genericQuery.getOcelotlParameters().getThread(); t++)
//				threadlist.add(new OcelotlThread(eventProducers, eventList, genericQuery.getOcelotlParameters().getThread(), t, false));
//			for (final Thread thread : threadlist)
//				thread.join();
//		dm.end("VECTORS COMPUTATION : " + genericQuery.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
//	}
//	
//	
//
//	@Override
//	public String descriptor() {
//		return descriptor;
//	}
//
//	@Override
//	public String traceType() {
//		return traceType;
//	}
//
// }
