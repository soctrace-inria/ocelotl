package fr.inria.soctrace.tools.ocelotl.ui.views;

import fr.inria.soctrace.tools.ocelotl.core.generic.config.ITraceTypeConfig;

public interface ISettingApplicationWindow{
	
	public void init(OcelotlView view, ITraceTypeConfig config);

	public void setBlockOnOpen(boolean b);


}
