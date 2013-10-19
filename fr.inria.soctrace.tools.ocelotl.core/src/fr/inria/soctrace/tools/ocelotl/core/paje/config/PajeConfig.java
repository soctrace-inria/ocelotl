package fr.inria.soctrace.tools.ocelotl.core.paje.config;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.ui.part.ViewPart;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.core.TraceTypeConfig;

public class PajeConfig implements TraceTypeConfig {

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

	public List<EventType> getTypes() {
		return types;
	}

	public void setIdles(List<String> idles) {
		this.idles = idles;
	}

	public void setTypes(List<EventType> types) {
		this.types = types;
	}

}
