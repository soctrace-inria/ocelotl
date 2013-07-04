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

package fr.inria.soctrace.tools.ocelotl.core.ts;

import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class State {

	private String					stateType	= null;
	private final TimeRegion		timeRegion;
	private final EventProducer		eventProducer;
	private final Event				eventStart;
	private final Event				eventEnd;
	private final TimeSliceManager	timeSliceManager;

	public State(final Event eventStart, final Event eventEnd, final TimeSliceManager timeSliceManager) {// TODO
		// use
		// TimeRegion
		super();
		this.eventStart = eventStart;
		this.eventEnd = eventEnd;
		timeRegion = new TimeRegion(eventStart.getTimestamp(), eventEnd.getTimestamp());
		eventProducer = eventStart.getEventProducer();
		for (int i = 0; i < eventStart.getType().getEventParamTypes().size(); i++)
			if (eventStart.getType().getEventParamTypes().get(i).getName().equals("Value")) {
				stateType = eventStart.getEventParams().get(i).getValue();
				break;
			}
		this.timeSliceManager = timeSliceManager;
	}

	public Event getEventEnd() {
		return eventEnd;
	}

	public EventProducer getEventProducer() {
		return eventProducer;
	}

	public Event getEventStart() {
		return eventStart;
	}

	public String getStateType() {
		return stateType;
	}

	public TimeRegion getTimeRegion() {
		return timeRegion;
	}

	public Map<Long, Long> getTimeSlicesDistribution() {
		return timeSliceManager.getTimeSlicesDistribution(timeRegion);
	}

}