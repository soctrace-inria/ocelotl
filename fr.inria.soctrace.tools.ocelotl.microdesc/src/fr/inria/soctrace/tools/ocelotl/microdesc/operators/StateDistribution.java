/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.events.IState;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop._3DMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSlice;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceStateManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;
import fr.inria.soctrace.tools.ocelotl.microdesc.config.DistributionConfig;
import fr.inria.soctrace.tools.ocelotl.microdesc.genericevents.GenericState;

public class StateDistribution extends _3DMicroDescription {

	private static final Logger logger = LoggerFactory.getLogger(StateDistribution.class);
	
	
	class OcelotlThread extends Thread {

		List<EventProducer> eventProducers;
		int threadNumber;
		int thread;
		int size;

		public OcelotlThread() {
			super();
		}

		public OcelotlThread(final int threadNumber, final int thread,
				final int size) {
			super();
			this.threadNumber = threadNumber;
			this.thread = thread;
			this.size = size;

			start();
		}

		protected void matrixUpdate(final IState state, final EventProducer ep,
				final Map<Long, Double> distrib) {
			synchronized (matrix) {
				if (!matrix.get(0).get(ep).containsKey(state.getType())) {
					logger.debug("Adding " + state.getType()
							+ " state");
					// addKey(state.getStateType());
					for (int incr = 0; incr < matrix.size(); incr++)
						for (final EventProducer epset : matrix.get(incr)
								.keySet())
							matrixPushType(incr, epset, state.getType());
				}
				for (final long it : distrib.keySet())
					matrixWrite(it, ep, state.getType(), distrib);
			}
		}

		@Override
		public void run() {
			while (true) {
				final List<Event> events = getEvents(size);
				if (events.size() == 0)
					break;
				IState state;
				// For each event
				for (final Event event : events) {
					// Convert to state
					state = new GenericState(event, timeSliceManager);
					// Get duration of the state for every time slice it is in
					final Map<Long, Double> distrib = state
							.getTimeSlicesDistribution();
					matrixUpdate(state, event.getEventProducer(), distrib);
				}
			}
		}
	}

	private TimeSliceStateManager timeSliceManager;

	public StateDistribution() throws SoCTraceException {
		super();
	}

	public StateDistribution(final OcelotlParameters parameters)
			throws SoCTraceException, OcelotlException {
		super(parameters);
	}

