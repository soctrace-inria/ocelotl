/* ===========================================================
 * LPAggreg core module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to visualize a Paje
 * trace across an aggregated representation.
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
 */

package fr.inria.soctrace.tools.paje.lpaggreg.core;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;

public class Queries {

	private LPAggregParameters	lpaggregParameters;

	public Queries(LPAggregParameters lpaggregParameters) throws SoCTraceException {
		super();
		this.lpaggregParameters = lpaggregParameters;
	}

	public void checkTimeStamps() {
		// TODO calculer le max
	}

	public List<EventProducer> getAllEventProducers() throws SoCTraceException {
		TraceDBObject traceDB = new TraceDBObject(lpaggregParameters.getTrace().getDbName(), DBMode.DB_OPEN);
		EventProducerQuery eventProducerQuery = new EventProducerQuery(traceDB);
		List<EventProducer> eplist = eventProducerQuery.getList();
		traceDB.close();
		return eplist;
	}

	public List<Event> getEvents(List<EventProducer> eventProducers) throws SoCTraceException {
		if ((eventProducers.size()==getAllEventProducers().size())){
			return getAllEvents();
		}else {
			ITraceSearch traceSearch = new TraceSearchLPAggreg().initialize();
			List<IntervalDesc> time= new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(lpaggregParameters.getTimeRegion().getTimeStampStart(), lpaggregParameters.getTimeRegion().getTimeStampEnd()));
			List<Event> elist = traceSearch.getEventsByEventTypesAndIntervalsAndEventProducers(lpaggregParameters.getTrace(), lpaggregParameters.getEventTypes(), time , eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}	

	public LPAggregParameters getLpaggregParameters() {
		return lpaggregParameters;
	}
	
	public List<Event> getAllEvents() throws SoCTraceException{
		ITraceSearch traceSearch = new TraceSearchLPAggreg().initialize();
		List<IntervalDesc> time= new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(lpaggregParameters.getTimeRegion().getTimeStampStart(), lpaggregParameters.getTimeRegion().getTimeStampEnd()));
		List<Event> elist = traceSearch.getEventsByEventTypesAndIntervalsAndEventProducers(lpaggregParameters.getTrace(), lpaggregParameters.getEventTypes(), time, null);
		traceSearch.uninitialize();
		return elist;
	}
	
	

}
