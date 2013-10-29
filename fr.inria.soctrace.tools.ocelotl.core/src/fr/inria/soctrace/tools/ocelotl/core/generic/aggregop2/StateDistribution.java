package fr.inria.soctrace.tools.ocelotl.core.generic.aggregop2;

import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop2.IAggregationOperator2;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop2.Part;

public class StateDistribution implements IAggregationOperator2 {
	
	List<Part> parts;
	final static String Descriptor = "State Distribution";
	OcelotlCore ocelotlCore;
	private int	timeSliceNumber;


	public String descriptor() {
		return Descriptor;
	}
	@Override
	public int getPartNumber() {
		return 0;
	}
	@Override
	public Part getPart(int i) {
		return null;

	}
	@Override
	public int getSliceNumber() {
		return timeSliceNumber;
	}
	@Override
	public OcelotlCore getOcelotlCore() {
		return ocelotlCore;
	}
	

}
