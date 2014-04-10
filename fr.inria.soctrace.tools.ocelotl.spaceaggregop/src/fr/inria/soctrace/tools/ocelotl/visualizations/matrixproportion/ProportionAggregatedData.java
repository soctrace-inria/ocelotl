package fr.inria.soctrace.tools.ocelotl.visualizations.matrixproportion;

import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.IPartData;
import fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts.VisualAggregatedData;

public class ProportionAggregatedData extends VisualAggregatedData{
	private MajState state;
	
	public ProportionAggregatedData(boolean visualAggregate, boolean aggregated,
			int value, boolean noCutInside, MajState state) {
		super(noCutInside, noCutInside, value, noCutInside);
		this.state=state;
	}

	public MajState getState() {
		return state;
	}

	public void setState(MajState state) {
		this.state = state;
	}

	

	
	
	
}
