/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
 * overview by using aggregation techniques
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

package fr.inria.soctrace.tools.ocelotl.microdesc.genericevents;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.tools.ocelotl.core.events.State;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceStateManager;

public class GenericState extends State {

	public GenericState(final Event event,
			final TimeSliceStateManager timeSliceManager) {
		super(timeSliceManager);
		timeRegion = new TimeRegion(event.getTimestamp(), event.getLongPar());
		eventProducerID = event.getEventProducer().getId();
		stateType = event.getType().getName();
	}

}