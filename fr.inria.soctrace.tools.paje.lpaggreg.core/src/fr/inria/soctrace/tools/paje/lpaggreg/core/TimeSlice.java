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

package fr.inria.soctrace.tools.paje.lpaggreg.core;

public class TimeSlice {

	private TimeRegion	timeRegion;
	private long		number;
	private long		value	= -1;

	public TimeSlice(TimeRegion timeRegion, long number) {
		super();
		this.timeRegion = timeRegion;
		this.number = number;
	}

	public TimeSlice(TimeRegion timeRegion, long number, long value) {
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

	public long regionInsideMe(TimeRegion testedTimeRegion) {
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

	public void setNumber(long number) {
		this.number = number;
	}

	public void setTimeRegion(TimeRegion timeRegion) {
		this.timeRegion = timeRegion;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public boolean startIsInsideMe(long eventStart) {
		return eventStart >= timeRegion.getTimeStampStart() && eventStart <= timeRegion.getTimeStampEnd();
	}

}
