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

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.lpaggreg.quality.DLPQuality;
import fr.inria.lpaggreg.spacetime.ISpaceTimeAggregation;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public abstract class SpaceTimeAggregationManager implements ISpaceTimeManager {

	protected List<DLPQuality> qualities = new ArrayList<DLPQuality>();
	protected List<Double> parameters = new ArrayList<Double>();
	protected ISpaceTimeAggregation timeAggregation;
	protected OcelotlParameters ocelotlParameters;
	protected EventProducerHierarchy hierarchy;
	private static final Logger logger = LoggerFactory.getLogger(SpaceTimeAggregationManager.class);

	public SpaceTimeAggregationManager(final OcelotlParameters ocelotlParameters) {
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
		timeAggregation.computeParts(ocelotlParameters.getParameter());
		updateHierarchy();
		int i = 0;
		for (i = 0; i < parameters.size() - 1; i++)
			if (ocelotlParameters.getParameter() == parameters.get(i))
				break;

		logger.debug("parameter: " + ocelotlParameters.getParameter()
				+ ", gain: " + qualities.get(i).getGain() + ", loss: "
				+ qualities.get(i).getLoss());
		dm.end("LPAGGREG - COMPUTE PARTS");
	}

	private void updateHierarchy() {
		for (int id : hierarchy.getEventProducers().keySet())
			hierarchy.setParts(id, timeAggregation.getParts(id));
	}

	@Override
	public void computeQualities() {
		final DeltaManager dm = new DeltaManagerOcelotl();
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
		for (int id : hierarchy.getEventProducers().keySet()) {
			buff.append(id + ": ");
			for (final int i : timeAggregation.getParts(id)) {
				buff.append(i + " ");
			}
			buff.append("\n");
		}
		logger.debug(buff.toString());
	}

	@Override
	public abstract void reset(IProgressMonitor monitor) throws OcelotlException;

	@Override
	public EventProducerHierarchy getHierarchy() {
		return hierarchy;
	}

	@Override
	public void print(OcelotlCore core) {

	}

}
