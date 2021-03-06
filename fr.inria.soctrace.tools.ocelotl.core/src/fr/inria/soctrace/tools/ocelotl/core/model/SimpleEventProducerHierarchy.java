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
package fr.inria.soctrace.tools.ocelotl.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.utils.AlphanumComparator;
import fr.inria.soctrace.lib.model.EventProducer;

public class SimpleEventProducerHierarchy {

	public class SimpleEventProducerNode {
		private int id;
		private String name;
		private EventProducer me;
		private SimpleEventProducerNode parentNode;
		private List<SimpleEventProducerNode> childrenNodes = new ArrayList<SimpleEventProducerNode>();
		// Depth in the hierarchy level of the node (the smaller, the higher
		// in the hierarchy)
		private int hierarchyLevel;
		
		public SimpleEventProducerNode() {
			me = null;
			id = -1;
			name = "abstractRootNode";
			hierarchyLevel = 0;
		}
		
		public SimpleEventProducerNode(EventProducer ep) {
			me = ep;
			id = me.getId();
			epnIndex.put(ep, this);
			name = me.getName();
			orphans.put(id, this);
			leaves.put(id, this);
			setParent();
		}

		public int getID() {
			return id;
		}

		private void setParent() {
			if (!eventProducerNodes.containsKey(me.getParentId())) {
				// If parent exists
				if (eventProducers.containsKey(me.getParentId())
						&& me.getId() != me.getParentId()) {
					eventProducerNodes.put(
							me.getParentId(),
							new SimpleEventProducerNode(eventProducers.get(me
									.getParentId())));
				} else { // It is a root
					parentNode = root;
					root.addChild(this);
					orphans.remove(id);
					return;
				}
			}
			
			parentNode = eventProducerNodes.get(me.getParentId());
			parentNode.addChild(this);
			orphans.remove(id);
			hierarchyLevel = parentNode.getHierarchyLevel() + 1;
			
			if(hierarchyLevel > maxHierarchyLevel)
				maxHierarchyLevel = hierarchyLevel;
		}

		public void addChild(SimpleEventProducerNode child) {
			childrenNodes.add(child);
			if (leaves.containsKey(id))
				leaves.remove(id);
		}

		public EventProducer getMe() {
			return me;
		}

		public SimpleEventProducerNode getParentNode() {
			return parentNode;
		}

