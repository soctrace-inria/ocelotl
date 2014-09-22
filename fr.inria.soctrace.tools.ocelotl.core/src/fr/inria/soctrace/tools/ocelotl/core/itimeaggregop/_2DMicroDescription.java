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

package fr.inria.soctrace.tools.ocelotl.core.itimeaggregop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.TimeAggregation2Manager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public abstract class _2DMicroDescription extends
		MultiThreadTimeAggregationOperator implements I2DMicroDescription {

	protected List<HashMap<EventProducer, Double>> matrix;
	
	private static final Logger logger = LoggerFactory.getLogger(_2DMicroDescription.class);

	public _2DMicroDescription() throws SoCTraceException {
		super();
	}

	public _2DMicroDescription(final OcelotlParameters parameters, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		super();
		try {
			setOcelotlParameters(parameters, monitor);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void computeMatrix(IProgressMonitor monitor) throws SoCTraceException, OcelotlException,
			InterruptedException {
		eventsNumber = 0;
		final DeltaManager dmt = new DeltaManagerOcelotl();
		dmt.start();
		final int epsize = getOcelotlParameters().getEventProducers().size();
		if (getOcelotlParameters().getMaxEventProducers() == 0
				|| epsize < getOcelotlParameters().getMaxEventProducers())
			computeSubMatrix(getOcelotlParameters().getEventProducers(), monitor);
		else {
			final List<EventProducer> producers = getOcelotlParameters()
					.getEventProducers().size() == 0 ? ocelotlQueries
					.getAllEventProducers() : getOcelotlParameters()
					.getEventProducers();
			for (int i = 0; i < epsize; i = i
					+ getOcelotlParameters().getMaxEventProducers())
				computeSubMatrix(producers.subList(i, Math.min(epsize - 1, i
						+ getOcelotlParameters().getMaxEventProducers())), monitor);
		}

		dmt.end("TOTAL (QUERIES + COMPUTATION) : " + epsize
				+ " Event Producers, " + eventsNumber + " Events");
	}

	@Override
	public TimeAggregation2Manager createManager(IProgressMonitor monitor) {
		return new TimeAggregation2Manager(this, monitor);

	}

	@Override
	public List<HashMap<EventProducer, Double>> getMatrix() {
		return matrix;
	}

	@Override
	public int getVectorSize() {
		return matrix.get(0).size();
	}

	@Override
	public int getVectorNumber() {
		return matrix.size();
	}

	@Override
	public void initVectors() throws SoCTraceException {
		matrix = new ArrayList<HashMap<EventProducer, Double>>();
		final List<EventProducer> producers = getOcelotlParameters()
				.getEventProducers();
		for (long i = 0; i < parameters.getTimeSlicesNumber(); i++) {
			matrix.add(new HashMap<EventProducer, Double>());

			for (final EventProducer ep : producers)
				matrix.get((int) i).put(ep, 0.0);
		}
	}

	public void matrixWrite(final long it, final EventProducer ep,
			final Map<Long, Long> distrib) {
		synchronized (matrix) {
			matrix.get((int) it).put(ep,
					matrix.get((int) it).get(ep) + distrib.get(it));
		}
	}

	@Override
	public void print() {
		logger.debug("");
		logger.debug("Distribution Vectors");
		int i = 0;
		for (final HashMap<EventProducer, Double> it : matrix) {
			logger.debug("");
			logger.debug("slice " + i++);
			logger.debug("");
			for (final EventProducer ep : it.keySet())
				logger.debug(ep.getName() + " = " + it.get(ep));
		}
	}

	@Override
	public String matrixToCSV() {
		StringBuffer stringBuf = new StringBuffer();
		int slice = 0;
		// For each slice
		for (final HashMap<EventProducer, Double> it : matrix) {
			// for each event producer
			for (final EventProducer ep : it.keySet()) {
				if (it.get(ep) != 0)
					stringBuf.append(slice + OcelotlConstants.CSVDelimiter
							+ ep.getId() + OcelotlConstants.CSVDelimiter
							+ it.get(ep) + "\n");
			}
			slice++;
		}
		return stringBuf.toString();
	}
	
	@Override
	public void rebuildMatrix(String[] values, EventProducer ep,
			int sliceMultiple) {
		int slice = Integer.parseInt(values[0]);
		double value = Double.parseDouble(values[2]);

		// If the number of time slice is a multiple of the cached time
		// slice number
		if (sliceMultiple > 1) {
			// Compute the correct slice number
			slice = slice / sliceMultiple;

			// And add the value to the one already in the matrix
			if (matrix.get(slice).get(ep) != null)
				value = matrix.get(slice).get(ep) + value;
		}
		matrix.get(slice).put(ep, value);
	}
	
	@Override
	public void initMatrixToZero(Collection<EventProducer> eventProducers) {
		for (int slice = 0; slice < parameters.getTimeSlicesNumber(); slice++) {
			for (EventProducer ep : eventProducers) {
				matrix.get(slice).put(ep, 0.0);
			}
		}
	}

}
