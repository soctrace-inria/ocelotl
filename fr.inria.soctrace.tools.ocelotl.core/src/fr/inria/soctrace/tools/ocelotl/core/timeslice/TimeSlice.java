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

	private TimeRegion timeRegion;
	private long number;
	private long value = -1;

	public TimeSlice(final TimeRegion timeRegion, final long number) {
		super();
		this.timeRegion = timeRegion;
		this.number = number;
	}

	public TimeSlice(final TimeRegion timeRegion, final long number,
			final long value) {
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

	/**
	 * Compute the duration of the time region that is within the time slice
	 * 
	 * @param testedTimeRegion
	 * @return the length of the time region in the time slice
	 */
	public double regionInsideMe(final TimeRegion testedTimeRegion) { 
		// If the state starts within the time region
		if (testedTimeRegion.getTimeStampStart() >= timeRegion
				.getTimeStampStart()
				&& testedTimeRegion.getTimeStampStart() <= timeRegion
						.getTimeStampEnd()) {
			// If it ends within the time region
			if (testedTimeRegion.getTimeStampEnd() < timeRegion
					.getTimeStampEnd())
				// State duration
				return testedTimeRegion.getTimeStampEnd()
						- testedTimeRegion.getTimeStampStart();
			else
				// State duration up until the time region ends
				return timeRegion.getTimeStampEnd()
						- testedTimeRegion.getTimeStampStart();
			// If state starts before the time region
		} else if (testedTimeRegion.getTimeStampStart() < timeRegion
				.getTimeStampStart())
			// If it ends within the time region
			if (testedTimeRegion.getTimeStampEnd() <= timeRegion
					.getTimeStampEnd()
					&& testedTimeRegion.getTimeStampEnd() >= timeRegion
							.getTimeStampStart())
				// State duration from the beginning of the state region
				return testedTimeRegion.getTimeStampEnd()
						- timeRegion.getTimeStampStart();
			// if it ends after the time region
			else if (testedTimeRegion.getTimeStampEnd() > timeRegion
					.getTimeStampEnd())
				// return time region duration
				return timeRegion.getTimeStampEnd()
						- timeRegion.getTimeStampStart();
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
		return eventStart >= timeRegion.getTimeStampStart()
				&& eventStart <= timeRegion.getTimeStampEnd();
	}

}
