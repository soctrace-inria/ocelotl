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

package fr.inria.soctrace.tools.ocelotl.core.itimeaggregop;

import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.IOcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;

public abstract class MultiThreadTimeAggregationOperator {

	protected TimeSliceManager	timeSliceManager;
	protected int				count	= 0;
	protected int				epit	= 0;
	protected DeltaManager		dm;
	public final static int		EPCOUNT	= 200;
	protected int				eventsNumber;
	protected OcelotlParameters	parameters;
	protected IOcelotlQueries	ocelotlQueries;

	abstract protected void computeMatrix() throws SoCTraceException, InterruptedException;

	abstract protected void computeSubMatrix(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException;

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

	public TimeSliceManager getTimeSlicesManager() {
		return timeSliceManager;
	}

	abstract public void initQueries();

	abstract protected void initVectors() throws SoCTraceException;

	public void setOcelotlParameters(final OcelotlParameters parameters) throws SoCTraceException, InterruptedException {
		this.parameters = parameters;
		// ocelotlQueries = new OcelotlQueries(parameters);
		// ocelotlQueries.checkTimeStamps();
		count = 0;
		epit = 0;
		timeSliceManager = new TimeSliceManager(getOcelotlParameters().getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		initQueries();
		initVectors();
		computeMatrix();
	}

	public void total(final int rows) {
		dm.end("VECTOR COMPUTATION " + rows + " rows computed");
	}
}
