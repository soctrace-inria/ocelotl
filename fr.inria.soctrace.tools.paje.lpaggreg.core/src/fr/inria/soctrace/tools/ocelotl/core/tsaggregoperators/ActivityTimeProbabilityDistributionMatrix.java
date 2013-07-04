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

package fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.Queries;
import fr.inria.soctrace.tools.ocelotl.core.State;

public class ActivityTimeProbabilityDistributionMatrix extends TimeSliceMatrix{


	public ActivityTimeProbabilityDistributionMatrix(Queries queries) throws SoCTraceException {
		super(queries);
		System.out.println("Activity Time Probability Distribution Matrix");
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
					for (long it : distrib.keySet())
						matrix.get((int) it).put(ep.getName(), matrix.get((int) it).get(ep.getName()) + distrib.get(it));
				}
			}
			long total=0;
			for (int i=0; i<matrix.size(); i++)
				total=matrix.get(i).get(ep.getName())+total;
			if (total!=0)
				for (int i=0; i<matrix.size(); i++)
					matrix.get(i).put(ep.getName(), matrix.get(i).get(ep.getName())*1000000000/total);		
		}
		dm.end("VECTORS COMPUTATION : " + queries.getLpaggregParameters().getTimeSlicesNumber() + " timeslices");
	}
}
