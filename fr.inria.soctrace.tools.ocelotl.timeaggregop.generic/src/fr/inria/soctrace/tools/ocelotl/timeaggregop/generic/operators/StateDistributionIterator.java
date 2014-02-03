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

package fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop._3DCacheMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.IteratorQuery.EventIterator;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.queries.eventproxy.EventProxy;
import fr.inria.soctrace.tools.ocelotl.core.state.IState;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.queries.EventCache;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.state.GenericState;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.config.StateDistributionConfig;


public class StateDistributionIterator extends _3DCacheMicroDescription {

	
	public StateDistributionIterator() throws SoCTraceException {
		super();
	}

	public StateDistributionIterator(final OcelotlParameters parameters) throws SoCTraceException {
		super(parameters);
	}

	@Override
	protected void computeSubMatrixCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
	}

	@Override
	protected void computeSubMatrixNonCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		dm = new DeltaManager();
		dm.start();
		EventIterator it = ((OcelotlQueries) ocelotlQueries).getStateIterator(eventProducers);
		//eventsNumber = fullEvents.size();
		//dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		IState state;
		Event event;
		while (it.getNext() != null){
			
			event=it.getEvent();
			state = new GenericState(event, timeSliceManager);
			matrixUpdate(state, event.getEventProducer(), state.getTimeSlicesDistribution());
		}
		((OcelotlQueries) ocelotlQueries).closeIterator();
		dm.end("VECTORS COMPUTATION : " + getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}
	
	private void matrixUpdate(final IState state, final EventProducer ep, final Map<Long, Long> distrib) {
		synchronized (matrix) {
			if (!matrix.get(0).get(ep.getName()).containsKey(state.getStateType())) {
				System.out.println("Adding " + state.getStateType() + " state");
				// addKey(state.getStateType());
				for (int incr = 0; incr < matrix.size(); incr++)
					for (final String epstring : matrix.get(incr).keySet())
						matrixPushType(incr, epstring, state.getStateType(), distrib);
			}
			for (final long it : distrib.keySet())
				matrixWrite(it, ep, state.getStateType(), distrib);
		}
	}

	@Override
	public void initQueries() {
		try {
			ocelotlQueries = new OcelotlQueries(parameters);
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ocelotlQueries.checkTimeStamps();
	}

	@Override
	public void setOcelotlParameters(final OcelotlParameters parameters) throws SoCTraceException, InterruptedException {
		this.parameters = parameters;
		ocelotlQueries = new OcelotlQueries(parameters);
		ocelotlQueries.checkTimeStamps();
		count = 0;
		epit = 0;
		timeSliceManager = new TimeSliceManager(getOcelotlParameters().getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		initVectors();
		computeMatrix();
	}

}
