package fr.inria.soctrace.tools.ocelotl.core.generic.spaceaggregop;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceAggregationOperator;

public class NoAggregation extends SpaceAggregationOperator {
	

	public NoAggregation(OcelotlCore ocelotlCore) {
		super(ocelotlCore);
	}

	final static String Descriptor = "No Aggregation";

	public String descriptor() {
		return Descriptor;
	}
	
	protected void computeParts(){
	}
	
	
	

}
