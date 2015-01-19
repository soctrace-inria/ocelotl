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

package fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.framesoc.ui.utils.AlphanumComparator;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.Microscopic3DDescription;

public class EventProducerHierarchy {

	public enum Aggregation {
		FULL, PARTIAL, NULL
	}

	public class EventProducerNode {
		private int id;
		private EventProducer me;
		private EventProducerNode parentNode;
		private List<EventProducerNode> childrenNodes = new ArrayList<EventProducerNode>();
		private List<Integer> parts;
		// Number of leaf event producers in the node
		private int weight = 1;
		private Aggregation aggregated = Aggregation.NULL;
		private Object values;
		private int index;
		// Depth in the hierarchy level of the node (the smaller, the higher
		// in the hierarchy)
		private int hierarchyLevel;
		
		public EventProducerNode(EventProducer ep) {
			if(ep == null)
				throw new NullPointerException();
			
			me = ep;
			id = me.getId();
			orphans.put(id, this);
			leaves.put(id, this);
			hierarchyLevel = 0;
			setParent();
		}

		public Aggregation isAggregated() {
			return aggregated;
		}

		public int getWeight() {
			return weight;
		}

		public List<Integer> getParts() {
			return parts;
		}

		public void setParts(List<Integer> parts) {
			this.parts = parts;
			if (!parts.contains(-1))
				aggregated = Aggregation.FULL;
			else {
				aggregated = Aggregation.NULL;
				for (int part : parts) {
					if (part != -1) {
						aggregated = Aggregation.PARTIAL;
					}
				}
			}
		}

		public int getID() {
			return id;
		}

		private void setParent() {
			try {
				if (!eventProducerNodes.containsKey(me.getParentId()))
					// Is the parent id is a known producer
					if (eventProducers.containsKey(me.getParentId())) {
						eventProducerNodes.put(
								me.getParentId(),
								new EventProducerNode(eventProducers.get(me
										.getParentId())));
					} else {
						// If not make it root (which has no parent)
						parentNode = null;
						if (root == null) {
							root = this;
							orphans.remove(id);
						}
						return;
					}

				parentNode = eventProducerNodes.get(me.getParentId());
				parentNode.addChild(this);
				hierarchyLevel = parentNode.getHierarchyLevel() + 1;
				
				if(hierarchyLevel > maxHierarchyLevel)
					maxHierarchyLevel = hierarchyLevel;
				
				orphans.remove(id);
			} catch (NullPointerException e) {
				parentNode = null;
				if (root == null) {
					root = this;
					orphans.remove(id);
				}
			}
		}

		public void addChild(EventProducerNode child) {
			childrenNodes.add(child);
			if (leaves.containsKey(id))
				leaves.remove(id);
		}

		public EventProducer getMe() {
			return me;
		}

		public EventProducerNode getParentNode() {
			return parentNode;
		}

		public List<EventProducerNode> getChildrenNodes() {
			return childrenNodes;
		}

		/**
		 * Sort children nodes alphabetically
		 */
		public void sortChildrenNodes() {
			Collections.sort(childrenNodes,
					new Comparator<EventProducerNode>() {
						@Override
						public int compare(EventProducerNode o1,
								EventProducerNode o2) {
							return AlphanumComparator.compare(o1.getMe()
									.getName(), o2.getMe().getName());
						}
					});
		}

		public void destroy() {
			for (EventProducerNode child : childrenNodes) {
				child.destroy();
			}
			eventProducerNodes.remove(id);
			if (orphans.containsKey(id))
				orphans.remove(id);
			if (leaves.containsKey(id))
				leaves.remove(id);
			eventProducers.remove(id);
			childrenNodes.clear();
		}

		public Object getValues() {
			return values;
		}

		public void setValues(Object values) {
			if (leaves.containsKey(id))
				this.values = values;
			else
				values = null;
		}

		public void setParentValues(Object values) {
			if (!leaves.containsKey(id))
				this.values = values;
			else
				values = null;
		}

		/**
		 * Compute the weight (number of leaves in the node) for the node and
		 * recursively for all its children
		 * 
		 * @return the newly computed weight
		 */
		public int setWeight() {
			if (childrenNodes.isEmpty()) {
				// If it is an aggregated leave
				if (microModel3D.getAggregatedProducers()
						.containsValue(me)) {
					weight = microModel3D
							.removeFilteredEP(
									microModel3D
											.getOcelotlParameters()
											.getEventProducerHierarchy()
											.getLeaves(
													microModel3D
															.getOcelotlParameters()
															.getEventProducerHierarchy()
															.getEventProducerNodes()
															.get(this.getMe()
																	.getId())))
							.size();
					
					aggLeaves.add(this);
				}

				return weight;
			} else
				weight = 0;

			for (EventProducerNode epn : childrenNodes) {
				weight += epn.setWeight();
			}
			
			return weight;
		}

