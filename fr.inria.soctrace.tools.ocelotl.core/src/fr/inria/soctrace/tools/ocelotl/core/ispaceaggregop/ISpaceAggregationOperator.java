package fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;

public interface ISpaceAggregationOperator {

	public String descriptor();

	public OcelotlCore getOcelotlCore();
	
	public void setOcelotlCore(OcelotlCore ocelotlCore);

	public Part getPart(int i);

	public int getPartNumber();

	public int getSliceNumber();

}
