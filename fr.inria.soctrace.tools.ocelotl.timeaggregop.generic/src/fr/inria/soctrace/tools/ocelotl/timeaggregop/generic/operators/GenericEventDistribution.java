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

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop._2DMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.queries.eventproxy.EventProxy;

public class GenericEventDistribution extends _2DMicroDescription {

	class OcelotlThread extends Thread {

		List<EventProducer>				eventProducers;
		Map<Integer, List<EventProxy>>	eventProxyList;
		int								threadNumber;
		int								thread;

		@SuppressWarnings("unchecked")
		public OcelotlThread(final List<EventProducer> eventProducers, final Object eventList, final int threadNumber, final int thread, final boolean cached) {
			super();
			this.eventProducers = eventProducers;
			eventProxyList = (Map<Integer, List<EventProxy>>) eventList;
			this.threadNumber = threadNumber;
			this.thread = thread;
			start();
		}

		private void matrixUpdate(final String type, final EventProducer ep, final Map<Long, Long> distrib) {
			synchronized (matrix) {
			}

		}

		@Override
		public void run() {
		}
	}

	public GenericEventDistribution() throws SoCTraceException {
		super();
	}

	public GenericEventDistribution(final OcelotlParameters parameters) throws SoCTraceException {
		super(parameters);
	}

	@Override
	protected void computeSubMatrix(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		final List<EventProxy> fullEvents = ((OcelotlQueries) ocelotlQueries).getEventProxies(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<EventProxy>> eventList = new HashMap<Integer, List<EventProxy>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<EventProxy>());
		for (final EventProxy e : fullEvents)
			eventList.get(e.EP).add(e);
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < getOcelotlParameters().getThread(); t++)
			threadlist.add(new OcelotlThread(eventProducers, eventList, getOcelotlParameters().getThread(), t, true));
		for (final Thread thread : threadlist)
			thread.join();
		dm.end("VECTORS COMPUTATION : " + getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
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

}
