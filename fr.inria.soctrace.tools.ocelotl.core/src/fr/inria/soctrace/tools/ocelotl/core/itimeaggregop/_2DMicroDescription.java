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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.TimeAggregation2Manager;

public abstract class _2DMicroDescription extends
		MultiThreadTimeAggregationOperator implements I2DMicroDescription {

	protected List<HashMap<EventProducer, Long>> matrix;

	public _2DMicroDescription() throws SoCTraceException {
		super();
	}

	public _2DMicroDescription(final OcelotlParameters parameters)
			throws SoCTraceException, OcelotlException {
		super();
		try {
			setOcelotlParameters(parameters);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void computeMatrix() throws SoCTraceException, OcelotlException,
			InterruptedException {
		eventsNumber = 0;
		final DeltaManager dmt = new DeltaManager();
		dmt.start();
		final int epsize = getOcelotlParameters().getEventProducers().size();
		if (getOcelotlParameters().getMaxEventProducers() == 0
				|| epsize < getOcelotlParameters().getMaxEventProducers())
			computeSubMatrix(getOcelotlParameters().getEventProducers());
		else {
			final List<EventProducer> producers = getOcelotlParameters()
					.getEventProducers().size() == 0 ? ocelotlQueries
					.getAllEventProducers() : getOcelotlParameters()
					.getEventProducers();
			for (int i = 0; i < epsize; i = i
					+ getOcelotlParameters().getMaxEventProducers())
				computeSubMatrix(producers.subList(i, Math.min(epsize - 1, i
						+ getOcelotlParameters().getMaxEventProducers())));

		}

		dmt.end("TOTAL (QUERIES + COMPUTATION) : " + epsize
				+ " Event Producers, " + eventsNumber + " Events");
	}

	@Override
	public TimeAggregation2Manager createManager() {
		return new TimeAggregation2Manager(this);

	}

	@Override
	public List<HashMap<EventProducer, Long>> getMatrix() {
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
		matrix = new ArrayList<HashMap<EventProducer, Long>>();
		final List<EventProducer> producers = getOcelotlParameters()
				.getEventProducers();
		for (long i = 0; i < timeSliceManager.getSlicesNumber(); i++) {
			matrix.add(new HashMap<EventProducer, Long>());

			for (final EventProducer ep : producers)
				matrix.get((int) i).put(ep, 0L);
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
		System.out.println();
		System.out.println("Distribution Vectors");
		int i = 0;
		for (final HashMap<EventProducer, Long> it : matrix) {
			System.out.println();
			System.out.println("slice " + i++);
			System.out.println();
			for (final EventProducer ep : it.keySet())
				System.out.println(ep.getName() + " = " + it.get(ep));
		}
	}

}
