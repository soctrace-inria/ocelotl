package fr.inria.soctrace.tools.ocelotl.core.paje.query;

public class PajeReducedEvent2 extends PajeReducedEvent1 {
	String TYPE;

	public PajeReducedEvent2(int id, int ep, int page, long ts, String type, String value) {
		super(id, ep, page, ts, value);
		TYPE=type;
		// TODO Auto-generated constructor stub
	}

}
