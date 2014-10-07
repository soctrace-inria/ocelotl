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

import java.io.File;
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
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacacheStrategy;
import fr.inria.soctrace.tools.ocelotl.core.datacache.DataCache;
import fr.inria.soctrace.tools.ocelotl.core.events.IVariable;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop._3DMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceVariableManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;
import fr.inria.soctrace.tools.ocelotl.microdesc.genericevents.GenericVariable;

public class VariableDistribution extends _3DMicroDescription {

	private static final Logger logger = LoggerFactory.getLogger(VariableDistribution.class);
	
	
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

		private void matrixUpdate(final IVariable variable, final EventProducer ep,
				final Map<Long, Double> distrib) {
			synchronized (microModel.getMatrix()) {
				if (!microModel.getMatrix().get(0).get(ep).containsKey(variable.getType())) {
					logger.debug("Adding " + variable.getType()
							+ " variable");
					// addKey(state.getStateType());
					for (int incr = 0; incr < microModel.getMatrix().size(); incr++)
						for (final EventProducer epset : microModel.getMatrix().get(incr)
								.keySet())
							matrixPushType(incr, epset, variable.getType());
				}
				for (final long it : distrib.keySet())
					matrixWrite(it, ep, variable.getType(), distrib);
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
				
				IVariable variable;
				for (final Event event : events) {
					variable = new GenericVariable(event, timeSliceManager);
					final Map<Long, Double> distrib = variable
							.getTimeSlicesDistribution();
					matrixUpdate(variable, event.getEventProducer(), distrib);
					if (monitor.isCanceled())
						return;
				}
			}
		}
	}

	private TimeSliceVariableManager timeSliceManager;

	public VariableDistribution() throws SoCTraceException {
		super();
	}

	public VariableDistribution(final OcelotlParameters parameters,
			IProgressMonitor monitor) throws SoCTraceException,
			OcelotlException {
		super(parameters, monitor);
	}

	@Override
	public void computeSubMatrix(List<EventProducer> eventProducers,
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
		monitor.subTask("Fill the matrix");
		for (int t = 0; t < getOcelotlParameters().getThreadNumber(); t++)
			threadlist.add(new OcelotlThread(getOcelotlParameters()
					.getThreadNumber(), t, getOcelotlParameters()
					.getEventsPerThread(), monitor));
		for (final Thread thread : threadlist)
			thread.join();
		ocelotlQueries.closeIterator();
		dm.end("VECTORS COMPUTATION: "
				+ getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}
	
	@Override
	protected boolean isCacheLoadable(File cacheFile, DataCache datacache) {
		return (cacheFile != null && (!datacache.isRebuildDirty() || datacache
				.getBuildingStrategy() != DatacacheStrategy.DATACACHE_DATABASE));
	}

}
