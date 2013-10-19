/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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

package fr.inria.soctrace.tools.ocelotl.core.paje.aggregop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop.ICubicMatrix;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop.CubicMatrix;
import fr.inria.soctrace.tools.ocelotl.core.paje.config.PajeConfig;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeEventProxy;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeReducedEvent;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeReducedEventCache;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.paje.state.PajeState;
import fr.inria.soctrace.tools.ocelotl.core.ts.IState;
import fr.inria.soctrace.tools.ocelotl.core.ts.State;
import fr.inria.soctrace.tools.ocelotl.core.ts.TimeSliceManager;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;

public class PajeStateTypeSum extends CubicMatrix {

	public final static String	descriptor	= "State Type Sum";
	public final static String	traceType	= PajeConstants.PajeFormatName;

	public PajeStateTypeSum(final OcelotlParameters parameters) throws SoCTraceException {
		super(parameters);
	}

	public PajeStateTypeSum() throws SoCTraceException {
		super();
	}

	public void computeMatrix() throws SoCTraceException {
		eventsNumber = 0;
		final DeltaManager dm = new DeltaManager();
		dm.start();
		final int epsize = query.getOcelotlParameters().getEventProducers().size();
		if (query.getOcelotlParameters().getMaxEventProducers() == 0 || epsize < query.getOcelotlParameters().getMaxEventProducers())
			computeSubMatrix(query.getOcelotlParameters().getEventProducers());
		else {
			final List<EventProducer> producers = query.getOcelotlParameters().getEventProducers().size() == 0 ? query.getAllEventProducers() : query.getOcelotlParameters().getEventProducers();
			for (int i = 0; i < epsize; i = i + query.getOcelotlParameters().getMaxEventProducers())
				computeSubMatrix(producers.subList(i, Math.min(epsize - 1, i + query.getOcelotlParameters().getMaxEventProducers())));
		}

		dm.end("TOTAL (QUERIES + COMPUTATION) : " + epsize + " Event Producers, " + eventsNumber + " Events");
	}

	protected void computeSubMatrixNonCached(final List<EventProducer> eventProducers) throws SoCTraceException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		final List<PajeReducedEvent> fullEvents = query.getReducedEvents(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<PajeReducedEvent>> eventList = new HashMap<Integer, List<PajeReducedEvent>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<PajeReducedEvent>());
		for (final PajeReducedEvent e : fullEvents)
			eventList.get(e.EP).add(e);
		for (final EventProducer ep : eventProducers) {
			IState state;
			final List<PajeReducedEvent> events = eventList.get(ep.getId());
			for (int i = 0; i < events.size() - 1; i++) {
				state = new PajeState(events.get(i), events.get(i + 1), timeSliceManager);
				if (!((PajeConfig) query.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
					final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
					if (!matrix.get(0).get(ep.getName()).containsKey(state.getStateType())) {
						System.out.println("Adding " + state.getStateType() + " state");
						for (int incr = 0; incr < matrix.size(); incr++)
							for (final String epstring : matrix.get(incr).keySet())
								matrix.get(incr).get(epstring).put(state.getStateType(), 0L);
					}
					for (final long it : distrib.keySet())
						matrix.get((int) it).get(ep.getName()).put(state.getStateType(), matrix.get((int) it).get(ep.getName()).get(state.getStateType()) + distrib.get(it));
				}
			}
		}
		dm.end("VECTORS COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	protected void computeSubMatrixCached(final List<EventProducer> eventProducers) throws SoCTraceException {
		int count = 0;
		DeltaManager dm = new DeltaManager();
		dm.start();
		PajeReducedEventCache cache = new PajeReducedEventCache(query.getOcelotlParameters());
		final List<PajeEventProxy> fullEvents = query.getEventsProxy(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		DeltaManager dm2 = new DeltaManager();
		dm.start();
		final Map<Integer, List<PajeEventProxy>> eventList = new HashMap<Integer, List<PajeEventProxy>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<PajeEventProxy>());
		for (final PajeEventProxy e : fullEvents)
			eventList.get(e.EP).add(e);
		for (final EventProducer ep : eventProducers) {
			dm2.start();
			IState state;
			final List<PajeEventProxy> events = eventList.get(ep.getId());
			for (int i = 0; i < events.size() - 1; i++) {
				state = (new PajeState(cache.getEventMultiPageEPCache(events.get(i)), cache.getEventMultiPageEPCache(events.get(i + 1)), timeSliceManager));
				if (!((PajeConfig) query.getOcelotlParameters().getTraceTypeConfig()).getIdles().contains(state.getStateType())) {
					final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
					if (!matrix.get(0).get(ep.getName()).containsKey(state.getStateType())) {
						System.out.println("Adding " + state.getStateType() + " state");
						for (int incr = 0; incr < matrix.size(); incr++)
							for (final String epstring : matrix.get(incr).keySet())
								matrix.get(incr).get(epstring).put(state.getStateType(), 0L);
					}
					for (final long it : distrib.keySet())
						matrix.get((int) it).get(ep.getName()).put(state.getStateType(), matrix.get((int) it).get(ep.getName()).get(state.getStateType()) + distrib.get(it));
				}
			}
			dm2.end("EP VECTORS (" + count++ + "): " + ep.getName());

		}
		dm.end("VECTORS COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	protected void computeSubMatrix(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (query.getOcelotlParameters().isCache()) {
			computeSubMatrixCached(eventProducers);
		} else {
			computeSubMatrixNonCached(eventProducers);
		}
	}

	@Override
	public String descriptor() {
		return descriptor;
	}

	@Override
	public String traceType() {
		return traceType;
	}

}
