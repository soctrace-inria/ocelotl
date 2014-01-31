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

package fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.queries.reducedevent1;

import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.eventproxy.EventProxy;

public class PajeReducedEvent1Cache {
	private final HashMap<Integer, PajeReducedEvent1>	cache	= new HashMap<Integer, PajeReducedEvent1>();
	private String										trace;
	private TraceDBObject								traceDB;
	private int											EPPAGE	= 100;
	private int											PAGE	= 8;
	private final int									MISS	= 0;

	private final int									HIT		= 0;

	final static boolean								DEBUG	= false;

	public PajeReducedEvent1Cache(final OcelotlParameters parameters) throws SoCTraceException {
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

	public PajeReducedEvent1 getEvent(final EventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		final PajeReducedEvent1Query query = new PajeReducedEvent1Query(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		final List<PajeReducedEvent1> elist = query.getReducedEventList();
		// traceDB.close();
		for (final PajeReducedEvent1 e : elist)
			cache.put(e.ID, e);
		return cache.get(event.ID);
	}

	public PajeReducedEvent1 getEventMultiPageCache(final EventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEvent1Query query = new PajeReducedEvent1Query(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent1> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEvent1Query(traceDB);
			final ValueListString vls = new ValueListString();
			for (int i = 0; i < PAGE; i++)
				vls.addValue(String.valueOf(elist.get(0).PAGE + i));
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, vls.getValueString()));
			elist = query.getReducedEventList();
			// traceDB.close();
			for (final PajeReducedEvent1 e : elist)
				cache.put(e.ID, e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}

	public PajeReducedEvent1 getEventMultiPageEPCache(final EventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEvent1Query query = new PajeReducedEvent1Query(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent1> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEvent1Query(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			final ValueListString vls = new ValueListString();
			for (int i = 0; i < EPPAGE; i++)
				vls.addValue(String.valueOf(elist.get(0).PAGE + i));
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(vls.getValueString())));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(elist.get(0).EP)));
			query.setElementWhere(and);
			elist = query.getReducedEventList();
			// traceDB.close();
			for (final PajeReducedEvent1 e : elist)
				cache.put(e.ID, e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}

	public PajeReducedEvent1 getEventPageCache(final EventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEvent1Query query = new PajeReducedEvent1Query(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent1> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEvent1Query(traceDB);
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(elist.get(0).PAGE)));
			elist = query.getReducedEventList();
			// traceDB.close();
			for (final PajeReducedEvent1 e : elist)
				cache.put(e.ID, e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}

	public PajeReducedEvent1 getEventPageEPCache(final EventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEvent1Query query = new PajeReducedEvent1Query(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent1> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEvent1Query(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.EQ, String.valueOf(elist.get(0).PAGE)));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(elist.get(0).EP)));
			query.setElementWhere(and);
			elist = query.getReducedEventList();
			// traceDB.close();
			for (final PajeReducedEvent1 e : elist)
				cache.put(e.ID, e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
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
