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
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
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
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.queries.IteratorQueries.EventIterator;
import fr.inria.soctrace.tools.ocelotl.core.queries.eventproxy.EventProxy;
import fr.inria.soctrace.tools.ocelotl.core.queries.eventproxy.EventProxyQuery;
import fr.inria.soctrace.tools.ocelotl.core.queries.reducedevent.GenericReducedEvent;
import fr.inria.soctrace.tools.ocelotl.core.queries.reducedevent.GenericReducedEventQuery;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class OcelotlTraceSearch extends TraceSearch {

	@SuppressWarnings("unused")
	private LogicalCondition buildIntervalCondition(final IntervalDesc interval) {
		final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, String.valueOf(interval.t1)));
		and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.LE, String.valueOf(interval.t2)));
		return and;
	}

	public void closeTraceDB() {
		try {
			traceDB.close();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public EventIterator getEventIterator(final Trace t, final List<EventType> eventTypes, final List<IntervalDesc> intervals, final List<EventProducer> eventProducers) throws SoCTraceException, OcelotlException {
		openTraceDBObject(t);
		final IteratorQueries query = new IteratorQueries(traceDB);
		final TimeRegion region = new TimeRegion(intervals.get(0).t1, intervals.get(0).t2);
		final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);

		// types
		if (eventTypes != null) {
			if (eventTypes.size() == 0)
				throw new OcelotlException(OcelotlException.INVALIDQUERY);	
			final ValueListString vls = new ValueListString();
			for (final EventType et : eventTypes)
				vls.addValue(String.valueOf(et.getId()));
			query.setTypeWhere(new SimpleCondition("EVENT_TYPE_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		// eventProducers
		if (eventProducers != null) {
			if (eventProducers.size() == 0)
				throw new OcelotlException(OcelotlException.INVALIDQUERY);	
			final ValueListString vls = new ValueListString();
			for (final EventProducer ep : eventProducers)
				vls.addValue(String.valueOf(ep.getId()));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		if (region != null) {
			if (traceDB.getMaxTimestamp() != region.getTimeStampEnd())
				and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.LE, Long.toString(region.getTimeStampEnd())));
			if (traceDB.getMinTimestamp() != region.getTimeStampStart())
				and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, Long.toString(region.getTimeStampStart())));
		}
		
//		if (and.getNumberOfConditions() == 1)
//			and.addCondition(new SimpleCondition("CATEGORY", ComparisonOperation.EQ, String.valueOf(EventCategory.)));
		if (and.getNumberOfConditions() >= 2)
			query.setElementWhere(and);
		query.setOrderBy("TIMESTAMP", OrderBy.ASC);
		query.setLoadParameters(false);
		// traceDB.close();
		return query.getIterator();
	}
	
	public EventIterator getStateIterator(final Trace t, final List<EventType> eventTypes, final List<IntervalDesc> intervals, final List<EventProducer> eventProducers) throws SoCTraceException, OcelotlException {
		openTraceDBObject(t);
		final IteratorQueries query = new IteratorQueries(traceDB);
		final TimeRegion region = new TimeRegion(intervals.get(0).t1, intervals.get(0).t2);
		final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);

		// types
		if (eventTypes != null) {
			if (eventTypes.size() == 0)
				throw new OcelotlException(OcelotlException.INVALIDQUERY);	
			final ValueListString vls = new ValueListString();
			for (final EventType et : eventTypes)
				vls.addValue(String.valueOf(et.getId()));
			query.setTypeWhere(new SimpleCondition("EVENT_TYPE_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		// eventProducers
		if (eventProducers != null) {
			if (eventProducers.size() == 0)
				throw new OcelotlException(OcelotlException.INVALIDQUERY);	
			final ValueListString vls = new ValueListString();
			for (final EventProducer ep : eventProducers)
				vls.addValue(String.valueOf(ep.getId()));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		// intervals
		final LogicalCondition ort = new LogicalCondition(LogicalOperation.OR);
		final LogicalCondition andt = new LogicalCondition(LogicalOperation.AND);
		final LogicalCondition andd = new LogicalCondition(LogicalOperation.AND);
		SimpleCondition t1 = null;
		SimpleCondition t2 = null;
		SimpleCondition d1 = null;
		SimpleCondition d2 = null;

		if (region != null) {
			if (traceDB.getMaxTimestamp() > region.getTimeStampEnd())
				t2 = new SimpleCondition("TIMESTAMP", ComparisonOperation.LE, Long.toString(region.getTimeStampEnd()));
			if (traceDB.getMinTimestamp() < region.getTimeStampStart()) {
				t1 = new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, Long.toString(region.getTimeStampStart()));
				d1 = new SimpleCondition("TIMESTAMP", ComparisonOperation.LT, Long.toString(region.getTimeStampStart()));
				d2 = new SimpleCondition("LPAR", ComparisonOperation.GE, Long.toString(region.getTimeStampStart()));
			}
			if (t1 == null && t2 != null)
				and.addCondition(t2);
			else if (t2 == null && t1 != null) {
				ort.addCondition(t1);
				andd.addCondition(d1);
				andd.addCondition(d2);
				ort.addCondition(andd);
				and.addCondition(ort);
			} else if (t2 != null && t1 != null) {
				andt.addCondition(t1);
				andt.addCondition(t2);
				ort.addCondition(andt);
				andd.addCondition(d1);
				andd.addCondition(d2);
				ort.addCondition(andd);
				and.addCondition(ort);
			}
		}
		if (and.getNumberOfConditions() == 1)
			and.addCondition(new SimpleCondition("CATEGORY", ComparisonOperation.EQ, String.valueOf(EventCategory.STATE)));
		if (and.getNumberOfConditions() >= 2)
			query.setElementWhere(and);
		query.setOrderBy("TIMESTAMP", OrderBy.ASC);
		query.setLoadParameters(false);
		return query.getIterator();
	}

	protected void openTraceDBObject(final Trace t) throws SoCTraceException {
		if (traceDB != null)
			if (traceDB.getDBName() == t.getDbName())
				return; // correct trace DB already opened
			else
				traceDB.close(); // close the current trace DB

		traceDB = new TraceDBObject(t.getDbName(), DBMode.DB_OPEN);
	}

}
