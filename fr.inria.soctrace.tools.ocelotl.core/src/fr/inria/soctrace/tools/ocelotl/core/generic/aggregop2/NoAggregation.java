package fr.inria.soctrace.tools.ocelotl.core.generic.aggregop2;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop2.AggregationOperator2;

public class NoAggregation extends AggregationOperator2 {
	

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
