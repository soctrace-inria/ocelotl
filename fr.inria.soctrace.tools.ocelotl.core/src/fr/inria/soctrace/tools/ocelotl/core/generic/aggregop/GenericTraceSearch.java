/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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

package fr.inria.soctrace.tools.ocelotl.core.generic.aggregop;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
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
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class GenericTraceSearch extends TraceSearch {

	@SuppressWarnings("unused")
	private LogicalCondition buildIntervalCondition(final IntervalDesc interval) {
		final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, String.valueOf(interval.t1)));
		and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.LE, String.valueOf(interval.t2)));
		return and;
	}

	public List<EventProxy> getEventProxysByEventTypesAndIntervalsAndEventProducers(final Trace t, final List<EventType> eventTypes, final List<IntervalDesc> intervals, final List<EventProducer> eventProducers) throws SoCTraceException {
		final DeltaManager dm = new DeltaManager();
		openTraceDBObject(t);
		final TimeRegion region = new TimeRegion(intervals.get(0).t1, intervals.get(0).t2);
		List<EventProxy> proxy = new ArrayList<EventProxy>();

		// types
		if (eventTypes != null)
			if (eventTypes.size() == 0)
				return proxy;
		if (eventProducers != null)
			if (eventProducers.size() == 0)
				return proxy;
		dm.start();
		final EventProxyQuery query = new EventProxyQuery(traceDB);
		final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		// and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.EQ,
		// Long.toString(i)));

		// intervals
		if (region != null) {
			if (traceDB.getMaxTimestamp() != region.getTimeStampEnd())
				and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.LE, Long.toString(region.getTimeStampEnd())));
			if (traceDB.getMinTimestamp() != region.getTimeStampStart())
				and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, Long.toString(region.getTimeStampStart())));
		}

		if (eventTypes != null) {
			final ValueListString vls = new ValueListString();
			for (final EventType et : eventTypes)
				vls.addValue(String.valueOf(et.getId()));
			and.addCondition(new SimpleCondition("EVENT_TYPE_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		// eventProducers
		if (eventProducers != null) {
			final ValueListString vls = new ValueListString();
			for (final EventProducer ep : eventProducers)
				vls.addValue(String.valueOf(ep.getId()));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		if (and.getNumberOfConditions() == 1)
			and.addCondition(new SimpleCondition("'1'", ComparisonOperation.EQ, "1"));
		query.setElementWhere(and);
		query.setOrderBy("TIMESTAMP", OrderBy.ASC);
		proxy = query.getIDList();
		traceDB.close();
		return proxy;
	}

	@Override
	public List<Event> getEventsByEventTypesAndIntervalsAndEventProducers(final Trace t, final List<EventType> eventTypes, final List<IntervalDesc> intervals, final List<EventProducer> eventProducers) throws SoCTraceException {
		openTraceDBObject(t);
		final EventProxyQuery query = new EventProxyQuery(traceDB);
		final TimeRegion region = new TimeRegion(intervals.get(0).t1, intervals.get(0).t2);
		final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);

		// types
		if (eventTypes != null) {
			if (eventTypes.size() == 0)
				return new LinkedList<Event>();
			final ValueListString vls = new ValueListString();
			for (final EventType et : eventTypes)
				vls.addValue(String.valueOf(et.getId()));
			and.addCondition(new SimpleCondition("EVENT_TYPE_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		// intervals
		if (region != null) {
			if (traceDB.getMaxTimestamp() != region.getTimeStampEnd())
				and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.LE, Long.toString(region.getTimeStampEnd())));
			if (traceDB.getMinTimestamp() != region.getTimeStampStart())
				and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, Long.toString(region.getTimeStampStart())));
		}

		// eventProducers
		if (eventProducers != null) {
			if (eventProducers.size() == 0)
				return new LinkedList<Event>();
			final ValueListString vls = new ValueListString();
			for (final EventProducer ep : eventProducers)
				vls.addValue(String.valueOf(ep.getId()));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.IN, vls.getValueString()));
		}
		// TODO improve
		if (and.getNumberOfConditions() == 1)
			and.addCondition(new SimpleCondition("'1'", ComparisonOperation.EQ, "1"));
		query.setElementWhere(and);
		query.setOrderBy("TIMESTAMP", OrderBy.ASC);
		traceDB.close();
		return query.getList();
	}

	
	public List<GenericReducedEvent> getReducedEventsByEventTypesAndIntervalsAndEventProducers(final Trace t, final List<EventType> eventTypes, final List<IntervalDesc> intervals, final List<EventProducer> eventProducers) throws SoCTraceException {
		final DeltaManager dm = new DeltaManager();
		openTraceDBObject(t);
		final TimeRegion region = new TimeRegion(intervals.get(0).t1, intervals.get(0).t2);
		List<GenericReducedEvent> proxy = new ArrayList<GenericReducedEvent>();

		// types
		if (eventTypes != null)
			if (eventTypes.size() == 0)
				return proxy;
		if (eventProducers != null)
			if (eventProducers.size() == 0)
				return proxy;
		dm.start();
		final GenericReducedEventQuery query = new GenericReducedEventQuery(traceDB);
		final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		// and.addCondition(new SimpleCondition("PAGE", ComparisonOperation.EQ,
		// Long.toString(i)));

		// intervals
		if (region != null) {
			if (traceDB.getMaxTimestamp() != region.getTimeStampEnd())
				and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.LE, Long.toString(region.getTimeStampEnd())));
			if (traceDB.getMinTimestamp() != region.getTimeStampStart())
				and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, Long.toString(region.getTimeStampStart())));
		}

		if (eventTypes != null) {
			final ValueListString vls = new ValueListString();
			for (final EventType et : eventTypes)
				vls.addValue(String.valueOf(et.getId()));
			and.addCondition(new SimpleCondition("EVENT_TYPE_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		// eventProducers
		if (eventProducers != null) {
			final ValueListString vls = new ValueListString();
			for (final EventProducer ep : eventProducers)
				vls.addValue(String.valueOf(ep.getId()));
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		if (and.getNumberOfConditions() == 1)
			and.addCondition(new SimpleCondition("'1'", ComparisonOperation.EQ, "1"));
		query.setElementWhere(and);
		query.setOrderBy("TIMESTAMP", OrderBy.ASC);
		proxy = query.getReducedEventList();
		traceDB.close();
		return proxy;
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
