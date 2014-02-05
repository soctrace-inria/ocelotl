/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
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
import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class OcelotlParameters {

	private List<EventProducer>	eventProducers		= new ArrayList<EventProducer>();
	private int					timeSlicesNumber	= 1;
	private TimeRegion			timeRegion;
	private double				parameter			= 0;
	private boolean				normalize			= false;
	private double				threshold			= 0.001;
	private Trace				trace				= null;
	private int					maxEventProducers	= 0;
	private String				timeAggOperator;
	private String				spaceAggOperator;
	private boolean				growingQualities	= true;
	private int					eventsPerThread		= 20;
	private int					thread				= 5;
	private ITraceTypeConfig	iTraceTypeConfig;

	public OcelotlParameters() {
		super();
	}

	public List<EventProducer> getEventProducers() {
		return eventProducers;
	}

	public int getEventsPerThread() {
		return eventsPerThread;
	}

	public int getMaxEventProducers() {
		return maxEventProducers;
	}

	public double getParameter() {
		return parameter;
	}

	public String getSpaceAggOperator() {
		return spaceAggOperator;
	}

	public int getThread() {
		return thread;
	}

	public double getThreshold() {
		return threshold;
	}

	public String getTimeAggOperator() {
		return timeAggOperator;
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

	public void setEventsPerThread(final int eventsPerThread) {
		this.eventsPerThread = eventsPerThread;
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

	public void setSpaceAggOperator(final String spaceAggOperator) {
		this.spaceAggOperator = spaceAggOperator;
	}

	public void setThread(final int thread) {
		this.thread = thread;
	}

	public void setThreshold(final double threshold) {
		this.threshold = threshold;
	}

	public void setTimeAggOperator(final String timeAggOperator) {
		this.timeAggOperator = timeAggOperator;
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

}