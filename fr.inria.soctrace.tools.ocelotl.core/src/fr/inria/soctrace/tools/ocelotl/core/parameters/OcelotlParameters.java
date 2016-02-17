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

package fr.inria.soctrace.tools.ocelotl.core.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.tools.ocelotl.core.caches.DataCache;
import fr.inria.soctrace.tools.ocelotl.core.caches.DichotomyCache;
import fr.inria.soctrace.tools.ocelotl.core.config.IVisuConfig;
import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacachePolicy;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.ParameterPPolicy;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.model.SimpleEventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.model.SimpleEventProducerHierarchy.SimpleEventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.settings.OcelotlSettings;
import fr.inria.soctrace.tools.ocelotl.core.statistics.IStatisticOperatorConfig;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;

public class OcelotlParameters {

	// Modify to deactivate JNI
	private static boolean forceJava = false;

	// List of the event producers taken into account for computation
	private List<EventProducer> currentProducers = new ArrayList<EventProducer>();
	// List of the event producers selected through a spatial selection 
	private List<EventProducer> spatiallySelectedProducers = new ArrayList<EventProducer>();
	// List of all the event producers present in the trace
	private List<EventProducer> allEventProducers = new ArrayList<EventProducer>();
	// List of all the event producers that are not filtered out
	private List<EventProducer> unfilteredEventProducers = new ArrayList<EventProducer>();
	// List of all the event producers that are aggregated 
	private List<EventProducer> aggregatedEventProducers = new ArrayList<EventProducer>();
	// List of all the event producer nodes selected through a spatial selection 
	private List<EventProducerNode> selectedEventProducerNodes = new ArrayList<EventProducerNode>();
	
	private List<EventType> eventTypes = new LinkedList<EventType>();
	private List<EventType> allEventTypes;
	private List<EventType> operatorEventTypes;

	private List<List<EventType>> catEventTypes;
	// Hierarchy to display the event producer in the settings windows
    private SimpleEventProducerHierarchy eventProducerHierarchy;
	private int timeSlicesNumber = OcelotlDefaultParameterConstants.TimeSliceNumber;
	private TimeRegion timeRegion;
	private double parameter = OcelotlDefaultParameterConstants.RunParameter;
	private boolean normalize = OcelotlDefaultParameterConstants.Normalize;
	private double threshold = OcelotlDefaultParameterConstants.Threshold;
	private Trace trace = null;
	private int maxEventProducers = OcelotlDefaultParameterConstants.EventProducersPerQuery;
	private int eventsPerThread = OcelotlDefaultParameterConstants.EVENTS_PER_THREAD;
	private int numberOfThread = OcelotlDefaultParameterConstants.NUMBER_OF_THREADS;
	private String dataAggOperator;
	private String visuOperator;
	private String statOperator;
	private String microModelType;
	private boolean spatialSelection;
	private boolean growingQualities = OcelotlDefaultParameterConstants.IncreasingQualities;
	private DataCache dataCache = new DataCache();
	private DichotomyCache dichotomyCache = new DichotomyCache();
	private DatacachePolicy dataCachePolicy = OcelotlDefaultParameterConstants.DEFAULT_CACHE_POLICY;
	private ParameterPPolicy parameterPPolicy = OcelotlDefaultParameterConstants.DEFAULT_PARAMETERP_POLICY;
	private OcelotlSettings	ocelotlSettings;
	private Integer	timeSliceFactor = 1;
	private boolean overvieweEnable = OcelotlDefaultParameterConstants.OVERVIEW_ENABLE;
	private HashMap<EventProducer, Integer> aggregatedLeavesIndex = new HashMap<EventProducer, Integer>();
	private boolean hasLeaveAggregated = false;
	private boolean approximateRebuild = false;
	private String currentMainViewUnit = "";
	private String currentYAxisUnit = "";
	private String currentStatsUnit = "";
	private TimeSliceManager timeSliceManager;
	private boolean aggregatedLeaveEnable = false;
	private int maxNumberOfLeaves;
	// Is there a zone of the display currently selected with the mouse
	private boolean displayedSubselection = false;

