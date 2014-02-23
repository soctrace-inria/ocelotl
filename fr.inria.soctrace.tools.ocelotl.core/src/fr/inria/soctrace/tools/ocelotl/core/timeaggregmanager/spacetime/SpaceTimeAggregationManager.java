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

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.quality.DLPQuality;
import fr.inria.dlpaggreg.spacetime.ISpaceTimeAggregation;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public abstract class SpaceTimeAggregationManager implements ISpaceTimeManager {

	protected List<DLPQuality>		qualities	= new ArrayList<DLPQuality>();
	protected List<Double>			parameters	= new ArrayList<Double>();
	protected ISpaceTimeAggregation		timeAggregation;
	protected OcelotlParameters		ocelotlParameters;
	protected EventProducerHierarchy hierarchy;


	
	public SpaceTimeAggregationManager(final OcelotlParameters ocelotlParameters) {
		super();
		this.ocelotlParameters = ocelotlParameters;
	}

	@Override
	public void computeDichotomy() {
		final DeltaManager dm = new DeltaManager();
		dm.start();
		timeAggregation.computeBestQualities(ocelotlParameters.getThreshold(), 0.0, 1.0);
		parameters = timeAggregation.getParameters();
		qualities = timeAggregation.getQualityList();
		dm.end("LPAGGREG - PARAMETERS LIST");

	}

	@Override
	public void computeParts() {
		final DeltaManager dm = new DeltaManager();
		dm.start();
		timeAggregation.computeParts(ocelotlParameters.getParameter());
		updateHierarchy();
		dm.end("LPAGGREG - COMPUTE PARTS");
	}

	private void updateHierarchy() {
		for (int id: hierarchy.getEventProducers().keySet())
			hierarchy.setParts(id, timeAggregation.getParts(id));
	}

	@Override
	public void computeQualities() {
		final DeltaManager dm = new DeltaManager();
		dm.start();
		timeAggregation.computeQualities(ocelotlParameters.isNormalize());
		dm.end("LPAGGREG - COMPUTE QUALITIES");
	}

	public void fillNodes() {
		if (OcelotlParameters.isJniFlag())
			fillNodesJNI();
		else
			fillNodesJava();

	}

	protected abstract void fillNodesJava();

	protected abstract void fillNodesJNI();

	@Override
	public List<Double> getParameters() {
		return parameters;
	}

	@Override
	public List<DLPQuality> getQualities() {
		return qualities;
	}
	
	protected void addHierarchyToJNI() {
		addRoot();
		addNodes();
		addLeaves();
		timeAggregation.validate();
		
	}

	protected abstract void addLeaves();

	protected abstract void addNodes();

	protected abstract void addRoot();

	@Override
	public void printParameters() {
		System.out.println();
		System.out.println("Parameters :");
		for (final Double i : parameters)
			System.out.print(i + " ");
		System.out.println();
	}

	@Override
	public void printParts() {
//		System.out.println();
//		System.out.println("Parts :");
//		for (final int i : parts)
//			System.out.print(i + " ");
//		System.out.println();
	}

	@Override
	public abstract void reset();
	
	@Override
	public EventProducerHierarchy getHierarchy(){
		return hierarchy;
	}
	
	@Override
	public void print(OcelotlCore core){
		
	}

}
