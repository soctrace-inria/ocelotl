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
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.paje.lpaggreg.core.Queries;
import fr.inria.soctrace.tools.paje.lpaggreg.core.State;

public class ActivityTimeMatrix extends TimeSliceMatrix{


	public ActivityTimeMatrix(Queries queries) throws SoCTraceException {
		super(queries);
		System.out.println("Activity Time Matrix");
	}
	
	protected void computeSubMatrix(List<EventProducer> eventProducers) throws SoCTraceException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		List<Event> fullEvents = queries.getEvents(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		for (EventProducer ep : eventProducers) {
			List<State> state = new ArrayList<State>();
			List<Event> events = new ArrayList<Event>();
			for (Event e : fullEvents)
				if (e.getEventProducer().getId() == ep.getId())
					events.add(e);
			for (int i = 0; i < events.size() - 1; i++){
				state.add(new State(events.get(i), events.get(i + 1), timeSliceManager));
				if (queries.getLpaggregParameters().getSleepingStates().contains(state.get(state.size()-1).getStateType()))
					state.remove(state.size()-1);
				else{
					Map<Long, Long> distrib = state.get(state.size()-1).getTimeSlicesDistribution();
					for (long it : distrib.keySet())
						matrix.get((int) it).put(ep.getName(), matrix.get((int) it).get(ep.getName()) + distrib.get(it));
				}
			}
		}
		dm.end("VECTORS COMPUTATION : " + queries.getLpaggregParameters().getTimeSlicesNumber() + " timeslices");
	}
}
