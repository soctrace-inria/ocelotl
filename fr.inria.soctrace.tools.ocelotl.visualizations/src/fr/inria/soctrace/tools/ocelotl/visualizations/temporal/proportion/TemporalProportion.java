/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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

package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.proportion;

import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.time.TimeAggregation3Manager;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.VisuTOperator;

public class TemporalProportion extends VisuTOperator {

	private List<String>		states;
	private double				max;
	
	public TemporalProportion() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TemporalProportion(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
		// TODO Auto-generated constructor stub
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

	private void normalize() {
		for (final Part part : parts)
			((PartMap) part.getData()).normalizeElements(timeSliceDuration, part.getPartSize());
	}
	
	public double getMaxValue(){
		return max;
	}

}
