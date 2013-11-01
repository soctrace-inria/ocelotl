package fr.inria.soctrace.tools.ocelotl.core.paje.config;

import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.core.generic.config.ITraceTypeConfig;

public class PajeConfig implements ITraceTypeConfig {

	public final static String	DefaultState	= "PajeSetState";
	public final static String	DefaultIdle		= "IDLE";
	private List<String>		idles			= new LinkedList<String>();
	private List<EventType>		types			= new LinkedList<EventType>();

	public PajeConfig() {
		super();
	}

	public List<String> getIdles() {
		return idles;
	}

	@Override
	public List<EventType> getTypes() {
		return types;
	}

	public void setIdles(final List<String> idles) {
		this.idles = idles;
	}

	public void setTypes(final List<EventType> types) {
		this.types = types;
	}

}
