package fr.inria.soctrace.tools.ocelotl.core.query;

import fr.inria.soctrace.lib.model.Event;

public class EventProxy {
	public int ID;
	public int EP;

	public EventProxy(Event event) {
		ID=event.getId();
		EP=event.getEventProducer().getId();
	}
	
	public EventProxy(int id, int ep) {
		ID=id;
		EP=ep;
	}
	
	
}
