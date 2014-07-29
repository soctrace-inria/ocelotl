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
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceStateManager;

public abstract class Variable implements IVariable {

	protected String variableType = null;
	protected TimeRegion timeRegion = null;
	protected int eventProducerID = -1;
	protected double value=0;
	protected final TimeSliceStateManager timeSliceManager;

	public Variable(final TimeSliceStateManager timeSliceManager) {// TODO
		this.timeSliceManager = timeSliceManager;
	}

	@Override
	public int getEventProducerID() {
		return eventProducerID;
	}

	@Override
	public String getType(){
		return variableType;
	}

	@Override
	public TimeRegion getTimeRegion() {
		return timeRegion;
	}

	@Override
	public Map<Long, Long> getTimeSlicesDistribution() {
		return timeSliceManager.getTimeSlicesDistribution(timeRegion);
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	

}