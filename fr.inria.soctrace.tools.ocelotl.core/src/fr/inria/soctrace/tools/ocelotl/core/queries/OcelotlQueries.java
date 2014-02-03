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

package fr.inria.soctrace.tools.ocelotl.core.queries;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.IteratorQueries.EventIterator;
import fr.inria.soctrace.tools.ocelotl.core.queries.eventproxy.EventProxy;
import fr.inria.soctrace.tools.ocelotl.core.queries.reducedevent.GenericReducedEvent;

public class OcelotlQueries {

	private final OcelotlParameters	ocelotlParameters;
	private OcelotlTraceSearch	traceSearch;

	public OcelotlQueries(final OcelotlParameters ocelotlParameters) throws SoCTraceException {
		super();
		this.ocelotlParameters = ocelotlParameters;
	}


	public List<EventProducer> getAllEventProducers() throws SoCTraceException {
		final TraceDBObject traceDB = new TraceDBObject(ocelotlParameters.getTrace().getDbName(), DBMode.DB_OPEN);
		final EventProducerQuery eventProducerQuery = new EventProducerQuery(traceDB);
		final List<EventProducer> eplist = eventProducerQuery.getList();
		traceDB.close();
		return eplist;
	}

	public List<EventProxy> getAllEventProxies() throws SoCTraceException {
		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
		final List<EventProxy> elist = traceSearch.getEventProxies(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, null);
		traceSearch.uninitialize();
		return elist;
	}

	public List<Event> getAllEvents() throws SoCTraceException {
		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
		final List<Event> elist = traceSearch.getEvents(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, null);
		traceSearch.uninitialize();
		return elist;
	}
	
	public List<Event> getAllStates() throws SoCTraceException {
		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
		final List<Event> elist = traceSearch.getStates(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, null);
		traceSearch.uninitialize();
		return elist;
	}
	
	public EventIterator getStateIterator() throws SoCTraceException {
		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
		EventIterator it = traceSearch.getStateIterator(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, null);
		//traceSearch.uninitialize();
		return it;
	}

	public List<GenericReducedEvent> getAllReducedEvents() throws SoCTraceException {
		traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
		final List<GenericReducedEvent> elist = traceSearch.getReducedEvents(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, null);
		traceSearch.uninitialize();
		return elist;
	}

	public List<EventProxy> getEventProxies(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getAllEventProxies();
		else {
			traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			final List<EventProxy> elist = traceSearch.getEventProxies(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}
	
	public List<EventProxy> getStateProxies(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getAllEventProxies();
		else {
			traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			final List<EventProxy> elist = traceSearch.getStateProxies(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}

	public List<Event> getEvents(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getAllEvents();
		else {
			traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			final List<Event> elist = traceSearch.getEvents(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}
	
	public List<Event> getEventsLight(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getAllEvents();
		else {
			traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			final List<Event> elist = traceSearch.getEventsLight(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}
	
	public List<Event> getStates(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getAllStates();
		else {
			traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			final List<Event> elist = traceSearch.getStates(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}
	
	public EventIterator getStateIterator(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getStateIterator();
		else {
			traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			EventIterator it = traceSearch.getStateIterator(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			//traceSearch.uninitialize();
			return it;
		}
	}

	public List<GenericReducedEvent> getReducedEvents(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getAllReducedEvents();
		else {
			traceSearch = (OcelotlTraceSearch) new OcelotlTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			final List<GenericReducedEvent> elist = traceSearch.getReducedEvents(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}

	public void closeIterator() {
		try {
			traceSearch.closeTraceDB();
			traceSearch.uninitialize();
		} catch (SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}
