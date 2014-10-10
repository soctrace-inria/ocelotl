package fr.inria.soctrace.tools.ocelotl.visualizations.simplemode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.ISpaceTimeManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.SpaceTimeAggregation2Manager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.visualizations.mode.MajState;

public class SpaceTimeMode extends SimpleMode {
	
	protected ISpaceTimeManager spaceTimeManager;
	HashMap<EventProducerNode, ArrayList<HashMap<String, Double>>> proportions;
	protected EventProducerHierarchy hierarchy;
	protected int timeSliceNumber;
	protected long timeSliceDuration;
	private List<String> states;
	
	public SpaceTimeMode(OcelotlCore ocelotlCore, IDataAggregManager lpaggregManager) {
		this.ocelotlCore = ocelotlCore;
		spaceTimeManager = (ISpaceTimeManager) lpaggregManager;
		hierarchy = spaceTimeManager.getHierarchy();
		timeSliceNumber = ocelotlCore.getOcelotlParameters()
				.getTimeSlicesNumber();
		timeSliceDuration = ocelotlCore.getOcelotlParameters().getTimeRegion()
				.getTimeDuration()
				/ timeSliceNumber;
		computeParts();
	}

	public void computeParts() {
		initParts();
		initStates();
		aggregateStates();
		computeMajStates();
	}
	
	protected void initParts() {
		parts = new ArrayList<Part>();
		//int oldPart = 0;
		parts.add(new Part(0, 1, new PartMap()));
		// Do not show aggregation, use time slice as granularity
		for (int i = 0; i < hierarchy.getRoot().getParts().size(); i++)
			parts.add(new Part(i, i+1, new PartMap()));
			/*if (hierarchy.getRoot().getParts().get(i) == oldPart)
				parts.get(parts.size() - 1).setEndPart(i + 1);
			else {
				oldPart = hierarchy.getRoot().getParts().get(i);
				parts.add(new Part(i, i + 1, new PartMap()));
			}*/

		proportions = new HashMap<EventProducerNode, ArrayList<HashMap<String, Double>>>();
		computeProportions(hierarchy.getRoot());
	}
	
	private void initStates() {
		states = getStates();
		for (final Part part : parts)
			for (final String state : states)
				((PartMap) part.getData()).putElement(state, 0.0);
	}
	
	private void aggregateStates() {
		for (final Part part : parts)
			for (int i = part.getStartPart(); i < part.getEndPart(); i++)
				for (final EventProducerNode epn : hierarchy.getEventProducerNodes().values())
					for (final String state : states)
						((PartMap) part.getData()).addElement(state, proportions.get(epn).get(i).get(state));
								
								
								/*((TimeAggregation3Manager) timeManager)
										.getTimeSliceMatrix().getMatrix()
										.get(i).get(ep).get(state));*/
	}
	
	public void computeMajStates() {
		majStates = new HashMap<Integer, MajState>();
		double max = 0.0;
		double tempMax = 0.0;
		MajState maj;
		int index;
	//	List<String> states = ((TimeAggregation3Manager) ocelotlCore
	//			.getLpaggregManager()).getKeys();

		for (index = 0; index < parts.size(); index++) {
			maj = new MajState("void", max);
			tempMax = 0.0;
			max = 0.0;
			for (String state : states) {
				tempMax = ((PartMap) parts.get(index).getData()).getElements()
						.get(state);
				if (tempMax > max) {
					maj = new MajState(state, tempMax);
					max = tempMax;
				}
			}
			majStates.put(index, maj);
		}
	}
	

	private List<String> getStates() {
		return ((SpaceTimeAggregation2Manager) spaceTimeManager).getKeys();
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
	public MajState getMajState(EventProducerNode epn, int start, int end) {
		hierarchy.getRoot().getParts();
		double max = 0.0;
		MajState maj = new MajState("void", max);
		for (String state : getStates()) {
			double amp = 0.0;
			// Compute the total presence of the state
			for (int i = start; i < end; i++)
				amp += proportions.get(epn).get(i).get(state);

			// Divide by duration
			amp /= (end - start);
			if (amp > max) {
				maj = new MajState(state, amp);
				max = amp;
			}
		}
		return maj;

	}

}
