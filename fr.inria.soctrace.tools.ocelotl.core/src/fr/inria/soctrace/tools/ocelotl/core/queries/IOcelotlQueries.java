package fr.inria.soctrace.tools.ocelotl.core.queries;

import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

public interface IOcelotlQueries {

	public void checkTimeStamps();

	public List<EventProducer> getAllEventProducers() throws SoCTraceException;

}
