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

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.lpaggreg.quality.DLPQuality;
import fr.inria.lpaggreg.time.ITimeAggregation;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.util.DeltaManagerOcelotl;

public abstract class TimeAggregationManager implements ITimeManager {

	protected List<Integer> parts = new ArrayList<Integer>();
	protected List<DLPQuality> qualities = new ArrayList<DLPQuality>();
	protected List<Double> parameters = new ArrayList<Double>();
	protected List<List<Boolean>> eqMatrix;
	protected ITimeAggregation timeAggregation;
	protected OcelotlParameters ocelotlParameters;
	
	private static final Logger logger = LoggerFactory.getLogger(TimeAggregationManager.class);
	

	public TimeAggregationManager(final OcelotlParameters ocelotlParameters) {
		super();
		this.ocelotlParameters = ocelotlParameters;
	}

	@Override
	public void computeDichotomy() {
		final DeltaManager dm = new DeltaManagerOcelotl();
		dm.start();
		timeAggregation.computeBestQualities(ocelotlParameters.getThreshold(),
				0.0, 1.0);
		parameters = timeAggregation.getParameters();
		qualities = timeAggregation.getQualityList();
		dm.end("LPAGGREG - PARAMETERS LIST");

	}

	@Override
	public void computeParts() {
		final DeltaManager dm = new DeltaManagerOcelotl();
		dm.start();
		parts = timeAggregation.getParts(ocelotlParameters.getParameter());
		dm.end("LPAGGREG - COMPUTE PARTS");
	}

	@Override
	public void computeQualities() {
		final DeltaManager dm = new DeltaManagerOcelotl();
		dm.start();
		timeAggregation.computeQualities(ocelotlParameters.isNormalize());
		dm.end("LPAGGREG - COMPUTE QUALITIES");
	}


	protected abstract void fillVectors();

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
		logger.info("");
		logger.info("Parameters :");
		StringBuffer buff = new StringBuffer();
		for (final Double i : parameters)
			buff.append(i + " ");
		logger.info(buff.toString());
	}

	@Override
	public void printParts() {
		logger.debug("");
		logger.debug("Parts :");
		StringBuffer buff = new StringBuffer();
		for (final int i : parts)
			buff.append(i + " ");
		logger.debug(buff.toString());
	}

	@Override
	public abstract void reset();

	@Override
	public void print(OcelotlCore core) {
		PartManager partManager = new PartManager(core);
		partManager.print();
	}

}
