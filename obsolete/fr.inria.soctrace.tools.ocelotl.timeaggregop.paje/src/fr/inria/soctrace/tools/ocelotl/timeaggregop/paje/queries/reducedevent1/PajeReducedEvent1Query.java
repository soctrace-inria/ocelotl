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

package fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.queries.reducedevent1;

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

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.queries.event.EventQuery;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeExternalConstants;

/**
 * OcelotlQueries class for Event self-defining-pattern tables.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class PajeReducedEvent1Query extends EventQuery {

	/**
	 * The constructor
	 * 
	 * @param traceDB
	 *            Trace DB object where the ocelotlQueries is performed.
	 */
	public PajeReducedEvent1Query(final TraceDBObject traceDB) {
		super(traceDB);
		clear();
	}

	public List<PajeReducedEvent1> getReducedEventList() throws SoCTraceException {
		try {
			final DeltaManager dm = new DeltaManager();
			dm.start();

			boolean first = true;
			final StringBuffer eventQuery = new StringBuffer("SELECT * FROM " + FramesocTable.EVENT + " ");

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
			final List<PajeReducedEvent1> elist = rebuildReducedEvent(rs);

			debug(dm.endMessage("EventQuery.getList()"));

			stm.close();
			return elist;

		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}

	}

	private List<PajeReducedEvent1> rebuildReducedEvent(final ResultSet rs) throws SoCTraceException {

		final HashMap<Integer, PajeReducedEvent1> list = new HashMap<Integer, PajeReducedEvent1>();
		final LinkedList<PajeReducedEvent1> llist = new LinkedList<PajeReducedEvent1>();
		final ValueListString vls = new ValueListString();
		new ValueListString();
		final List<Integer> li = new ArrayList<Integer>();
		try {

			while (rs.next()) {
				final PajeReducedEvent1 re = new PajeReducedEvent1(rs.getInt("ID"), rs.getInt("EVENT_PRODUCER_ID"), rs.getInt("PAGE"), rs.getLong("TIMESTAMP"), null);
				list.put(re.ID, re);
				llist.add(re);
				vls.addValue(String.valueOf(re.ID));
			}
			if (llist.size() == 0)
				return llist;

			final Statement stm = dbObj.getConnection().createStatement();
			final ResultSet pprs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_PARAM_TYPE + " WHERE NAME='" + PajeExternalConstants.PajeStateValue + "'");
			while (pprs.next())
				li.add(pprs.getInt("ID"));
			String query;
			query = "SELECT * FROM " + FramesocTable.EVENT_PARAM + " WHERE EVENT_ID IN " + vls.getValueString();// +
																												// " AND EVENT_PARAM_TYPE_ID IN "
																												// +
																												// pvls.getValueString();

			final ResultSet prs = stm.executeQuery(query);// TODO verifier
			while (prs.next())
				if (li.contains(prs.getInt("EVENT_PARAM_TYPE_ID")))
					list.get(prs.getInt("EVENT_ID")).VALUE = prs.getString("VALUE");
			return llist;
		} catch (final SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
