package fr.inria.soctrace.tools.ocelotl.core.paje.query;

import fr.inria.soctrace.lib.model.Event;

public class PajeReducedEvent extends PajeEventProxy {
	
	public String VALUE;
	public int PAGE;
	public long TS;

	public PajeReducedEvent(int id, int ep, int page, long ts, String value) {
		super(id, ep);
		this.VALUE=value;
		this.PAGE=page;
		this.TS=ts;
		// TODO Auto-generated constructor stub
	}

}
