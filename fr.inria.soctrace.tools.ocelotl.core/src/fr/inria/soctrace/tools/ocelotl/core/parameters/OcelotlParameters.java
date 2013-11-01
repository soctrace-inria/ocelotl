/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.tools.ocelotl.core.generic.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class OcelotlParameters {

	private List<EventProducer>	eventProducers		= new ArrayList<EventProducer>();
	// private List<EventType> eventTypes = new ArrayList<EventType>(); ;
	// private List<String> sleepingStates = new ArrayList<String>(); ;
	private int					timeSlicesNumber	= 1;
	private TimeRegion			timeRegion;
	private float				parameter			= 0;
	private boolean				normalize			= false;
	private float				threshold			= (float) 0.001;
	private Trace				trace				= null;
	private int					maxEventProducers	= 0;
	private String				aggOperator;
	private String				aggOperator2;
	private boolean				growingQualities	= true;
	private boolean				cache				= true;
	private int					epCache				= 100;
	private int					pageCache			= 20;
	private int					thread				= 5;
	private ITraceTypeConfig		iTraceTypeConfig;

	public OcelotlParameters() {
		super();
	}

	public String getAggOperator() {
		return aggOperator;
	}

	public int getEpCache() {
		return epCache;
	}

	public List<EventProducer> getEventProducers() {
		return eventProducers;
	}

	public int getMaxEventProducers() {
		return maxEventProducers;
	}

	public int getPageCache() {
		return pageCache;
	}

	public float getParameter() {
		return parameter;
	}

	public int getThread() {
		return thread;
	}

	public float getThreshold() {
		return threshold;
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

	public boolean isCache() {
		return cache;
	}

	public boolean isGrowingQualities() {
		return growingQualities;
	}

	public boolean isNormalize() {
		return normalize;
	}

	// public List<String> getSleepingStates() {
	// return sleepingStates;
	// }

	public void setAggOperator(final String aggOperator) {
		this.aggOperator = aggOperator;
	}
	
	

	public String getAggOperator2() {
		return aggOperator2;
	}

	public void setAggOperator2(String aggOperator2) {
		this.aggOperator2 = aggOperator2;
	}

	public void setCache(final boolean cache) {
		this.cache = cache;
	}

	public void setEpCache(final int epCache) {
		this.epCache = epCache;
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

	public void setPageCache(final int pageCache) {
		this.pageCache = pageCache;
	}

	public void setParameter(final float parameter) {
		this.parameter = parameter;
	}

	// public void setSleepingStates(final List<String> sleepingStates) {
	// this.sleepingStates = sleepingStates;
	// }

	public void setThread(final int thread) {
		this.thread = thread;
	}

	public void setThreshold(final float threshold) {
		this.threshold = threshold;
	}

	public void setTimeRegion(final TimeRegion timeRegion) {
		this.timeRegion = timeRegion;
	}

	public void setTimeSlicesNumber(final int timeSlicesNumber) {
		this.timeSlicesNumber = timeSlicesNumber;
	}

	// @Override
	// public String toString() { // TODO update this
	// final StringBuilder builder = new StringBuilder();
	// builder.append("OcelotlParameters \n[\neventProducers=").append(eventProducers).append("\neventTypes=").append(eventTypes).append("\nsleepingStates=").append(sleepingStates).append("\ntimeSlicesNumber=").append(timeSlicesNumber)
	// .append("\ntimeRegion=").append(timeRegion).append("\nparameter=").append(parameter).append("\nnormalize=").append(normalize).append("\nthreshold=").append(threshold).append("\ntrace=").append(trace).append("\n]");
	// return builder.toString();
	// }

	public void setTrace(final Trace trace) {
		this.trace = trace;
	}

	public void setTraceTypeConfig(final ITraceTypeConfig iTraceTypeConfig) {
		this.iTraceTypeConfig = iTraceTypeConfig;
	}

}