package fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop;

public class AggregatedData implements IPartData{
	boolean aggregated=true;
	int value=-2;

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

	public AggregatedData(boolean aggregated, int value) {
		super();
		this.aggregated = aggregated;
		this.value = value;
	}
	
	
	
}
