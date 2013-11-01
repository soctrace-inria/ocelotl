package fr.inria.soctrace.tools.ocelotl.core.itimeaggregop;

import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.generic.query.GenericQuery;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;

public abstract class TimeAggregationOperator {
	
	protected TimeSliceManager				timeSliceManager;
	protected int										count	= 0;
	protected int										epit	= 0;
	protected DeltaManager					dm;
	public final static int					EPCOUNT	= 200;
	protected int							eventsNumber;
	protected GenericQuery					genericQuery;
	protected OcelotlParameters				parameters;
	
	public synchronized int getCount() {
		count++;
		return count;
	}

	public synchronized int getEP() {
		epit++;
		return epit - 1;
	}
	
	public OcelotlParameters getOcelotlParameters() {
		return parameters;
	}

	public GenericQuery getQuery() {
		return genericQuery;
	}

	public TimeSliceManager getTimeSlicesManager() {
		return timeSliceManager;
	}
	
	protected void computeSubMatrix(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		if (genericQuery.getOcelotlParameters().isCache())
			computeSubMatrixCached(eventProducers);
		else
			computeSubMatrixNonCached(eventProducers);
	}

	protected abstract void computeSubMatrixNonCached(List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException;

	protected abstract void computeSubMatrixCached(List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException;

	public void setOcelotlParameters(final OcelotlParameters parameters) throws SoCTraceException, InterruptedException {
		this.parameters = parameters;
		genericQuery = new GenericQuery(parameters);
		genericQuery.checkTimeStamps();
		count = 0;
		epit = 0;
		timeSliceManager = new TimeSliceManager(genericQuery.getOcelotlParameters().getTimeRegion(), genericQuery.getOcelotlParameters().getTimeSlicesNumber());
		initVectors();
		computeMatrix();
	}
	
	

	abstract protected void computeMatrix() throws SoCTraceException, InterruptedException;

	abstract protected void initVectors() throws SoCTraceException;
	
	public void total(final int rows) {
		dm.end("VECTOR COMPUTATION " + rows + " rows computed");
	}
}
