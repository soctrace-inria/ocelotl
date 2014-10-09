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

package fr.inria.soctrace.tools.ocelotl.visualizations.mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.SpaceTimeAggregation2Manager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.VisuSTOperator;

public class MatrixProportion extends VisuSTOperator {

	HashMap<EventProducerNode, ArrayList<HashMap<String, Double>>> proportions;
	static final String Void = "void";

	public MatrixProportion() {
		super();
	}

	public MatrixProportion(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
	}

	@Override
	protected void computeParts() {
	}

	@Override
	protected void initParts() {
		proportions = new HashMap<EventProducerNode, ArrayList<HashMap<String, Double>>>();
		computeProportions(hierarchy.getRoot());
	}

	private List<String> getStates() {
		return ((SpaceTimeAggregation2Manager) lpaggregManager).getKeys();
	}

	@SuppressWarnings("unchecked")
	private void computeProportions(EventProducerNode node) {
		proportions.put(node, new ArrayList<HashMap<String, Double>>());
		for (int i = 0; i < node.getParts().size(); i++) {
			proportions.get(node).add(new HashMap<String, Double>());
			for (String state : getStates())
				proportions.get(node).get(i).put(state, 0.0);
		}
		if (node.getChildrenNodes().isEmpty()) {
			for (int i = 0; i < node.getParts().size(); i++) {
				for (String state : getStates())
					proportions
							.get(node)
							.get(i)
							.put(state,
									((List<HashMap<String, Double>>) node
											.getValues()).get(i).get(state)
											/ (Long.valueOf(timeSliceDuration)
													.doubleValue()));
			}
		} else {
			for (EventProducerNode child : node.getChildrenNodes()) {
				computeProportions(child);
				for (int i = 0; i < node.getParts().size(); i++) {
					for (String state : getStates())
						proportions
								.get(node)
								.get(i)
								.put(state,
										proportions.get(node).get(i).get(state)
												+ proportions.get(child).get(i)
														.get(state)
												/ (node.getChildrenNodes()
														.size()));
				}
			}

		}
	}

	public MajState getMajState(EventProducerNode epn, int start, int end) {
		double max = 0.0;
		MajState maj = new MajState(Void, max);
		for (String state : getStates()) {
			double amp = 0.0;
			for (int i = start; i < end; i++)
				amp += proportions.get(epn).get(i).get(state);
			amp /= (end - start);
			if (amp > max) {
				maj = new MajState(state, amp);
				max = amp;
			}

		}
		return maj;

	}

}
