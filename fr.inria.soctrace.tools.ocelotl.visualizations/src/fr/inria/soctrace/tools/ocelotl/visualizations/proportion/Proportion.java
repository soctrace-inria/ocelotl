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

package fr.inria.soctrace.tools.ocelotl.visualizations.proportion;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.ISpaceAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceTAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.TimeAggregation3Manager;

public class Proportion extends SpaceTAggregationOperator {

	private List<String>		states;
	private double				max;

	public Proportion() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Proportion(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
		// TODO Auto-generated constructor stub
	}

	private void aggregateStates() {
		for (final Part part : parts)
			for (int i = part.getStartPart(); i < part.getEndPart(); i++)
				for (final EventProducer ep : ((TimeAggregation3Manager) lpaggregManager).getEventProducers())
					for (final String state : states)
						((PartMap) part.getData()).addElement(state, ((TimeAggregation3Manager) lpaggregManager).getTimeSliceMatrix().getMatrix().get(i).get(ep).get(state));

	}

	private void computeMax() {
		max = 0;
		for (final Part part : parts)
			if (((PartMap) part.getData()).getTotal() > max)
				max = ((PartMap) part.getData()).getTotal();
	}

	@Override
	protected void computeParts() {
		initParts();
		initStates();
		aggregateStates();
		normalize();
		computeMax();
	}

	public double getMax() {
		return max;
	}

	public List<String> getStates() {
		return states;
	}

	@Override
	protected void initParts() {
		int oldPart = 0;
		parts.add(new Part(0, 1, new PartMap()));
		for (int i = 0; i < lpaggregManager.getParts().size(); i++)
			if (lpaggregManager.getParts().get(i) == oldPart)
				parts.get(parts.size() - 1).setEndPart(i + 1);
			else {
				oldPart = lpaggregManager.getParts().get(i);
				parts.add(new Part(i, i + 1, new PartMap()));
			}
	}

	private void initStates() {
		states = ((TimeAggregation3Manager) lpaggregManager).getKeys();
		for (final Part part : parts)
			for (final String state : states)
				((PartMap) part.getData()).putElement(state, 0.0);
	}

	private void normalize() {
		for (final Part part : parts)
			((PartMap) part.getData()).normalizeElements(timeSliceDuration, part.getPartSize());
	}

	@Override
	public ISpaceAggregationOperator copy() {
		Proportion newProp = new Proportion();
		newProp.states = states;
		newProp.ocelotlCore = ocelotlCore;
		newProp.timeSliceNumber = timeSliceNumber;
		newProp.timeSliceDuration = timeSliceDuration;
		newProp.lpaggregManager = lpaggregManager;

		newProp.parts = new ArrayList<Part>();
		int i;
		for (i = 0; i < parts.size(); i++)
			newProp.parts.add(parts.get(i));

		return newProp;
	}

}
