/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.time.ITimeManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.time.TimeAggregation3Manager;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.MainEvent;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.VisuTOperator;

public abstract class TemporalMode extends VisuTOperator {

	protected HashMap<Integer, MainEvent> mainEvents;
	protected HashMap<Integer, Double> proportions;
	protected List<String> states;
	protected ITimeManager timeManager;
	protected double max;

	@Override
	public OcelotlCore getOcelotlCore() {
		return ocelotlCore;
	}

	@Override
	public void setOcelotlCore(OcelotlCore ocelotlCore) {
		this.ocelotlCore = ocelotlCore;
		timeManager = (ITimeManager) ocelotlCore.getLpaggregManager();
		timeSliceNumber = ocelotlCore.getOcelotlParameters()
				.getTimeSlicesNumber();
		timeSliceDuration = ocelotlCore.getOcelotlParameters().getTimeRegion()
				.getTimeDuration()
				/ timeSliceNumber;
		computeParts();
	}
	
	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	@Override
	public void initManager(OcelotlCore ocelotlCore, IDataAggregManager aManager) {
		this.ocelotlCore = ocelotlCore;
		timeManager = (ITimeManager) aManager;
		timeSliceNumber = ocelotlCore.getOcelotlParameters()
				.getTimeSlicesNumber();
		timeSliceDuration = ocelotlCore.getOcelotlParameters().getTimeRegion()
				.getTimeDuration()
				/ timeSliceNumber;
		computeParts();
	}

	public TemporalMode(OcelotlCore ocelotlCore) {
		super();
		setOcelotlCore(ocelotlCore);
	}

	public TemporalMode() {
		super();
	}

	public HashMap<Integer, MainEvent> getMajStates() {
		return mainEvents;
	}

	public void setMajStates(HashMap<Integer, MainEvent> mainEvents) {
		this.mainEvents = mainEvents;
	}
	
	@Override
	public void computeParts() {
		initParts();
		initStates();
		aggregateStates();
		normalize();
		computeMax();
		computeMainStates();
	}

	protected void initParts() {
		parts = new ArrayList<Part>();
		int oldPart = 0;
		parts.add(new Part(0, 1, new PartMap()));
		for (int i = 0; i < timeManager.getParts().size(); i++)
			if (timeManager.getParts().get(i) == oldPart)
				parts.get(parts.size() - 1).setEndPart(i + 1);
			else {
				oldPart = timeManager.getParts().get(i);
				parts.add(new Part(i, i + 1, new PartMap()));
			}
	}

	private void initStates() {
		states = ((TimeAggregation3Manager) timeManager).getKeys();
		for (final Part part : parts)
			for (final String state : states)
				((PartMap) part.getData()).putElement(state, 0.0);
	}

	private void aggregateStates() {
		for (final Part part : parts)
			for (int i = part.getStartPart(); i < part.getEndPart(); i++)
				for (final EventProducer ep : ((TimeAggregation3Manager) timeManager)
						.getEventProducers())
					for (final String state : states)
						((PartMap) part.getData()).addElement(state,
								((TimeAggregation3Manager) timeManager)
										.getTimeSliceMatrix().getMatrix()
										.get(i).get(ep).get(state));
	}
	
	public void computeMax() {
		max = 0;
		for (final Part part : parts)
			if (((PartMap) part.getData()).getTotal() > max)
				max = ((PartMap) part.getData()).getTotal();
	}

	public List<String> getStates() {
		return ((TimeAggregation3Manager) timeManager).getKeys();
	}
	
	private void normalize() {
		for (final Part part : parts)
			((PartMap) part.getData()).normalizeElements(timeSliceDuration,
					part.getPartSize());
	}
	
	public abstract void computeMainStates();

}