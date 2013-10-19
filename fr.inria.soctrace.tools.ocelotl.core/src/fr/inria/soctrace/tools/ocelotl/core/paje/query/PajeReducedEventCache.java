package fr.inria.soctrace.tools.ocelotl.core.paje.query;

import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlParameters;

public class PajeReducedEventCache {
	private HashMap<Integer, PajeReducedEvent>	cache	= new HashMap<Integer, PajeReducedEvent>();
	private String								trace;
	private TraceDBObject						traceDB;
	private int									EPPAGE	= 100;
	private int									PAGE	= 8;
	private int									MISS	= 0;

	public int getMISS() {
		return MISS;
	}

	public int getHIT() {
		return HIT;
	}

	private int				HIT		= 0;
	final static boolean	DEBUG	= false;

	private void openDB(OcelotlParameters parameters) throws SoCTraceException {
		trace = parameters.getTrace().getDbName();
		traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
	}

	public PajeReducedEventCache(OcelotlParameters parameters) throws SoCTraceException {
		super();
		this.EPPAGE = parameters.getEpCache();
		this.PAGE = parameters.getPageCache();
		openDB(parameters);

	}

	public PajeReducedEvent getEvent(PajeEventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID)) {
			// HIT++;
			return cache.get(event.ID);
		}
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEventQuery query = new PajeReducedEventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent> elist = query.getReducedEventList();
		// traceDB.close();
		for (PajeReducedEvent e : elist)
			cache.put(e.ID, e);
		return cache.get(event.ID);
	}

	public PajeReducedEvent getEventPageCache(PajeEventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID)) {
			// HIT++;
			return cache.get(event.ID);
		}
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEventQuery query = new PajeReducedEventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEventQuery(traceDB);
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(elist.get(0).PAGE)));
			elist = query.getReducedEventList();
			// traceDB.close();
			for (PajeReducedEvent e : elist)
				cache.put(e.ID, e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}

	public PajeReducedEvent getEventMultiPageCache(PajeEventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID)) {
			// HIT++;
			return cache.get(event.ID);
		}
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEventQuery query = new PajeReducedEventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEventQuery(traceDB);
			final ValueListString vls = new ValueListString();
			for (int i = 0; i < PAGE; i++)
				vls.addValue(String.valueOf(elist.get(0).PAGE + i));
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, vls.getValueString()));
			elist = query.getReducedEventList();
			// traceDB.close();
			for (PajeReducedEvent e : elist)
				cache.put(e.ID, e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}

	public PajeReducedEvent getEventMultiPageEPCache(PajeEventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID)) {
			// HIT++;
			return cache.get(event.ID);
		}
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEventQuery query = new PajeReducedEventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEventQuery(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			final ValueListString vls = new ValueListString();
			for (int i = 0; i < EPPAGE; i++)
				vls.addValue(String.valueOf(elist.get(0).PAGE + i));
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(vls.getValueString())));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(elist.get(0).EP)));
			query.setElementWhere(and);
			elist = query.getReducedEventList();
			// traceDB.close();
			for (PajeReducedEvent e : elist)
				cache.put(e.ID, e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}

	public PajeReducedEvent getEventPageEPCache(PajeEventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID)) {
			// HIT++;
			return cache.get(event.ID);
		}
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEventQuery query = new PajeReducedEventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEventQuery(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.EQ, String.valueOf(elist.get(0).PAGE)));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(elist.get(0).EP)));
			query.setElementWhere(and);
			elist = query.getReducedEventList();
			// traceDB.close();
			for (PajeReducedEvent e : elist)
				cache.put(e.ID, e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}

	public void close() {
		try {
			traceDB.close();
		} catch (SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
