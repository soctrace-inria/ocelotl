package fr.inria.soctrace.tools.filters.timefilter;

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
