/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
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

package fr.inria.soctrace.tools.ocelotl.core.state;

import java.util.Map;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;

public abstract class State implements IState {

	protected String stateType = null;
	protected TimeRegion timeRegion = null;
	protected int eventProducerID = -1;
	protected final TimeSliceManager timeSliceManager;

	public State(final TimeSliceManager timeSliceManager) {// TODO
		this.timeSliceManager = timeSliceManager;
	}

	@Override
	public int getEventProducerID() {
		return eventProducerID;
	}

	@Override
	public String getStateType() {
		return stateType;
	}

	@Override
	public TimeRegion getTimeRegion() {
		return timeRegion;
	}

	@Override
	public Map<Long, Long> getTimeSlicesDistribution() {
		return timeSliceManager.getTimeSlicesDistribution(timeRegion);
	}

}