	private static boolean jniFlag = true;
	private ITraceTypeConfig iTraceTypeConfig;
	private IVisuConfig iVisuConfig;
	private IStatisticOperatorConfig statisticOperatorConfig;
	private StatisticsTableSettings sortTableSettings = new StatisticsTableSettings();

	public OcelotlParameters() {
		super();
		ocelotlSettings = new OcelotlSettings();
	}
	
	public OcelotlParameters(OcelotlParameters op) {
		super();
		this.currentProducers = op.currentProducers;
		// Make a deep copy
		this.spatiallySelectedProducers = new ArrayList<EventProducer>(op.spatiallySelectedProducers);
		this.unfilteredEventProducers = new ArrayList<EventProducer>(op.unfilteredEventProducers);
		this.aggregatedEventProducers = new ArrayList<EventProducer>(op.aggregatedEventProducers);
		this.selectedEventProducerNodes = new ArrayList<EventProducerNode>(op.selectedEventProducerNodes);
		this.currentProducers = op.currentProducers;
		this.eventTypes = op.eventTypes;
		this.allEventTypes = op.allEventTypes;
		this.operatorEventTypes = op.operatorEventTypes;
		this.allEventProducers = op.allEventProducers;
		this.catEventTypes = op.catEventTypes;
		this.eventProducerHierarchy = op.eventProducerHierarchy;
		this.timeSlicesNumber = op.timeSlicesNumber;
		this.timeRegion = op.timeRegion;
		this.parameter = op.parameter;
		this.normalize = op.normalize;
		this.threshold = op.threshold;
		this.trace = op.trace;
		this.maxEventProducers = op.maxEventProducers;
		this.eventsPerThread = op.eventsPerThread;
		this.numberOfThread = op.numberOfThread;
		this.dataAggOperator = op.dataAggOperator;
		this.visuOperator = op.visuOperator;
		this.statOperator = op.statOperator;
		this.microModelType = op.microModelType;
		this.spatialSelection = op.spatialSelection;
		this.growingQualities = op.growingQualities;
		this.dataCache = op.dataCache;
		this.dataCachePolicy = op.dataCachePolicy;
		this.dichotomyCache = op.dichotomyCache;
		this.ocelotlSettings = op.ocelotlSettings;
		this.timeSliceManager = op.timeSliceManager;
		this.iTraceTypeConfig = op.iTraceTypeConfig;
		this.iVisuConfig = op.iVisuConfig;
		this.statisticOperatorConfig = op.statisticOperatorConfig;
		this.timeSliceFactor = op.timeSliceFactor;
		this.hasLeaveAggregated = op.hasLeaveAggregated;
		this.approximateRebuild = op.approximateRebuild;
		this.currentMainViewUnit = op.currentMainViewUnit;
		this.sortTableSettings = op.sortTableSettings;
	}
	
	public int getMaxEventProducers() {
		return maxEventProducers;
	}

	public double getParameter() {
		return parameter;
	}

	public String getVisuOperator() {
		return visuOperator;
	}

	public double getThreshold() {
		return threshold;
	}

	public String getDataAggOperator() {
		return dataAggOperator;
	}

	public TimeRegion getTimeRegion() {
		return timeRegion;
	}

	public int getTimeSlicesNumber() {
		return timeSlicesNumber;
	}

	public Trace getTrace() {
		return trace;
	}

	public ITraceTypeConfig getTraceTypeConfig() {
		return iTraceTypeConfig;
	}

	public boolean isGrowingQualities() {
		return growingQualities;
	}

	public boolean isNormalize() {
		return normalize;
	}

	public void setGrowingQualities(final boolean growingQualities) {
		this.growingQualities = growingQualities;
	}

	public void setMaxEventProducers(final int maxEventProducers) {
		this.maxEventProducers = maxEventProducers;
	}

	public void setNormalize(final boolean normalize) {
		this.normalize = normalize;
	}

	public void setParameter(final double parameter) {
		this.parameter = parameter;
	}

