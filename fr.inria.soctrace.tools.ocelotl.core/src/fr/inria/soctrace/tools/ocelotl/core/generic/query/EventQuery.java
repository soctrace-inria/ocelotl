/*******************************************************************************
 * Copyright (c) 2013 Generoso Pagano.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.core.generic.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.SelfDefiningElementQuery;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.conditions.ICondition;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * GenericQuery class for Event self-defining-pattern tables.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class EventQuery extends SelfDefiningElementQuery {

	protected static final boolean	USE_JOIN	= false;

	protected ICondition			typeWhere;
	protected ICondition			eventProducerWhere;
	private ModelElementCache		eventProducerCache;

	/**
	 * The constructor
	 * 
	 * @param traceDB
	 *            Trace DB object where the genericQuery is performed.
	 */
	public EventQuery(final TraceDBObject traceDB) {
		super(traceDB);
		clear();
	}

	/**
	 * Clear genericQuery: removes all the conditions. EventProducer cache is not
	 * cleared, since we are not changing trace DB.
	 */
	@Override
	public void clear() {
		super.clear();
		typeWhere = null;
		eventProducerWhere = null;
		if (eventProducerCache != null)
			eventProducerCache.clear();
		eventProducerCache = null;
	}

	@Override
	public DBObject getDBObject() {
		return dbObj;
	}

	@Override
	public String getElementTableName() {
		return FramesocTable.EVENT.toString();
	}

	/**
	 * Get the event param type with the given name, for the event type whose ID
	 * is passed.
	 * 
	 * TODO: this can be optimized with the type cache
	 * 
	 * @param name
	 *            the event param type name
	 * @param eventTypeId
	 *            the event type ID
	 * @return the corresponding event param type
	 * @throws SoCTraceException
	 */
	private ParamType getEventParamType(final String name, final int eventTypeId) throws SoCTraceException {
		try {
			final Statement stm = dbObj.getConnection().createStatement();
			final ResultSet rs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_PARAM_TYPE + " WHERE NAME='" + name + "' AND EVENT_TYPE_ID=" + eventTypeId);
			if (rs.next())
				return new ParamType(rs.getInt("ID"), name, rs.getString("TYPE"));
			return null;
		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Build the EventProducer object corresponding to the passed ID
	 * 
	 * @param id
	 *            event producer ID
	 * @return a EventProducer
	 * @throws SoCTraceException
	 */
	private EventProducer getEventProducer(final int id) throws SoCTraceException {
		if (eventProducerCache == null) {
			eventProducerCache = new ModelElementCache();
			eventProducerCache.addElementMap(EventProducer.class);
		}

		EventProducer eventProducer;
		if ((eventProducer = eventProducerCache.get(EventProducer.class, id)) != null)
			return eventProducer;

		try {
			final Statement stm = dbObj.getConnection().createStatement();
			final ResultSet rs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_PRODUCER + " WHERE ID=" + id);
			if (rs.next()) {
				eventProducer = new EventProducer(id);
				eventProducer.setType(rs.getString("TYPE"));
				eventProducer.setLocalId(rs.getString("LOCAL_ID"));
				eventProducer.setName(rs.getString("NAME"));
				eventProducer.setParentId(rs.getInt("PARENT_ID"));
				eventProducerCache.put(eventProducer);
				return eventProducer;
			}
			return null;
		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Get the event type id, given the event type name.
	 * 
	 * TODO: this can be optimized with the type cache
	 * 
	 * @param name
	 *            event type name
	 * @return the corresponding event type ID
	 * @throws SoCTraceException
	 */
	private int getEventTypeId(final String name) throws SoCTraceException {
		try {
			final Statement stm = dbObj.getConnection().createStatement();
			final ResultSet rs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_TYPE + " WHERE NAME='" + name + "'");
			if (rs.next())
				return rs.getInt("ID");
			return -1;
		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Builds a list of Event respecting the condition specified by elementWhere
	 * AND typeWhere AND eventProducerWhere AND parametersConditions. The
	 * different parameter conditions are evaluated in OR, since they refer to
	 * different event types so it makes no sense having an AND.
	 * 
	 * @return the event list.
	 * @throws SoCTraceException
	 */
	@Override
	public List<Event> getList() throws SoCTraceException {

		try {
			final DeltaManager dm = new DeltaManager();
			dm.start();

			boolean first = true;
			StringBuffer eventQuery = null;
			if (USE_JOIN) {
				debug("Experimental genericQuery with join");
				eventQuery = new StringBuffer("SELECT * FROM " + FramesocTable.EVENT + " join " + FramesocTable.EVENT_PARAM + " on " + FramesocTable.EVENT + ".ID = " + FramesocTable.EVENT_PARAM + ".EVENT_ID ");
			} else
				eventQuery = new StringBuffer("SELECT * FROM " + FramesocTable.EVENT + " ");

			if (where)
				eventQuery.append(" WHERE ");

			if (elementWhere != null) {
				first = false;
				eventQuery.append(elementWhere.getSQLString());
			}

			if (typeWhere != null) {
				if (!first)
					eventQuery.append(" AND ");
				else
					first = false;
				eventQuery.append("( EVENT_TYPE_ID IN ( SELECT ID FROM " + FramesocTable.EVENT_TYPE + " WHERE " + typeWhere.getSQLString() + " ) )");
			}

			if (eventProducerWhere != null) {
				if (!first)
					eventQuery.append(" AND ");
				else
					first = false;
				eventQuery.append("( EVENT_PRODUCER_ID IN ( SELECT ID FROM " + FramesocTable.EVENT_PRODUCER + " WHERE " + eventProducerWhere.getSQLString() + " ) )");
			}

			if (parametersConditions.size() > 0) {
				if (!first)
					eventQuery.append(" AND ");
				else
					first = false;

				eventQuery.append(getParamConditionsString());
			}

			if (orderBy)
				eventQuery.append(" ORDER BY " + orderByColumn + " " + orderByCriterium);

			final String query = eventQuery.toString();
			debug(query);

			final Statement stm = dbObj.getConnection().createStatement();

			final ResultSet rs = stm.executeQuery(query);
			List<Event> elist = null;
			if (USE_JOIN)
				elist = rebuildEventsJoin(rs);
			else
				elist = rebuildEvents(rs);

			debug(dm.endMessage("EventQuery.getList()"));

			stm.close();
			return elist;

		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}

	}

	@Override
	public ParamType getParamType(final String typeName, final int typeId) throws SoCTraceException {
		return getEventParamType(typeName, typeId);
	}

	/*
	 * U t i l i t i e s
	 */

	@Override
	public int getTypeId(final String typeName) throws SoCTraceException {
		return getEventTypeId(typeName);
	}

	/**
	 * Rebuild an Event, given the corresponding EVENT table row.
	 * 
	 * @param rs
	 *            EVENT table row
	 * @param epVls
	 * @return the Event
	 * @throws SQLException
	 * @throws SoCTraceException
	 */
	private Event rebuildEvent(final ResultSet rs) throws SQLException, SoCTraceException {
		final Event e = new Event(rs.getInt(1));
		final TraceDBObject traceDB = (TraceDBObject) dbObj;
		final EventType et = traceDB.getEventTypeCache().get(EventType.class, rs.getInt(2));
		final EventProducer s = getEventProducer(rs.getInt(3));
		e.setEventProducer(s);
		e.setType(et);
		e.setTimestamp(rs.getLong(4));
		e.setCpu(rs.getInt(5));
		e.setPage(rs.getInt(6));
		return e;
	}

	/**
	 * Rebuild an EventParam, given the corresponding join result set row.
	 * 
	 * @param rs
	 *            Result set row corresponding to a SELECT * FROM EVENT join
	 *            EVENT_PARAM ...
	 * @param e
	 *            Event related to this parameter
	 * @return the EventParameter
	 * @throws SQLException
	 * @throws SoCTraceException
	 */
	private EventParam rebuildEventParam(final ResultSet rs, final Event e) throws SQLException, SoCTraceException {
		final TraceDBObject traceDB = (TraceDBObject) dbObj;
		final EventParam ep = new EventParam(rs.getInt(7));
		ep.setEvent(e);
		ep.setEventParamType(traceDB.getEventTypeCache().get(EventParamType.class, rs.getInt(9)));
		ep.setValue(rs.getString(10));
		return ep;
	}

	/**
	 * Rebuild an EventParam, given the corresponding EVENT_PARAM table row.
	 * 
	 * @param prs
	 *            EVENT_PARAM table row
	 * @param tmp
	 *            map containing the Events returned by the genericQuery
	 * @return the EventParam
	 * @throws SQLException
	 * @throws SoCTraceException
	 */
	private EventParam rebuildEventParam(final ResultSet prs, final Map<Integer, Event> tmp) throws SQLException, SoCTraceException {
		final EventParam ep = new EventParam(prs.getInt(1));
		ep.setEvent(tmp.get(prs.getInt(2)));
		final TraceDBObject traceDB = (TraceDBObject) dbObj;
		ep.setEventParamType(traceDB.getEventTypeCache().get(EventParamType.class, prs.getInt(3)));
		ep.setValue(prs.getString(4));
		return ep;
	}

	/**
	 * Rebuilds the events corresponding to the result set.
	 * 
	 * @param rs
	 *            Result set corresponding to a SELECT * FROM EVENT ...
	 * @return a list of Event
	 * @throws SoCTraceException
	 */
	private List<Event> rebuildEvents(final ResultSet rs) throws SoCTraceException {

		final ValueListString vls = new ValueListString();
		final List<Event> list = new LinkedList<Event>();
		final Map<Integer, Event> tmp = new HashMap<Integer, Event>();
		try {

			while (rs.next()) {
				final Event e = rebuildEvent(rs);
				list.add(e);

				// to rebuild all params
				tmp.put(e.getId(), e);
				vls.addValue(String.valueOf(e.getId()));
			}

			if (vls.size() == 0)
				return list;

			final Statement stm = dbObj.getConnection().createStatement();
			final ResultSet prs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_PARAM + " WHERE EVENT_ID IN " + vls.getValueString());
			while (prs.next())
				rebuildEventParam(prs, tmp);

			return list;
		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Rebuilds the events corresponding to the result set.
	 * 
	 * @param rs
	 *            Result set corresponding to a SELECT * FROM EVENT join
	 *            EVENT_PARAM ...
	 * @return a list of Event
	 * @throws SoCTraceException
	 */
	private List<Event> rebuildEventsJoin(final ResultSet rs) throws SoCTraceException {
		final List<Event> list = new LinkedList<Event>();
		final Map<Integer, Event> tmp = new HashMap<Integer, Event>();
		try {
			while (rs.next()) {
				final int id = rs.getInt(1);
				if (!tmp.containsKey(id)) {
					final Event e = rebuildEvent(rs);
					rebuildEventParam(rs, e);
					list.add(e);
					tmp.put(id, e);
				} else {
					final Event e = tmp.get(id);
					rebuildEventParam(rs, e);
				}
			}
			return list;
		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Set the condition to be put in the WHERE clause of EVENT_PRODUCER table.
	 * 
	 * @param eventProducerCondition
	 *            condition to be applied to the event producer table
	 */
	public void setEventProducerWhere(final ICondition eventProducerCondition) {
		where = true;
		eventProducerWhere = eventProducerCondition;
	}

	/**
	 * Set the condition to be put in the WHERE clause of EVENT_TYPE table.
	 * 
	 * @param typeCondition
	 *            condition to be applied to the event type table
	 */
	public void setTypeWhere(final ICondition typeCondition) {
		where = true;
		typeWhere = typeCondition;
	}

}
