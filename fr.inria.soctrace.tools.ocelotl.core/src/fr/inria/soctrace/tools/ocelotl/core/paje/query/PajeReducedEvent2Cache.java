package fr.inria.soctrace.tools.ocelotl.core.paje.query;

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
import fr.inria.soctrace.tools.ocelotl.core.OcelotlParameters;

public class PajeReducedEvent2Cache {
	private final HashMap<Integer, PajeReducedEvent2>	cache	= new HashMap<Integer, PajeReducedEvent2>();
	private String										trace;
	private TraceDBObject								traceDB;
	private int											EPPAGE	= 100;
	private int											PAGE	= 8;
	private final int									MISS	= 0;

	private final int									HIT		= 0;

	final static boolean								DEBUG	= false;

	public PajeReducedEvent2Cache(final OcelotlParameters parameters) throws SoCTraceException {
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

	public PajeReducedEvent2 getEvent(final PajeEventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		final PajeReducedEvent2Query query = new PajeReducedEvent2Query(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		final List<PajeReducedEvent2> elist = query.getReducedEventList();
		// traceDB.close();
		for (final PajeReducedEvent2 e : elist)
			cache.put(e.ID, e);
		return cache.get(event.ID);
	}

	public PajeReducedEvent2 getEventMultiPageCache(final PajeEventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEvent2Query query = new PajeReducedEvent2Query(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent2> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEvent2Query(traceDB);
			final ValueListString vls = new ValueListString();
			for (int i = 0; i < PAGE; i++)
				vls.addValue(String.valueOf(elist.get(0).PAGE + i));
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, vls.getValueString()));
			elist = query.getReducedEventList();
			// traceDB.close();
			for (final PajeReducedEvent2 e : elist)
				cache.put(e.ID, e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}

	public PajeReducedEvent2 getEventMultiPageEPCache(final PajeEventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEvent2Query query = new PajeReducedEvent2Query(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent2> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEvent2Query(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			final ValueListString vls = new ValueListString();
			for (int i = 0; i < EPPAGE; i++)
				vls.addValue(String.valueOf(elist.get(0).PAGE + i));
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(vls.getValueString())));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(elist.get(0).EP)));
			query.setElementWhere(and);
			elist = query.getReducedEventList();
			// traceDB.close();
			for (final PajeReducedEvent2 e : elist)
				cache.put(e.ID, e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}

	public PajeReducedEvent2 getEventPageCache(final PajeEventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEvent2Query query = new PajeReducedEvent2Query(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent2> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEvent2Query(traceDB);
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(elist.get(0).PAGE)));
			elist = query.getReducedEventList();
			// traceDB.close();
			for (final PajeReducedEvent2 e : elist)
				cache.put(e.ID, e);
			// dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}

	public PajeReducedEvent2 getEventPageEPCache(final PajeEventProxy event) throws SoCTraceException {
		if (cache.containsKey(event.ID))
			// HIT++;
			return cache.get(event.ID);
		// DeltaManager dm = new DeltaManager();
		// dm.start();
		cache.clear();
		// TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		PajeReducedEvent2Query query = new PajeReducedEvent2Query(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<PajeReducedEvent2> elist = query.getReducedEventList();
		if (!elist.isEmpty()) {
			query = new PajeReducedEvent2Query(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.EQ, String.valueOf(elist.get(0).PAGE)));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(elist.get(0).EP)));
			query.setElementWhere(and);
			elist = query.getReducedEventList();
			// traceDB.close();
			for (final PajeReducedEvent2 e : elist)
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
