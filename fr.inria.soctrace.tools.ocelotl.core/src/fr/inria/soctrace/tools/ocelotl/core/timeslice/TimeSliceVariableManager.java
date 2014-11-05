package fr.inria.soctrace.tools.ocelotl.core.timeslice;

import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class TimeSliceVariableManager extends TimeSliceManager{

	public TimeSliceVariableManager(TimeRegion timeRegion, long slicesNumber) {
		super(timeRegion, slicesNumber);
	}

	public Map<Long, Double> getVariableDistribution(
			final TimeRegion testedTimeRegion, double value) {
		final Map<Long, Double> timeSlicesDistribution = new HashMap<Long, Double>();
		long startSlice = Math.max(0L,
				(long) ((testedTimeRegion.getTimeStampStart() - timeRegion
						.getTimeStampStart()) / sliceDuration) - 1);

		double temp = 0;
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
				timeSlicesDistribution.put(i,
						(((double) temp / (double) sliceDuration) * value));
		}
		return timeSlicesDistribution;
	}
}
