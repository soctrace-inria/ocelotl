package fr.inria.soctrace.tools.ocelotl.core;

import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class OcelotlTraceSearch extends TraceSearch {

	public List<Event> getEventsByEventTypesAndIntervalsAndEventProducers(
			Trace t, List<EventType> eventTypes, List<IntervalDesc> intervals,
			List<EventProducer> eventProducers) throws SoCTraceException {
		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		TimeRegion region = new TimeRegion(intervals.get(0).t1, intervals.get(0).t2);
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		
		// types
		if (eventTypes!=null) {
			if (eventTypes.size() == 0)
				return new LinkedList<Event>();
			ValueListString vls = new ValueListString();
			for (EventType et : eventTypes)
			vls.addValue(String.valueOf(et.getId()));
		and.addCondition(new SimpleCondition("EVENT_TYPE_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		// intervals
		if (region!=null) {
			if (traceDB.getMaxTimestamp()!=region.getTimeStampEnd())
				and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.LE, Long.toString(region.getTimeStampEnd())));
			if (traceDB.getMinTimestamp()!=region.getTimeStampStart())
				and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, Long.toString(region.getTimeStampStart())));
		}
		
		// eventProducers
		if (eventProducers!=null) {
			if (eventProducers.size() == 0)
				return new LinkedList<Event>();
			ValueListString vls = new ValueListString();
			for (EventProducer ep : eventProducers) {
				vls.addValue(String.valueOf(ep.getId()));
			}
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.IN, vls.getValueString()));
		}
		//TODO improve
		if (and.getNumberOfConditions()==1)
			and.addCondition(new SimpleCondition("'1'", ComparisonOperation.EQ, "1"));
		query.setElementWhere(and);
		query.setOrderBy("TIMESTAMP", OrderBy.ASC);
		return query.getList();
	}
	
//	public List<Event> getEventsByEventTypesAndIntervalsAndEventProducers(
//			Trace t, List<EventType> eventTypes, List<IntervalDesc> intervals,
//			List<EventProducer> eventProducers) throws SoCTraceException {
//		openTraceDBObject(t);
//		EventQuery query = new EventQuery(traceDB);
//
//		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
//		
//		// types
//		if (eventTypes!=null) {
//			if (eventTypes.size() == 0)
//				return new LinkedList<Event>();
//			ValueListString vls = new ValueListString();
//			for (EventType et : eventTypes) {
//				vls.addValue(String.valueOf(et.getId()));
//			}
//			and.addCondition(new SimpleCondition("EVENT_TYPE_ID", ComparisonOperation.IN, vls.getValueString()));
//		}
//
//		// intervals
//		if (intervals!=null) {
//			if (intervals.size() == 0)
//				return new LinkedList<Event>();
//			if (intervals.size() == 1) {
//				IntervalDesc interval = intervals.get(0);
//				and.addCondition(buildIntervalCondition(interval));
//			} else {
//				LogicalCondition iOr = new LogicalCondition(LogicalOperation.OR);
//				for (IntervalDesc interval: intervals) {
//					iOr.addCondition(buildIntervalCondition(interval));
//				}
//				and.addCondition(iOr);
//			}
//		}
//		
//		// eventProducers
//		if (eventProducers!=null) {
//			if (eventProducers.size() == 0)
//				return new LinkedList<Event>();
//			ValueListString vls = new ValueListString();
//			for (EventProducer ep : eventProducers) {
//				vls.addValue(String.valueOf(ep.getId()));
//			}
//			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.IN, vls.getValueString()));
//		}
//		
//		query.setElementWhere(and);
//		query.setOrderBy("TIMESTAMP", OrderBy.ASC);
//		return query.getList();
//	}
	
	protected void openTraceDBObject(Trace t) throws SoCTraceException {
		if (traceDB != null) {
			if (traceDB.getDBName() == t.getDbName())
				return; // correct trace DB already opened
			else
				traceDB.close(); // close the current trace DB
		}
			
		traceDB = new TraceDBObject(t.getDbName(), DBMode.DB_OPEN);
	}
	
	@SuppressWarnings("unused")
	private LogicalCondition buildIntervalCondition(IntervalDesc interval) {
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, String.valueOf(interval.t1)));
		and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.LE, String.valueOf(interval.t2)));
		return and;
	}

}
