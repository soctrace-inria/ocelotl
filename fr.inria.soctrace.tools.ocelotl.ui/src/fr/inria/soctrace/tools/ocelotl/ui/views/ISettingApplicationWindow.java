package fr.inria.soctrace.tools.ocelotl.ui.views;

import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;

public interface ISettingApplicationWindow {

	public void init(OcelotlView view, ITraceTypeConfig config);
	
	public int open();

}
