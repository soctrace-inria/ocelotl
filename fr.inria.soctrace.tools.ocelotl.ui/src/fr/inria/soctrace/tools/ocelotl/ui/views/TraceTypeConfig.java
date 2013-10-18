package fr.inria.soctrace.tools.ocelotl.ui.views;

import java.util.List;

import org.eclipse.ui.part.ViewPart;

import fr.inria.soctrace.lib.model.EventType;

public interface TraceTypeConfig {
	
	
	public void setViewPart(OcelotlView ocelotlView);
	public ViewPart getViewPart();
	public List<EventType> getTypes();

}
