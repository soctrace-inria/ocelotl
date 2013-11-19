/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
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

package fr.inria.soctrace.tools.ocelotl.core.generic.query;

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
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * GenericQuery class for Event self-defining-pattern tables.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class EventProxyQuery extends EventQuery {

	private static final boolean	USE_JOIN	= false;

	/**
	 * The constructor
	 * 
	 * @param traceDB
	 *            Trace DB object where the genericQuery is performed.
	 */
	public EventProxyQuery(final TraceDBObject traceDB) {
		super(traceDB);
		clear();
	}

	public List<EventProxy> getIDList() throws SoCTraceException {

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
			List<EventProxy> elist = null;
			elist = rebuildEventID(rs);

			debug(dm.endMessage("EventQuery.getList()"));

			stm.close();
			return elist;

		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}

	}

	private List<EventProxy> rebuildEventID(final ResultSet rs) throws SoCTraceException {

		final List<EventProxy> list = new LinkedList<EventProxy>();
		try {

			while (rs.next())
				list.add(new EventProxy(rs.getInt("ID"), rs.getInt("EVENT_PRODUCER_ID")));
			return list;
		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}
	}
}
