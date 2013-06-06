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

package fr.inria.soctrace.tools.paje.tracemanager.traceimporter.managers;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;
import fr.inria.soctrace.tools.paje.tracemanager.common.model.PajeEventProducer;
import fr.inria.soctrace.tools.paje.tracemanager.common.model.PajeEventType;

/**
 *         This class provides a Paje Event manager to 1)add and manage Event
 *         and their associate Event Param (including id management) 2)create
 *         corresponding Event Producers 3)save them in the database and commit
 *         4)eventually destroy objects after saving into the db to avoid memory
 *         explosion
 */
public class PajeEventManager {

	private PajeEventTypeManager	eventTypesManager;

	private PajeEventProducer			defaultEventProducer;
	private static final String		defaultEventProducerLocalId	= "NoId";
	private static final String		defaultEventProducerName	= "NoProducer";
	private static final String		defaultEventProducerType	= "NoType";
	private int						currentPage					= 0;
	private IdManager				eventIdManager				= new IdManager();
	private IdManager				eventParamIdManager			= new IdManager();
	private IdManager				eventProducerIdManager		= new IdManager();
	private List<Event>				events;
	private List<PajeEventProducer>		eventProducers;

	/**
	 * Constructor set a default Event Producer because of non management of
	 * event producers
	 * 
	 * @param eventTypeManager
	 */
	public PajeEventManager(PajeEventTypeManager eventTypeManager) {
		super();
		eventTypesManager = eventTypeManager;
		defaultEventProducer = new PajeEventProducer(eventProducerIdManager.getNextId());
		defaultEventProducer.setName(defaultEventProducerName);
		defaultEventProducer.setType(defaultEventProducerType);
		defaultEventProducer.setLocalId(defaultEventProducerLocalId);
		events = new ArrayList<Event>();
		eventProducers = new ArrayList<PajeEventProducer>();
		eventProducers.add(defaultEventProducer);
	}

	/**
	 * create an event and his associate event params
	 * 
	 * @param pajeId
	 * @param param
	 */
	public void createEvent(String pajeId, List<String> param) {
		Event currentEvent = new Event(eventIdManager.getNextId());
		PajeEventType currentEventType = new PajeEventType(IdManager.RESERVED_NO_ID);
		// initialize in case of paje id is wrong
		currentEvent.setEventProducer(defaultEventProducer);
		currentEvent.setCpu(0);
		currentEvent.setPage(currentPage);
		currentEvent.setTimestamp(PajeConstants.NoTimestamp);
		for (PajeEventType it : eventTypesManager.getEventTypes())
			if (it.getPajeId().equals(pajeId)) {
				currentEventType = it;
				break;
			}
		// TODO error management : if pajeId doesn't exist (= event type not
		// valid)
		currentEvent.setType(currentEventType);
		for (int i = 0; i < param.size(); i++) {
			EventParam currentEventParam = new EventParam(eventParamIdManager.getNextId());
			currentEventParam.setEventParamType(currentEventType.getEventParamTypes().get(i));
			currentEventParam.setValue(param.get(i));
			// if the param is a timestamp, copy the value to event field
			// timestamp
			// careful, checking is done according the type, not the name
			if (currentEventType.getEventParamTypes().get(i).getType().equals(PajeConstants.PajeTimestampType)) {
				double timestamp = Double.parseDouble(param.get(i));
				timestamp = Math.pow(10, PajeConstants.TimestampShifter) * timestamp;
				currentEvent.setTimestamp((long) timestamp);
			}
			// if the param is a container, set event producer
			if (currentEventType.getEventParamTypes().get(i).getName().equals(PajeConstants.PajeEventDefParamContainerName))
				for (PajeEventProducer it : eventProducers)
					if (it.getName().equals(param.get(i))||it.getAlias().equals(param.get(i))) {
						currentEvent.setEventProducer(it);
						break;
					}
			// TODO error management : there is not enough (or too much) param
			currentEventParam.setEvent(currentEvent);
		}
		// event producer creation
		if (currentEventType.getPajeName().equals(PajeConstants.PajeCreateContainerEventDefName)) {
			// if event is a container creation
			PajeEventProducer currentEventProducer = new PajeEventProducer(eventProducerIdManager.getNextId());
			// set fields by defaults
			currentEventProducer.setLocalId(Integer.toString(currentEventProducer.getId()));
			currentEventProducer.setName(defaultEventProducerName);
			currentEventProducer.setType(defaultEventProducerType);
			currentEventProducer.setParentId(defaultEventProducer.getId());
			currentEventProducer.setAlias("");
			// iteration for set type, name and parent id field
			for (int i = 0; i < currentEvent.getEventParams().size(); i++)
				// if parameter is a container type
				if (currentEventType.getEventParamTypes().get(i).getName().equals(PajeConstants.PajeCreateContainerEventDefParamTypeName))
					currentEventProducer.setType(currentEvent.getEventParams().get(i).getValue());
				// else if it's a container name
				else if (currentEventType.getEventParamTypes().get(i).getName().equals(PajeConstants.PajeCreateContainerEventDefParamNameName))
					currentEventProducer.setName(currentEvent.getEventParams().get(i).getValue());
				else if (currentEventType.getEventParamTypes().get(i).getName().equals(PajeConstants.PajeCreateContainerEventDefParamAliasName))
					currentEventProducer.setAlias(currentEvent.getEventParams().get(i).getValue());
				// else if it's the name of its parent container
				else if (currentEventType.getEventParamTypes().get(i).getName().equals(PajeConstants.PajeEventDefParamContainerName))
					for (EventProducer it : eventProducers)
						if (currentEvent.getEventParams().get(i).getValue().equals(it.getName()))
							currentEventProducer.setParentId(it.getId());
			eventProducers.add(currentEventProducer);
		}
		events.add(currentEvent);

	}

	/**
	 * @return the events
	 */
	public List<Event> getEvents() {
		return events;
	}

	/**
	 * @return the eventTypeManager
	 */
	public PajeEventTypeManager getEventTypeManager() {
		return eventTypesManager;
	}

	/**
	 * allow to destroy objects stocked in list to avoid memory explosion
	 * careful with this method : all events not saved will be lost
	 */
	public void reallocateEvents() {
		events.clear();
		events = new ArrayList<Event>();
	}

	/**
	 * save all the objects in the trace database and commit increment current
	 * page : pages correspond to group of events saved at the same time -> will
	 * be useful to regenerate or export a trace
	 * 
	 * @param traceDB
	 * @throws SoCTraceException
	 */
	public void save(TraceDBObject traceDB) throws SoCTraceException {
		for (Event e : events) {
			traceDB.save(e);
			for (EventParam p : e.getEventParams())
				traceDB.save(p);
		}
		traceDB.flushVisitorBatches();
		currentPage++;
	}

	public void saveEventProducer(TraceDBObject traceDB) throws SoCTraceException {
		for (EventProducer ep : eventProducers)
			traceDB.save(ep);
		traceDB.flushVisitorBatches();
	}

	/**
	 * @param eventTypesManager
	 *            the eventTypeManager to set
	 */
	public void setEventTypeManager(PajeEventTypeManager eventTypesManager) {
		this.eventTypesManager = eventTypesManager;
	}

}
