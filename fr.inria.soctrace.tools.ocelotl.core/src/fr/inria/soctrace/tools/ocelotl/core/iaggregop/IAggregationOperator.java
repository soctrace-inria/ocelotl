package fr.inria.soctrace.tools.ocelotl.core.iaggregop;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.ts.TimeSliceManager;

public interface IAggregationOperator {
	
	public String descriptor();

	public String traceType();

	public Query getQueries();
	
	public void setQueries(Query query);

	public TimeSliceManager getTimeSlicesManager();
	
	public int getVectorSize();

	public int getVectorsNumber();
	
	public void initVectors() throws SoCTraceException;
	
	public void print();

}