	public void setVisuOperator(final String visuOperator) {
		this.visuOperator = visuOperator;
	}

	public void setThreshold(final double threshold) {
		this.threshold = threshold;
	}

	public void setDataAggOperator(final String timeAggOperator) {
		this.dataAggOperator = timeAggOperator;
	}

	public void setTimeRegion(final TimeRegion timeRegion) {
		this.timeRegion = timeRegion;
	}

	public void setTimeSlicesNumber(final int timeSlicesNumber) {
		this.timeSlicesNumber = timeSlicesNumber;
	}

	public void setTrace(final Trace trace) {
		this.trace = trace;
	}

	public void setTraceTypeConfig(final ITraceTypeConfig iTraceTypeConfig) {
		this.iTraceTypeConfig = iTraceTypeConfig;
	}

	public static boolean isJniFlag() {
		return jniFlag;
	}

	public static void setJniFlag(boolean jniFlag) {
		OcelotlParameters.jniFlag = jniFlag;
	}

	public static boolean isForceJava() {
		return forceJava;
	}

	public void setForceJava(boolean forceJava) {
		OcelotlParameters.forceJava = forceJava;
	}

	public IVisuConfig getVisuConfig() {
		return iVisuConfig;
	}

	public void setVisuConfig(IVisuConfig iVisuConfig) {
		this.iVisuConfig = iVisuConfig;
	}
	
	public DataCache getDataCache() {
		return dataCache;
	}

	public void setDataCache(DataCache dataCache) {
		this.dataCache = dataCache;
	}

	public DichotomyCache getDichotomyCache() {
		return dichotomyCache;
	}

	public void setDichotomyCache(DichotomyCache dichotomyCache) {
		this.dichotomyCache = dichotomyCache;
	}

	public SimpleEventProducerHierarchy getEventProducerHierarchy() {
		return eventProducerHierarchy;
	}

	public void setEventProducerHierarchy(
			SimpleEventProducerHierarchy eventProducerHierarchy) {
		this.eventProducerHierarchy = eventProducerHierarchy;
		checkLeaveAggregation();
	}

	public TimeSliceManager getTimeSliceManager() {
		return timeSliceManager;
	}

	public void setTimeSliceManager(TimeSliceManager timeSliceManager) {
		this.timeSliceManager = timeSliceManager;
	}

	public List<EventType> getAllEventTypes() {
		return allEventTypes;
	}

	public void setAllEventTypes(List<EventType> allEventTypes) {
		this.allEventTypes = allEventTypes;
	}

	public OcelotlSettings getOcelotlSettings() {
		return ocelotlSettings;
	}

	public void setOcelotlSettings(OcelotlSettings ocelotlSettings) {
		this.ocelotlSettings = ocelotlSettings;
	}

	public List<EventType> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<EventType> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public List<EventType> getOperatorEventTypes() {
		return operatorEventTypes;
	}

	public void setOperatorEventTypes(List<EventType> operatorEventTypes) {
		this.operatorEventTypes = operatorEventTypes;
	}

	public List<List<EventType>> getCatEventTypes() {
		return catEventTypes;
	}
	
	public List<EventType> getEventTypes(int category) {
		return catEventTypes.get(category);
	}

	public void setCatEventTypes(List<List<EventType>> catEventTypes) {
		this.catEventTypes = catEventTypes;
	}

	public List<EventProducer> getAllEventProducers() {
		return allEventProducers;
	}

	public void setAllEventProducers(List<EventProducer> list) {
		this.allEventProducers = list;
	}

	public DatacachePolicy getDataCachePolicy() {
		return dataCachePolicy;
	}

	public void setDataCachePolicy(DatacachePolicy dataCachePolicy) {
		this.dataCachePolicy = dataCachePolicy;
	}

	public int getNumberOfThreads() {
		return numberOfThread;
	}

	public void setNumberOfThread(int threadNumber) {
		this.numberOfThread = threadNumber;
	}

	public int getEventsPerThread() {
		return eventsPerThread;
	}

