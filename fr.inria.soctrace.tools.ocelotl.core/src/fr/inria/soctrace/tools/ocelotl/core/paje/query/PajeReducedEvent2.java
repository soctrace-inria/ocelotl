package fr.inria.soctrace.tools.ocelotl.core.paje.query;

public class PajeReducedEvent2 extends PajeReducedEvent1 {
	public String	TYPE;

	public PajeReducedEvent2(final int id, final int ep, final int page, final long ts, final String type, final String value) {
		super(id, ep, page, ts, value);
		TYPE = type;
		// TODO Auto-generated constructor stub
	}

}
