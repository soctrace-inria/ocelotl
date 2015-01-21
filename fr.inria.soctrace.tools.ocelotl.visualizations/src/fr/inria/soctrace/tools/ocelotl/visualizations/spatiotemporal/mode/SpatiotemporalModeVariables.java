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
package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;

public class SpatiotemporalModeVariables extends SpatiotemporalMode {

	protected Double amplitudeMax;
	
	public SpatiotemporalModeVariables() {
		super();
	}
	
	public SpatiotemporalModeVariables(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	protected void computeProportions(EventProducerNode node) {
		// Init for the current producer node
		proportions.put(node, new ArrayList<HashMap<String, Double>>());
		amplitudeMax = 0.0;
		// Init for each part of the node
		for (int i = 0; i < node.getParts().size(); i++) {
			proportions.get(node).add(new HashMap<String, Double>());
			// And for each state of the part
			for (String event : getAllEvents())
				// Init to zero
				proportions.get(node).get(i).put(event, 0.0);
		}
		// If node is a leaf
		if (node.getChildrenNodes().isEmpty()) {
			for (int i = 0; i < node.getParts().size(); i++) {
				double tempAmp = 0.0;
				for (String event : getAllEvents()) {
					// Add value (= value / time slice duration)
					Double computedValue = ((List<HashMap<String, Double>>) node
							.getValues()).get(i).get(event)
							/ ((Long.valueOf(timeSliceDuration).doubleValue() * node
									.getWeight()));
					// Compute amplitude max
					tempAmp += computedValue;
					proportions.get(node).get(i).put(event, computedValue);
				}
				if (tempAmp > amplitudeMax) {
					amplitudeMax = tempAmp;
				}
			}
		} else {
			// Compute proportions recursively for each children node
			for (EventProducerNode child : node.getChildrenNodes()) {
				computeProportions(child);
				for (int i = 0; i < node.getParts().size(); i++) {
					double tempAmp = 0.0;
					for (String event : getAllEvents()) {
						Double computedValue = proportions.get(node).get(i)
								.get(event)
								+ proportions.get(child).get(i).get(event)
								/ (node.getChildrenNodes().size());
						tempAmp += computedValue;
						proportions.get(node).get(i).put(event, computedValue);
					}
					if (tempAmp > amplitudeMax) {
						amplitudeMax = tempAmp;
					}
				}
			}
		}
	}

	@Override
	public MainEvent getMainEvent(EventProducerNode epn, int start, int end) {
		double max = 0.0;
		MainEvent maj = new MainEvent(Void, max);
		for (String event : getEvents()) {
			double amp = 0.0;
			// Compute the total presence of the event
			for (int i = start; i < end; i++)
				amp += proportions.get(epn).get(i).get(event);

			// Divide by duration
			amp = (amp / (end - start)) / amplitudeMax;
			if (amp > max) {
				maj = new MainEvent(event, amp);
				max = amp;
			}
		}

		return maj;
	}

}
