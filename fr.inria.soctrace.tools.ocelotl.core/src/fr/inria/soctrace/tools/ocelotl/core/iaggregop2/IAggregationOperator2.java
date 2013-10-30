package fr.inria.soctrace.tools.ocelotl.core.iaggregop2;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;

public interface IAggregationOperator2 {

	public String descriptor();
	public int getPartNumber();
	public Part getPart(int i);
	public int getSliceNumber();
	public OcelotlCore getOcelotlCore();
	
	
}
