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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.IteratorQueries.EventIterator;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class OcelotlTraceSearch extends TraceSearch {

	OcelotlParameters parameters;

	public OcelotlTraceSearch(OcelotlParameters parameters) {
		super();
		this.parameters = parameters;
	}

	@SuppressWarnings("unused")
	private LogicalCondition buildIntervalCondition(final IntervalDesc interval) {
		final LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("TIMESTAMP",
				ComparisonOperation.GE, String.valueOf(interval.t1)));
		and.addCondition(new SimpleCondition("TIMESTAMP",
				ComparisonOperation.LE, String.valueOf(interval.t2)));
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

	/**
	 * Build the query to get all events
	 * 
	 * @param t
	 *            The trace from which the events are taken
	 * @param eventTypes
	 *            The list of selected event types
	 * @param intervals
	 *            The list of selected time intervals
	 * @param eventProducers
	 *            The list of selected event producers
	 * @return an event iterator
	 * @throws SoCTraceException
	 * @throws OcelotlException
	 */
	public EventIterator getEventIterator(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		openTraceDBObject(t);
		final IteratorQueries query = new IteratorQueries(traceDB);
		// final LogicalCondition and = new
		// LogicalCondition(LogicalOperation.AND);
		final LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		ConditionManager conditionManager = new ConditionManager();

		// If we filter the event types
		if (eventTypes != null) {
			if (eventTypes.size() == 0)
				throw new OcelotlException(OcelotlException.NO_EVENT_TYPE);
			if (eventTypes.size() != parameters.getAllEventTypes().size()) {
				final ValueListString vls = new ValueListString();
				for (final EventType et : eventTypes)
					vls.addValue(String.valueOf(et.getId()));
				conditionManager.addCondition(new SimpleCondition(
						"EVENT_TYPE_ID", ComparisonOperation.IN, vls
								.getValueString()));
			}
		}

		// If we filter the eventProducers
		if (eventProducers != null) {
			if (eventProducers.size() == 0)
				throw new OcelotlException(OcelotlException.NO_EVENT_PRODUCER);
			if (eventProducers.size() != parameters.getAllEventProducers()
					.size()) {
				final ValueListString vls = new ValueListString();
				for (final EventProducer ep : eventProducers)
					vls.addValue(String.valueOf(ep.getId()));
				conditionManager.addCondition(new SimpleCondition(
						"EVENT_PRODUCER_ID", ComparisonOperation.IN, vls
								.getValueString()));
			}
		}

		// If we filter based on the timestamps
		if (!intervals.isEmpty()) {
			long min = parameters.getTrace().getMinTimestamp();
			long max = parameters.getTrace().getMaxTimestamp();

			// For each timestamps intervals
			for (IntervalDesc anInterval : intervals) {
				TimeRegion aRegion = new TimeRegion(anInterval.t1,
						anInterval.t2);
				final LogicalCondition andTimeStamps = new LogicalCondition(
						LogicalOperation.AND);

				// Optimize by checking if one of the timestamps is a boundary
				// of the trace
				if (min != aRegion.getTimeStampStart())
					andTimeStamps.addCondition(new SimpleCondition("TIMESTAMP",
							ComparisonOperation.GE, Long.toString(aRegion
									.getTimeStampStart())));
				if (max != aRegion.getTimeStampEnd())
					andTimeStamps.addCondition(new SimpleCondition("TIMESTAMP",
							ComparisonOperation.LE, Long.toString(aRegion
									.getTimeStampEnd())));

				if (intervals.size() == 1
						&& andTimeStamps.getNumberOfConditions() > 0) {
					if (andTimeStamps.getNumberOfConditions() == 1) {
						andTimeStamps.addCondition(new SimpleCondition("'1'",
								ComparisonOperation.EQ, String.valueOf(1)));
					}
					conditionManager.addCondition(andTimeStamps);
				} else {
					if (andTimeStamps.getNumberOfConditions() == 1) {
						andTimeStamps.addCondition(new SimpleCondition("'1'",
								ComparisonOperation.EQ, String.valueOf(1)));
					}

					// add a or condition between each interval
					or.addCondition(andTimeStamps);
				}
			}

			if (or.getNumberOfConditions() > 1) {
				conditionManager.addCondition(or);
			}
		}
		conditionManager.setWhere(query);
		query.setLoadParameters(false);
		return query.getIterator(monitor);
	}
	
	private void eventTypeConditionOPT(final List<EventType> eventTypes, ConditionManager conditionManager, int aCategory) throws OcelotlException{
		if (eventTypes != null) {
			if (eventTypes.size() == 0)
				throw new OcelotlException(OcelotlException.NO_EVENT_TYPE);
			if (eventTypes.size() != parameters.getAllEventTypes().size()) {
				if (eventTypes.containsAll(parameters.getCatEventTypes().get(
						aCategory))) {
					conditionManager.addCondition(new SimpleCondition(
							"CATEGORY", ComparisonOperation.EQ, String
									.valueOf(aCategory)));
				} else {
					final ValueListString vls = new ValueListString();
					for (final EventType et : eventTypes)
						vls.addValue(String.valueOf(et.getId()));
					conditionManager.addCondition(new SimpleCondition(
							"EVENT_TYPE_ID", ComparisonOperation.IN, vls
									.getValueString()));
				}
			}
		}
	}
	
	private void eventTypeConditionNOPT(final List<EventType> eventTypes, ConditionManager conditionManager, int aCategory) throws OcelotlException{
		if (eventTypes != null) {
			if (eventTypes.size() == 0)
				throw new OcelotlException(OcelotlException.NO_EVENT_TYPE);
					final ValueListString vls = new ValueListString();
					for (final EventType et : eventTypes)
						vls.addValue(String.valueOf(et.getId()));
					conditionManager.addCondition(new SimpleCondition(
							"EVENT_TYPE_ID", ComparisonOperation.IN, vls
									.getValueString()));
				
			}
	}
	
	
	private void eventProducerConditionOPT(final List<EventProducer> eventProducers, ConditionManager conditionManager) throws OcelotlException{
	if (eventProducers != null) {
		if (eventProducers.size() == 0)
			throw new OcelotlException(OcelotlException.NO_EVENT_PRODUCER);
		if (eventProducers.size() != parameters.getAllEventProducers()
				.size()) {
			final ValueListString vls = new ValueListString();
			for (final EventProducer ep : eventProducers)
				vls.addValue(String.valueOf(ep.getId()));
			conditionManager.addCondition(new SimpleCondition(
					"EVENT_PRODUCER_ID", ComparisonOperation.IN, vls
							.getValueString()));
		}
	}
	}
	
	private void eventProducerConditionNOPT(final List<EventProducer> eventProducers, ConditionManager conditionManager) throws OcelotlException{
	if (eventProducers != null) {
		if (eventProducers.size() == 0)
			throw new OcelotlException(OcelotlException.NO_EVENT_PRODUCER);
			final ValueListString vls = new ValueListString();
			for (final EventProducer ep : eventProducers)
				vls.addValue(String.valueOf(ep.getId()));
			conditionManager.addCondition(new SimpleCondition(
					"EVENT_PRODUCER_ID", ComparisonOperation.IN, vls
							.getValueString()));
		
	}
	}
	
	private void timeConditionOPT(final List<IntervalDesc> intervals, ConditionManager conditionManager) throws OcelotlException{
		final LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		if (!intervals.isEmpty()) {
			long min = parameters.getTrace().getMinTimestamp();
			long max = parameters.getTrace().getMaxTimestamp();

			// For each timestamp intervals
			for (IntervalDesc anInterval : intervals) {

				SimpleCondition t1 = null;
				SimpleCondition t2 = null;
				SimpleCondition d1 = null;
				SimpleCondition d2 = null;

				TimeRegion aRegion = new TimeRegion(anInterval.t1,
						anInterval.t2);
				final LogicalCondition ort = new LogicalCondition(
						LogicalOperation.OR);
				final LogicalCondition andt = new LogicalCondition(
						LogicalOperation.AND);
				final LogicalCondition andd = new LogicalCondition(
						LogicalOperation.AND);

				if (max > aRegion.getTimeStampEnd())
					t2 = new SimpleCondition("TIMESTAMP",
							ComparisonOperation.LE, Long.toString(aRegion
									.getTimeStampEnd()));
				// If the state ends after the start date select it also
				if (min < aRegion.getTimeStampStart()) {
					t1 = new SimpleCondition("TIMESTAMP",
							ComparisonOperation.GE, Long.toString(aRegion
									.getTimeStampStart()));
					d1 = new SimpleCondition("TIMESTAMP",
							ComparisonOperation.LT, Long.toString(aRegion
									.getTimeStampStart()));
					d2 = new SimpleCondition("LPAR", ComparisonOperation.GE,
							Long.toString(aRegion.getTimeStampStart()));
				}
				if (t1 == null && t2 != null)
					if (intervals.size() == 1)
						conditionManager.addCondition(t2);
					else
						or.addCondition(t2);

				else if (t2 == null && t1 != null) {
					ort.addCondition(t1);
					andd.addCondition(d1);
					andd.addCondition(d2);
					ort.addCondition(andd);
					if (intervals.size() == 1)
						conditionManager.addCondition(ort);
					else
						or.addCondition(ort);

				} else if (t2 != null && t1 != null) {
					andt.addCondition(t1);
					andt.addCondition(t2);
					ort.addCondition(andt);
					andd.addCondition(d1);
					andd.addCondition(d2);
					ort.addCondition(andd);
					if (intervals.size() == 1)
						conditionManager.addCondition(ort);
					else
						or.addCondition(ort);
				}
			}
			if (or.getNumberOfConditions() > 1) {
				conditionManager.addCondition(or);
			}
		}
	}
	
	private void timeConditionNOPT(final List<IntervalDesc> intervals, ConditionManager conditionManager) throws OcelotlException{
		final LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		if (!intervals.isEmpty()) {

			// For each timestamp intervals
			for (IntervalDesc anInterval : intervals) {

				SimpleCondition t1 = null;
				SimpleCondition t2 = null;
				SimpleCondition d1 = null;
				SimpleCondition d2 = null;

				TimeRegion aRegion = new TimeRegion(anInterval.t1,
						anInterval.t2);
				final LogicalCondition ort = new LogicalCondition(
						LogicalOperation.OR);
				final LogicalCondition andt = new LogicalCondition(
						LogicalOperation.AND);
				final LogicalCondition andd = new LogicalCondition(
						LogicalOperation.AND);

					t2 = new SimpleCondition("TIMESTAMP",
							ComparisonOperation.LE, Long.toString(aRegion
									.getTimeStampEnd()));
				// If the state ends after the start date select it also
					t1 = new SimpleCondition("TIMESTAMP",
							ComparisonOperation.GE, Long.toString(aRegion
									.getTimeStampStart()));
					d1 = new SimpleCondition("TIMESTAMP",
							ComparisonOperation.LT, Long.toString(aRegion
									.getTimeStampStart()));
					d2 = new SimpleCondition("LPAR", ComparisonOperation.GE,
							Long.toString(aRegion.getTimeStampStart()));
					andt.addCondition(t1);
					andt.addCondition(t2);
					ort.addCondition(andt);
					andd.addCondition(d1);
					andd.addCondition(d2);
					ort.addCondition(andd);
					if (intervals.size() == 1)
						conditionManager.addCondition(ort);
					else
						or.addCondition(ort);
			}
			if (or.getNumberOfConditions() > 1) {
				conditionManager.addCondition(or);
			}
		}
	}
	
	
	

	public EventIterator getCategorySpecificIteratorOPT(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, int aCategory,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		openTraceDBObject(t);
		final IteratorQueries query = new IteratorQueries(traceDB);
		ConditionManager conditionManager = new ConditionManager();
		eventTypeConditionOPT(eventTypes, conditionManager, aCategory);
		eventProducerConditionOPT(eventProducers, conditionManager);
		timeConditionOPT(intervals, conditionManager);
		conditionManager.setWhere(query);
		query.setLoadParameters(false);
		return query.getIterator(monitor);
	}
	
	public EventIterator getCategorySpecificIteratorT(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, int aCategory,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		openTraceDBObject(t);
		final IteratorQueries query = new IteratorQueries(traceDB);
		ConditionManager conditionManager = new ConditionManager();
		eventTypeConditionNOPT(eventTypes, conditionManager, aCategory);
		eventProducerConditionNOPT(eventProducers, conditionManager);
		timeConditionOPT(intervals, conditionManager);
		conditionManager.setWhere(query);
		query.setLoadParameters(false);
		return query.getIterator(monitor);
	}
	
	public EventIterator getCategorySpecificIteratorEP(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, int aCategory,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		openTraceDBObject(t);
		final IteratorQueries query = new IteratorQueries(traceDB);
		ConditionManager conditionManager = new ConditionManager();
		eventTypeConditionNOPT(eventTypes, conditionManager, aCategory);
		eventProducerConditionOPT(eventProducers, conditionManager);
		timeConditionNOPT(intervals, conditionManager);
		conditionManager.setWhere(query);
		query.setLoadParameters(false);
		return query.getIterator(monitor);
	}
	
	public EventIterator getCategorySpecificIteratorET(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, int aCategory,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		openTraceDBObject(t);
		final IteratorQueries query = new IteratorQueries(traceDB);
		ConditionManager conditionManager = new ConditionManager();
		eventTypeConditionOPT(eventTypes, conditionManager, aCategory);
		eventProducerConditionNOPT(eventProducers, conditionManager);
		timeConditionNOPT(intervals, conditionManager);
		conditionManager.setWhere(query);
		query.setLoadParameters(false);
		return query.getIterator(monitor);
	}
	
	public EventIterator getCategorySpecificIteratorTEP(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, int aCategory,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		openTraceDBObject(t);
		final IteratorQueries query = new IteratorQueries(traceDB);
		ConditionManager conditionManager = new ConditionManager();
		eventTypeConditionNOPT(eventTypes, conditionManager, aCategory);
		eventProducerConditionOPT(eventProducers, conditionManager);
		timeConditionOPT(intervals, conditionManager);
		conditionManager.setWhere(query);
		query.setLoadParameters(false);
		return query.getIterator(monitor);
	}
	
	public EventIterator getCategorySpecificIteratorTET(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, int aCategory,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		openTraceDBObject(t);
		final IteratorQueries query = new IteratorQueries(traceDB);
		ConditionManager conditionManager = new ConditionManager();
		eventTypeConditionOPT(eventTypes, conditionManager, aCategory);
		eventProducerConditionNOPT(eventProducers, conditionManager);
		timeConditionOPT(intervals, conditionManager);
		conditionManager.setWhere(query);
		query.setLoadParameters(false);
		return query.getIterator(monitor);
	}
	
	public EventIterator getCategorySpecificIteratorEPET(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, int aCategory,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		openTraceDBObject(t);
		final IteratorQueries query = new IteratorQueries(traceDB);
		ConditionManager conditionManager = new ConditionManager();
		eventTypeConditionOPT(eventTypes, conditionManager, aCategory);
		eventProducerConditionOPT(eventProducers, conditionManager);
		timeConditionNOPT(intervals, conditionManager);
		conditionManager.setWhere(query);
		query.setLoadParameters(false);
		return query.getIterator(monitor);
	}
	
	public EventIterator getCategorySpecificIteratorNOPT(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, int aCategory,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		openTraceDBObject(t);
		final IteratorQueries query = new IteratorQueries(traceDB);
		ConditionManager conditionManager = new ConditionManager();
		eventTypeConditionOPT(eventTypes, conditionManager, aCategory);
		eventProducerConditionOPT(eventProducers, conditionManager);
		timeConditionNOPT(intervals, conditionManager);
		conditionManager.setWhere(query);
		query.setLoadParameters(false);
		return query.getIterator(monitor);
	}

	public EventIterator getStateIteratorOPT(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		return getCategorySpecificIteratorOPT(t, eventTypes, intervals,
				eventProducers, EventCategory.STATE, monitor);
	}
	public EventIterator getStateIteratorT(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		return getCategorySpecificIteratorT(t, eventTypes, intervals,
				eventProducers, EventCategory.STATE, monitor);
	}
	
	public EventIterator getStateIteratorET(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		return getCategorySpecificIteratorET(t, eventTypes, intervals,
				eventProducers, EventCategory.STATE, monitor);
	}
	
	public EventIterator getStateIteratorEP(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		return getCategorySpecificIteratorEP(t, eventTypes, intervals,
				eventProducers, EventCategory.STATE, monitor);
	}
	
	public EventIterator getStateIteratorTEP(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		return getCategorySpecificIteratorTEP(t, eventTypes, intervals,
				eventProducers, EventCategory.STATE, monitor);
	}
	
	public EventIterator getStateIteratorTET(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		return getCategorySpecificIteratorTET(t, eventTypes, intervals,
				eventProducers, EventCategory.STATE, monitor);
	}
	
	public EventIterator getStateIteratorEPET(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		return getCategorySpecificIteratorEPET(t, eventTypes, intervals,
				eventProducers, EventCategory.STATE, monitor);
	}
	
	public EventIterator getStateIteratorNOPT(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		return getCategorySpecificIteratorNOPT(t, eventTypes, intervals,
				eventProducers, EventCategory.STATE, monitor);
	}

	public EventIterator getVariableIterator(final Trace t,
			final List<EventType> eventTypes,
			final List<IntervalDesc> intervals,
			final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		return getCategorySpecificIteratorOPT(t, eventTypes, intervals,
				eventProducers, EventCategory.VARIABLE, monitor);
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
