/* ===========================================================
 * Filter Tool module
 * ===========================================================
 *
 * (C) Copyright 2013 Damien Dosimont. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 */

package fr.inria.soctrace.tools.filters.timefilter;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;

public class Queries {
	private TimeFilterParameters timeFilterParameters;
	final static boolean PAGE=true;

	public Queries(TimeFilterParameters timeFilterParameters)
			throws SoCTraceException {
		super();
		this.timeFilterParameters=timeFilterParameters;
	}
	
	public List<EventProducer> getEventProducers() throws SoCTraceException{
		if (PAGE){
			if (timeFilterParameters.isInclude())
				return getIncludedEventProducersPerPage();
			else
				return getExcludedEventProducersPerPage();
		}else{
		if (timeFilterParameters.isInclude())
			return getIncludedEventProducers();
		else
			return getExcludedEventProducers();
		}
	}
	
	public List<EventProducer> getAllEventProducers() throws SoCTraceException{
		TraceDBObject traceDB = new TraceDBObject(timeFilterParameters.getTrace().getDbName(), DBMode.DB_OPEN);
		EventProducerQuery eventProducerQuery= new EventProducerQuery(traceDB);
		List<EventProducer> eplist = eventProducerQuery.getList();
		traceDB.close();
		return eplist;
	}
	
	public List<EventProducer> getIncludedEventProducers() throws SoCTraceException{
		TraceDBObject traceDB = new TraceDBObject(timeFilterParameters.getTrace().getDbName(), DBMode.DB_OPEN);
		List<EventProducer> ieplist = new ArrayList<EventProducer>();
		EventQuery eventQuery = new EventQuery(traceDB);
		LogicalCondition where = new LogicalCondition(LogicalOperation.AND);
		where.addCondition(new SimpleCondition("TIMESTAMP",
				ComparisonOperation.GE, String.valueOf(timeFilterParameters
						.getTimeRegion().getTimeStampStart())));
		where.addCondition(new SimpleCondition("TIMESTAMP",
				ComparisonOperation.LE, String.valueOf(timeFilterParameters
						.getTimeRegion().getTimeStampEnd())));
		eventQuery.setElementWhere(where);
		eventQuery.setOrderBy("TIMESTAMP", OrderBy.ASC);
		List<Event> elist = eventQuery.getList();
		for (Event e : elist){
			if (!ieplist.contains(e.getEventProducer()))
				ieplist.add(e.getEventProducer());
		}
		traceDB.close();
		return ieplist;
	}
	
	
	public List<EventProducer> getIncludedEventProducersPerPage() throws SoCTraceException{
		TraceDBObject traceDB = new TraceDBObject(timeFilterParameters.getTrace().getDbName(), DBMode.DB_OPEN);
		List<EventProducer> ieplist = new ArrayList<EventProducer>();
		for (long i=0; i<=traceDB.getMaxPage(); i++){
			EventQuery eventQuery = new EventQuery(traceDB);
			LogicalCondition where = new LogicalCondition(LogicalOperation.AND);
			where.addCondition(new SimpleCondition("TIMESTAMP",
					ComparisonOperation.GE, String.valueOf(timeFilterParameters
							.getTimeRegion().getTimeStampStart())));
			where.addCondition(new SimpleCondition("TIMESTAMP",
					ComparisonOperation.LE, String.valueOf(timeFilterParameters
							.getTimeRegion().getTimeStampEnd())));
			where.addCondition(new SimpleCondition("PAGE",
					ComparisonOperation.EQ, String.valueOf(i)));
			eventQuery.setElementWhere(where);
			eventQuery.setOrderBy("TIMESTAMP", OrderBy.ASC);
			List<Event> elist = eventQuery.getList();
			for (Event e : elist){
				if (!ieplist.contains(e.getEventProducer()))
					ieplist.add(e.getEventProducer());
			}
		}
		traceDB.close();
		return ieplist;
	}
	
	public List<EventProducer> getExcludedEventProducers() throws SoCTraceException{
		TraceDBObject traceDB = new TraceDBObject(timeFilterParameters.getTrace().getDbName(), DBMode.DB_OPEN);
		List<EventProducer> eeplist = new ArrayList<EventProducer>();
		eeplist.addAll(getAllEventProducers());
		EventQuery eventQuery = new EventQuery(traceDB);
		LogicalCondition where = new LogicalCondition(LogicalOperation.AND);
		where.addCondition(new SimpleCondition("TIMESTAMP",
				ComparisonOperation.GE, String.valueOf(timeFilterParameters
						.getTimeRegion().getTimeStampStart())));
		where.addCondition(new SimpleCondition("TIMESTAMP",
				ComparisonOperation.LE, String.valueOf(timeFilterParameters
						.getTimeRegion().getTimeStampEnd())));
		eventQuery.setElementWhere(where);
		eventQuery.setOrderBy("TIMESTAMP", OrderBy.ASC);
		List<Event> elist = eventQuery.getList();
		for (Event e : elist){
				for (EventProducer ep : eeplist){
					if (ep.getId()==e.getEventProducer().getId()){
						eeplist.remove(ep);
						break;
					}
				}
			}
		
		traceDB.close();
		return eeplist;
	}
	
	public List<EventProducer> getExcludedEventProducersPerPage() throws SoCTraceException{
		TraceDBObject traceDB = new TraceDBObject(timeFilterParameters.getTrace().getDbName(), DBMode.DB_OPEN);
		List<EventProducer> eeplist = new ArrayList<EventProducer>();
		eeplist.addAll(getAllEventProducers());
		for (long i=0; i<=traceDB.getMaxPage(); i++){
			EventQuery eventQuery = new EventQuery(traceDB);
			LogicalCondition where = new LogicalCondition(LogicalOperation.AND);
			where.addCondition(new SimpleCondition("TIMESTAMP",
					ComparisonOperation.GE, String.valueOf(timeFilterParameters
							.getTimeRegion().getTimeStampStart())));
			where.addCondition(new SimpleCondition("TIMESTAMP",
					ComparisonOperation.LE, String.valueOf(timeFilterParameters
							.getTimeRegion().getTimeStampEnd())));
			where.addCondition(new SimpleCondition("PAGE",
					ComparisonOperation.EQ, String.valueOf(i)));
			eventQuery.setElementWhere(where);
			eventQuery.setOrderBy("TIMESTAMP", OrderBy.ASC);
			List<Event> elist = eventQuery.getList();
			for (Event e : elist){
				for (EventProducer ep : eeplist){
					if (ep.getId()==e.getEventProducer().getId()){
						eeplist.remove(ep);
						break;
					}
				}
			}
		}
		traceDB.close();
		return eeplist;
	}
	
	
	

}
