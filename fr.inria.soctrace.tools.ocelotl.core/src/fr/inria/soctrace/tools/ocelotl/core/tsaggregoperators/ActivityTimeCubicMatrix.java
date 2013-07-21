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
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.query.EventProxy;
import fr.inria.soctrace.tools.ocelotl.core.query.OcelotlEventCache;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.ts.State;
import fr.inria.soctrace.tools.ocelotl.core.ts.TimeSliceManager;

public class ActivityTimeCubicMatrix implements ITimeSliceCubicMatrix {

	protected Query											query;
	protected List<HashMap<String, HashMap<String, Long>>>	matrix	= new ArrayList<HashMap<String, HashMap<String, Long>>>();
	protected int											eventsNumber;
	protected TimeSliceManager								timeSliceManager;

	public ActivityTimeCubicMatrix(final Query query) throws SoCTraceException {
		super();
		this.query = query;
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
		final List<Event> fullEvents = query.getEvents(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<Event>> eventList = new HashMap<Integer, List<Event>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<Event>());
		for (final Event e : fullEvents)
			eventList.get(e.getEventProducer().getId()).add(e);
		for (final EventProducer ep : eventProducers) {
			State state;
			final List<Event> events = eventList.get(ep.getId());
			for (int i = 0; i < events.size() - 1; i++) {
				state=new State(events.get(i), events.get(i + 1), timeSliceManager);
				if (!query.getOcelotlParameters().getSleepingStates().contains(state.getStateType())){
					final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
					if (!matrix.get(0).get(ep.getName()).containsKey(state.getStateType())) {
						System.out.println("Adding " + state.getStateType() + " state");
						for (int incr = 0; incr < matrix.size(); incr++)
							for (final EventProducer epname : eventProducers)
								matrix.get(incr).get(epname.getName()).put(state.getStateType(), 0L);
					}
					for (final long it : distrib.keySet())
						matrix.get((int) it).get(ep.getName()).put(state.getStateType(), matrix.get((int) it).get(ep.getName()).get(state.getStateType()) + distrib.get(it));
				}
			}
		}
		dm.end("VECTORS COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}
	
	protected void computeSubMatrixCached(final List<EventProducer> eventProducers) throws SoCTraceException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		OcelotlEventCache cache = new OcelotlEventCache(query.getOcelotlParameters());
		final List<EventProxy> fullEvents = query.getEventsProxy(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<EventProxy>> eventList = new HashMap<Integer, List<EventProxy>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<EventProxy>());
		for (final EventProxy e : fullEvents)
			eventList.get(e.EP).add(e);
		for (final EventProducer ep : eventProducers) {
			State state;
			final List<EventProxy> events = eventList.get(ep.getId());
			for (int i = 0; i < events.size() - 1; i++) {
				state=(new State(cache.getEventMultiPageEPCache(events.get(i)), cache.getEventMultiPageEPCache(events.get(i + 1)), timeSliceManager));
				if (!query.getOcelotlParameters().getSleepingStates().contains(state.getStateType())){
					final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
					if (!matrix.get(0).get(ep.getName()).containsKey(state.getStateType())) {
						System.out.println("Adding " + state.getStateType() + " state");
						for (int incr = 0; incr < matrix.size(); incr++)
							for (final EventProducer epname : eventProducers)
								matrix.get(incr).get(epname.getName()).put(state.getStateType(), 0L);
					}
					for (final long it : distrib.keySet())
						matrix.get((int) it).get(ep.getName()).put(state.getStateType(), matrix.get((int) it).get(ep.getName()).get(state.getStateType()) + distrib.get(it));
				}
			}
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
	public void computeVectors() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<HashMap<String, HashMap<String, Long>>> getMatrix() {
		return matrix;
	}

	@Override
	public Query getQueries() {
		return query;
	}

	@Override
	public TimeSliceManager getTimeSlicesManager() {
		return timeSliceManager;
	}

	@Override
	public int getVectorSize() {
		return matrix.get(0).size();
	}

	@Override
	public int getVectorsNumber() {
		return matrix.size();
	}

	@Override
	public void initVectors() throws SoCTraceException {
		final List<EventProducer> producers = query.getOcelotlParameters().getEventProducers();
		for (long i = 0; i < timeSliceManager.getSlicesNumber(); i++) {
			matrix.add(new HashMap<String, HashMap<String, Long>>());

			for (final EventProducer ep : producers)
				matrix.get((int) i).put(ep.getName(), new HashMap<String, Long>());
		}
	}

	@Override
	public void print() {
		System.out.println();
		System.out.println("Distribution Vectors");
		int i = 0;
		for (final HashMap<String, HashMap<String, Long>> it : matrix) {
			System.out.println();
			System.out.println("slice " + i++);
			System.out.println();
			for (final String ep : it.keySet())
				System.out.println(ep + " = " + it.get(ep));
		}
	}

	@Override
	public void setQueries(final Query query) {
		this.query = query;
	}

}