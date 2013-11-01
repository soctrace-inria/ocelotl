package fr.inria.soctrace.tools.ocelotl.core.paje.query;

import fr.inria.soctrace.tools.ocelotl.core.generic.query.EventProxy;

public class PajeReducedEvent1 extends EventProxy {

	public String	VALUE;
	public int		PAGE;
	public long		TS;

	public PajeReducedEvent1(final int id, final int ep, final int page, final long ts, final String value) {
		super(id, ep);
		VALUE = value;
		PAGE = page;
		TS = ts;
		// TODO Auto-generated constructor stub
	}

}