		public List<SimpleEventProducerNode> getChildrenNodes() {
			return childrenNodes;
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		public int getHierarchyLevel() {
			return hierarchyLevel;
		}

		public void setHierarchyLevel(int hierarchyLevel) {
			this.hierarchyLevel = hierarchyLevel;
		}

		/**
		 * Sort the children node and all its children
		 */
		public void sortChildrenNodes() {
			Collections.sort(childrenNodes,
					new Comparator<SimpleEventProducerNode>() {
						@Override
						public int compare(SimpleEventProducerNode o1,
								SimpleEventProducerNode o2) {
							return AlphanumComparator.compare(o1.getMe()
									.getName(), o2.getMe().getName());
						}
					});
			for (SimpleEventProducerNode aChildNode : childrenNodes) {
				aChildNode.sortChildrenNodes();
			}
		}

		public void destroy() {
			for (SimpleEventProducerNode child : childrenNodes) {
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
	}

	private Map<Integer, SimpleEventProducerNode> eventProducerNodes = new HashMap<Integer, SimpleEventProducerNode>();
	private Map<EventProducer, SimpleEventProducerNode> epnIndex = new HashMap<EventProducer, SimpleEventProducerNode>();
	private Map<Integer, SimpleEventProducerNode> orphans = new HashMap<Integer, SimpleEventProducerNode>();
	private Map<Integer, SimpleEventProducerNode> leaves = new HashMap<Integer, SimpleEventProducerNode>();
	private Map<Integer, EventProducer> eventProducers = new HashMap<Integer, EventProducer>();
	private static final Logger logger = LoggerFactory.getLogger(SimpleEventProducerHierarchy.class);
	protected int maxHierarchyLevel;
	
	/**
	 * Used as an abstract root node since the hierarchy might have several top-level node
	 */
	private SimpleEventProducerNode root = null;

	public SimpleEventProducerHierarchy(List<EventProducer> eventProducers) {
		super();
		for (EventProducer ep : eventProducers) {
			this.eventProducers.put(ep.getId(), ep);
		}
		root = new SimpleEventProducerNode();
		maxHierarchyLevel = 0;
		setHierarchy();
	}

	private void setHierarchy() {
		for (EventProducer ep : eventProducers.values()) {
			if (!eventProducerNodes.containsKey(ep.getId()))
				eventProducerNodes.put(ep.getId(), new SimpleEventProducerNode(ep));
		}
		
		root.sortChildrenNodes();
		
		if (!orphans.isEmpty()) {
			logger.error("Error: The event producer hierarchy contains elements without a parent.");
			for (Integer orphan : orphans.keySet()) {
				if (orphans.containsKey(orphan))
					orphans.get(orphan).destroy();
			}
		}
	}

	public Map<Integer, SimpleEventProducerNode> getEventProducerNodes() {
		return eventProducerNodes;
	}

	public Map<Integer, SimpleEventProducerNode> getLeaves() {
		return leaves;
	}

	public Map<Integer, SimpleEventProducerNode> getNodes() {
		Map<Integer, SimpleEventProducerNode> nodes = new HashMap<Integer, SimpleEventProducerNode>();
		for (int id : eventProducerNodes.keySet()) {
			if (!leaves.containsKey(id) && root.getID() != id)
				nodes.put(id, eventProducerNodes.get(id));
		}
		return nodes;
	}

	public Map<Integer, EventProducer> getEventProducers() {
		return eventProducers;
	}

	public SimpleEventProducerNode getRoot() {
		return root;
	}
	
	public int getMaxHierarchyLevel() {
		return maxHierarchyLevel;
	}

	public void setMaxHierarchyLevel(int maxHierarchyLevel) {
		this.maxHierarchyLevel = maxHierarchyLevel;
	}

	public Map<EventProducer, SimpleEventProducerNode> getEpnIndex() {
		return epnIndex;
	}

	public void setEpnIndex(Map<EventProducer, SimpleEventProducerNode> epnIndex) {
		this.epnIndex = epnIndex;
	}

	public int getParentID(int id) {
		return eventProducerNodes.get(id).getParentNode().getID();
	}
	
	/**
	 * Check if an event producer is a leaf
	 * 
	 * @param ep
	 *            the tested event producer
	 * @return true if it is a leaf, false otherwise
	 */
	public boolean isLeaf(EventProducer ep) {
		for (SimpleEventProducerNode sepn : leaves.values())
			if (sepn.getMe().getId() == ep.getId())
				return true;

		return false;
	}
	
	/**
	 * Get all the producer nodes of a given level of hierarchy
	 * 
	 * @param hierarchyLevel
	 *            the wanted hierarchy level
	 * @return the list of corresponding event producer nodes
	 */
	public ArrayList<SimpleEventProducerNode> getEventProducerNodesFromHierarchyLevel(
			int hierarchyLevel) {
		ArrayList<SimpleEventProducerNode> selectedEpn = new ArrayList<SimpleEventProducerNode>();

		for (SimpleEventProducerNode epn : eventProducerNodes.values()) {
			if (epn.getHierarchyLevel() == hierarchyLevel)
				selectedEpn.add(epn);
		}
		return selectedEpn;
	}
	
	/**
	 * Get leaf producers that are under a given node in the hierarchy
	 * 
	 * @param aNode
	 *            the node from which we want to get the leaves
	 * @return the  leaves
	 */
	public ArrayList<SimpleEventProducerNode> getLeaves(
			SimpleEventProducerNode aNode) {
		ArrayList<SimpleEventProducerNode> theLeaves = new ArrayList<SimpleEventProducerNode>();

		for (SimpleEventProducerNode aLeaf : leaves.values()) {
			SimpleEventProducerNode parent = aLeaf.getParentNode();
			while (parent != aNode && parent != root && parent != null) {
				parent = parent.getParentNode();
			}

			if (parent == aNode)
				theLeaves.add(aLeaf);
		}

		return theLeaves;
	}
	
	/**
	 * Recursively get all the children node (down to the leaves) of a node
	 * 
	 * @param aNode
	 *            the node from which we want all children
	 * @return a collection containing all the children of a node plus itself
	 */
	public ArrayList<SimpleEventProducerNode> getAllChildrenNodes(
			SimpleEventProducerNode aNode) {
		ArrayList<SimpleEventProducerNode> children = new ArrayList<SimpleEventProducerNode>();
		children.add(aNode);

		for (SimpleEventProducerNode aChild : aNode.getChildrenNodes()) {
			children.addAll(getAllChildrenNodes(aChild));
		}

		return children;
	}
}
