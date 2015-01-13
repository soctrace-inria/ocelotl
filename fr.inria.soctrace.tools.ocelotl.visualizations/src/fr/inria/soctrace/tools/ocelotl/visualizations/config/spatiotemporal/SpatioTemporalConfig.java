package fr.inria.soctrace.tools.ocelotl.visualizations.config.spatiotemporal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.core.config.IVisuConfig;
import fr.inria.soctrace.tools.ocelotl.visualizations.temporal.proportion.views.EventColorManager;

public class SpatioTemporalConfig implements IVisuConfig {

	protected List<EventType>	types	= new LinkedList<EventType>();
	protected EventColorManager colors;

	public SpatioTemporalConfig() {
		super();
	}
	
	public List<EventType> getTypes() {
		return types;
	}
	
	public List<String> getTypeNames() {
		List<String> l = new ArrayList<String>();
		for (EventType et: types){
			l.add(et.getName());
		}
		return l;
	}

	public void setTypes(final List<EventType> types) {
		this.types = types;
	}

	public EventColorManager getColors() {
		return colors;
	}

	public void initColors() {
		this.colors = new EventColorManager();
	}
}
