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

package fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.ts.TimeSliceManager;

public abstract class TimeSliceMatrix implements ITimeSliceMatrix {

	protected Query							query;
	protected List<HashMap<String, Long>>	matrix	= new ArrayList<HashMap<String, Long>>();
	protected int							eventsNumber;
	protected TimeSliceManager				timeSliceManager;
	int										count			= 0;
	int										epit			= 0;
	DeltaManager							dm;
	final static int 						EPCOUNT			= 200;			

	public TimeSliceMatrix(final Query query) throws SoCTraceException {
		super();
		this.query = query;
		query.checkTimeStamps();
		timeSliceManager = new TimeSliceManager(query.getOcelotlParameters().getTimeRegion(), query.getOcelotlParameters().getTimeSlicesNumber());
		initVectors();
		computeMatrix();
	}

	public void computeMatrix() throws SoCTraceException {
		eventsNumber = 0;
		final DeltaManager dmt = new DeltaManager();
		dmt.start();
		final int epsize = query.getOcelotlParameters().getEventProducers().size();
		if (query.getOcelotlParameters().getMaxEventProducers() == 0 || epsize < query.getOcelotlParameters().getMaxEventProducers())
			try {
				computeSubMatrix(query.getOcelotlParameters().getEventProducers());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else {
			final List<EventProducer> producers = query.getOcelotlParameters().getEventProducers().size() == 0 ? query.getAllEventProducers() : query.getOcelotlParameters().getEventProducers();
			for (int i = 0; i < epsize; i = i + query.getOcelotlParameters().getMaxEventProducers())
				try {
					computeSubMatrix(producers.subList(i, Math.min(epsize - 1, i + query.getOcelotlParameters().getMaxEventProducers())));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		dmt.end("TOTAL (QUERIES + COMPUTATION) : " + epsize + " Event Producers, " + eventsNumber + " Events");
	}

	protected abstract void computeSubMatrix(List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException;

	public synchronized int getCount() {
		count++;
		return count;
	}

	public void total(int rows) {
		dm.end("VECTOR COMPUTATION " + rows + " rows computed");
	}

	public synchronized int getEP() {
		epit++;
		return epit - 1;
	}
	
	@Override
	public void computeVectors() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<HashMap<String, Long>> getMatrix() {
		return matrix;
	}

	@Override
	public Query getQueries() {
		return query;
	}

	@Override
	public TimeSliceManager getTimeSlicesManager() {
		return timeSliceManager;
	}

	@Override
	public int getVectorSize() {
		return matrix.get(0).size();
	}

	@Override
	public int getVectorsNumber() {
		return matrix.size();
	}

	@Override
	public void initVectors() throws SoCTraceException {
		final List<EventProducer> producers = query.getOcelotlParameters().getEventProducers();
		for (long i = 0; i < timeSliceManager.getSlicesNumber(); i++) {
			matrix.add(new HashMap<String, Long>());

			for (final EventProducer ep : producers)
				matrix.get((int) i).put(ep.getName(), 0L);
		}
	}

	@Override
	public void print() {
		System.out.println();
		System.out.println("Distribution Vectors");
		int i = 0;
		for (final HashMap<String, Long> it : matrix) {
			System.out.println();
			System.out.println("slice " + i++);
			System.out.println();
			for (final String ep : it.keySet())
				System.out.println(ep + " = " + it.get(ep));
		}
	}

	@Override
	public void setQueries(final Query query) {
		this.query = query;
	}

}
