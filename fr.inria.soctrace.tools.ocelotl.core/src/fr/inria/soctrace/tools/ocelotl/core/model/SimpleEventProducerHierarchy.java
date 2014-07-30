package fr.inria.soctrace.tools.ocelotl.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.EventProducer;

public class SimpleEventProducerHierarchy {

	public class SimpleEventProducerNode {

		private int id;
		private String name;
		private EventProducer me;
		private SimpleEventProducerNode parentNode;
		private List<SimpleEventProducerNode> childrenNodes = new ArrayList<SimpleEventProducerNode>();

		public SimpleEventProducerNode() {
			me = null;
			id = -1;
			name = "abstractRootNode";
		}

		
		public SimpleEventProducerNode(EventProducer ep) {
			me = ep;
			id = me.getId();
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
				if (eventProducers.containsKey(me.getParentId()) && me.getId() != me.getParentId()) {
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

		public void sortChildrenNodes() {
			Collections.sort(childrenNodes,
					new Comparator<SimpleEventProducerNode>() {
						@Override
						public int compare(SimpleEventProducerNode arg0,
								SimpleEventProducerNode arg1) {
							return arg0
									.getMe()
									.getName()
									.compareToIgnoreCase(arg1.getMe().getName());
						}
					});
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
	private Map<Integer, SimpleEventProducerNode> orphans = new HashMap<Integer, SimpleEventProducerNode>();
	private Map<Integer, SimpleEventProducerNode> leaves = new HashMap<Integer, SimpleEventProducerNode>();
	private Map<Integer, EventProducer> eventProducers = new HashMap<Integer, EventProducer>();
	
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
		setHierarchy();
	}

	private void setHierarchy() {
		for (EventProducer ep : eventProducers.values()) {
			if (!eventProducerNodes.containsKey(ep.getId()))
				eventProducerNodes.put(ep.getId(), new SimpleEventProducerNode(ep));
		}
		if (!orphans.isEmpty()) {
			System.err
					.println("Careful: hierarchy is incomplete and some elements will be destroyed!");
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

	public int getParentID(int id) {
		return eventProducerNodes.get(id).getParentNode().getID();
	}
}