	public void setEventsPerThread(int eventsPerThread) {
		this.eventsPerThread = eventsPerThread;
	}

	public Integer getTimeSliceFactor() {
		return timeSliceFactor;
	}

	public void setTimeSliceFactor(Integer timeSliceFactor) {
		this.timeSliceFactor = timeSliceFactor;
	}

	public String getMicroModelType() {
		return microModelType;
	}

	public void setMicroModelType(String microModelType) {
		this.microModelType = microModelType;
	}

	public IStatisticOperatorConfig getStatisticOperatorConfig() {
		return statisticOperatorConfig;
	}

	public void setStatisticOperatorConfig(IStatisticOperatorConfig statisticOperatorConfig) {
		this.statisticOperatorConfig = statisticOperatorConfig;
	}

	public ParameterPPolicy getParameterPPolicy() {
		return parameterPPolicy;
	}

	public void setParameterPPolicy(ParameterPPolicy parameterPPolicy) {
		this.parameterPPolicy = parameterPPolicy;
	}

	public String getStatOperator() {
		return statOperator;
	}

	public void setStatOperator(String statOperator) {
		this.statOperator = statOperator;
	}

	public boolean isOvervieweEnable() {
		return overvieweEnable;
	}

	public void setOvervieweEnable(boolean overvieweEnable) {
		this.overvieweEnable = overvieweEnable;
	}

	public boolean isHasLeaveAggregated() {
		return hasLeaveAggregated;
	}

	public void setHasLeaveAggregated(boolean hasLeaveAggregated) {
		this.hasLeaveAggregated = hasLeaveAggregated;
	}

	public HashMap<EventProducer, Integer> getAggregatedLeavesIndex() {
		return aggregatedLeavesIndex;
	}

	public void setAggregatedLeavesIndex(
			HashMap<EventProducer, Integer> aggregatedLeavesIndex) {
		this.aggregatedLeavesIndex = aggregatedLeavesIndex;
	}

	public List<EventProducer> getCurrentProducers() {
		return currentProducers;
	}

	public void setCurrentProducers(List<EventProducer> selectedEventProducers) {
		// Make sure we make a deep copy
		this.currentProducers = new ArrayList<EventProducer>();
		this.currentProducers.addAll(selectedEventProducers);
	}
	
	public List<EventProducer> getUnfilteredEventProducers() {
		return unfilteredEventProducers;
	}

	public void setUnfilteredEventProducers(
			List<EventProducer> unfilteredEventProducers) {
		this.unfilteredEventProducers = unfilteredEventProducers;
	}

	public List<EventProducer> getAggregatedEventProducers() {
		return aggregatedEventProducers;
	}

	public void setAggregatedEventProducers(
			List<EventProducer> aggregatedEventProducers) {
		this.aggregatedEventProducers = aggregatedEventProducers;
	}

	public List<EventProducer> getSpatiallySelectedProducers() {
		return spatiallySelectedProducers;
	}

	public void setSpatiallySelectedProducers(
			List<EventProducer> spatiallySelectedProducers) {
		// Make sure we make a deep copy
		this.spatiallySelectedProducers = new ArrayList<EventProducer>();
		this.spatiallySelectedProducers.addAll(spatiallySelectedProducers);
	}
	
	public List<EventProducerNode> getSelectedEventProducerNodes() {
		return selectedEventProducerNodes;
	}

	public void setSelectedEventProducerNodes(
			List<EventProducerNode> selectedEventProducerNodes) {
		// Make sure we make a deep copy
		this.selectedEventProducerNodes = new ArrayList<EventProducerNode>();
		this.selectedEventProducerNodes.addAll(selectedEventProducerNodes);
	}

	public String getCurrentMainViewUnit() {
		return currentMainViewUnit;
	}

	public StatisticsTableSettings getSortTableSettings() {
		return sortTableSettings;
	}

	public void setSortTableSettings(StatisticsTableSettings sortTableSettings) {
		this.sortTableSettings = sortTableSettings;
	}

	public boolean isSpatialSelection() {
		return spatialSelection;
	}

