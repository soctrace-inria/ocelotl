package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.EventProducer;

public class EventProducerHierarchy {
	
	private class EventProducerNode{

		EventProducer me;
		EventProducer parent;
		List<EventProducer> childrens = new ArrayList<EventProducer>();
		
		public EventProducerNode(EventProducer ep) {
			me=ep;
		}
		
		public int getID(){
			return me.getId();
		}
		
		public void addParent(EventProducer parent){
			this.parent=parent;
		}
		
		public void addChild(EventProducer child){
			childrens.add(child);
		}

		public EventProducer getMe() {
			return me;
		}

		public void setMe(EventProducer me) {
			this.me = me;
		}

		public EventProducer getParent() {
			return parent;
		}

		public void setParent(EventProducer parent) {
			this.parent = parent;
		}

		public List<EventProducer> getChildrens() {
			return childrens;
		}

		public void setChildrens(List<EventProducer> childrens) {
			this.childrens = childrens;
		}
		
		
		
	}
	
	Map<Integer, EventProducer> eventProducers = new HashMap<Integer, EventProducer>();
	List<>

	public EventProducerHierarchy(List<EventProducer> eventProducers) {
		super();
		for (EventProducer ep: eventProducers)
			this.eventProducers.put(ep.getId(), ep);
			
	}
	
	
	

}
