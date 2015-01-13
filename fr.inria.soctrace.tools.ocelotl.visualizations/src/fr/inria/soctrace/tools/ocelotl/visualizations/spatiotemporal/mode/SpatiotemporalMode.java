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

package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.ISpaceTimeManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.VisuSTOperator;
import fr.inria.soctrace.tools.ocelotl.visualizations.config.spatiotemporal.SpatioTemporalConfig;

public abstract class SpatiotemporalMode extends VisuSTOperator {

	HashMap<EventProducerNode, ArrayList<HashMap<String, Double>>> proportions;
	static final String Void = "void";

	public SpatiotemporalMode() {
		super();
	}

	public SpatiotemporalMode(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
	}

	@Override
	protected void computeParts() {
	}

	@Override
	public void initParts() {
		proportions = new HashMap<EventProducerNode, ArrayList<HashMap<String, Double>>>();
		computeProportions(hierarchy.getRoot());
	}

	protected List<String> getEvents() {
		return ((SpatioTemporalConfig) ocelotlCore.getOcelotlParameters().getVisuConfig()).getTypeNames();
	}
	
	@Override
	public void setOcelotlCore(final OcelotlCore ocelotlCore) {
		this.ocelotlCore = ocelotlCore;
		lpaggregManager = (ISpaceTimeManager) ocelotlCore.getLpaggregManager();
		timeSliceNumber = ocelotlCore.getOcelotlParameters()
				.getTimeSlicesNumber();
		timeSliceDuration = ocelotlCore.getOcelotlParameters().getTimeRegion()
				.getTimeDuration()
				/ timeSliceNumber;
		hierarchy = lpaggregManager.getHierarchy();
		
		SpatioTemporalConfig config = ((SpatioTemporalConfig) ocelotlCore
				.getOcelotlParameters().getVisuConfig());
		if (config.getTypes().isEmpty())
			config.getTypes().addAll(
					ocelotlCore.getOcelotlParameters().getOperatorEventTypes());
		
		config.checkForFilteredType(ocelotlCore.getOcelotlParameters().getTraceTypeConfig().getTypes());
		
		initParts();
		computeParts();
	}

	protected abstract void computeProportions(EventProducerNode node);

	public abstract MainEvent getMainEvent(EventProducerNode epn, int start,
			int end);

}
