package fr.inria.soctrace.tools.ocelotl.core.query;

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

public class OcelotlEventCache {
	private HashMap<Integer, ReducedEvent> cache= new HashMap<Integer, ReducedEvent>();
	private String trace;
	private int EPPAGE = 100;
	private int PAGE = 8;
	private int MISS=0;
	public int getMISS() {
		return MISS;
	}

	public int getHIT() {
		return HIT;
	}

	private int HIT=0;
	final static boolean DEBUG=false;
	
	public OcelotlEventCache(OcelotlParameters parameters) throws SoCTraceException {
		super();
		this.trace = parameters.getTrace().getDbName();
		this.EPPAGE = parameters.getEpCache();
		this.PAGE = parameters.getPageCache();
	}
	
	public ReducedEvent getEvent(EventProxy event) throws SoCTraceException{
		if (cache.containsKey(event.ID)) {
			HIT++;
			return cache.get(event.ID);
		}
		cache.clear();
		TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		ReducedEventQuery query = new ReducedEventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<ReducedEvent> elist =query.getReducedEventList();
		traceDB.close();
		for (ReducedEvent e: elist)
			cache.put(e.ID, e);
		return cache.get(event.ID);
	}
	
	public ReducedEvent getEventPageCache(EventProxy event) throws SoCTraceException{
		if (cache.containsKey(event.ID)) {
			HIT++;
			return cache.get(event.ID);
		}
//		DeltaManager dm = new DeltaManager();
//		dm.start();
		cache.clear();
		TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		ReducedEventQuery query = new ReducedEventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<ReducedEvent> elist =query.getReducedEventList();
		if (!elist.isEmpty()){
			query = new ReducedEventQuery(traceDB);
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(elist.get(0).PAGE)));
			elist=query.getReducedEventList();
		traceDB.close();
		for (ReducedEvent e: elist)
			cache.put(e.ID, e);
		//dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
		return cache.get(event.ID);
	}
		return null;
	}
	
	public ReducedEvent getEventMultiPageCache(EventProxy event) throws SoCTraceException{
		if (cache.containsKey(event.ID)) {
			HIT++;
			return cache.get(event.ID);
		}
//		DeltaManager dm = new DeltaManager();
//		dm.start();
		cache.clear();
		TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		ReducedEventQuery query = new ReducedEventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<ReducedEvent> elist =query.getReducedEventList();
		if (!elist.isEmpty()){
			query = new ReducedEventQuery(traceDB);
			final ValueListString vls = new ValueListString();
			for (int i=0; i<PAGE; i++)	
				vls.addValue(String.valueOf(elist.get(0).PAGE+i));
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, vls.getValueString()));
			elist=query.getReducedEventList();
		traceDB.close();
		for (ReducedEvent e: elist)
			cache.put(e.ID, e);
		//dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
		return cache.get(event.ID);
		}
		return null;
	}
	
	public ReducedEvent getEventMultiPageEPCache(EventProxy event) throws SoCTraceException{
		if (cache.containsKey(event.ID)) {
			HIT++;
			return cache.get(event.ID);
		}
//		DeltaManager dm = new DeltaManager();
//		dm.start();
		cache.clear();
		TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		ReducedEventQuery query = new ReducedEventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<ReducedEvent> elist =query.getReducedEventList();
		if (!elist.isEmpty()){
			query = new ReducedEventQuery(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			final ValueListString vls = new ValueListString();
			for (int i=0; i<EPPAGE; i++)	
				vls.addValue(String.valueOf(elist.get(0).PAGE+i));
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(vls.getValueString())));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(elist.get(0).EP)));
			query.setElementWhere(and);
			elist=query.getReducedEventList();
			traceDB.close();
			for (ReducedEvent e: elist)
			cache.put(e.ID, e);
			//dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}
	
	public ReducedEvent getEventPageEPCache(EventProxy event) throws SoCTraceException{
		if (cache.containsKey(event.ID)) {
			HIT++;
			return cache.get(event.ID);
		}
//		DeltaManager dm = new DeltaManager();
//		dm.start();
		cache.clear();
		TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		ReducedEventQuery query = new ReducedEventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<ReducedEvent> elist =query.getReducedEventList();
		if (!elist.isEmpty()){
			query = new ReducedEventQuery(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.EQ, String.valueOf(elist.get(0).PAGE)));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(elist.get(0).EP)));
			query.setElementWhere(and);
			elist=query.getReducedEventList();
			traceDB.close();
			for (ReducedEvent e: elist)
			cache.put(e.ID, e);
			//dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}
	
 
	
	
}
