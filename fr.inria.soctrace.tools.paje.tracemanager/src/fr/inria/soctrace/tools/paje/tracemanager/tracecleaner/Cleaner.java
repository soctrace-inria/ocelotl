/* ===========================================================
 * Paje Trace Manager module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to import, export and
 * process Pajé trace files
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

package fr.inria.soctrace.tools.paje.tracemanager.tracecleaner;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.IdManager;

class Cleaner {

	private TraceDBObject	traceDB;
	private IdManager		eventParamIdManager	= new IdManager();

	public Cleaner(TraceDBObject traceDB) throws SoCTraceException {
		this.traceDB = traceDB;
		eventParamIdManager.setNextId(traceDB.getMaxId("EVENT_PARAM", "ID") + 1);
	}

	public void cleanPopStateEventTypes(List<Event> events) throws SoCTraceException {
		for (int i = 2; i < events.size(); i++) {
			if (events.get(i).getType().equals(getPopStateEventTypes().get(0))) {
				List<EventParam> oldEventParam = events.get(i).getEventParams();
				List<EventParam> newEventParam = new ArrayList<EventParam>();
				for (int j = 0; j < getSetStateEventTypes().get(0).getEventParamTypes().size(); j++) {
					EventParam currentEventParam = new EventParam(eventParamIdManager.getNextId());
					currentEventParam.setEventParamType(getSetStateEventTypes().get(0).getEventParamTypes().get(j));
					for (int k = 0; k < getSetStateEventTypes().get(0).getEventParamTypes().size(); k++) {
						if (events.get(i - 2).getType().getEventParamTypes().get(k).getName().equals(currentEventParam.getEventParamType().getName()) && events.get(i - 2).getType().getEventParamTypes().get(k).getType().equals(currentEventParam.getEventParamType().getType())) {
							currentEventParam.setValue(events.get(i - 2).getEventParams().get(k).getValue());
							break;
						}
					}
					for (int k = 0; k < oldEventParam.size(); k++) {
						if (events.get(i).getType().getEventParamTypes().get(k).getName().equals(currentEventParam.getEventParamType().getName()) && events.get(i).getType().getEventParamTypes().get(k).getType().equals(currentEventParam.getEventParamType().getType())) {
							currentEventParam.setValue(oldEventParam.get(k).getValue());
							break;
						}
					}
					newEventParam.add(currentEventParam);
				}
				for (EventParam currentEventParam : oldEventParam) {
					traceDB.delete(currentEventParam);
					//System.out.println("commit " + currentEventParam.toString());
					//traceDB.commit();
				}
				oldEventParam.clear();
				events.get(i).setType(getSetStateEventTypes().get(0));
				for (EventParam currentEventParam : newEventParam) {
					currentEventParam.setEvent(events.get(i));
					traceDB.save(currentEventParam);
					//System.out.println("commit " + currentEventParam.toString());
					//traceDB.commit();
				}
				traceDB.update(events.get(i));
				//System.out.println("commit " + events.get(i));
				//traceDB.commit();
			}
		}

	}

	public void cleanPushStateEventTypes(List<Event> events) throws SoCTraceException {
		for (int i = 0; i < events.size(); i++)
			if (events.get(i).getType().equals(getPushStateEventTypes().get(0))) {
				List<EventParam> oldEventParam = events.get(i).getEventParams();
				List<EventParam> newEventParam = new ArrayList<EventParam>();
				for (int j = 0; j < getSetStateEventTypes().get(0).getEventParamTypes().size(); j++) {
					EventParam currentEventParam = new EventParam(eventParamIdManager.getNextId());
					currentEventParam.setEventParamType(getSetStateEventTypes().get(0).getEventParamTypes().get(j));
					for (int k = 0; k < oldEventParam.size(); k++) {
						if (events.get(i).getType().getEventParamTypes().get(k).getName().equals(currentEventParam.getEventParamType().getName()) && events.get(i).getType().getEventParamTypes().get(k).getType().equals(currentEventParam.getEventParamType().getType())) {
							currentEventParam.setValue(oldEventParam.get(k).getValue());
							break;
						} else
							currentEventParam.setValue("Unknown");//TODO improve management		
					}
					newEventParam.add(currentEventParam);
				}
				for (EventParam currentEventParam : oldEventParam) {
					traceDB.delete(currentEventParam);
					//System.out.println("delete " + currentEventParam.toString());
					//traceDB.commit();
				}
				oldEventParam.clear();
				events.get(i).setType(getSetStateEventTypes().get(0));
				for (EventParam currentEventParam : newEventParam) {
					currentEventParam.setEvent(events.get(i));
					traceDB.save(currentEventParam);
					//System.out.println("commit " + currentEventParam.toString());
					//traceDB.commit();
				}
				traceDB.update(events.get(i));
				//System.out.println("commit " + events.get(i));
				//traceDB.commit();
			}
	}

	public void cleanup() throws SoCTraceException {
		List<Event> events = getEvents();
		for (EventProducer ep : getEventProducers()) {
			System.out.println(ep.toString());
			List<Event> subEvents = getSubsetEvents(events, ep);
			if (!subEvents.isEmpty()) {
				cleanPushStateEventTypes(subEvents);
				System.out.println("Push");
				cleanPopStateEventTypes(subEvents);
				System.out.println("Pop");
			}
		}
		traceDB.flushVisitorBatches();
	}

	//	public void cleanup() throws SoCTraceException {
	//		for (EventProducer ep : getEventProducers()) {
	//			List<Event> subEvents = getEvents(ep);
	//			if (!subEvents.isEmpty()) {
	//				cleanPushStateEventTypes(subEvents);
	//				cleanPopStateEventTypes(subEvents);
	//			}
	//		}
	//		traceDB.flushVisitorBatches();
	//	}

	public List<EventProducer> getEventProducers() throws SoCTraceException {
		EventProducerQuery pQuery = new EventProducerQuery(traceDB);
		return pQuery.getList();
	}

	public List<Event> getEvents() throws SoCTraceException {
		EventQuery eQuery = new EventQuery(traceDB);
		LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		//TODO management of Paje Extra type (PushState)
		if (!getPushStateEventTypes().isEmpty() && !getPopStateEventTypes().isEmpty()) {
			if (!getSetStateEventTypes().isEmpty())
				or.addCondition(new SimpleCondition("ID", ComparisonOperation.EQ, Integer.toString(getSetStateEventTypes().get(0).getId())));
			or.addCondition(new SimpleCondition("ID", ComparisonOperation.EQ, Integer.toString(getPushStateEventTypes().get(0).getId())));
			or.addCondition(new SimpleCondition("ID", ComparisonOperation.EQ, Integer.toString(getPopStateEventTypes().get(0).getId())));
			eQuery.setTypeWhere(or);
			eQuery.setOrderBy("TIMESTAMP", OrderBy.ASC);
			return eQuery.getList();
		}
		return null;
	}

	public List<Event> getEvents(EventProducer producer) throws SoCTraceException {
		EventQuery eQuery = new EventQuery(traceDB);
		LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		//TODO management of Paje Extra type (PushState)
		if (!getPushStateEventTypes().isEmpty() && !getPopStateEventTypes().isEmpty()) {
			if (!getSetStateEventTypes().isEmpty())
				or.addCondition(new SimpleCondition("ID", ComparisonOperation.EQ, Integer.toString(getSetStateEventTypes().get(0).getId())));
			or.addCondition(new SimpleCondition("ID", ComparisonOperation.EQ, Integer.toString(getPushStateEventTypes().get(0).getId())));
			or.addCondition(new SimpleCondition("ID", ComparisonOperation.EQ, Integer.toString(getPopStateEventTypes().get(0).getId())));
			eQuery.setTypeWhere(or);
			eQuery.setEventProducerWhere(new SimpleCondition("ID", ComparisonOperation.EQ, Integer.toString(producer.getId())));
			eQuery.setOrderBy("TIMESTAMP", OrderBy.ASC);
			return eQuery.getList();
		}
		return null;
	}

	public List<EventType> getPopStateEventTypes() throws SoCTraceException {
		EventTypeQuery tQuery = new EventTypeQuery(traceDB);
		tQuery.setElementWhere(new SimpleCondition("NAME", ComparisonOperation.LIKE, "%PajePopState"));//TODO check
		return tQuery.getList();
	}

	public List<EventType> getPushStateEventTypes() throws SoCTraceException {
		EventTypeQuery tQuery = new EventTypeQuery(traceDB);
		tQuery.setElementWhere(new SimpleCondition("NAME", ComparisonOperation.LIKE, "%PajePushState"));//TODO check
		return tQuery.getList();
	}

	public List<EventType> getSetStateEventTypes() throws SoCTraceException {
		EventTypeQuery tQuery = new EventTypeQuery(traceDB);
		tQuery.setElementWhere(new SimpleCondition("NAME", ComparisonOperation.LIKE, "%PajeSetState"));//TODO check
		return tQuery.getList();
	}

	public List<Event> getSubsetEvents(List<Event> events, EventProducer producer) {
		List<Event> subEvents = new ArrayList<Event>();
		for (Event e : events) {
			if (e.getEventProducer().getId() == producer.getId())
				subEvents.add(e);
		}
		return subEvents;

	}
}
