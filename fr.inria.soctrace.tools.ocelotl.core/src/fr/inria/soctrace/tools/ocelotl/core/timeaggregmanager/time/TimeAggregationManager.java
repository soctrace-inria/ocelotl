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

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.quality.DLPQuality;
import fr.inria.dlpaggreg.time.ITimeAggregation;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public abstract class TimeAggregationManager implements ITimeManager {

	protected List<Integer>			parts		= new ArrayList<Integer>();
	protected List<DLPQuality>		qualities	= new ArrayList<DLPQuality>();
	protected List<Double>			parameters	= new ArrayList<Double>();
	protected List<List<Boolean>>	eqMatrix;
	protected ITimeAggregation		timeAggregation;
	protected OcelotlParameters		ocelotlParameters;

	
	public TimeAggregationManager(final OcelotlParameters ocelotlParameters) {
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
		parts = timeAggregation.getParts(ocelotlParameters.getParameter());
		dm.end("LPAGGREG - COMPUTE PARTS");
	}

	@Override
	public void computeQualities() {
		final DeltaManager dm = new DeltaManager();
		dm.start();
		timeAggregation.computeQualities(ocelotlParameters.isNormalize());
		dm.end("LPAGGREG - COMPUTE QUALITIES");
	}

	
	public void fillVectors() {
		if (OcelotlParameters.isJniFlag())
			fillVectorsJNI();
		else
			fillVectorsJava();

	}

	protected abstract void fillVectorsJava();

	protected abstract void fillVectorsJNI();

	@Override
	public List<Double> getParameters() {
		return parameters;
	}

	@Override
	public List<Integer> getParts() {
		return parts;
	}

	@Override
	public List<DLPQuality> getQualities() {
		return qualities;
	}

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
		System.out.println();
		System.out.println("Parts :");
		for (final int i : parts)
			System.out.print(i + " ");
		System.out.println();
	}

	@Override
	public abstract void reset();

}
