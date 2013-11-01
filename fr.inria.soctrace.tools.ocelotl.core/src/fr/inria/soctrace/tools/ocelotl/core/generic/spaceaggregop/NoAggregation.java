package fr.inria.soctrace.tools.ocelotl.core.generic.spaceaggregop;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceAggregationOperator;

public class NoAggregation extends SpaceAggregationOperator {

	final static String	Descriptor	= "No Aggregation";

	public NoAggregation(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
	}

	@Override
	protected void computeParts() {
		initParts();
	}

	@Override
	public String descriptor() {
		return Descriptor;
	}

}
