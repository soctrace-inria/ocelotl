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

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class PartManager {

	private OcelotlCore lpaggregCore;
	private ITimeManager lpaggregManager;
	private final List<TimeRegion> timeStamps = new ArrayList<TimeRegion>();
	private TimeRegion traceRegion;
	private int timeSliceNumber;
	
	private static final Logger logger = LoggerFactory.getLogger(PartManager.class);
	

	public PartManager(final OcelotlCore lpaggregCore) {
		super();
		setLpaggregCore(lpaggregCore);
	}

	private void computeTimeStamps() {
		int oldPart = 0;
		timeStamps.add(new TimeRegion(traceRegion.getTimeStampStart(),
				traceRegion.getTimeStampEnd()));
		for (int i = 1; i < lpaggregManager.getParts().size() - 1; i++)
			if (lpaggregManager.getParts().get(i) != oldPart) {
				timeStamps.get(timeStamps.size() - 1).setTimeStampEnd(
						traceRegion.getTimeStampStart()
								+ traceRegion.getTimeDuration() * i
								/ timeSliceNumber);
				timeStamps.add(new TimeRegion(traceRegion.getTimeStampStart()
						+ traceRegion.getTimeDuration() * i / timeSliceNumber,
						traceRegion.getTimeStampStart()
								+ traceRegion.getTimeDuration() * i
								/ timeSliceNumber));
				oldPart = lpaggregManager.getParts().get(i);
			}
		timeStamps.get(timeStamps.size() - 1).setTimeStampEnd(
				traceRegion.getTimeStampEnd());
	}

	public OcelotlCore getLpaggregCore() {
		return lpaggregCore;
	}

	public ITimeManager getLpaggregManager() {
		return lpaggregManager;
	}

	public int getTimeSliceNumber() {
		return timeSliceNumber;
	}

	public List<TimeRegion> getTimeStamps() {
		return timeStamps;
	}

	public TimeRegion getTraceRegion() {
		return traceRegion;
	}

	public void print() {
	

		logger.info("");
		logger.info("*******************");
		logger.info("AGGREGATION RESULTS");
		logger.info("*******************");
		logger.info("");
		logger.info("Time region:  [" + traceRegion.getTimeStampStart()
				+ " - " + traceRegion.getTimeStampEnd() + "] - duration: "
				+ traceRegion.getTimeDuration());
		logger.info("Time slice number: " + timeSliceNumber);
		//logger.info("Time slice duration: " + lpaggregCore.getOcelotlParameters().getTimeSliceManager().getSliceDuration());
		logger.info("Aggregation timeOperator: "
				+ lpaggregCore.getOcelotlParameters().getTimeAggOperator());
		logger.info("Gain/Loss parameter p: "
				+ lpaggregCore.getOcelotlParameters().getParameter());
		logger.info("*******************");
		logger.info("");
		logger.info("Aggregation timestamps:");
		StringBuffer buff = new StringBuffer();
		for (final TimeRegion tr : timeStamps)
			buff.append(tr.getTimeStampStart() + ", ");
		buff.append(timeStamps.get(timeStamps.size() - 1)
				.getTimeStampEnd());
		logger.info(buff.toString());
		logger.info("");

	}

	public void setLpaggregCore(final OcelotlCore lpaggregCore) {
		this.lpaggregCore = lpaggregCore;
		lpaggregManager = (ITimeManager) lpaggregCore.getLpaggregManager();
		timeSliceNumber = lpaggregManager.getParts().size();
		traceRegion = lpaggregCore.getOcelotlParameters().getTimeRegion();
		computeTimeStamps();
	}

}
