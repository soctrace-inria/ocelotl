package fr.inria.soctrace.tools.ocelotl.ui.views;

import fr.inria.soctrace.tools.ocelotl.core.config.ISpaceConfig;

public interface IVisualizationWindow {

	public void init(OcelotlView view, ISpaceConfig config);
	
	public int open();

}
