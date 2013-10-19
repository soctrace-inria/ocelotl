package fr.inria.soctrace.tools.ocelotl.core.paje.query;


public class PajeReducedEvent extends PajeEventProxy {

	public String	VALUE;
	public int		PAGE;
	public long		TS;

	public PajeReducedEvent(final int id, final int ep, final int page, final long ts, final String value) {
		super(id, ep);
		VALUE = value;
		PAGE = page;
		TS = ts;
		// TODO Auto-generated constructor stub
	}

}
