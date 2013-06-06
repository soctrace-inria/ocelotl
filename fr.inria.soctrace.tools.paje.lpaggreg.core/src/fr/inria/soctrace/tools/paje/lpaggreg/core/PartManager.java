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

package fr.inria.soctrace.tools.paje.lpaggreg.core;

import java.util.ArrayList;
import java.util.List;

public class PartManager {

	private TimeSliceManager	timeSliceManager;
	private LPAggregManager		lpaggregManager;
	private List<TimeSlice>		timeSlices	= new ArrayList<TimeSlice>();

	public PartManager(LPAggregManager lpaggregManager) {
		super();
		this.lpaggregManager = lpaggregManager;
		//timeSliceManager = lpaggregManager.getTimeSliceMatrix().getTimeSlicesManager();
		setTimeSlices();
	}

	public LPAggregManager getLpaggregManager() {
		return lpaggregManager;
	}

	public List<TimeSlice> getTimeSlices() {
		return timeSlices;
	}

	public TimeSliceManager getTimeSlicesManager() {
		return timeSliceManager;
	}

	public void setTimeSlices() {
		timeSliceManager.setValues(lpaggregManager.getParts());
		int j = 0;
		timeSlices.add(new TimeSlice(new TimeRegion(timeSliceManager.getTimeSlices().get(j).getTimeRegion().getTimeStampStart(), timeSliceManager.getTimeSlices().get(j).getTimeRegion().getTimeStampEnd()), j, timeSliceManager.getTimeSlices().get(j).getValue()));
		for (int i = 1; i < timeSliceManager.getTimeSlices().size(); i++)
			if (timeSliceManager.getTimeSlices().get(i).getValue() == timeSliceManager.getTimeSlices().get(i - 1).getValue())
				timeSlices.get(j).getTimeRegion().setTimeStampEnd(timeSliceManager.getTimeSlices().get(i).getTimeRegion().getTimeStampEnd());
			else {
				j++;
				timeSlices.add(new TimeSlice(new TimeRegion(timeSliceManager.getTimeSlices().get(i).getTimeRegion().getTimeStampStart(), timeSliceManager.getTimeSlices().get(i).getTimeRegion().getTimeStampEnd()), j, timeSliceManager.getTimeSlices().get(i).getValue()));
			}
	}

}
