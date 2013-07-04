package fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators;

import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.ts.TimeSliceManager;

public interface ITimeSliceMatrix {
	public void initVectors() throws SoCTraceException;
	public void computeVectors();
	public void print();
	public void setQueries(Query query);
	public Query getQueries();
	public TimeSliceManager getTimeSlicesManager();
	public int getVectorSize();
	public int getVectorsNumber();
	public List<HashMap<String, Long>> getMatrix();
}
