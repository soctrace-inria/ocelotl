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

package fr.inria.soctrace.tools.ocelotl.core.ts;

import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public abstract class State implements IState {

	protected String					stateType		= null;
	protected TimeRegion				timeRegion		= null;
	protected int						eventProducerID	= -1;
	protected final TimeSliceManager	timeSliceManager;

	public State(final TimeSliceManager timeSliceManager) {// TODO
		this.timeSliceManager = timeSliceManager;
	}

	public int getEventProducerID() {
		return eventProducerID;
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