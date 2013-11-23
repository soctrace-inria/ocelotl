/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.timeaggregop.paje;

import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.generic.query.GenericQuery;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;

public abstract class TimeAggregationOperator {

	protected TimeSliceManager	timeSliceManager;
	protected int				count	= 0;
	protected int				epit	= 0;
	protected DeltaManager		dm;
	public final static int		EPCOUNT	= 200;
	protected int				eventsNumber;
	protected GenericQuery		genericQuery;
	protected OcelotlParameters	parameters;

	abstract protected void computeMatrix() throws SoCTraceException, InterruptedException;

	protected void computeSubMatrix(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		if (genericQuery.getOcelotlParameters().isCache())
			computeSubMatrixCached(eventProducers);
		else
			computeSubMatrixNonCached(eventProducers);
	}

	protected abstract void computeSubMatrixCached(List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException;

	protected abstract void computeSubMatrixNonCached(List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException;

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

	abstract protected void initVectors() throws SoCTraceException;

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

	public void total(final int rows) {
		dm.end("VECTOR COMPUTATION " + rows + " rows computed");
	}
}
