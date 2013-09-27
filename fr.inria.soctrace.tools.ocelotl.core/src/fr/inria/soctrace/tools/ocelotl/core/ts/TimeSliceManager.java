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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class TimeSliceManager {

	private final List<TimeSlice>	timeSlices	= new ArrayList<TimeSlice>();
	private final TimeRegion		timeRegion;
	private long					slicesNumber;

	private long					sliceDuration;

	public TimeSliceManager(final TimeRegion timeRegion, final long slicesNumber) {// TODO
		// use
		// region
		super();
		this.timeRegion = timeRegion;
		this.slicesNumber = slicesNumber;
		sliceDuration = timeRegion.getTimeDuration() / slicesNumber;
		if (timeRegion.getTimeDuration() % slicesNumber != 0)
			sliceDuration++;
		timeSlicesInit();
	}

	public long getSliceDuration() {
		return sliceDuration;
	}

	public long getSlicesNumber() {
		return slicesNumber;
	}

	public TimeRegion getTimeRegion() {
		return timeRegion;
	}

	public List<TimeSlice> getTimeSlices() {
		return timeSlices;
	}

	public Map<Long, Long> getTimeSlicesDistribution(final TimeRegion testedTimeRegion) {
		final Map<Long, Long> timeSlicesDistribution = new HashMap<Long, Long>();
		long startSlice = Math.max(0, (testedTimeRegion.getTimeStampStart()/sliceDuration)-1);
		long temp = 0;
		for (long i = startSlice; i< timeSlices.size(); i++){
			TimeSlice it=timeSlices.get((int)i);
			if (it.startIsInsideMe(testedTimeRegion.getTimeStampStart())) {
				startSlice = it.getNumber();
				break;
			}
		}
		for (long i = startSlice; i < slicesNumber; i++) {
			temp = timeSlices.get((int) i).regionInsideMe(testedTimeRegion);
			if (temp == 0)
				break;
			else
				timeSlicesDistribution.put(i, temp);
		}
		return timeSlicesDistribution;
	}

	public void printInfos() {
		System.out.println("TimeSliceManager: " + slicesNumber + " slices, " + sliceDuration + " ns duration");
	}

	public void setValues(final List<Integer> values) {
		for (int i = 0; i < values.size(); i++)
			timeSlices.get(i).setValue(values.get(i));
	}

	public void timeSlicesInit() {
		int i = 0;
		long currentTime = timeRegion.getTimeStampStart();
		while (currentTime < timeRegion.getTimeStampEnd()) {
			timeSlices.add(new TimeSlice(new TimeRegion(currentTime, currentTime + sliceDuration), i));
			currentTime += sliceDuration;
			i++;
		}
		slicesNumber = i;
	}

}
