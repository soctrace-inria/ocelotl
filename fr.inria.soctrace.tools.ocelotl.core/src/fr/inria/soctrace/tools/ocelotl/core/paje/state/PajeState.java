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

package fr.inria.soctrace.tools.ocelotl.core.paje.state;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeReducedEvent1;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeReducedEvent2;
import fr.inria.soctrace.tools.ocelotl.core.state.State;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeExternalConstants;

public class PajeState extends State {

	public PajeState(final Event eventStart, final Event eventEnd, final TimeSliceManager timeSliceManager) {
		super(timeSliceManager);
		timeRegion = new TimeRegion(eventStart.getTimestamp(), eventEnd.getTimestamp());
		eventProducerID = eventStart.getEventProducer().getId();
		for (int i = 0; i < eventStart.getType().getEventParamTypes().size(); i++)
			if (eventStart.getType().getEventParamTypes().get(i).getName().equals(PajeExternalConstants.PajeStateValue)) {
				stateType = eventStart.getEventParams().get(i).getValue();
				break;
			}
	}

	public PajeState(final PajeReducedEvent1 eventStart, final long end, final TimeSliceManager timeSliceManager) {
		super(timeSliceManager);
		timeRegion = new TimeRegion(eventStart.TS, end);
		eventProducerID = eventStart.EP;
		stateType = eventStart.VALUE;
	}

	public PajeState(final PajeReducedEvent1 eventStart, final PajeReducedEvent1 eventEnd, final TimeSliceManager timeSliceManager) {
		super(timeSliceManager);
		timeRegion = new TimeRegion(eventStart.TS, eventEnd.TS);
		eventProducerID = eventStart.EP;
		stateType = eventStart.VALUE;
	}

	public PajeState(final PajeReducedEvent2 eventStart, final PajeReducedEvent2 eventEnd, final TimeSliceManager timeSliceManager) {
		super(timeSliceManager);
		timeRegion = new TimeRegion(eventStart.TS, eventEnd.TS);
		eventProducerID = eventStart.EP;
		stateType = eventStart.VALUE;
	}

	public void setEndState(final PajeReducedEvent2 eventEnd) {
		timeRegion = new TimeRegion(timeRegion.getTimeStampStart(), eventEnd.TS);
	}

}