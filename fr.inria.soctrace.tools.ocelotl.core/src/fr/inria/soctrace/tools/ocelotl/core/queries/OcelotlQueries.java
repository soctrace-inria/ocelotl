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

package fr.inria.soctrace.tools.ocelotl.core.queries;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
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
		final TraceDBObject traceDB = new TraceDBObject(ocelotlParameters
				.getTrace().getDbName(), DBMode.DB_OPEN);
		final EventProducerQuery eventProducerQuery = new EventProducerQuery(
				traceDB);
		final List<EventProducer> eplist = eventProducerQuery.getList();
		traceDB.close();
		return eplist;
	}

	/**
	 * Query all the events of type State for all producers
	 * 
	 * @return an EventIterator on all events of the category State produced by
	 *         the event producers in eventProducers
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getStateIterator() throws SoCTraceException,
			OcelotlException {
		return getStateIterator(getAllEventProducers());
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
			final List<EventProducer> eventProducers) throws SoCTraceException,
			OcelotlException {

		final List<IntervalDesc> time = setTimeInterval();
		return getStateIterator(eventProducers, time);
	}
	
	public EventIterator getStateIterator(List<EventProducer> eventProducers,
			List<IntervalDesc> time) throws SoCTraceException, OcelotlException {

		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch()
				.initialize();

		// If we do not filter event producers
		if (eventProducers == null
				|| eventProducers.size() == getAllEventProducers().size()) {
			return traceSearch.getStateIterator(ocelotlParameters.getTrace(),
					ocelotlParameters.getTraceTypeConfig().getTypes(), time,
					null);
		} else {
			return traceSearch.getStateIterator(ocelotlParameters.getTrace(),
					ocelotlParameters.getTraceTypeConfig().getTypes(), time,
					eventProducers);
		}
	}


	/**
	 * Query all the events of type State for all producers
	 * 
	 * @return an EventIterator on all events without filtering
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getEventIterator() throws SoCTraceException,
			OcelotlException {
		return getEventIterator(getAllEventProducers());
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
			final List<EventProducer> eventProducers) throws SoCTraceException,
			OcelotlException {

		final List<IntervalDesc> time = setTimeInterval();
		return getEventIterator(eventProducers, time);
	}
	
	/**
	 * 
	 * @param eventProducers
	 * 	list of event producers from which to select all the events. A
	 *            null value means all event producers.
	 * @param time
	 * @return an EventIterator on all events produced by the event producers in
	 *         eventProducers and in the time range defined in time
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getEventIterator(List<EventProducer> eventProducers,
			List<IntervalDesc> time) throws SoCTraceException, OcelotlException {
		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch()
				.initialize();

		// If we do not filter event producers
		if (eventProducers == null
				|| eventProducers.size() == getAllEventProducers().size()) {
			return traceSearch.getEventIterator(ocelotlParameters.getTrace(),
					ocelotlParameters.getTraceTypeConfig().getTypes(), time,
					null);
		} else {
			return traceSearch.getEventIterator(ocelotlParameters.getTrace(),
					ocelotlParameters.getTraceTypeConfig().getTypes(), time,
					eventProducers);
		}
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
	 * Query all the events of type Variable for all producers
	 * 
	 * @return an EventIterator on all events of the category State produced by
	 *         the event producers in eventProducers
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getVariableIterator() throws SoCTraceException,
			OcelotlException {
		return getVariableIterator(getAllEventProducers());
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
	public EventIterator getVariableIterator(List<EventProducer> eventProducers)
			throws SoCTraceException, OcelotlException {
		final List<IntervalDesc> time = setTimeInterval();
		return getVariableIterator(eventProducers, time);
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
			List<EventProducer> eventProducers, List<IntervalDesc> time)
			throws SoCTraceException, OcelotlException {
		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch()
				.initialize();

		// If we do not filter event producers
		if (eventProducers == null
				|| eventProducers.size() == getAllEventProducers().size()) {
			return traceSearch.getEventIterator(ocelotlParameters.getTrace(),
					ocelotlParameters.getTraceTypeConfig().getTypes(), time,
					null);
		} else {
			return traceSearch.getEventIterator(ocelotlParameters.getTrace(),
					ocelotlParameters.getTraceTypeConfig().getTypes(), time,
					eventProducers);
		}
	}

}