		/**
		 * Recursively compute the index of the node based on the sum of the
		 * weights of the previous children so that each node indicates the
		 * previous leaves in the sorting order (currently alphabetical)
		 */
		public void setChildIndex() {
			if (this == root) {
				index = 0;
			}
			sortChildrenNodes();
			int currentweight = 0;
			for (EventProducerNode e : childrenNodes) {
				e.setIndex(currentweight + index);
				e.setChildIndex();
				currentweight += e.getWeight();
			}
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}
		
		public int getHierarchyLevel() {
			return hierarchyLevel;
		}

		public void setHierarchyLevel(int hierarchyLevel) {
			this.hierarchyLevel = hierarchyLevel;
		}

		/**
		 * Check whether or not the current epn contain another epn given as parameter
		 * 
		 * @param anEpn
		 * @return true if is the same or one of the children is the same, false
		 *         otherwise
		 */
		public boolean contains(EventProducerNode anEpn) {
			if (this == anEpn)
				return true;

			if (!childrenNodes.isEmpty()) {
				for (EventProducerNode epn : childrenNodes) {
					if (epn.contains(anEpn))
						return true;
				}
			}
			return false;
		}
		
		/**
		 * Provide all the children nodes that contain the epn given as input
		 * 
		 * @param epns
		 *            List of epn
		 * @return The list of all the epn containing all the epn provided in
		 *         parameters
		 */
		public List<EventProducerNode> containsAll(List<EventProducerNode> epns) {
			ArrayList<EventProducerNode> containingEpn = new ArrayList<EventProducerNode>();
			boolean containsAll = true;
			
			// Check if we contains all
			for (EventProducerNode anEpn : epns) {
				if (!contains(anEpn)) {
					containsAll = false;
					break;
				}
			}

			// And if so, add ourself
			if (containsAll)
				containingEpn.add(this);

			// Recursively check for the children nodes
			if (!childrenNodes.isEmpty()) {
				for (EventProducerNode epnChild : this.getChildrenNodes()) {
					containingEpn.addAll(epnChild.containsAll(epns));
				}
			}

			return containingEpn;
		}
		
		/**
		 * Check whether or not a node and its children is included within the
		 * given boundaries
		 * 
		 * @param start
		 *            the starting boundary
		 * @param end
		 *            the ending boundaries
		 * @return The list of nodes contains within the given boundaries
		 */
		public List<EventProducerNode> withinBoundary(int start, int end) {
			ArrayList<EventProducerNode> containedEpn = new ArrayList<EventProducerNode>();
			
			// Check if we are within the boundaries
			if ((index + weight >= start && index + weight <= end)
					|| (index >= start && index <= end))
				containedEpn.add(this);

			// Recursively check for the children nodes
			if (!childrenNodes.isEmpty()) {
				for (EventProducerNode epnChild : this.getChildrenNodes()) {
					containedEpn.addAll(epnChild.withinBoundary(start, end));
				}
			}

			return containedEpn;
		}

		/**
		 * Search for all the event producers in the hierarchy of the current
		 * epn (including itself)
		 * 
		 * @return the list of all the found event producers
		 */
		public ArrayList<EventProducer> getContainedProducers() {
			ArrayList<EventProducer> producers = new ArrayList<EventProducer>();
			producers.add(this.me);

			// Recursively get producers of the children nodes
			if (!childrenNodes.isEmpty()) {
				for (EventProducerNode epnChild : this.getChildrenNodes()) {
					producers.addAll(epnChild.getContainedProducers());
				}
			}

			return producers;
		}
	}

	private Map<Integer, EventProducerNode> eventProducerNodes = new HashMap<Integer, EventProducerNode>();
	private Map<Integer, EventProducerNode> orphans = new HashMap<Integer, EventProducerNode>();
	private Map<Integer, EventProducerNode> leaves = new HashMap<Integer, EventProducerNode>();
	private Map<Integer, EventProducer> eventProducers = new HashMap<Integer, EventProducer>();
	private ArrayList<EventProducerNode> aggLeaves = new ArrayList<EventProducerNode>();
	private EventProducerNode root = null;
	protected int maxHierarchyLevel;
	protected Microscopic3DDescription microModel3D;

	public EventProducerHierarchy(List<EventProducer> eventProducers,
			Microscopic3DDescription microModel3D) throws OcelotlException {
		super();

		for (EventProducer ep : eventProducers) {
			this.eventProducers.put(ep.getId(), ep);
		}

		root = null;
		this.microModel3D = microModel3D;
		maxHierarchyLevel = 0;
		setHierarchy();
	}

