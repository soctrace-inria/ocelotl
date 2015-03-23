/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
 * overview by using aggregation techniques
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

package fr.inria.soctrace.tools.ocelotl.microdesc.operators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.events.IState;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.Microscopic3DDescription;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceStateManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;
import fr.inria.soctrace.tools.ocelotl.microdesc.genericevents.GenericState;

public class StateDistributionQuery extends Microscopic3DDescription {

	private static final Logger logger = LoggerFactory
			.getLogger(StateDistributionQuery.class);

	class OcelotlThread extends Thread {

		List<EventProducer> localActiveEventProducers;
		int threadNumber;
		int thread;
		int size;
		EventProducer currentEP;
		IProgressMonitor monitor;
		List<IntervalDesc> time;
		List<EventProducer> producers;
		List<EventType> types;
		boolean typeFiltering;
		boolean prodFiltering;
		boolean timeFiltering;
		
		public OcelotlThread() {
			super();
		}

		public OcelotlThread(final int threadNumber, final int thread,
				final int size, List<IntervalDesc> time,
				List<EventProducer> producers, List<EventType> types,
				boolean typeFiltering, boolean prodFiltering,
				boolean timeFiltering, IProgressMonitor monitor) {
			super();
			this.threadNumber = threadNumber;
			this.thread = thread;
			this.size = size;
			this.monitor = monitor;
			this.time = time;
			this.producers = producers;
			this.types = types;
			this.typeFiltering = typeFiltering;
			this.prodFiltering = prodFiltering;
			this.timeFiltering = timeFiltering;
			
			localActiveEventProducers = new ArrayList<EventProducer>();

			start();
		}

		protected void matrixUpdate(final IState state, final EventProducer ep,
				final Map<Long, Double> distrib) {
			// Mutex
			synchronized (getMatrix()) {
				// If the event type is not in the matrix yet
				if (!getMatrix().get(0).get(ep)
						.containsKey(state.getType())) {
					logger.debug("Adding " + state.getType() + " state");

					// Add the type for each slice and ep and init to zero
					for (int incr = 0; incr < getMatrix().size(); incr++)
						for (final EventProducer epset : getMatrix()
								.get(incr).keySet())
							matrixPushType(incr, epset, state.getType());
				}
				for (final long it : distrib.keySet())
					matrixWrite(it, ep, state.getType(), distrib);
			}
		}

		protected void handleEvent() {
			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (events.size() == 0)
					break;
				if (monitor.isCanceled())
					return;

				// For each event
				for (final Event event : events) {
					if (event.getCategory() != EventCategory.STATE)
						continue;

					convertToState(event);
				}
				if (monitor.isCanceled())
					return;
				monitor.worked(events.size());
			}
		}

