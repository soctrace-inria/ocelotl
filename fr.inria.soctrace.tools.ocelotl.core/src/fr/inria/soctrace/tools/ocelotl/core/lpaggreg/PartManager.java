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

package fr.inria.soctrace.tools.ocelotl.core.lpaggreg;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class PartManager {

	private OcelotlCore				lpaggregCore;
	private ILPAggregManager		lpaggregManager;
	private final List<TimeRegion>	timeStamps	= new ArrayList<TimeRegion>();
	private TimeRegion				traceRegion;
	private int						timeSliceNumber;

	public PartManager(final OcelotlCore lpaggregCore) {
		super();
		this.lpaggregCore = lpaggregCore;
		lpaggregManager = lpaggregCore.getLpaggregManager();
		timeSliceNumber = lpaggregManager.getParts().size();
		traceRegion = lpaggregCore.getOcelotlParameters().getTimeRegion();
		computeTimeStamps();
	}

	private void computeTimeStamps() {
		int oldPart = 0;
		timeStamps.add(new TimeRegion(traceRegion.getTimeStampStart(), traceRegion.getTimeStampEnd()));
		for (int i = 1; i < lpaggregManager.getParts().size() - 1; i++)
			if (lpaggregManager.getParts().get(i) != oldPart) {
				timeStamps.get(timeStamps.size() - 1).setTimeStampEnd(traceRegion.getTimeStampStart() + traceRegion.getTimeDuration() * i / timeSliceNumber);
				timeStamps.add(new TimeRegion(traceRegion.getTimeStampStart() + traceRegion.getTimeDuration() * i / timeSliceNumber, traceRegion.getTimeStampStart() + traceRegion.getTimeDuration() * i / timeSliceNumber));
				oldPart = lpaggregManager.getParts().get(i);
			}
		timeStamps.get(timeStamps.size() - 1).setTimeStampEnd(traceRegion.getTimeStampEnd());
	}

	public OcelotlCore getLpaggregCore() {
		return lpaggregCore;
	}

	public ILPAggregManager getLpaggregManager() {
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
		System.out.println("");
		System.out.println("*******************");
		System.out.println("AGGREGATION RESULTS");
		System.out.println("*******************");
		System.out.println("");
		System.out.println("Time region:  [" + traceRegion.getTimeStampStart() + " - " + traceRegion.getTimeStampEnd() + "] - duration: " + traceRegion.getTimeDuration());
		System.out.println("Time slice number: " + timeSliceNumber);
		System.out.println("Aggregation timeOperator: " + lpaggregCore.getOcelotlParameters().getTimeAggOperator());
		System.out.println("Gain/Loss parameter p: " + lpaggregCore.getOcelotlParameters().getParameter());
		System.out.println("*******************");
		System.out.println("");
		System.out.println("Aggregation timestamps:");
		for (final TimeRegion tr : timeStamps)
			System.out.print(tr.getTimeStampStart() + ", ");
		System.out.print(timeStamps.get(timeStamps.size() - 1).getTimeStampEnd());
		System.out.println();
		System.out.println();
	}

	public void setLpaggregCore(final OcelotlCore lpaggregCore) {
		this.lpaggregCore = lpaggregCore;
		lpaggregManager = lpaggregCore.getLpaggregManager();
		timeSliceNumber = lpaggregManager.getParts().size();
		traceRegion = lpaggregCore.getOcelotlParameters().getTimeRegion();
		computeTimeStamps();
	}

}
