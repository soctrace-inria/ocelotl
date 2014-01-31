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

package fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.queries;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.queries.reducedevent1.PajeReducedEvent1;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.queries.reducedevent2.PajeReducedEvent2;

public class PajeQueries extends OcelotlQueries {

	private final OcelotlParameters	ocelotlParameters;

	public PajeQueries(final OcelotlParameters ocelotlParameters) throws SoCTraceException {
		super(ocelotlParameters);
		this.ocelotlParameters = ocelotlParameters;
	}

	public List<PajeReducedEvent1> getAllReducedEvents1() throws SoCTraceException {
		final PajeTraceSearch traceSearch = (PajeTraceSearch) new PajeTraceSearch().initialize();
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
		final List<PajeReducedEvent1> elist = traceSearch.getReducedEvents1ByEventTypesAndIntervalsAndEventProducers(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, null);
		traceSearch.uninitialize();
		return elist;
	}

	public List<PajeReducedEvent2> getAllReducedEvents2() throws SoCTraceException {
		final PajeTraceSearch traceSearch = (PajeTraceSearch) new PajeTraceSearch().initialize();
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
		final List<PajeReducedEvent2> elist = traceSearch.getReducedEvents2ByEventTypesAndIntervalsAndEventProducers(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, null);
		traceSearch.uninitialize();
		return elist;
	}

	public List<PajeReducedEvent1> getReducedEvents1(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getAllReducedEvents1();
		else {
			final PajeTraceSearch traceSearch = (PajeTraceSearch) new PajeTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			final List<PajeReducedEvent1> elist = traceSearch.getReducedEvents1ByEventTypesAndIntervalsAndEventProducers(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}

	public List<PajeReducedEvent2> getReducedEvents2(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getAllReducedEvents2();
		else {
			final PajeTraceSearch traceSearch = (PajeTraceSearch) new PajeTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			final List<PajeReducedEvent2> elist = traceSearch.getReducedEvents2ByEventTypesAndIntervalsAndEventProducers(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}

}
