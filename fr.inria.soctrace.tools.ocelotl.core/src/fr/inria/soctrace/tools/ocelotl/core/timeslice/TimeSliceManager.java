package fr.inria.soctrace.tools.ocelotl.core.timeslice;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class TimeSliceManager {

	private static final Logger logger = LoggerFactory.getLogger(TimeSliceManager.class);
	
	protected final List<TimeSlice> timeSlices = new ArrayList<TimeSlice>();
	protected final TimeRegion timeRegion;
	protected long slicesNumber;
	protected double sliceDuration;
	
	
	public TimeSliceManager(final TimeRegion timeRegion,
			final long slicesNumber) {// TODO use region
		super();
		this.timeRegion = timeRegion;
		this.slicesNumber = slicesNumber;
		sliceDuration = ((double) timeRegion.getTimeDuration())
				/ (double) slicesNumber;
		timeSlicesInit();
	}

	public double getSliceDuration() {
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

	/**
	 * Get the number of a time slice the timestamp is in
	 * 
	 * @param timeStamp
	 * @return the number of the time slice the time stamp is in
	 * 
	 */
	public long getTimeSlice(final long timeStamp) {
		long slice = Math.max(0L, (long) Math.floor(((timeStamp - timeRegion
				.getTimeStampStart()) / sliceDuration)) - 1);
		for (long i = slice; i < timeSlices.size(); i++) {
			final TimeSlice it = timeSlices.get((int) i);
			if (it.startIsInsideMe(timeStamp)) {
				slice = it.getNumber();
				break;
			}
		}
		return slice;
	}

	public TimeSlice getATimeSlice(final long timeStamp) {
		TimeSlice slice = null;

		long presumeTimeSlice = Math
				.max(0L,
						(long) ((timeStamp - timeRegion.getTimeStampStart()) / sliceDuration) - 1);
		for (long i = presumeTimeSlice; i < timeSlices.size(); i++) {
			final TimeSlice it = timeSlices.get((int) i);
			if (it.startIsInsideMe(timeStamp)) {
				slice = it;
				break;
			}
		}
		return slice;
	}
	
	public void printInfos() {
		logger.info("TimeSliceManager: " + slicesNumber + " slices, "
				+ sliceDuration + " ns duration");
	}

	public void setValues(final List<Integer> values) {
		for (int i = 0; i < values.size(); i++)
			timeSlices.get(i).setValue(values.get(i));
	}

	public void timeSlicesInit() {
		double currentTime = timeRegion.getTimeStampStart();
		for (int i=0; i<slicesNumber-1; i++){
			timeSlices.add(new TimeSlice(new TimeRegion((long) currentTime,
					(long) (currentTime + sliceDuration)), i));
			currentTime += sliceDuration;
		}
		timeSlices.add(new TimeSlice(new TimeRegion((long) currentTime,
				(long) timeRegion.getTimeStampEnd()), slicesNumber-1));
	}

}
