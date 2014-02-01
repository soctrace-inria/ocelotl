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

import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.tools.paje.tracemanager.common.model.PajeEventType;

/**
 *         This class provides a Paje Event Types manager to 1)add and manage
 *         Event Types and their associate Event Param Types (including id
 *         management) 2)save them in the database and commit
 */
public class PajeEventDefManager {

	private List<PajeEventType>	eventTypes				= new ArrayList<PajeEventType>();
	private final IdManager		eventTypeIDManager		= new IdManager();
	private final IdManager		eventParamTypeIDManager	= new IdManager();

	public void addPajeEventParamType(final String pajeId, final String paramName, final String paramType) {
		for (final PajeEventType it : eventTypes)
			if (it.getPajeId() == pajeId) {
				it.addEventParamType(eventParamTypeIDManager.getNextId(), paramName, paramType);
				break;
			}
	}

	public void addPajeEventType(final String name, final String pajeId) {
		eventTypes.add(new PajeEventType(eventTypeIDManager.getNextId(), name, pajeId));
	}

	/**
	 * @return the eventTypes
	 */
	public List<PajeEventType> getEventTypes() {
		return eventTypes;
	}

	/**
	 * allows to save all objects in the trace data base
	 * 
	 * @param traceDB
	 * @throws SoCTraceException
	 */
	public void save(final TraceDBObject traceDB) throws SoCTraceException {
		for (final PajeEventType e : eventTypes) {
			traceDB.save(e);
			final List<EventParamType> ept = e.getEventParamTypes();
			for (final EventParamType p : ept)
				traceDB.save(p);
		}
		traceDB.flushVisitorBatches();
	}

	/**
	 * @param eventTypes
	 *            the eventTypes to set
	 */
	public void setEventTypes(final List<PajeEventType> eventTypes) {
		this.eventTypes = eventTypes;
	}
}