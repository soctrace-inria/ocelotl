package fr.inria.soctrace.tools.ocelotl.core.timeslice;

import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class TimeSliceVariableManager extends TimeSliceStateManager{

	private double value;

	public TimeSliceVariableManager(TimeRegion timeRegion, long slicesNumber, double value) {
		super(timeRegion, slicesNumber);
		this.value = value;
	}

	public Map<Long, Long> getTimeSlicesDistribution(
			final TimeRegion testedTimeRegion) {
		final Map<Long, Long> timeSlicesDistribution = new HashMap<Long, Long>();
		long startSlice = Math.max(
				0,
				(testedTimeRegion.getTimeStampStart() - timeRegion
						.getTimeStampStart()) / sliceDuration - 1);
		long temp = 0;
		if (testedTimeRegion.getTimeStampStart()
				- timeRegion.getTimeStampStart() >= 0)
			for (long i = startSlice; i < timeSlices.size(); i++) {
				final TimeSlice it = timeSlices.get((int) i);
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
				timeSlicesDistribution.put(i, (long) (((double) temp/ (double) sliceDuration) * value));
		}
		return timeSlicesDistribution;
	}
}
