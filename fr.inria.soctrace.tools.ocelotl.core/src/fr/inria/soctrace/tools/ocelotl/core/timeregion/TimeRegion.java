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

package fr.inria.soctrace.tools.ocelotl.core.timeregion;

public class TimeRegion {

	private long timeStampStart = 0;
	private long timeStampEnd = Long.MAX_VALUE;

	public TimeRegion() {
		super();
	}

	public TimeRegion(final long timeStampStart, final long timeStampEnd) {
		super();
		setTimeStampStart(timeStampStart);
		setTimeStampEnd(timeStampEnd);
	}

	public TimeRegion(final TimeRegion time) {
		super();
		setTimeStampStart(time.getTimeStampStart());
		setTimeStampEnd(time.getTimeStampEnd());
	}

	/**
	 * Check if two time regions represent the time period
	 * 
	 * @param timeRegion
	 *            The TimeRegion to compare with
	 * @return true if the time regions are identical
	 */
	public boolean compareTimeRegion(final TimeRegion timeRegion) {
		return getTimeStampStart() == timeRegion.getTimeStampStart()
				&& getTimeStampEnd() == timeRegion.getTimeStampEnd();
	}

	public boolean containsTimeStamp(final long timeStamp) {
		if (timeStamp < timeStampStart || timeStamp > timeStampEnd)
			return false;
		return true;
	}
	
	/**
	 * Test if a time region is contained within another
	 * 
	 * @param timeRegion
	 *            the time region to test if its contained
	 * @return false if the time region is not contained, true otherwise
	 * 
	 */
	public boolean containsTimeRegion(final TimeRegion timeRegion) {
		if (timeRegion.getTimeStampStart() < timeStampStart
				|| timeRegion.getTimeStampEnd() > timeStampEnd)
			return false;
		return true;
	}

	public long getTimeDuration() {
		return timeStampEnd - timeStampStart;
	}

	public long getTimeStampEnd() {
		return timeStampEnd;
	}

	public long getTimeStampStart() {
		return timeStampStart;
	}

	public void setTimeStampEnd(final long timeStampEnd) {
		if (timeStampEnd >= timeStampStart)
			this.timeStampEnd = timeStampEnd;
	}

	public void setTimeStamps(final long timeStampStart, final long timeStampEnd) {
		if (timeStampStart <= timeStampEnd) {
			this.timeStampStart = timeStampStart;
			this.timeStampEnd = timeStampEnd;
		}
	}

	public void setTimeStampStart(final long timeStampStart) {
		if (timeStampStart <= timeStampEnd)
			this.timeStampStart = timeStampStart;
	}

	@Override
	public String toString() {
		return "TimeRegion [timeStampStart=" + timeStampStart
				+ ", timeStampEnd=" + timeStampEnd + "]";
	}

}
