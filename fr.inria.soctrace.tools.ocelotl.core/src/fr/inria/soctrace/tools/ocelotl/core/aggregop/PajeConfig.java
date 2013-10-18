package fr.inria.soctrace.tools.ocelotl.core.aggregop;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.ui.part.ViewPart;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.PajeView;
import fr.inria.soctrace.tools.ocelotl.ui.views.TraceTypeConfig;

public class PajeConfig implements TraceTypeConfig {
	
	public final static String	DefaultState    = "PajeSetState";
	public final static String	DefaultIdle    = "IDLE";
	private List<String>		idles			= new LinkedList<String>();
	private List<EventType>		types			= new LinkedList<EventType>();
	
	private 					PajeView					pajeView;
	
	
	
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
	@Override
	public void setViewPart(OcelotlView ocelotlView) {
		pajeView = new PajeView(ocelotlView, this);
		
	}
	@Override
	public ViewPart getViewPart() {
		return pajeView;
	}
	
	
	
	
	

}