	public void setSpatialSelection(boolean spatialSelection) {
		this.spatialSelection = spatialSelection;
	}
	
	public boolean isDisplayedSubselection() {
		return displayedSubselection;
	}

	public void setDisplayedSubselection(boolean displayedSubselection) {
		this.displayedSubselection = displayedSubselection;
	}

	public boolean isApproximateRebuild() {
		return approximateRebuild;
	}

	public void setApproximateRebuild(boolean approximateRebuild) {
		this.approximateRebuild = approximateRebuild;
	}

	public boolean isAggregatedLeaveEnable() {
		return aggregatedLeaveEnable;
	}

	public void setAggregatedLeaveEnable(boolean aggregatedLeaveEnable) {
		this.aggregatedLeaveEnable = aggregatedLeaveEnable;
	}

	public int getMaxNumberOfLeaves() {
		return maxNumberOfLeaves;
	}

	public void setMaxNumberOfLeaves(int maxNumberOfLeaves) {
		this.maxNumberOfLeaves = maxNumberOfLeaves;
	}

	/**
	 * Update the selected producers when the filtered event producers has
	 * changed
	 */
	public void updateCurrentProducers() {
		// If there is no current spatial selection
		if (spatialSelection == false) {
			// Then selectedProducer is identical to eventProducers
			setCurrentProducers(unfilteredEventProducers);
		} else {
			ArrayList<EventProducer> currentSelection = new ArrayList<EventProducer>();

			// Make the intersection of the selected producers and the filtered
			// ones
			for (EventProducer anEP : unfilteredEventProducers) {
				if (spatiallySelectedProducers.contains(anEP)) {
					currentSelection.add(anEP);

					// If there are aggregated leaves then add them to the
					// selection
					if (aggregatedLeavesIndex.containsKey(anEP)) {
						List<SimpleEventProducerNode> childNodes = eventProducerHierarchy
								.getAllChildrenNodes(eventProducerHierarchy
										.getEventProducerNodes().get(
												anEP.getId()));
						for (SimpleEventProducerNode anAggregEPN : childNodes) {
							if (aggregatedEventProducers.contains(anAggregEPN
									.getMe())
									&& unfilteredEventProducers
											.contains(anAggregEPN.getMe()))
								currentSelection.add(anAggregEPN.getMe());
						}
					}
				}
			}

			setCurrentProducers(currentSelection);
		}
	}

	/**
	 * Check if there will be leave aggregation
	 */
	public void checkLeaveAggregation() {
		setHasLeaveAggregated(false);
		int numberOfLeaves = 0;

		// Get the current number of leaves
		if (aggregatedLeaveEnable) {
			for (EventProducer anEP : currentProducers)
				if (getEventProducerHierarchy().getLeaves().keySet()
						.contains(anEP.getId()))
					numberOfLeaves++;
		} else {
			return;
		}
		
		if (numberOfLeaves > maxNumberOfLeaves)
			setHasLeaveAggregated(true);
	}
	
	/**
	 * Get the corresponding units
	 * 
	 * @param aUnitType
	 *            the unit type provided by the extension point
	 * @return the corresponding unit as String
	 */
	public void setMainViewUnit(String aUnitType) {
		this.currentMainViewUnit=UnitManager.getUnit(this, aUnitType);
	}
	
	/**
	 * Get the corresponding units
	 * 
	 * @param aUnitType
	 *            the unit type provided by the extension point
	 * @return the corresponding unit as String
	 */
	public void setYAxisUnit(String aUnitType) {
		this.currentYAxisUnit=UnitManager.getUnit(this, aUnitType);
	}
	
	/**
	 * Get the corresponding units
	 * 
	 * @param aUnitType
	 *            the unit type provided by the extension point
	 * @return the corresponding unit as String
	 */
	public void setStatsUnit(String aUnitType) {
		this.currentStatsUnit=UnitManager.getUnit(this, aUnitType);
	}

	public String getCurrentYAxisUnit() {
		return currentYAxisUnit;
	}

	public String getCurrentStatsUnit() {
		return currentStatsUnit;
	}

	
}