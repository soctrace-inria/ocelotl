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

package fr.inria.soctrace.tools.ocelotl.core.generic.spaceaggregop;

import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.MLPAggregManager;

public class StateDistribution extends SpaceAggregationOperator {

	public final static String		descriptor	= "State Distribution";
	private List<String>	states;
	private double max;

	public StateDistribution(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
		// TODO Auto-generated constructor stub
	}
	
	public StateDistribution() {
		super();
		// TODO Auto-generated constructor stub
	}

	private void aggregateStates() {
		for (final Part part : parts)
			for (int i = part.getStartPart(); i < part.getEndPart(); i++)
				for (final String ep : ((MLPAggregManager) lpaggregManager).getEventProducers())
					for (final String state : states)
						((PartMap) part.getData()).addElement(state, ((MLPAggregManager) lpaggregManager).getTimeSliceMatrix().getMatrix().get(i).get(ep).get(state).doubleValue());

	}
	
	

	public List<String> getStates() {
		return states;
	}

	@Override
	protected void computeParts() {
		initParts();
		initStates();
		aggregateStates();
		normalize();
		computeMax();
	}

	private void computeMax() {
		max=0;
		for (final Part part : parts)
			if (((PartMap) part.getData()).getTotal()>max)
				max=((PartMap) part.getData()).getTotal();
	}

	public double getMax() {
		return max;
	}

	@Override
	public String descriptor() {
		return descriptor;
	}


	protected void initParts() {
		int oldPart = 0;
		parts.add(new Part(0, 1, new PartMap()));
		for (int i = 0; i < lpaggregManager.getParts().size(); i++)
			if (lpaggregManager.getParts().get(i) == oldPart)
				parts.get(parts.size() - 1).setEndPart(i+1);
			else {
				oldPart = lpaggregManager.getParts().get(i);
				parts.add(new Part(i, i + 1, new PartMap()));
			}
	}

	private void initStates() {
		states = ((MLPAggregManager) lpaggregManager).getTimeSliceMatrix().getKeys();
		for (final Part part : parts)
			for (final String state : states)
				((PartMap) part.getData()).putElement(state, 0.0);
	}

	private void normalize() {
		for (final Part part : parts)
			((PartMap) part.getData()).normalizeElements(timeSliceDuration, part.getPartSize());
	}

}