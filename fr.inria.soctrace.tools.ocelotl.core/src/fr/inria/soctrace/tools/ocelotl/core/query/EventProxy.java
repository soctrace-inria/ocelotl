package fr.inria.soctrace.tools.ocelotl.core.query;

import fr.inria.soctrace.lib.model.Event;

public class EventProxy {
	public int ID;

	public EventProxy(Event event) {
		ID=event.getId();
	}
	
	public EventProxy(int id) {
		ID=id;
	}
	
	
}