		protected void handleEventProdFilter() {
			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (events.size() == 0)
					break;
				if (monitor.isCanceled())
					return;

				// For each event
				for (final Event event : events) {
					if (event.getCategory() != EventCategory.STATE)
						continue;

					if (!producers.contains(event.getEventProducer()))
						continue;

					convertToState(event);
				}
				if (monitor.isCanceled())
					return;
				monitor.worked(events.size());
			}
		}

		protected void handleEventProdTimeFilter() {
			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (events.size() == 0)
					break;
				if (monitor.isCanceled())
					return;

				// For each event
				for (final Event event : events) {
					if (event.getCategory() != EventCategory.STATE)
						continue;

					if (!producers.contains(event.getEventProducer()))
						continue;

					for (IntervalDesc anInterval : time)
						if (!((event.getTimestamp() >= anInterval.t1 && event
								.getTimestamp() <= anInterval.t2) || (event
								.getTimestamp() < anInterval.t1 && event
								.getLongPar() > anInterval.t1)))
							continue;

					convertToState(event);
				}
				if (monitor.isCanceled())
					return;
				monitor.worked(events.size());
			}
		}

		protected void handleEventTimeFilter() {
			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (events.size() == 0)
					break;
				if (monitor.isCanceled())
					return;

				// For each event
				for (final Event event : events) {
					if (event.getCategory() != EventCategory.STATE)
						continue;

					for (IntervalDesc anInterval : time)
						if (!((event.getTimestamp() >= anInterval.t1 && event
								.getTimestamp() <= anInterval.t2) || (event
								.getTimestamp() < anInterval.t1 && event
								.getLongPar() > anInterval.t1)))
							continue;

					convertToState(event);
				}
				if (monitor.isCanceled())
					return;
				monitor.worked(events.size());
			}
		}

		protected void handleEventTypeFilter() {
			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (events.size() == 0)
					break;
				if (monitor.isCanceled())
					return;

				// For each event
				for (final Event event : events) {
					if (!types.contains(event.getType()))
						continue;

					convertToState(event);
				}
				if (monitor.isCanceled())
					return;
				monitor.worked(events.size());
			}
		}

		protected void handleEventTimeTypeFilter() {
			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (events.size() == 0)
					break;
				if (monitor.isCanceled())
					return;

				// For each event
				for (final Event event : events) {
					if (!types.contains(event.getType()))
						continue;

					for (IntervalDesc anInterval : time)
						if (!((event.getTimestamp() >= anInterval.t1 && event
								.getTimestamp() <= anInterval.t2) || (event
								.getTimestamp() < anInterval.t1 && event
								.getLongPar() > anInterval.t1)))
							continue;

					convertToState(event);
				}
				if (monitor.isCanceled())
					return;
				monitor.worked(events.size());
			}
		}

		protected void handleEventTimeTypeProdFilter() {

			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (events.size() == 0)
					break;
				if (monitor.isCanceled())
					return;

				// For each event
				for (final Event event : events) {
					if (!types.contains(event.getType()))
						continue;

					if (!producers.contains(event.getEventProducer()))
						continue;

					for (IntervalDesc anInterval : time)
						if (!((event.getTimestamp() >= anInterval.t1 && event
								.getTimestamp() <= anInterval.t2) || (event
								.getTimestamp() < anInterval.t1 && event
								.getLongPar() > anInterval.t1)))
							continue;

					convertToState(event);
				}
				if (monitor.isCanceled())
					return;
				monitor.worked(events.size());
			}
		}

		protected void convertToState(Event event)
		{
			// Convert to state
			IState state = new GenericState(event, (TimeSliceStateManager) timeSliceManager);
			// Get duration of the state for every time slice it is in
			final Map<Long, Double> distrib = state
					.getTimeSlicesDistribution();
			EventProducer eventEP = event.getEventProducer();
			
			if(aggregatedProducers.containsKey(event.getEventProducer()))
				eventEP = aggregatedProducers.get(event.getEventProducer());
			
			matrixUpdate(state, eventEP, distrib);
			if (currentEP != eventEP) {
				currentEP = eventEP;
				// If the event producer is not in the active producers list
				if (!localActiveEventProducers.contains(eventEP)) {
					// Add it
					localActiveEventProducers.add(eventEP);
				}
			}
		}

		@Override
		public void run() {
			currentEP = null;
		
			if (typeFiltering) {
				if (prodFiltering) {
					handleEventTimeTypeProdFilter();
				} else {
					if (timeFiltering) {
						handleEventTimeTypeFilter();
					} else {
						handleEventTypeFilter();
					}
				}
			} else {
				if (prodFiltering) {
					if (timeFiltering) {
						handleEventProdTimeFilter();
					} else {
						handleEventProdFilter();
					}
				} else {
					if (timeFiltering) {
						handleEventTimeFilter();
					} else {
						handleEvent();
					}
				}
			}
		
			// Merge local active event producers to the global one
			synchronized (activeProducers) {
				for (EventProducer ep : localActiveEventProducers) {
					if (!activeProducers.contains(ep))
						activeProducers.add(ep);
				}
			}
		}
	}

	public StateDistributionQuery() {
		super();
	}
	
	@Override
	public void computeSubMatrix(final List<EventProducer> eventProducers,
			List<IntervalDesc> time, IProgressMonitor monitor)
			throws SoCTraceException, InterruptedException, OcelotlException {
		dm = new DeltaManagerOcelotl();
		dm.start();
		monitor.subTask("Querying Database...");
		eventIterator = ocelotlQueries.getEventIterator(monitor);
		if (monitor.isCanceled()) {
			ocelotlQueries.closeIterator();
			return;
		}
		
		boolean typeFiltering = false;
		boolean prodFiltering = false;
		boolean timeFiltering = false;
		
		if (parameters.getTraceTypeConfig().getTypes().size() != parameters
				.getOperatorEventTypes().size())
			typeFiltering = true;

		if (eventProducers.size() != parameters.getAllEventProducers().size())
			prodFiltering = true;

		if (time.size() > 1
				|| parameters.getTrace().getMinTimestamp() != time.get(0).t1
				|| parameters.getTrace().getMaxTimestamp() != time.get(0).t2)
			timeFiltering = true;

		setTimeSliceManager(new TimeSliceStateManager(getOcelotlParameters()
				.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber()));
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		monitor.subTask("Loading Data From Database...");
		for (int t = 0; t < getOcelotlParameters().getNumberOfThreads(); t++)
			threadlist.add(new OcelotlThread(getOcelotlParameters()
					.getNumberOfThreads(), t, getOcelotlParameters()
					.getEventsPerThread(), time, eventProducers, parameters
					.getTraceTypeConfig().getTypes(), typeFiltering,
					prodFiltering, timeFiltering, monitor));
		for (final Thread thread : threadlist)
			thread.join();

		ocelotlQueries.closeIterator();
		dm.end("VECTORS COMPUTATION: "
				+ getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	@Override
	public void rebuildDirty(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException, SoCTraceException,
			InterruptedException, OcelotlException {
		buildNormalMatrix(monitor);
	}

}
