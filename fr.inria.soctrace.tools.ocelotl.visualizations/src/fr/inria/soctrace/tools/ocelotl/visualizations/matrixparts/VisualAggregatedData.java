package fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts;

import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IPartData;

public class VisualAggregatedData implements IPartData {
	private boolean visualAggregate=false;
	private boolean aggregated=true;
	private int value=-2;
	private boolean noCutInside =false;
	
	public VisualAggregatedData(boolean visualAggregate, boolean aggregated,
			int value, boolean noCutInside) {
		super();
		this.visualAggregate = visualAggregate;
		this.aggregated = aggregated;
		this.value = value;
		this.noCutInside = noCutInside;
	}
	public boolean isVisualAggregate() {
		return visualAggregate;
	}
	public void setVisualAggregate(boolean visualAggregate) {
		this.visualAggregate = visualAggregate;
	}
	public boolean isAggregated() {
		return aggregated;
	}
	public void setAggregated(boolean aggregated) {
		this.aggregated = aggregated;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public boolean isNoCutInside() {
		return noCutInside;
	}
	public void setNoCutInside(boolean noCutInside) {
		this.noCutInside = noCutInside;
	}

	
	
	
	
}
