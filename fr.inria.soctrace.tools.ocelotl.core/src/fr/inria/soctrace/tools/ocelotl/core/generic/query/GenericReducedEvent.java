package fr.inria.soctrace.tools.ocelotl.core.generic.query;

public class GenericReducedEvent extends EventProxy {
	public String	TYPE;
	public int		PAGE;
	public long		TS;

	public GenericReducedEvent(final int id, final int ep, final int page, final long ts, final String type) {
		super(id, ep);
		PAGE = page;
		TS = ts;
		TYPE = type;
	}

}
