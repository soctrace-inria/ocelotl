package fr.inria.soctrace.tools.ocelotl.core.datacache;

import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

/**
 * Describe the parameters used to check if a trace can be loaded from cache
 */
public class CacheParameters {
	
		private String traceName;
		private int traceID;
		private long startTimestamp;
		private long endTimestamp;
		private int nbTimeSlice;
		private String timeAggOperator;
		private String microModelType;
		private String spaceAggOperator;

		public CacheParameters() {
			traceName = "";
			traceID = -1;
			startTimestamp = 0L;
			endTimestamp = 0L;
			nbTimeSlice = 0;
			timeAggOperator = "null";
			spaceAggOperator = "null";
			microModelType = "null";
		}

		/**
		 * Create a CacheParameter from an OcelotlParameter
		 * 
		 * @param oParam
		 *            the Ocelotl parameters
		 */
		CacheParameters(OcelotlParameters oParam) {
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
			
			if (oParam.getMicroModelType() == null) {
				microModelType = "null";
			} else {
				microModelType = oParam.getMicroModelType();
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

		public String getMicroModelType() {
			return microModelType;
		}

		public void setMicroModelType(String microModelType) {
			this.microModelType = microModelType;
		}
}
