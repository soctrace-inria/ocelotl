package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;

public class SpatiotemporalModeStates extends SpatiotemporalMode {

	//protected HashMap<EventProducerNode, ArrayList<ArrayList<Double>>> amplitudeMax;
	protected Double amplitudeMax;
	
	public SpatiotemporalModeStates() {
		super();
	}
	
	public SpatiotemporalModeStates(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
	}

	@SuppressWarnings("unchecked")
	protected void computeProportions(EventProducerNode node) {
		// Init for the current producer node
		proportions.put(node, new ArrayList<HashMap<String, Double>>());
		// Init for each part of the node
		for (int i = 0; i < node.getParts().size(); i++) {
			proportions.get(node).add(new HashMap<String, Double>());
			// And for each state of the part
			for (String state : getStates())
				// Init to zero
				proportions.get(node).get(i).put(state, 0.0);
		}
		// If node is a leaf
		if (node.getChildrenNodes().isEmpty()) {
			for (int i = 0; i < node.getParts().size(); i++) {
				for (String state : getStates())
					// Add value (= value / time slice duration)
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
			// Compute proportions recursively for each children node
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
	public MainEvent getMainEvent(EventProducerNode epn, int start, int end) {
		double max = 0.0;
		MainEvent maj = new MainEvent(Void, max);
		for (String state : getStates()) {
			double amp = 0.0;
			// Compute the total presence of the state
			for (int i = start; i < end; i++)
				amp += proportions.get(epn).get(i).get(state);

			// Divide by duration
			amp /= (end - start);
			if (amp > max) {
				maj = new MainEvent(state, amp);
				max = amp;
			}
		}
		return maj;

	}
	
}