package fr.inria.soctrace.tools.ocelotl.core.iaggregop;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.ILPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.ts.TimeSliceManager;

public interface IAggregationOperator {

	public Query getQueries();

	// public void setQueries(Query query) throws SoCTraceException;

	public void setOcelotlParameters(OcelotlParameters parameters) throws SoCTraceException;

	public OcelotlParameters getOcelotlParameters();

	public TimeSliceManager getTimeSlicesManager();

	public int getVectorSize();

	public int getVectorsNumber();

	public void initVectors() throws SoCTraceException;

	public void print();

	public ILPAggregManager createManager();

	public String descriptor();

	public String traceType();

}
