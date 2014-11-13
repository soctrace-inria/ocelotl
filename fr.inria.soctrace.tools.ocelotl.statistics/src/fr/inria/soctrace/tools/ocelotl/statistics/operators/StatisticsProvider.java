package fr.inria.soctrace.tools.ocelotl.statistics.operators;

import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;
import fr.inria.soctrace.tools.ocelotl.core.statistics.IStatisticsProvider;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public abstract class StatisticsProvider implements IStatisticsProvider {
	
	protected OcelotlView ocelotlview;
	protected TimeRegion  timeRegion;
	
	public StatisticsProvider(OcelotlView aView) {
		this.ocelotlview = aView;
	}
	
	public void setTimeRegion(TimeRegion aRegion) {
		timeRegion = aRegion;
	}
	
	public void setTimeRegion(Long startTimeStamp, Long endTimeStamp) {
		timeRegion = new TimeRegion(startTimeStamp, endTimeStamp);
	}
	
	/**
	 * Compute the statistics data
	 */
	public abstract void computeData();
	
	public abstract void setMicroMode(MicroscopicDescription microModel);

	/*
	 * Provide a list of data to put in the table
	 */
	public abstract List getTableData();
	
	/**
	 * Update the color of the event types
	 */
	public abstract void updateColor();
}
