package fr.inria.soctrace.tools.ocelotl.ui.views;

import fr.inria.soctrace.tools.ocelotl.core.config.ISpaceConfig;

public interface ISetting2ApplicationWindow {

	public void init(OcelotlView view, ISpaceConfig config);

	public void setBlockOnOpen(boolean b);

}
