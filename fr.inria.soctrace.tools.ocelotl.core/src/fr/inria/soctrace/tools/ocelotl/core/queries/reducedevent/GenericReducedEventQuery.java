/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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

package fr.inria.soctrace.tools.ocelotl.core.queries.reducedevent;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.ValueListString;
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
public class GenericReducedEventQuery extends EventQuery {

	/**
	 * The constructor
	 * 
	 * @param traceDB
	 *            Trace DB object where the ocelotlQueries is performed.
	 */
	public GenericReducedEventQuery(final TraceDBObject traceDB) {
		super(traceDB);
		clear();
	}

	public List<GenericReducedEvent> getReducedEventList()
			throws SoCTraceException {
		try {
			final DeltaManager dm = new DeltaManagerOcelotl();
			dm.start();

			boolean first = true;
			final StringBuffer eventQuery = new StringBuffer("SELECT * FROM "
					+ FramesocTable.EVENT + " ");

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

			final String query = eventQuery.toString();
			debug(query);

			final Statement stm = dbObj.getConnection().createStatement();

			final ResultSet rs = stm.executeQuery(query);
			final List<GenericReducedEvent> elist = rebuildReducedEvent(rs);

			debug(dm.endMessage("EventQuery.getList()"));

			stm.close();
			return elist;

		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}

	}

	private List<GenericReducedEvent> rebuildReducedEvent(final ResultSet rs)
			throws SoCTraceException {

		final HashMap<Integer, GenericReducedEvent> list = new HashMap<Integer, GenericReducedEvent>();
		final LinkedList<GenericReducedEvent> llist = new LinkedList<GenericReducedEvent>();
		final ValueListString vls = new ValueListString();
		new ValueListString();
		new ArrayList<Integer>();
		final TraceDBObject traceDB = (TraceDBObject) dbObj;
		try {

			while (rs.next()) {
				final GenericReducedEvent re = new GenericReducedEvent(
						rs.getInt("ID"), rs.getInt("EVENT_PRODUCER_ID"),
						rs.getInt("PAGE"), rs.getLong("TIMESTAMP"), traceDB
								.getEventTypeCache()
								.get(EventType.class, rs.getInt(2)).getName());
				list.put(re.ID, re);
				llist.add(re);
				vls.addValue(String.valueOf(re.ID));
			}

			return llist;
		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
