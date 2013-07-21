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
	private HashMap<Integer, Event> cache= new HashMap<Integer, Event>();
	private ITraceSearch traceSearch;
	private String trace;
	private int EPPAGE = 100;
	private int PAGE = 8;
	private int MISS=0;
	private int HIT=0;
	
	public OcelotlEventCache(OcelotlParameters parameters) throws SoCTraceException {
		super();
		this.traceSearch = new TraceSearch().initialize();
		this.trace = parameters.getTrace().getDbName();
		this.EPPAGE = parameters.getEpCache();
		this.PAGE = parameters.getPageCache();
	}
	
	public Event getEvent(EventProxy event) throws SoCTraceException{
		if (cache.containsKey(event.ID)){
			HIT++;
			return cache.get(event.ID);
		}
		cache.clear();
		TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<Event> elist =query.getList();
		traceDB.close();
		for (Event e: elist)
			cache.put(e.getId(), e);
		return cache.get(event.ID);
	}
	
	public Event getEventPageCache(EventProxy event) throws SoCTraceException{
		if (cache.containsKey(event.ID)){
			HIT++;
			return cache.get(event.ID);
		}
//		DeltaManager dm = new DeltaManager();
//		dm.start();
		cache.clear();
		TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<Event> elist =query.getList();
		if (!elist.isEmpty()){
			query = new EventQuery(traceDB);
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(elist.get(0).getPage())));
			elist=query.getList();
		traceDB.close();
		for (Event e: elist)
			cache.put(e.getId(), e);
		//dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
		return cache.get(event.ID);
	}
		return null;
	}
	
	public Event getEventMultiPageCache(EventProxy event) throws SoCTraceException{
		if (cache.containsKey(event.ID)){
			HIT++;
			return cache.get(event.ID);
		}
//		DeltaManager dm = new DeltaManager();
//		dm.start();
		cache.clear();
		TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<Event> elist =query.getList();
		if (!elist.isEmpty()){
			query = new EventQuery(traceDB);
			final ValueListString vls = new ValueListString();
			for (int i=0; i<PAGE; i++)	
				vls.addValue(String.valueOf(elist.get(0).getPage()+i));
			query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.IN, vls.getValueString()));
			elist=query.getList();
		traceDB.close();
		for (Event e: elist)
			cache.put(e.getId(), e);
		//dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
		return cache.get(event.ID);
		}
		return null;
	}
	
	public Event getEventMultiPageEPCache(EventProxy event) throws SoCTraceException{
		if (cache.containsKey(event.ID)){
			HIT++;
			return cache.get(event.ID);
		}
//		DeltaManager dm = new DeltaManager();
//		dm.start();
		cache.clear();
		TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<Event> elist =query.getList();
		if (!elist.isEmpty()){
			query = new EventQuery(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			final ValueListString vls = new ValueListString();
			for (int i=0; i<EPPAGE; i++)	
				vls.addValue(String.valueOf(elist.get(0).getPage()+i));
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.IN, String.valueOf(vls.getValueString())));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(elist.get(0).getEventProducer().getId())));
			query.setElementWhere(and);
			elist=query.getList();
			traceDB.close();
			for (Event e: elist)
			cache.put(e.getId(), e);
			//dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}
	
	public Event getEventPageEPCache(EventProxy event) throws SoCTraceException{
		if (cache.containsKey(event.ID)){
			HIT++;
			return cache.get(event.ID);
		}
//		DeltaManager dm = new DeltaManager();
//		dm.start();
		cache.clear();
		TraceDBObject traceDB = new TraceDBObject(trace, DBMode.DB_OPEN);
		EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(event.ID)));
		List<Event> elist =query.getList();
		if (!elist.isEmpty()){
			query = new EventQuery(traceDB);
			final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.EQ, String.valueOf(elist.get(0).getPage())));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(elist.get(0).getEventProducer().getId())));
			query.setElementWhere(and);
			elist=query.getList();
			traceDB.close();
			for (Event e: elist)
			cache.put(e.getId(), e);
			//dm.end("CACHING :" + MISS++ + " MISS " + HIT + " HIT");
			return cache.get(event.ID);
		}
		return null;
	}
	
 
	
	
}
