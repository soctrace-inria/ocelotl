package fr.inria.soctrace.tools.ocelotl.ui;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacacheStrategy;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlDefaultParameterConstants;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class TestParameters {
	private String traceName;
	private int traceID;
	private long startTimestamp;
	private long endTimestamp;
	private int nbTimeSlice;
	private List<Integer> timeSlicesCollection;
	private List<Integer> timeSlicesNumber;
	private int nbEventProd;
	private String microModelType;
	private String dataAggOperator;
	private String visuOperator;
	private List<Double> parameters;
	private DatacacheStrategy datacacheStrat;
	private String directory;
	private int numberOfRepetition;
	private boolean activateCache;
	private int numberOfThreads;
	private int eventPerThread;
	private int numberOfEventProd;
	private int numberOfEventType;
	private List<String> filteredEventType;
	private List<String> filteredEventProducer;
	
	public TestParameters() {
		traceName = "";
		traceID = -1;
		startTimestamp = 0L;
		endTimestamp = 0L;
		nbTimeSlice = 0;
		nbEventProd = 0;
		microModelType = "null";
		visuOperator = "null";
		setParameters(new ArrayList<Double>());
		setDatacacheStrat(DatacacheStrategy.DATACACHE_PROPORTIONAL);
		directory = "";
		setNumberOfRepetition(1);
		setActivateCache(false);
		timeSlicesCollection =  new ArrayList<Integer>();
		timeSlicesNumber = new ArrayList<Integer>();
		filteredEventType = new ArrayList<String>();
		filteredEventProducer = new ArrayList<String>();
		numberOfThreads = OcelotlDefaultParameterConstants.MAX_NUMBER_OF_THREAD;
		eventPerThread = OcelotlDefaultParameterConstants.EVENTS_PER_THREAD;
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
		if (oParam.getMicroModelType() == null) {
			microModelType = "null";
		} else {
			microModelType = oParam.getMicroModelType();
		}
		
		if (oParam.getDataAggOperator() == null) {
			dataAggOperator = "null";
		} else {
			dataAggOperator = oParam.getDataAggOperator();
		}

		if (oParam.getVisuOperator() == null) {
			visuOperator = "null";
		} else {
			visuOperator = oParam.getVisuOperator();
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

	public String getMicroModelType() {
		return microModelType;
	}

	public void setMicroModelType(String timeAggOperator) {
		this.microModelType = timeAggOperator;
	}

	public String getVisuOperator() {
		return visuOperator;
	}

	public void setVisuOperator(String spaceAggOperator) {
		this.visuOperator = spaceAggOperator;
	}

	public String getDataAggOperator() {
		return dataAggOperator;
	}

	public void setDataAggOperator(String dataAggOperator) {
		this.dataAggOperator = dataAggOperator;
	}

	public List<Double> getParameters() {
		return parameters;
	}

	public void setParameters(ArrayList<Double> parameters) {
		this.parameters = parameters;
	}

	public DatacacheStrategy getDatacacheStrat() {
		return datacacheStrat;
	}

	public void setDatacacheStrat(DatacacheStrategy datacacheStrat) {
		this.datacacheStrat = datacacheStrat;
	}
	
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	public int getNumberOfRepetition() {
		return numberOfRepetition;
	}

	public void setNumberOfRepetition(int numberOfRepetition) {
		this.numberOfRepetition = numberOfRepetition;
	}

	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public int getEventPerThread() {
		return eventPerThread;
	}

	public void setEventPerThread(int eventPerThread) {
		this.eventPerThread = eventPerThread;
	}

	public List<Integer> getTimeSlicesNumber() {
		return timeSlicesNumber;
	}

	public void setTimeSlicesNumber(List<Integer> timeSlicesNumber) {
		this.timeSlicesNumber = timeSlicesNumber;
	}

	public boolean isActivateCache() {
		return activateCache;
	}

	public void setActivateCache(boolean activateCache) {
		this.activateCache = activateCache;
	}

	/**
	 * Load a trace from a cache file
	 * 
	 * @param cacheFilePath
	 */
	public int loadDataTest(OcelotlParameters oParam) throws OcelotlException {

		// Invalid data file
		if (getTraceID() == -1) {
			throw new OcelotlException(OcelotlException.INVALID_CACHEFILE);
		} else {
			oParam.setTimeSlicesNumber(getNbTimeSlice());
			TimeRegion timeRegion = new TimeRegion(getStartTimestamp(), getEndTimestamp());
			oParam.setTimeRegion(timeRegion);

			oParam.setParameter(getParameters().get(0));

			if (!getMicroModelType().equals("null")) {
				oParam.setMicroModelType(getMicroModelType());
			}
			
			if (!getDataAggOperator().equals("null")) {
				oParam.setDataAggOperator(getDataAggOperator());
			}

			if (!getVisuOperator().equals("null")) {
				oParam.setVisuOperator(getVisuOperator());
			}

			oParam.getDataCache().setBuildingStrategy(getDatacacheStrat());
		}

		return getTraceID();
	}

	public String toString() {
		String newString = traceName + "_" + traceID + "_" + startTimestamp + "_" + endTimestamp + "_" + nbTimeSlice + "_" + microModelType + "_" + datacacheStrat;
		return newString;
	}

	public int getNbEventProd() {
		return nbEventProd;
	}

	public void setNbEventProd(int nbEventProd) {
		this.nbEventProd = nbEventProd;
	}

	public List<String> getFilteredEventType() {
		return filteredEventType;
	}

	public void setFilteredEventType(List<String> filteredEventType) {
		this.filteredEventType = filteredEventType;
	}

	public List<String> getFilteredEventProducer() {
		return filteredEventProducer;
	}

	public void setFilteredEventProducer(List<String> filteredEventProducer) {
		this.filteredEventProducer = filteredEventProducer;
	}

	public List<Integer> getTimeSlicesCollection() {
		return timeSlicesCollection;
	}

	public void setTimeSlicesCollection(List<Integer> timeSlicesCollection) {
		this.timeSlicesCollection = timeSlicesCollection;
	}

	public int getNumberOfEventProd() {
		return numberOfEventProd;
	}

	public void setNumberOfEventProd(int numberOfEventProd) {
		this.numberOfEventProd = numberOfEventProd;
	}

	public int getNumberOfEventType() {
		return numberOfEventType;
	}

	public void setNumberOfEventType(int numberOfEventType) {
		this.numberOfEventType = numberOfEventType;
	}
}