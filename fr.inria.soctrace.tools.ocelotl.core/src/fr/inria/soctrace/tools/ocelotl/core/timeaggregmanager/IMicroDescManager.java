package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager;

import java.util.List;

import fr.inria.dlpaggreg.quality.DLPQuality;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;

public interface IMicroDescManager {

	public void computeDichotomy() throws OcelotlException;

	public void computeParts();

	public void computeQualities();

	public List<EventProducer> getEventProducers();

	public List<Double> getParameters();

	public List<DLPQuality> getQualities();

	public void printParameters();

	public void printParts();

	public void reset();

	public void print(OcelotlCore core);
	
}
