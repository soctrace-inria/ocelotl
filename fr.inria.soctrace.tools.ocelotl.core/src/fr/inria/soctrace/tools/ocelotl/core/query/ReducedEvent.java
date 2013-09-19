package fr.inria.soctrace.tools.ocelotl.core.query;

import fr.inria.soctrace.lib.model.Event;

public class ReducedEvent extends EventProxy {
	
	public
	String VALUE;

	public ReducedEvent(int id, int ep, String value) {
		super(id, ep);
		this.VALUE=value;
		// TODO Auto-generated constructor stub
	}
	
	public ReducedEvent(Event event, String value){
		super(event);
		this.VALUE=value;
	}

}
