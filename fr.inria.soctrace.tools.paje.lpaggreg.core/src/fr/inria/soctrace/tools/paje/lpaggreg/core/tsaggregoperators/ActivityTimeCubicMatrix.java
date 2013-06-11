/* ===========================================================
 * LPAggreg core module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to visualize a Paje
 * trace across an aggregated representation.
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
 */

package fr.inria.soctrace.tools.paje.lpaggreg.core.tsaggregoperators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.paje.lpaggreg.core.Queries;
import fr.inria.soctrace.tools.paje.lpaggreg.core.State;
import fr.inria.soctrace.tools.paje.lpaggreg.core.TimeSliceManager;

public class ActivityTimeCubicMatrix implements ITimeSliceCubicMatrix{

	protected Queries						queries;
	protected List<HashMap<String, HashMap<String, Long>>>	matrix	= new ArrayList<HashMap<String, HashMap<String, Long>>>();
	protected int							eventsNumber;
	protected TimeSliceManager				timeSliceManager;

	public ActivityTimeCubicMatrix(Queries queries) throws SoCTraceException {
		super();
		this.queries = queries;
		queries.checkTimeStamps();
		timeSliceManager = new TimeSliceManager(queries.getLpaggregParameters().getTimeRegion(), queries.getLpaggregParameters().getTimeSlicesNumber());
		initVectors();
		computeMatrix();
	}
	
	protected void computeSubMatrix(List<EventProducer> eventProducers) throws SoCTraceException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		List<Event> fullEvents = queries.getEvents(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		Map<Integer, List<Event>> eventList = new HashMap<Integer, List<Event>>();
		for (EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<Event>());
		for (Event e : fullEvents)
			eventList.get(e.getEventProducer().getId()).add(e);
		for (EventProducer ep : eventProducers) {
			List<State> state = new ArrayList<State>();
			List<Event> events = eventList.get(ep.getId());
			for (int i=0; i<events.size()-1; i++){
				state.add(new State(events.get(i), events.get(i + 1), timeSliceManager));
				if (queries.getLpaggregParameters().getSleepingStates().contains(state.get(state.size()-1).getStateType()))
					state.remove(state.size()-1);
				else{
					Map<Long, Long> distrib = state.get(state.size()-1).getTimeSlicesDistribution();
					if (!matrix.get(0).get(ep.getName()).containsKey(state.get(state.size()-1).getStateType())){
						System.out.println("Adding " + state.get(state.size()-1).getStateType() + " state");
						for (int incr = 0; incr < matrix.size(); incr++){
							for (EventProducer epname : eventProducers){
								matrix.get(incr).get(epname.getName()).put(state.get(state.size()-1).getStateType(), 0L);
							}
						}
					}
					for (long it : distrib.keySet()){
						matrix.get((int) it).get(ep.getName()).put(state.get(state.size()-1).getStateType(), matrix.get((int) it).get(ep.getName()).get(state.get(state.size()-1).getStateType()) + distrib.get(it));
					}
				}
			}
		}
		dm.end("VECTORS COMPUTATION : " + queries.getLpaggregParameters().getTimeSlicesNumber() + " timeslices");
	}


	public void computeMatrix() throws SoCTraceException {
		eventsNumber = 0;
		DeltaManager dm = new DeltaManager();
		dm.start();	
		int epsize = queries.getLpaggregParameters().getEventProducers().size();
		if ((queries.getLpaggregParameters().getMaxEventProducers()==0) || (epsize<queries.getLpaggregParameters().getMaxEventProducers())){
			computeSubMatrix(queries.getLpaggregParameters().getEventProducers());
		}else{
			List<EventProducer> producers = (queries.getLpaggregParameters().getEventProducers().size() == 0) ? queries.getAllEventProducers() : queries.getLpaggregParameters().getEventProducers();
			for (int i=0; i<epsize; i=i+queries.getLpaggregParameters().getMaxEventProducers()){
				computeSubMatrix(producers.subList(i, Math.min(epsize-1, i+queries.getLpaggregParameters().getMaxEventProducers())));
			}
		}

		dm.end("TOTAL (QUERIES + COMPUTATION) : " + epsize + " Event Producers, " + eventsNumber + " Events");
	}

	public List<HashMap<String, HashMap<String, Long>>> getMatrix() {
		return matrix;
	}

	public Queries getQueries() {
		return queries;
	}

	public TimeSliceManager getTimeSlicesManager() {
		return timeSliceManager;
	}

	public int getVectorSize() {
		return matrix.get(0).size();
	}

	public int getVectorsNumber() {
		return matrix.size();
	}

	public void initVectors() throws SoCTraceException {
		List<EventProducer> producers = queries.getLpaggregParameters().getEventProducers();
		for (long i = 0; i < timeSliceManager.getSlicesNumber(); i++) {
			matrix.add(new HashMap<String, HashMap<String, Long>>());

			for (EventProducer ep : producers)
				matrix.get((int) i).put(ep.getName(), new HashMap<String, Long>());
		}
	}

	public void print() {
		System.out.println();
		System.out.println("Distribution Vectors");
		int i = 0;
		for (HashMap<String, HashMap<String, Long>> it : matrix) {
			System.out.println();
			System.out.println("slice " + i++);
			System.out.println();
			for (String ep : it.keySet())
				System.out.println(ep + " = " + it.get(ep));
		}
	}

	public void setQueries(Queries queries) {
		this.queries = queries;
	}

	@Override
	public void computeVectors() {
		// TODO Auto-generated method stub
		
	}

}
