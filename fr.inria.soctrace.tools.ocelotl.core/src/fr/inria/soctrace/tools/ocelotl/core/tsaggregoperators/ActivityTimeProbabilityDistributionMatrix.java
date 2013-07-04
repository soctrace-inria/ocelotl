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
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.ts.State;

public class ActivityTimeProbabilityDistributionMatrix extends TimeSliceMatrix {

	public ActivityTimeProbabilityDistributionMatrix(final Query query) throws SoCTraceException {
		super(query);
		System.out.println("Activity Time Probability Distribution Matrix");
	}

	@Override
	protected void computeSubMatrix(final List<EventProducer> eventProducers) throws SoCTraceException {
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
			final List<State> state = new ArrayList<State>();
			final List<Event> events = eventList.get(ep.getId());
			for (int i = 0; i < events.size() - 1; i++) {
				state.add(new State(events.get(i), events.get(i + 1), timeSliceManager));
				if (query.getLpaggregParameters().getSleepingStates().contains(state.get(state.size() - 1).getStateType()))
					state.remove(state.size() - 1);
				else {
					final Map<Long, Long> distrib = state.get(state.size() - 1).getTimeSlicesDistribution();
					for (final long it : distrib.keySet())
						matrix.get((int) it).put(ep.getName(), matrix.get((int) it).get(ep.getName()) + distrib.get(it));
				}
			}
			long total = 0;
			for (int i = 0; i < matrix.size(); i++)
				total = matrix.get(i).get(ep.getName()) + total;
			if (total != 0)
				for (int i = 0; i < matrix.size(); i++)
					matrix.get(i).put(ep.getName(), matrix.get(i).get(ep.getName()) * 1000000000 / total);
		}
		dm.end("VECTORS COMPUTATION : " + query.getLpaggregParameters().getTimeSlicesNumber() + " timeslices");
	}
}
