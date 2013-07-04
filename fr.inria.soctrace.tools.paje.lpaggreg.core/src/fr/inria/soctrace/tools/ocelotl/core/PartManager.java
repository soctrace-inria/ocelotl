/* ===========================================================
 * LPAggreg core module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to visualize a Paje
 * trace across an aggregated representation.
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
 */

package fr.inria.soctrace.tools.ocelotl.core;

import java.util.ArrayList;
import java.util.List;

public class PartManager {

	private LPAggregCore		lpaggregCore;
	private ILPAggregManager	lpaggregManager;
	private List<TimeRegion>	timeStamps	= new ArrayList<TimeRegion>();
	private TimeRegion			traceRegion;
	private int					timeSliceNumber;

	public PartManager(LPAggregCore lpaggregCore) {
		super();
		this.lpaggregCore = lpaggregCore;
		this.lpaggregManager = lpaggregCore.getLpaggregManager();
		this.timeSliceNumber = lpaggregManager.getParts().size();
		this.traceRegion = lpaggregCore.getLpaggregParameters().getTimeRegion();
		computeTimeStamps();
	}

	private void computeTimeStamps(){
		int oldPart=0;
		timeStamps.add(new TimeRegion(traceRegion.getTimeStampStart(), traceRegion.getTimeStampEnd()));
		for (int i=1; i<lpaggregManager.getParts().size()-1; i++){
			if (lpaggregManager.getParts().get(i)!=oldPart){
				timeStamps.get(timeStamps.size()-1).setTimeStampEnd(traceRegion.getTimeStampStart()+(traceRegion.getTimeDuration()*i/timeSliceNumber));
				timeStamps.add(new TimeRegion(traceRegion.getTimeStampStart()+(traceRegion.getTimeDuration()*i/timeSliceNumber), traceRegion.getTimeStampStart()+(traceRegion.getTimeDuration()*i/timeSliceNumber)));
				oldPart=lpaggregManager.getParts().get(i);
			}
		}
		timeStamps.get(timeStamps.size()-1).setTimeStampEnd(traceRegion.getTimeStampEnd());
	}

	public LPAggregCore getLpaggregCore() {
		return lpaggregCore;
	}

	public void setLpaggregCore(LPAggregCore lpaggregCore) {
		this.lpaggregCore = lpaggregCore;
		this.lpaggregManager = lpaggregCore.getLpaggregManager();
		this.timeSliceNumber = lpaggregManager.getParts().size();
		this.traceRegion = lpaggregCore.getLpaggregParameters().getTimeRegion();
		computeTimeStamps();
	}

	public ILPAggregManager getLpaggregManager() {
		return lpaggregManager;
	}

	public List<TimeRegion> getTimeStamps() {
		return timeStamps;
	}

	public TimeRegion getTraceRegion() {
		return traceRegion;
	}

	public int getTimeSliceNumber() {
		return timeSliceNumber;
	}
	
	public void print(){
		System.out.println("");
		System.out.println("*******************");
		System.out.println("AGGREGATION RESULTS");
		System.out.println("*******************");
		System.out.println("");
		System.out.println("Time region:  ["+traceRegion.getTimeStampStart()+" - "+traceRegion.getTimeStampEnd()+"] - duration: "+traceRegion.getTimeDuration());
		System.out.println("Time slice number: "+timeSliceNumber);
		System.out.println("Aggregation operator: "+lpaggregCore.lpaggregParameters.getAggOperator());
		System.out.println("Gain/Loss parameter p: "+lpaggregCore.lpaggregParameters.getParameter());
		System.out.println("*******************");
		System.out.println("");
		System.out.println("Aggregation timestamps:");
		for (TimeRegion tr: timeStamps)
			System.out.print(tr.getTimeStampStart()+", ");
		System.out.print(timeStamps.get(timeStamps.size()-1).getTimeStampEnd());
	}
	


}