	@Override
	protected void computeSubMatrix(final List<EventProducer> eventProducers)
			throws SoCTraceException, InterruptedException, OcelotlException {
		dm = new DeltaManagerOcelotl();
		dm.start();
		eventIterator = ocelotlQueries.getStateIterator(eventProducers);
		dm = new DeltaManagerOcelotl();
		dm.start();
		timeSliceManager = new TimeSliceStateManager(getOcelotlParameters()
		.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		getOcelotlParameters().setTimeSliceManager(timeSliceManager);
		
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < ((DistributionConfig) getOcelotlParameters()
				.getTraceTypeConfig()).getThreadNumber(); t++)
			threadlist.add(new OcelotlThread(
					((DistributionConfig) getOcelotlParameters()
							.getTraceTypeConfig()).getThreadNumber(), t,
					((DistributionConfig) getOcelotlParameters()
							.getTraceTypeConfig()).getEventsPerThread()));
		for (final Thread thread : threadlist)
			thread.join();
		ocelotlQueries.closeIterator();
		dm.end("VECTORS COMPUTATION: "
				+ getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}
	
	@Override
	protected void computeSubMatrix(final List<EventProducer> eventProducers,
			List<IntervalDesc> time) throws SoCTraceException,
			InterruptedException, OcelotlException {
		dm = new DeltaManagerOcelotl();
		dm.start();
		eventIterator = ocelotlQueries.getStateIterator(eventProducers, time);
		dm = new DeltaManagerOcelotl();
		dm.start();
		timeSliceManager = new TimeSliceStateManager(getOcelotlParameters()
				.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		getOcelotlParameters().setTimeSliceManager(timeSliceManager);
		
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < ((DistributionConfig) getOcelotlParameters()
				.getTraceTypeConfig()).getThreadNumber(); t++)
			threadlist.add(new OcelotlThread(
					((DistributionConfig) getOcelotlParameters()
							.getTraceTypeConfig()).getThreadNumber(), t,
					((DistributionConfig) getOcelotlParameters()
							.getTraceTypeConfig()).getEventsPerThread()));
		for (final Thread thread : threadlist)
			thread.join();
		ocelotlQueries.closeIterator();
		dm.end("VECTORS COMPUTATION: "
				+ getOcelotlParameters().getTimeSlicesNumber() + " timeslices " + eventsNumber);
	}

	@Override
	public void initQueries() {
		try {
			ocelotlQueries = new OcelotlQueries(parameters);
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	class CachedOcelotlThread extends OcelotlThread  {

		private HashMap<Long, List<TimeSlice>> timesliceIndex;
		
		public CachedOcelotlThread(final int threadNumber, final int thread,
				final int size, HashMap<Long, List<TimeSlice>> timesliceIndex) {
			super();
			this.threadNumber = threadNumber;
			this.thread = thread;
			this.size = size;
			this.timesliceIndex = timesliceIndex;
			
			start();
		}

		@Override
		public void run() {
			while (true) {
				final List<Event> events = getEvents(size);
				if (events.size() == 0)
					break;
				IState state;
				// For each event
				for (final Event event : events) {
					// Convert to state
					state = new GenericState(event, timeSliceManager);
					// Get duration of the state for every time slice it is in
					final Map<Long, Double> distrib = computeDistribution(state);
					matrixUpdate(state, event.getEventProducer(), distrib);
				}
			}
		}
		

		public Map<Long, Double> computeDistribution(IState state) {
			
			TimeRegion testedTimeRegion = state.getTimeRegion();
			
			final Map<Long, Double> timeSlicesDistribution = new HashMap<Long, Double>();

			// Find the number of the slice where the state event starts
			long startSlice = Math.max(0L,
					(long) ((testedTimeRegion.getTimeStampStart() - timeSliceManager
							.getTimeRegion().getTimeStampStart())
							/ timeSliceManager.getSliceDuration()) - 1);
			
			// If the state starts within the actual time region
			if (testedTimeRegion.getTimeStampStart()
					- timeSliceManager.getTimeRegion().getTimeStampStart() >= 0)
				for (long i = startSlice; i < timeSliceManager.getTimeSlices()
						.size(); i++) {
					final TimeSlice it = timeSliceManager.getTimeSlices().get(
							(int) i);
					// Make sure we got the right starting time slice?
					if (it.startIsInsideMe(testedTimeRegion.getTimeStampStart())) {
						startSlice = it.getNumber();
						break;
					}
				}

			double temp = 0;
			for (long i = startSlice; i < timeSliceManager.getSlicesNumber(); i++) {

				long timesliceStart = timeSliceManager.getTimeSlices()
						.get((int) i).getTimeRegion().getTimeStampStart();

				if (timesliceStart > testedTimeRegion.getTimeStampEnd())
					break;

				long timesliceEnd = timeSliceManager.getTimeSlices()
						.get((int) i).getTimeRegion().getTimeStampEnd();

				if (timesliceIndex.get(i) != null
						&& !timesliceIndex.get(i).isEmpty()) {
					for (TimeSlice aCachedTimeSlice : timesliceIndex.get(i)) {

						long timeStampStart = Math.max(timesliceStart,
								aCachedTimeSlice.getTimeRegion()
										.getTimeStampStart());
						long timeStampEnd = Math.min(timesliceEnd,
								aCachedTimeSlice.getTimeRegion()
										.getTimeStampEnd());

						// Create the custom time slice
						TimeSlice currentTimeSlice = new TimeSlice(
								new TimeRegion(timeStampStart, timeStampEnd),
								-1);

						// Get the duration of the state in the time
						// slice i
						temp = currentTimeSlice
								.regionInsideMe(testedTimeRegion);

						// If the state has ended in the previous time
						// slice
						if (timeSlicesDistribution.get(i) != null)
							temp = temp + timeSlicesDistribution.get(i);

						timeSlicesDistribution.put(i, temp);
					}
				}
			}

			return timeSlicesDistribution;
		}
	}

	
	@Override
	protected void computeDirtyCacheMatrix(
			final List<EventProducer> eventProducers, List<IntervalDesc> time,
			HashMap<Long, List<TimeSlice>> timesliceIndex)
			throws SoCTraceException, InterruptedException, OcelotlException {
		dm = new DeltaManagerOcelotl();
		dm.start();
		eventIterator = ocelotlQueries.getStateIterator(eventProducers, time);

		timeSliceManager = new TimeSliceStateManager(getOcelotlParameters()
				.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		getOcelotlParameters().setTimeSliceManager(timeSliceManager);
		

		final List<CachedOcelotlThread> threadlist = new ArrayList<CachedOcelotlThread>();
		for (int t = 0; t < ((DistributionConfig) getOcelotlParameters()
				.getTraceTypeConfig()).getThreadNumber(); t++)
			threadlist.add(new CachedOcelotlThread(
					((DistributionConfig) getOcelotlParameters()
							.getTraceTypeConfig()).getThreadNumber(), t,
					((DistributionConfig) getOcelotlParameters()
							.getTraceTypeConfig()).getEventsPerThread(),
					timesliceIndex));
		for (final Thread thread : threadlist)
			thread.join();
		ocelotlQueries.closeIterator();
		dm.end("VECTORS COMPUTATION: "
				+ getOcelotlParameters().getTimeSlicesNumber() + " timeslices "
				+ eventsNumber);
	}

}
