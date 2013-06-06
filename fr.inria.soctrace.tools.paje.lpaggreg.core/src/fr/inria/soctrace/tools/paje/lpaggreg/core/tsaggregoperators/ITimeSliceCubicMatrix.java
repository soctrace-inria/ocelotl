package fr.inria.soctrace.tools.paje.lpaggreg.core.tsaggregoperators;

import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.paje.lpaggreg.core.Queries;
import fr.inria.soctrace.tools.paje.lpaggreg.core.TimeSliceManager;

public interface ITimeSliceCubicMatrix {
	public void initVectors() throws SoCTraceException;
	public void computeVectors();
	public void print();
	public void setQueries(Queries queries);
	public Queries getQueries();
	public TimeSliceManager getTimeSlicesManager();
	public int getVectorSize();
	public int getVectorsNumber();
	public List<HashMap<String, HashMap<String, Long>>> getMatrix();
}
