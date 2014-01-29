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

package fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.queries;

import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.eventproxy.EventProxy;

public class EventCache {
	private final HashMap<Integer, Event>	cache	= new HashMap<Integer, Event>();
	private String										trace;
	private TraceDBObject								traceDB;
	private int											EPPAGE	= 100;
	private int											PAGE	= 8;
	private final int									MISS	= 0;

	private final int									HIT		= 0;

	final static boolean								DEBUG	= false;

	public EventCache(final OcelotlParameters parameters) throws SoCTraceException {
		super();
		EPPAGE = parameters.getEpCache();
		PAGE = parameters.getPageCache();
		openDB(parameters);

	}

	public void close() {
		try {
			traceDB.close();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Event getEvent(final EventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		final EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		final List<Event> elist = query.getList();
		// traceDB.close();
		for (final Event e : elist)
			cache.put(e.getId(), e);
		return cache.get(event.ID);
	}

	public Event getEventMultiPageCache(final EventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
			EventQuery query = new EventQuery(traceDB);
			final ValueListString vls = new ValueListString();
			for (int i = 0; i < PAGE; i++)
				vls.addValue(String.valueOf(event.PAGE + i));
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, vls.getValueString()));
			List<Event> elist = query.getList();
			// traceDB.close();
			for (final Event e : elist)
				cache.put(e.getId(), e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
	}

	public Event getEventMultiPageEPCache(final EventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		
			EventQuery query = new EventQuery(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			final ValueListString vls = new ValueListString();
			for (int i = 0; i < EPPAGE; i++)
				vls.addValue(String.valueOf(event.PAGE + i));
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(vls.getValueString())));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(event.EP)));
			query.setElementWhere(and);
			List<Event> elist = query.getList();
			// traceDB.close();
			for (final Event e : elist)
				cache.put(e.getId(), e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
	}

	public Event getEventPageCache(final EventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		EventQuery query = new EventQuery(traceDB);
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(event.PAGE)));
			List<Event> elist = query.getList();
			// traceDB.close();
			for (final Event e : elist)
				cache.put(e.getId(), e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
	}

	public Event getEventPageEPCache(final EventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		EventQuery query = new EventQuery(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.EQ, String.valueOf(event.PAGE)));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(event.EP)));
			query.setElementWhere(and);
			List<Event> elist = query.getList();
			// traceDB.close();
			for (final Event e : elist)
				cache.put(e.getId(), e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
	}

	public int getHIT() {
		return HIT;
	}

	public int getMISS() {
		return MISS;
	}

	private void openDB(final OcelotlParameters parameters) throws SoCTraceException {
		trace = parameters.getTrace().getDbName();
		traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
	}

}