	private void setHierarchy() throws OcelotlException {
		for (EventProducer ep : eventProducers.values()) {
			if (!eventProducerNodes.containsKey(ep.getId()))
				eventProducerNodes.put(ep.getId(), new EventProducerNode(ep));
		}
		
		// If there are some node with no parent
		if (!orphans.isEmpty()) {
			throw new OcelotlException(OcelotlException.INCOMPLETE_HIERARCHY);
		}
		root.setWeight();
		root.setChildIndex();
	}

	public void setParts(EventProducer ep, List<Integer> parts) {
		eventProducerNodes.get(ep.getId()).setParts(parts);
	}

	public Map<Integer, EventProducerNode> getEventProducerNodes() {
		return eventProducerNodes;
	}

	public Map<Integer, EventProducerNode> getLeaves() {
		return leaves;
	}

	public Map<Integer, EventProducerNode> getNodes() {
		Map<Integer, EventProducerNode> nodes = new HashMap<Integer, EventProducerNode>();
		for (int id : eventProducerNodes.keySet()) {
			if (!leaves.containsKey(id) && root.getID() != id)
				nodes.put(id, eventProducerNodes.get(id));
		}
		return nodes;
	}

	public Map<Integer, EventProducer> getEventProducers() {
		return eventProducers;
	}

	public EventProducerNode getRoot() {
		return root;
	}

	public void setValues(HashMap<EventProducer, Object> values) {
		for (EventProducer ep : values.keySet())
			eventProducerNodes.get(ep.getId()).setValues(values.get(ep));
	}

	public void setValues(EventProducer ep, Object values) {
		eventProducerNodes.get(ep.getId()).setValues(values);
	}

	public void setParentValues(EventProducer ep, Object values) {
		eventProducerNodes.get(ep.getId()).setParentValues(values);
	}

	public void setParts(int id, List<Integer> parts) {
		eventProducerNodes.get(id).setParts(parts);
	}

	public int getParentID(int id) {
		return eventProducerNodes.get(id).getParentNode().getID();
	}

	public Object getValues(int id) {
		return eventProducerNodes.get(id).getValues();
	}

	public int getMaxHierarchyLevel() {
		return maxHierarchyLevel;
	}

	public void setMaxHierarchyLevel(int maxHierarchyLevel) {
		this.maxHierarchyLevel = maxHierarchyLevel;
	}

	/**
	 * Get all the producer nodes of a given level of hierarchy
	 * 
	 * @param hierarchyLevel
	 *            the wanted hierarchy level
	 * @return the list of corresponding event producer nodes
	 */
	public ArrayList<EventProducerNode> getEventProducerNodesFromHierarchyLevel(
			int hierarchyLevel) {
		ArrayList<EventProducerNode> selectedEpn = new ArrayList<EventProducerNode>();

		for (EventProducerNode epn : eventProducerNodes.values()) {
			if (epn.getHierarchyLevel() == hierarchyLevel)
				selectedEpn.add(epn);
		}
		return selectedEpn;
	}

	/**
	 * Find the node the lowest in the hierarchy that contains all the event
	 * producer nodes given as arguments
	 * 
	 * @param epns
	 *            List of event producer nodes that we want to embedded
	 * @return The found event producer node
	 */
	public EventProducerNode findSmallestContainingNode(
			List<EventProducerNode> epns) {
		ArrayList<EventProducerNode> containingEpn = new ArrayList<EventProducerNode>();
		containingEpn.addAll(root.containsAll(epns));

		EventProducerNode smallestContainingNode = containingEpn.get(0);

		// Find the deepest epn in the hierarchy (should be unique)
		for (EventProducerNode epn : containingEpn) {
			if (epn.getHierarchyLevel() > smallestContainingNode
					.getHierarchyLevel())
				smallestContainingNode = epn;
		}

		return smallestContainingNode;
	}
	
	/**
	 * Search for all the leave nodes that are within the given boundaries
	 * 
	 * @param start
	 *            the starting boundary
	 * @param end
	 *            the ending boundaries
	 * @return The list of nodes contains within the given boundaries
	 */
	public ArrayList<EventProducerNode> findNodeWithin(int start, int end) {
		ArrayList<EventProducerNode> containedEpn = new ArrayList<EventProducerNode>();
		
		for (EventProducerNode epn : leaves.values()) {
			if ((epn.index + epn.weight > start && epn.index + epn.weight < end)
					|| (epn.index >= start && epn.index <= end))
				containedEpn.add(epn);
		}

		if (containedEpn.isEmpty()) {
			for (EventProducerNode epn : leaves.values()) {
				if (start >= epn.index && end <= epn.index + epn.weight)
					containedEpn.add(epn);
			}
		}
		return containedEpn;
	}

}
