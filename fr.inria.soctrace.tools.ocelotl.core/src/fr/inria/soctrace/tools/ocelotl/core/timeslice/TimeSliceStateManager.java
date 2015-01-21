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

package fr.inria.soctrace.tools.ocelotl.core.timeslice;

import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class TimeSliceStateManager extends TimeSliceManager {

	public TimeSliceStateManager(final TimeRegion timeRegion,
			final long slicesNumber) {// TODO use region
		super(timeRegion, slicesNumber);
	}

	/**
	 * Set the length of the state in each time slice
	 * 
	 * @param testedTimeRegion
	 *            the start and end value of the state (i.e. its duration
	 * @return a hashmap, with the time slice number oas the key and the
	 *         duration of the state in teh time slice as the value
	 */
	public Map<Long, Double> getStateDistribution(
			final TimeRegion testedTimeRegion) {
		final Map<Long, Double> timeSlicesDistribution = new HashMap<Long, Double>();
		// Find the number of the slice where the state event starts
		long startSlice = Math.max(
				0L,
				(long) ((testedTimeRegion.getTimeStampStart() - timeRegion
						.getTimeStampStart()) / sliceDuration) - 1);
		
		double temp = 0;
		// If the state starts within the actual time region
		if (testedTimeRegion.getTimeStampStart()
				- timeRegion.getTimeStampStart() >= 0)
			for (long i = startSlice; i < timeSlices.size(); i++) {
				final TimeSlice it = timeSlices.get((int) i);
				// Make sure we got the right starting time slice?
				if (it.startIsInsideMe(testedTimeRegion.getTimeStampStart())) {
					startSlice = it.getNumber();
					break;
				}
			}
		
		// For each slice
		for (long i = startSlice; i < slicesNumber; i++) {
			// Get the duration of the state in the time slice i
			temp = timeSlices.get((int) i).regionInsideMe(testedTimeRegion);
			// If the state has ended in the previous time slice
			if (temp == 0)
				break;
			else
				timeSlicesDistribution.put(i, (double) temp);
		}
		return timeSlicesDistribution;
	}

}
