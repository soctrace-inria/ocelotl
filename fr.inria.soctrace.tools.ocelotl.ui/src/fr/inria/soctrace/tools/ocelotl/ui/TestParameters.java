package fr.inria.soctrace.tools.ocelotl.ui;

import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class TestParameters {
	private String traceName;
	private int traceID;
	private long startTimestamp;
	private long endTimestamp;
	private int nbTimeSlice;
	private String timeAggOperator;
	private String spaceAggOperator;
	private double parameter;

	public TestParameters() {
		traceName = "";
		traceID = -1;
		startTimestamp = 0L;
		endTimestamp = 0L;
		nbTimeSlice = 0;
		timeAggOperator = "null";
		spaceAggOperator = "null";
		parameter = 0.0;
	}

	/**
	 * Create a TestParameter from an OcelotlParameter
	 * 
	 * @param oParam
	 *            the Ocelotl parameters
	 */
	TestParameters(OcelotlParameters oParam) {
		traceName = oParam.getTrace().getAlias();
		traceID = oParam.getTrace().getId();

		startTimestamp = oParam.getTimeRegion().getTimeStampStart();
		endTimestamp = oParam.getTimeRegion().getTimeStampEnd();
		nbTimeSlice = oParam.getTimeSlicesNumber();
		if (oParam.getTimeAggOperator() == null) {
			timeAggOperator = "null";
		} else {
			timeAggOperator = oParam.getTimeAggOperator();
		}

		if (oParam.getSpaceAggOperator() == null) {
			spaceAggOperator = "null";
		} else {
			spaceAggOperator = oParam.getSpaceAggOperator();
		}
	}
	
	public String getTraceName() {
		return traceName;
	}

	public void setTraceName(String traceName) {
		this.traceName = traceName;
	}

	public int getTraceID() {
		return traceID;
	}

	public void setTraceID(int traceID) {
		this.traceID = traceID;
	}

	public long getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public long getEndTimestamp() {
		return endTimestamp;
	}

	public void setEndTimestamp(long endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	public int getNbTimeSlice() {
		return nbTimeSlice;
	}

	public void setNbTimeSlice(int nbTimeSlice) {
		this.nbTimeSlice = nbTimeSlice;
	}

	public String getTimeAggOperator() {
		return timeAggOperator;
	}

	public void setTimeAggOperator(String timeAggOperator) {
		this.timeAggOperator = timeAggOperator;
	}

	public String getSpaceAggOperator() {
		return spaceAggOperator;
	}

	public void setSpaceAggOperator(String spaceAggOperator) {
		this.spaceAggOperator = spaceAggOperator;
	}

	public double getParameter() {
		return parameter;
	}

	public void setParameter(double parameter) {
		this.parameter = parameter;
	}
	
	/**
	 * Load a trace from a cache file
	 * 
	 * @param cacheFilePath
	 */
	public int loadDataTest(OcelotlParameters oParam)
			throws OcelotlException {
		
		// Invalid data file
		if (getTraceID() == -1) {
			throw new OcelotlException(OcelotlException.INVALID_CACHEFILE);
		} else {
			oParam.setTimeSlicesNumber(getNbTimeSlice());
			TimeRegion timeRegion = new TimeRegion(getStartTimestamp(),
					getEndTimestamp());
			oParam.setTimeRegion(timeRegion);
			oParam.setParameter(getParameter());
			
			if (!getTimeAggOperator().equals("null")) {
				oParam.setTimeAggOperator(getTimeAggOperator());
			}

			if (!getSpaceAggOperator().equals("null")) {
				oParam.setSpaceAggOperator(getSpaceAggOperator());
			}
		}

		return getTraceID();
	}
	
	public String toString() {
		String newString = traceName + "_" + traceID + "_" + startTimestamp + "_" + endTimestamp + "_" + nbTimeSlice + "_" + timeAggOperator + "_" + parameter;
		return newString;
	}
}