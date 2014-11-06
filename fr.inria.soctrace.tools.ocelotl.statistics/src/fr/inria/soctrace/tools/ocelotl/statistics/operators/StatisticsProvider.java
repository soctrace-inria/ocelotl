package fr.inria.soctrace.tools.ocelotl.statistics.operators;

import java.util.List;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public abstract class StatisticsProvider {
	
	protected OcelotlView ocelotlview;
	
	public StatisticsProvider(OcelotlView aView) {
		this.ocelotlview = aView;
	}
	
	public abstract void computeData();

	public abstract List getTableData();
	
	public abstract void updateColor();
}
