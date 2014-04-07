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

package fr.inria.soctrace.tools.ocelotl.core.timeslice;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class TimeSlice {

	private TimeRegion	timeRegion;
	private long		number;
	private long		value	= -1;

	public TimeSlice(final TimeRegion timeRegion, final long number) {
		super();
		this.timeRegion = timeRegion;
		this.number = number;
	}

	public TimeSlice(final TimeRegion timeRegion, final long number, final long value) {
		super();
		this.timeRegion = timeRegion;
		this.number = number;
		this.value = value;
	}

	public long getNumber() {
		return number;
	}

	public TimeRegion getTimeRegion() {
		return timeRegion;
	}

	public long getValue() {
		return value;
	}

	public long regionInsideMe(final TimeRegion testedTimeRegion) {
		if (testedTimeRegion.getTimeStampStart() >= timeRegion.getTimeStampStart() && testedTimeRegion.getTimeStampStart() <= timeRegion.getTimeStampEnd()) {
			if (testedTimeRegion.getTimeStampEnd() < timeRegion.getTimeStampEnd())
				return testedTimeRegion.getTimeStampEnd() - testedTimeRegion.getTimeStampStart();
			else
				return timeRegion.getTimeStampEnd() - testedTimeRegion.getTimeStampStart();
		} else if (testedTimeRegion.getTimeStampStart() < timeRegion.getTimeStampStart())
			if (testedTimeRegion.getTimeStampEnd() <= timeRegion.getTimeStampEnd() && testedTimeRegion.getTimeStampEnd() >= timeRegion.getTimeStampStart())
				return testedTimeRegion.getTimeStampEnd() - timeRegion.getTimeStampStart();
			else if (testedTimeRegion.getTimeStampEnd() > timeRegion.getTimeStampEnd())
				return timeRegion.getTimeStampEnd() - timeRegion.getTimeStampStart();
		return 0;
	}

	public void setNumber(final long number) {
		this.number = number;
	}

	public void setTimeRegion(final TimeRegion timeRegion) {
		this.timeRegion = timeRegion;
	}

	public void setValue(final long value) {
		this.value = value;
	}

	public boolean startIsInsideMe(final long eventStart) {
		return eventStart >= timeRegion.getTimeStampStart() && eventStart <= timeRegion.getTimeStampEnd();
	}

}
