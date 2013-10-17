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

package fr.inria.soctrace.tools.ocelotl.core.aggregop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop.ITimeSliceCubicMatrix;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop.TimeSliceCubicMatrix;
import fr.inria.soctrace.tools.ocelotl.core.query.EventProxy;
import fr.inria.soctrace.tools.ocelotl.core.query.OcelotlEventCache;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.query.ReducedEvent;
import fr.inria.soctrace.tools.ocelotl.core.ts.IState;
import fr.inria.soctrace.tools.ocelotl.core.ts.PajeState;
import fr.inria.soctrace.tools.ocelotl.core.ts.State;
import fr.inria.soctrace.tools.ocelotl.core.ts.TimeSliceManager;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;

public class ActivityTimeCubicMatrix extends TimeSliceCubicMatrix {

	protected Query											query;
	protected List<HashMap<String, HashMap<String, Long>>>	matrix	= new ArrayList<HashMap<String, HashMap<String, Long>>>();
	protected int											eventsNumber;
	protected TimeSliceManager								timeSliceManager;

	public ActivityTimeCubicMatrix(final Query query) throws SoCTraceException {
		super(query);
		query.checkTimeStamps();
		timeSliceManager = new TimeSliceManager(query.getOcelotlParameters().getTimeRegion(), query.getOcelotlParameters().getTimeSlicesNumber());
		initVectors();
		computeMatrix();
	}

	public void computeMatrix() throws SoCTraceException {
		eventsNumber = 0;
		final DeltaManager dm = new DeltaManager();
		dm.start();
		final int epsize = query.getOcelotlParameters().getEventProducers().size();
		if (query.getOcelotlParameters().getMaxEventProducers() == 0 || epsize < query.getOcelotlParameters().getMaxEventProducers())
			computeSubMatrix(query.getOcelotlParameters().getEventProducers());
		else {
			final List<EventProducer> producers = query.getOcelotlParameters().getEventProducers().size() == 0 ? query.getAllEventProducers() : query.getOcelotlParameters().getEventProducers();
			for (int i = 0; i < epsize; i = i + query.getOcelotlParameters().getMaxEventProducers())
				computeSubMatrix(producers.subList(i, Math.min(epsize - 1, i + query.getOcelotlParameters().getMaxEventProducers())));
		}

		dm.end("TOTAL (QUERIES + COMPUTATION) : " + epsize + " Event Producers, " + eventsNumber + " Events");
	}

	protected void computeSubMatrixNonCached(final List<EventProducer> eventProducers) throws SoCTraceException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		final List<ReducedEvent> fullEvents = query.getReducedEvents(eventProducers);
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
			IState state;
			final List<ReducedEvent> events = eventList.get(ep.getId());
			for (int i = 0; i < events.size() - 1; i++) {
				state=new PajeState(events.get(i), events.get(i + 1), timeSliceManager);
				if (!query.getOcelotlParameters().getSleepingStates().contains(state.getStateType())){
					final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
					if (!matrix.get(0).get(ep.getName()).containsKey(state.getStateType())) {
						System.out.println("Adding " + state.getStateType() + " state");
						for (int incr = 0; incr < matrix.size(); incr++)
							for (final String epstring : matrix.get(incr).keySet())
								matrix.get(incr).get(epstring).put(state.getStateType(), 0L);
					}
					for (final long it : distrib.keySet())
						matrix.get((int) it).get(ep.getName()).put(state.getStateType(), matrix.get((int) it).get(ep.getName()).get(state.getStateType()) + distrib.get(it));
				}
			}
		}
		dm.end("VECTORS COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}
	
	protected void computeSubMatrixCached(final List<EventProducer> eventProducers) throws SoCTraceException {
		int count = 0;
		DeltaManager dm = new DeltaManager();
		dm.start();
		OcelotlEventCache cache = new OcelotlEventCache(query.getOcelotlParameters());
		final List<EventProxy> fullEvents = query.getEventsProxy(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		DeltaManager dm2 = new DeltaManager();
		dm.start();
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
				state=(new PajeState(cache.getEventMultiPageEPCache(events.get(i)), cache.getEventMultiPageEPCache(events.get(i + 1)), timeSliceManager));
				if (!query.getOcelotlParameters().getSleepingStates().contains(state.getStateType())){
					final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
					if (!matrix.get(0).get(ep.getName()).containsKey(state.getStateType())) {
						System.out.println("Adding " + state.getStateType() + " state");
						for (int incr = 0; incr < matrix.size(); incr++)
							for (final String epstring : matrix.get(incr).keySet())
								matrix.get(incr).get(epstring).put(state.getStateType(), 0L);
					}
					for (final long it : distrib.keySet())
						matrix.get((int) it).get(ep.getName()).put(state.getStateType(), matrix.get((int) it).get(ep.getName()).get(state.getStateType()) + distrib.get(it));
				}
			}
			dm2.end("EP VECTORS (" + count++ + "): " + ep.getName());

		}
		dm.end("VECTORS COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}
	
	protected void computeSubMatrix(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (query.getOcelotlParameters().isCache()){
			computeSubMatrixCached(eventProducers);
		}else{
			computeSubMatrixNonCached(eventProducers);
		}
	}
	
	@Override
	public String descriptor() {
		return "State Type Sum";
	}

	@Override
	public String traceType() {
		return PajeConstants.PajeFormatName;
	}
	

}


