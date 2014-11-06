package fr.inria.soctrace.tools.ocelotl.statistics.operators;

import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public abstract class StatisticsProvider {
	
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
	
	public abstract void computeData();

	public abstract List getTableData();
	
	public abstract void updateColor();
}
