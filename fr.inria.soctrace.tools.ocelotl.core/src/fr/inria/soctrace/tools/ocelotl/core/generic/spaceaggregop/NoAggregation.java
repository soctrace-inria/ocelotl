package fr.inria.soctrace.tools.ocelotl.core.generic.spaceaggregop;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceAggregationOperator;

public class NoAggregation extends SpaceAggregationOperator {

	final public static String	descriptor	= "No Aggregation";

	public NoAggregation(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
	}
	
	
	public NoAggregation() {
		super();
	}

	@Override
	protected void computeParts() {
		initParts();
	}

	@Override
	public String descriptor() {
		return descriptor;
	}

}
