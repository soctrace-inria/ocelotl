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

/*******************************************************************************
 * Copyright (c) 2013 Damien Dosimont
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 ******************************************************************************/

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

/**
 * OcelotlQueries class for Event self-defining-pattern tables.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class IteratorQueries extends EventQuery {
	
	private static final Logger logger = LoggerFactory.getLogger(IteratorQueries.class);

	public class EventIterator {

		Event current = null;

		public Event getEvent() {
			return current;
		}

		public Event getNext() {
			try {
				current = rebuildNextEvent();
				if (current != null)
					return current;
			} catch (SQLException | SoCTraceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				mystm.close();
			} catch (final SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

	EventIterator iterator = new EventIterator();
	ResultSet rs = null;

	private Statement mystm;

	/**
	 * The constructor
	 * 
	 * @param traceDB
	 *            Trace DB object where the ocelotlQueries is performed.
	 */
	public IteratorQueries(final TraceDBObject traceDB) {
		super(traceDB);
		clear();
	}

	public EventIterator getIterator() throws SoCTraceException {

		try {
			final DeltaManager dm = new DeltaManagerOcelotl();
			dm.start();

			boolean first = true;
			StringBuilder eventQuery = null;
			eventQuery = new StringBuilder("SELECT * FROM "
					+ FramesocTable.EVENT + " ");
			loadParameters = false;

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
				eventQuery.append("( EVENT_TYPE_ID IN ( SELECT ID FROM "
						+ FramesocTable.EVENT_TYPE + " WHERE "
						+ typeWhere.getSQLString() + " ) )");
			}

			if (eventProducerWhere != null) {
				if (!first)
					eventQuery.append(" AND ");
				else
					first = false;
				eventQuery.append("( EVENT_PRODUCER_ID IN ( SELECT ID FROM "
						+ FramesocTable.EVENT_PRODUCER + " WHERE "
						+ eventProducerWhere.getSQLString() + " ) )");
			}

			if (parametersConditions.size() > 0) {
				if (!first)
					eventQuery.append(" AND ");
				else
					first = false;

				eventQuery.append(getParamConditionsString());
			}

			if (orderBy)
				eventQuery.append(" ORDER BY " + orderByColumn + " "
						+ orderByCriterium);

			if (isLimitSet())
				eventQuery.append(" LIMIT " + getLimit());

			final String query = eventQuery.toString();
			logger.debug(query);

			mystm = dbObj.getConnection().createStatement();

			final DeltaManager steps = new DeltaManagerOcelotl();
			steps.start();
			rs = mystm.executeQuery(query);
			steps.start();

		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}

		return iterator;
	}

	private Event rebuildEvent(final ResultSet rs) throws SQLException,
			SoCTraceException {
		final int category = rs.getInt(7);
		final Event e = Event.createCategorizedEvent(category, rs.getInt(1));
		final TraceDBObject traceDB = (TraceDBObject) dbObj;
		final EventType et = traceDB.getEventTypeCache().get(EventType.class,
				rs.getInt(2));
		final EventProducer s = getEventProducer(rs.getInt(3));
		e.setEventProducer(s);
		e.setCategory(rs.getInt(7));
		e.setType(et);
		e.setTimestamp(rs.getLong(4));
		e.setCpu(rs.getInt(5));
		e.setPage(rs.getInt(6));
		e.setLongPar(rs.getLong(8));
		e.setDoublePar(rs.getDouble(9));
		if (e.getCategory() == EventCategory.LINK)
			((Link) e).setEndProducer(getEventProducer(((Double) e
					.getDoublePar()).intValue()));
		return e;
	}

	public Event rebuildNextEvent() throws SQLException, SoCTraceException {
		if (rs.next())
			return rebuildEvent(rs);
		return null;
	}

}
