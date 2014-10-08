/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
 * overview by using aggregation techniques
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.micromodel.IMicroscopicModel;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.queries.IteratorQueries.EventIterator;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSlice;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public abstract class MultiThreadTimeAggregationOperator implements IMicroscopicModel {

	protected EventIterator eventIterator;
	protected int count = 0;
	protected int epit = 0;
	protected DeltaManagerOcelotl dm;
	protected int eventsNumber;
	protected OcelotlParameters parameters;
	protected OcelotlQueries ocelotlQueries;
	protected ArrayList<String> typeNames = new ArrayList<String>();

	public abstract void computeMatrix(IProgressMonitor monitor)
			throws SoCTraceException, InterruptedException, OcelotlException;

	protected void computeSubMatrix(final List<EventProducer> eventProducers,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		// Default time interval
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(parameters.getTimeRegion()
				.getTimeStampStart(), parameters.getTimeRegion()
				.getTimeStampEnd()));

		computeSubMatrix(eventProducers, time, monitor);
	}

	abstract public void computeSubMatrix(
			final List<EventProducer> eventProducers, List<IntervalDesc> time,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException;

	protected void computeDirtyCacheMatrix(
			final List<EventProducer> eventProducers, List<IntervalDesc> time,
			HashMap<Long, List<TimeSlice>> timesliceIndex,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		computeSubMatrix(eventProducers, time, monitor);
	}

	
	public abstract void buildMicroscopicModel(IProgressMonitor monitor)
			throws SoCTraceException, InterruptedException, OcelotlException;

	public synchronized int getCount() {
		count++;
		return count;
	}

	public synchronized int getEP() {
		epit++;
		return epit - 1;
	}
	
	public int getEventsNumber() {
		return eventsNumber;
	}

	public void setEventsNumber(int eventsNumber) {
		this.eventsNumber = eventsNumber;
	}

	public OcelotlParameters getOcelotlParameters() {
		return parameters;
	}

	// public TimeSliceStateManager getTimeSlicesManager() {
	// return timeSliceManager;
	// }

	abstract public void initQueries();

	public void setOcelotlParameters(final OcelotlParameters parameters,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		this.parameters = parameters;
		count = 0;
		epit = 0;
		eventsNumber = 0;

		initQueries();
		if (monitor.isCanceled())
			return;

		buildMicroscopicModel(monitor);	
	}

	public void total(final int rows) {
		dm.end("VECTOR COMPUTATION " + rows + " rows computed");
	}

	public List<Event> getEvents(final int size, IProgressMonitor monitor) {
		final List<Event> events = new ArrayList<Event>();
		if (monitor.isCanceled())
			return events;
		synchronized (eventIterator) {
			for (int i = 0; i < size; i++) {
				if (eventIterator.getNext(monitor) == null)
					return events;
				events.add(eventIterator.getEvent());
				eventsNumber++;
			}
		}
		return events;
	}
	
}
