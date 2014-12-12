package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;

public class SpatiotemporalModeEvents extends SpatiotemporalMode {

	//protected HashMap<EventProducerNode, ArrayList<ArrayList<Double>>> amplitudeMax;
	protected Double amplitudeMax;
	
	public SpatiotemporalModeEvents() {
		super();
	}
	
	public SpatiotemporalModeEvents(final OcelotlCore ocelotlCore) {
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
			for (String event : getStates())
				// Init to zero
				proportions.get(node).get(i).put(event, 0.0);
		}
		// If node is a leaf
		if (node.getChildrenNodes().isEmpty()) {
			for (int i = 0; i < node.getParts().size(); i++) {
				for (String event : getStates()){
					// Add value (= value / time slice duration)
					Double computedValue = ((List<HashMap<String, Double>>) node
							.getValues()).get(i).get(event)
							/ (Long.valueOf(timeSliceDuration).doubleValue());
					if (computedValue > amplitudeMax) {
						amplitudeMax = computedValue;
					}
					proportions.get(node).get(i).put(event, computedValue);
				}
			}
		} else {
			// Compute proportions recursively for each children node
			for (EventProducerNode child : node.getChildrenNodes()) {
				computeProportions(child);
				for (int i = 0; i < node.getParts().size(); i++) {
					for (String event : getStates()) {
						Double computedValue = proportions.get(node).get(i)
								.get(event)
								+ proportions.get(child).get(i).get(event)
								/ (node.getChildrenNodes().size());
						if (computedValue > amplitudeMax) {
							amplitudeMax = computedValue;
						}
						proportions.get(node).get(i).put(event, computedValue);
					}
				}
			}
		}
	}
	
	/**
	 * Compute the state that has the biggest proportion in the given time
	 * region
	 * 
	 * @param epn
	 *            The considered event producer node
	 * @param start
	 *            the index of the starting slice
	 * @param end
	 *            the index of the ending slice
	 * @return the state with the biggest proportion
	 */
	@Override
	public MainState getMainState(EventProducerNode epn, int start, int end) {
		double max = 0.0;
		MainState maj = new MainState(Void, max);
		for (String event : getStates()) {
			double amp = 0.0;
			// Compute the total presence of the event
			for (int i = start; i < end; i++)
				amp += proportions.get(epn).get(i).get(event);

			// Divide by duration
			amp /= amplitudeMax * (end - start);
			if (amp > max) {
				maj = new MainState(event, amp);
				max = amp;
			}
		}
		return maj;

	}
	
}
