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

public class TimeRegion {

	private long	timeStampStart	= 0;
	private long	timeStampEnd	= Long.MAX_VALUE;

	public TimeRegion() {
		super();
	}

	public TimeRegion(long timeStampStart, long timeStampEnd) {
		super();
		setTimeStampStart(timeStampStart);
		setTimeStampEnd(timeStampEnd);
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

	public void setTimeStampEnd(long timeStampEnd) {
		if (timeStampEnd >= timeStampStart)
			this.timeStampEnd = timeStampEnd;
	}

	public void setTimeStamps(long timeStampStart, long timeStampEnd) {
		if (timeStampStart <= timeStampEnd) {
			this.timeStampStart = timeStampStart;
			this.timeStampEnd = timeStampEnd;
		}
	}

	public void setTimeStampStart(long timeStampStart) {
		if (timeStampStart <= timeStampEnd)
			this.timeStampStart = timeStampStart;
	}

	@Override
	public String toString() {
		return "TimeRegion [timeStampStart=" + timeStampStart + ", timeStampEnd=" + timeStampEnd + "]";
	}

}
