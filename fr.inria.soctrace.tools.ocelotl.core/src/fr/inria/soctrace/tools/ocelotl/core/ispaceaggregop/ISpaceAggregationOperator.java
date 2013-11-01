package fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;

public interface ISpaceAggregationOperator {

	public String descriptor();
	public int getPartNumber();
	public Part getPart(int i);
	public int getSliceNumber();
	public OcelotlCore getOcelotlCore();
	
	
}
