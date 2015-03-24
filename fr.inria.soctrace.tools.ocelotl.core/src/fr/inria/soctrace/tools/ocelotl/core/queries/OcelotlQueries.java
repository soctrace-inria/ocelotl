/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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

package fr.inria.soctrace.tools.ocelotl.core.queries;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.IteratorQueries.EventIterator;

public class OcelotlQueries {

	private final OcelotlParameters ocelotlParameters;
	private OcelotlTraceSearch traceSearch;

	public OcelotlQueries(final OcelotlParameters ocelotlParameters)
			throws SoCTraceException {
		super();
		this.ocelotlParameters = ocelotlParameters;
	}

	public void closeIterator() {
		try {
			traceSearch.closeTraceDB();
			traceSearch.uninitialize();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get all the event producers
	 * 
	 * @return a list of all the event producers
	 * @throws SoCTraceException
	 */
	public List<EventProducer> getAllEventProducers() throws SoCTraceException {
		return ocelotlParameters.getAllEventProducers();
	}

	/**
	 * Query all the events of type State for the given list of producers
	 * 
	 * @param eventProducers
	 *            list of event producers from which to select all the state. A
	 *            null value means all event producers.
	 * @return an EventIterator on all events of the category State produced by
	 *         the event producers in eventProducers
	 * @return
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getStateIterator(
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {

		final List<IntervalDesc> time = setTimeInterval();
		return getStateIterator(eventProducers, time, monitor);
	}

	public EventIterator getStateIterator(List<EventProducer> eventProducers,
			List<IntervalDesc> time, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {

		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch(
				ocelotlParameters).initialize();
		return traceSearch.getStateIterator(ocelotlParameters.getTrace(),
				ocelotlParameters.getTraceTypeConfig().getTypes(), time,
				eventProducers, monitor);
	}

	/**
	 * Query all the events for the given list of producers
	 * 
	 * @param eventProducers
	 *            list of event producers from which to select all the events. A
	 *            null value means all event producers.
	 * @return an EventIterator on all events produced by the event producers in
	 *         eventProducers
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getEventIterator(
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {

		final List<IntervalDesc> time = setTimeInterval();
		return getEventIterator(eventProducers, time, monitor);
	}
	
	/**
	 * Get the event iterator with no condition at all
	 * 
	 * @param monitor
	 * @return the produced eventIterator
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getEventIterator(IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch(
				ocelotlParameters).initialize();
		
		return traceSearch.getEventIterator(ocelotlParameters.getTrace(), null,
				new ArrayList<IntervalDesc>(), null, monitor);
	}
	
	
	/**
	 * Make a query with only condition on time
	 * 
	 * @param monitor
	 * @return the produced eventIterator
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getEventIteratorTime(List<IntervalDesc> time,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch(
				ocelotlParameters).initialize();

		return traceSearch.getEventIterator(ocelotlParameters.getTrace(), null,
				time, null, monitor);
	}

	/**
	 * 
	 * @param eventProducers
	 *            list of event producers from which to select all the events. A
	 *            null value means all event producers.
	 * @param time
	 * @return an EventIterator on all events produced by the event producers in
	 *         eventProducers and in the time range defined in time
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getEventIterator(List<EventProducer> eventProducers,
			List<IntervalDesc> time, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch(
				ocelotlParameters).initialize();

		return traceSearch.getEventIterator(ocelotlParameters.getTrace(),
				ocelotlParameters.getTraceTypeConfig().getTypes(), time,
				eventProducers, monitor);
	}

	/**
	 * Set the time intervals of the events we want to query
	 * 
	 * @return a list of time intervals
	 */
	public List<IntervalDesc> setTimeInterval() {
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(ocelotlParameters.getTimeRegion()
				.getTimeStampStart(), ocelotlParameters.getTimeRegion()
				.getTimeStampEnd()));

		return time;
	}

	/**
	 * Query all the events of type Variable for the given list of producers
	 * 
	 * @param eventProducers
	 *            list of event producers from which to select all the
	 *            variables. A null value means all event producers.
	 * @return an EventIterator on all events of the category Variable produced
	 *         by the event producers in eventProducers
	 * @return
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getVariableIterator(
			List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		final List<IntervalDesc> time = setTimeInterval();
		return getVariableIterator(eventProducers, time, monitor);
	}

	/**
	 * 
	 * @param eventProducers
	 *            list of event producers from which to select all the events. A
	 *            null value means all event producers.
	 * @param time
	 *            List of time intervals from which the events are selected
	 * @return an EventIterator on all events produced by the event producers in
	 *         eventProducers and in the time range defined in time
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getVariableIterator(
			List<EventProducer> eventProducers, List<IntervalDesc> time,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch(
				ocelotlParameters).initialize();

		return traceSearch.getEventIterator(ocelotlParameters.getTrace(),
				ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers,
				monitor);
	}
}
