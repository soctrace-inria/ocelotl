package fr.inria.soctrace.tools.ocelotl.core.query;

import fr.inria.soctrace.lib.model.Event;

public class ReducedEvent extends EventProxy {
	
	public String VALUE;
	public int PAGE;
	public long TS;

	public ReducedEvent(int id, int ep, int page, long ts, String value) {
		super(id, ep);
		this.VALUE=value;
		this.PAGE=page;
		this.TS=ts;
		// TODO Auto-generated constructor stub
	}

}
