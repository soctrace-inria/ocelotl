package fr.inria.soctrace.tools.ocelotl.core.query;

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
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.SelfDefiningElementQuery;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.conditions.ICondition;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeExternalConstants;

/**
 * Query class for Event self-defining-pattern tables.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class ReducedEventQuery extends EventQuery {

	/**
	 * The constructor
	 * @param traceDB Trace DB object where the query is performed.
	 */
	public ReducedEventQuery(TraceDBObject traceDB) {
		super(traceDB);
		clear();
	}

	
	
public List<ReducedEvent> getReducedEventList() throws SoCTraceException {
		
		try {
			DeltaManager dm = new DeltaManager();
			dm.start();
			
			boolean first = true;
			StringBuffer eventQuery = new StringBuffer("SELECT * FROM " + FramesocTable.EVENT + " ");

			if (where) {
				eventQuery.append(" WHERE ");
			}

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
					+ FramesocTable.EVENT_TYPE + " WHERE " + typeWhere.getSQLString() + " ) )");
			} 

			if (eventProducerWhere != null) {
				if (!first)
					eventQuery.append(" AND ");
				else
					first = false;
				eventQuery.append("( EVENT_PRODUCER_ID IN ( SELECT ID FROM " 
					+ FramesocTable.EVENT_PRODUCER + " WHERE " + eventProducerWhere.getSQLString() + " ) )");
			} 

			if (parametersConditions.size()>0) {
				if (!first)
					eventQuery.append(" AND ");
				else
					first = false;

				eventQuery.append(getParamConditionsString());
			}

			if (orderBy) {
				eventQuery.append(" ORDER BY " + orderByColumn + " " + orderByCriterium);
			}

			String query = eventQuery.toString();
			debug(query);

			Statement stm = dbObj.getConnection().createStatement();
			
			ResultSet rs = stm.executeQuery(query);
			List<ReducedEvent> elist = rebuildReducedEvent(rs);

			debug(dm.endMessage("EventQuery.getList()"));
			
			stm.close();
			return elist;
			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

	}



	

	private List<ReducedEvent> rebuildReducedEvent(ResultSet rs) throws SoCTraceException {
				
		HashMap<Integer, ReducedEvent> list = new HashMap<Integer, ReducedEvent>();
		LinkedList<ReducedEvent> llist = new LinkedList<ReducedEvent>();
		ValueListString vls = new ValueListString();
		ValueListString pvls = new ValueListString();
		try {		
		
			while (rs.next()) {
				ReducedEvent re = new ReducedEvent(rs.getInt("ID"), rs.getInt("EVENT_PRODUCER_ID"), rs.getInt("PAGE"), rs.getLong("TIMESTAMP"), null);
				list.put(re.ID, re);
				llist.add(re);
				vls.addValue(String.valueOf(re.ID));
			}
			if (llist.size()==0)
				return llist;
			
			Statement stm = dbObj.getConnection().createStatement();
			ResultSet pprs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_PARAM_TYPE + 
					" WHERE NAME='" + PajeExternalConstants.PajeStateValue+"'");
			while (pprs.next())
				pvls.addValue(String.valueOf(pprs.getInt("ID")));
			String query;
			query = "SELECT * FROM " + FramesocTable.EVENT_PARAM + 
					" WHERE EVENT_ID IN " + vls.getValueString() + " AND EVENT_PARAM_TYPE_ID IN " + pvls.getValueString();
					
			ResultSet prs = stm.executeQuery(query);//TODO verifier
			while (prs.next())
				list.get(prs.getInt("EVENT_ID")).VALUE=prs.getString("VALUE");
			return llist;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
}




