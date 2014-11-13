/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
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
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.tools.ocelotl.core.config.ISpaceConfig;
import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacachePolicy;
import fr.inria.soctrace.tools.ocelotl.core.model.SimpleEventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.datacache.DataCache;
import fr.inria.soctrace.tools.ocelotl.core.settings.OcelotlSettings;
import fr.inria.soctrace.tools.ocelotl.core.statistics.IStatisticOperatorConfig;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;

public class OcelotlParameters {

	// Modify to deactivate JNI
	private static boolean forceJava = false;

	private List<EventProducer> eventProducers = new ArrayList<EventProducer>();
	private List<EventType> eventTypes = new LinkedList<EventType>();
	private List<EventType> allEventTypes;
	private List<EventType> operatorEventTypes;
	private List<EventProducer> allEventProducers;
	private List<List<EventType>> catEventTypes;
    private SimpleEventProducerHierarchy eventProducerHierarchy;
	private int timeSlicesNumber = OcelotlDefaultParameterConstants.TimeSliceNumber;
	private TimeRegion timeRegion;
	private double parameter = OcelotlDefaultParameterConstants.RunParameter;
	private boolean normalize = OcelotlDefaultParameterConstants.Normalize;
	private double threshold = OcelotlDefaultParameterConstants.Threshold;
	private Trace trace = null;
	private int maxEventProducers = OcelotlDefaultParameterConstants.EventProducersPerQuery;
	private int eventsPerThread = OcelotlDefaultParameterConstants.EVENTS_PER_THREAD;
	private int threadNumber = OcelotlDefaultParameterConstants.NUMBER_OF_THREADS;
	private String dataAggOperator;
	private String visuOperator;
	private String statOperator;
	private String microModelType;
	private boolean growingQualities = OcelotlDefaultParameterConstants.IncreasingQualities;
	private DataCache dataCache = new DataCache();
	private DatacachePolicy dataCachePolicy = OcelotlDefaultParameterConstants.DEFAULT_CACHE_POLICY;
	private OcelotlSettings	ocelotlSettings = new OcelotlSettings();
	
	private TimeSliceManager timeSliceManager;

	private static boolean jniFlag = true;
	private ITraceTypeConfig iTraceTypeConfig;
	private ISpaceConfig iSpaceConfig;
	private IStatisticOperatorConfig statisticOperatorConfig;

	public OcelotlParameters() {
		super();
	}
	
	public OcelotlParameters(OcelotlParameters op) {
		super();
		this.eventProducers = op.eventProducers;
		this.eventProducerHierarchy = op.eventProducerHierarchy;
		this.timeSlicesNumber = op.timeSlicesNumber;
		this.timeRegion = op.timeRegion;
		this.parameter = op.parameter;
		this.normalize = op.normalize;
		this.threshold = op.threshold;
		this.trace = op.trace;
		this.maxEventProducers = op.maxEventProducers;
		this.dataAggOperator = op.dataAggOperator;
		this.visuOperator = op.visuOperator;
		this.microModelType = op.microModelType;
		this.growingQualities = op.growingQualities;
		this.iTraceTypeConfig = op.iTraceTypeConfig;
		this.iSpaceConfig = op.iSpaceConfig;
		this.timeSliceManager = op.timeSliceManager;
		this.ocelotlSettings = op.ocelotlSettings;
	}
	
	
	public List<EventProducer> getEventProducers() {
		return eventProducers;
	}

	public int getMaxEventProducers() {
		return maxEventProducers;
	}

	public double getParameter() {
		return parameter;
	}

	public List<EventType> getTypes() {
		return eventTypes;
	}

	public void setTypes(final List<EventType> types) {
		this.eventTypes = types;
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

	public void setEventProducers(final List<EventProducer> eventProducers) {
		this.eventProducers = eventProducers;
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

	public ISpaceConfig getSpaceConfig() {
		return iSpaceConfig;
	}

	public void setSpaceConfig(ISpaceConfig iSpaceConfig) {
		this.iSpaceConfig = iSpaceConfig;
	}
	
	public DataCache getDataCache() {
		return dataCache;
	}

	public void setDataCache(DataCache dataCache) {
		this.dataCache = dataCache;
	}

	public SimpleEventProducerHierarchy getEventProducerHierarchy() {
		return eventProducerHierarchy;
	}

	public void setEventProducerHierarchy(
			SimpleEventProducerHierarchy eventProducerHierarchy) {
		this.eventProducerHierarchy = eventProducerHierarchy;
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

	public int getThreadNumber() {
		return threadNumber;
	}

	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}

	public int getEventsPerThread() {
		return eventsPerThread;
	}

	public void setEventsPerThread(int eventsPerThread) {
		this.eventsPerThread = eventsPerThread;
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

	public String getStatOperator() {
		return statOperator;
	}

	public void setStatOperator(String statOperator) {
		this.statOperator = statOperator;
	}

}