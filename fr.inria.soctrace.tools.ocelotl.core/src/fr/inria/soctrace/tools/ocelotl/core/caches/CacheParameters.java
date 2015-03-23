/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.core.caches;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlDefaultParameterConstants;
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
	private String dataAggOperator;
	private String microModelType;
	private String visuAggOperator;
	private double treshold;
	private boolean normalized;
	private List<Integer> eventProducers;
	private List<Integer> eventTypes;

	public List<Integer> getEventProducers() {
		return eventProducers;
	}

	public void setEventProducers(List<Integer> eventProducers) {
		this.eventProducers = eventProducers;
	}

	public List<Integer> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<Integer> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public CacheParameters() {
		traceName = "";
		traceID = -1;
		startTimestamp = 0L;
		endTimestamp = 0L;
		nbTimeSlice = 0;
		dataAggOperator = "null";
		visuAggOperator = "null";
		microModelType = "null";
		treshold = OcelotlDefaultParameterConstants.Threshold;
		normalized = false;
		eventProducers = new ArrayList<Integer>();
		eventTypes = new ArrayList<Integer>();
	}

	/**
	 * Create a CacheParameter from an OcelotlParameter
	 * 
	 * @param oParam
	 *            the Ocelotl parameters
	 */
	public CacheParameters(OcelotlParameters oParam) {
		traceName = oParam.getTrace().getAlias();
		traceID = oParam.getTrace().getId();

		startTimestamp = oParam.getTimeRegion().getTimeStampStart();
		endTimestamp = oParam.getTimeRegion().getTimeStampEnd();
		nbTimeSlice = oParam.getTimeSlicesNumber();
		treshold = oParam.getThreshold();
		normalized = oParam.isNormalize();
		eventProducers = new ArrayList<Integer>();
		eventTypes = new ArrayList<Integer>();
		
		for (EventType anET : oParam.getTraceTypeConfig().getTypes())
			eventTypes.add(anET.getId());
		
		for (EventProducer anEP : oParam.getCurrentProducers())
			eventProducers.add(anEP.getId());

		if (oParam.getDataAggOperator() == null) {
			dataAggOperator = "null";
		} else {
			dataAggOperator = oParam.getDataAggOperator();
		}

		if (oParam.getVisuOperator() == null) {
			visuAggOperator = "null";
		} else {
			visuAggOperator = oParam.getVisuOperator();
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

	public String getDataAggOperator() {
		return dataAggOperator;
	}

	public void setDataAggOperator(String timeAggOperator) {
		this.dataAggOperator = timeAggOperator;
	}

	public String getVisuAggOperator() {
		return visuAggOperator;
	}

	public void setVisuAggOperator(String spaceAggOperator) {
		this.visuAggOperator = spaceAggOperator;
	}

	public String getMicroModelType() {
		return microModelType;
	}

	public void setMicroModelType(String microModelType) {
		this.microModelType = microModelType;
	}

	public double getTreshold() {
		return treshold;
	}

	public void setTreshold(double treshold) {
		this.treshold = treshold;
	}

	public boolean isNormalized() {
		return normalized;
	}

	public void setNormalized(boolean normalized) {
		this.normalized = normalized;
	}
}
