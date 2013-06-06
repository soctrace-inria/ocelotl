/* ===========================================================
 * LPAggreg core module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to visualize a Paje
 * trace across an aggregated representation.
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
 */

package fr.inria.soctrace.tools.paje.lpaggreg.core.tsaggregoperators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.paje.lpaggreg.core.Queries;
import fr.inria.soctrace.tools.paje.lpaggreg.core.State;
import fr.inria.soctrace.tools.paje.lpaggreg.core.TimeSliceManager;

public abstract class TimeSliceMatrix implements ITimeSliceMatrix{

	protected Queries						queries;
	protected List<HashMap<String, Long>>	matrix	= new ArrayList<HashMap<String,  Long>>();
	protected int							eventsNumber;
	protected TimeSliceManager				timeSliceManager;

	public TimeSliceMatrix(Queries queries) throws SoCTraceException {
		super();
		this.queries = queries;
		queries.checkTimeStamps();
		timeSliceManager = new TimeSliceManager(queries.getLpaggregParameters().getTimeRegion(), queries.getLpaggregParameters().getTimeSlicesNumber());
		initVectors();
		computeMatrix();
	}
	
	protected abstract void computeSubMatrix(List<EventProducer> eventProducers) throws SoCTraceException ;


	public void computeMatrix() throws SoCTraceException {
		eventsNumber = 0;
		DeltaManager dm = new DeltaManager();
		dm.start();	
		int epsize = queries.getLpaggregParameters().getEventProducers().size();
		if ((queries.getLpaggregParameters().getMaxEventProducers()==0) || (epsize<queries.getLpaggregParameters().getMaxEventProducers())){
			computeSubMatrix(queries.getLpaggregParameters().getEventProducers());
		}else{
			List<EventProducer> producers = (queries.getLpaggregParameters().getEventProducers().size() == 0) ? queries.getAllEventProducers() : queries.getLpaggregParameters().getEventProducers();
			for (int i=0; i<epsize; i=i+queries.getLpaggregParameters().getMaxEventProducers()){
				computeSubMatrix(producers.subList(i, Math.min(epsize-1, i+queries.getLpaggregParameters().getMaxEventProducers())));
			}
		}

		dm.end("TOTAL (QUERIES + COMPUTATION) : " + epsize + " Event Producers, " + eventsNumber + " Events");
	}

	public List<HashMap<String, Long>> getMatrix() {
		return matrix;
	}

	public Queries getQueries() {
		return queries;
	}

	public TimeSliceManager getTimeSlicesManager() {
		return timeSliceManager;
	}

	public int getVectorSize() {
		return matrix.get(0).size();
	}

	public int getVectorsNumber() {
		return matrix.size();
	}

	public void initVectors() throws SoCTraceException {
		List<EventProducer> producers = queries.getLpaggregParameters().getEventProducers();
		for (long i = 0; i < timeSliceManager.getSlicesNumber(); i++) {
			matrix.add(new HashMap<String, Long>());

			for (EventProducer ep : producers)
				matrix.get((int) i).put(ep.getName(), 0L);
		}
	}

	public void print() {
		System.out.println();
		System.out.println("Distribution Vectors");
		int i = 0;
		for (HashMap<String, Long> it : matrix) {
			System.out.println();
			System.out.println("slice " + i++);
			System.out.println();
			for (String ep : it.keySet())
				System.out.println(ep + " = " + it.get(ep));
		}
	}

	public void setQueries(Queries queries) {
		this.queries = queries;
	}

	@Override
	public void computeVectors() {
		// TODO Auto-generated method stub
		
	}

}
