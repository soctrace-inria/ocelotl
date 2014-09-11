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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.events.IVariable;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop._2DSpaceTimeMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceVariableManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;
import fr.inria.soctrace.tools.ocelotl.microdesc.genericevents.GenericVariable;

public class VariableDistributionSpaceTime extends _2DSpaceTimeMicroDescription {

	private static final Logger logger = LoggerFactory
			.getLogger(VariableDistributionSpaceTime.class);

	class OcelotlThread extends Thread {

		List<EventProducer> eventProducers;
		int threadNumber;
		int thread;
		int size;
		IProgressMonitor monitor;

		public OcelotlThread(final int threadNumber, final int thread,
				final int size, IProgressMonitor monitor) {
			super();
			this.threadNumber = threadNumber;
			this.thread = thread;
			this.size = size;
			this.monitor = monitor;

			start();
		}

		private void matrixUpdate(final IVariable Variable,
				final EventProducer ep, final Map<Long, Double> distrib) {
			synchronized (matrix) {
				if (!matrix.get(0).get(ep).containsKey(Variable.getType())) {
					logger.debug("Adding " + Variable.getType() + " Variable");
					// addKey(Variable.getVariableType());
					for (int incr = 0; incr < matrix.size(); incr++)
						for (final EventProducer epset : matrix.get(incr)
								.keySet())
							matrixPushType(incr, epset, Variable.getType());
				}
				for (final long it : distrib.keySet())
					matrixWrite(it, ep, Variable.getType(), distrib);
			}
		}

		@Override
		public void run() {
			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (events.size() == 0)
					break;
				if (monitor.isCanceled())
					return;
				
				IVariable Variable;
				for (final Event event : events) {
					Variable = new GenericVariable(event, timeSliceManager);
					final Map<Long, Double> distrib = Variable
							.getTimeSlicesDistribution();
					matrixUpdate(Variable, event.getEventProducer(), distrib);
					if (monitor.isCanceled())
						return;
				}
			}
		}
	}

	private TimeSliceVariableManager timeSliceManager;

	public VariableDistributionSpaceTime() throws SoCTraceException {
		super();
	}

	public VariableDistributionSpaceTime(final OcelotlParameters parameters,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		super(parameters, monitor);
	}

	@Override
	protected void computeSubMatrix(List<EventProducer> eventProducers,
			List<IntervalDesc> time, IProgressMonitor monitor)
			throws SoCTraceException, InterruptedException, OcelotlException {
		dm = new DeltaManagerOcelotl();
		dm.start();
		monitor.subTask("Query variables");
		eventIterator = ocelotlQueries.getVariableIterator(eventProducers,
				time, monitor);
		if (monitor.isCanceled()) {
			ocelotlQueries.closeIterator();
			return;
		}
		timeSliceManager = new TimeSliceVariableManager(getOcelotlParameters()
				.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < getOcelotlParameters().getThreadNumber(); t++)
			threadlist.add(new OcelotlThread(getOcelotlParameters()
					.getThreadNumber(), t, getOcelotlParameters()
					.getEventsPerThread(), monitor));
		for (final Thread thread : threadlist)
			thread.join();
		ocelotlQueries.closeIterator();
		dm.end("VECTORS COMPUTATION : "
				+ getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

}
