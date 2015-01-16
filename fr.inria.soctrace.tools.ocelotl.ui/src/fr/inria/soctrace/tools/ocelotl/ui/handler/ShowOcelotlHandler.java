package fr.inria.soctrace.tools.ocelotl.ui.handler;

import fr.inria.soctrace.framesoc.ui.handlers.ShowTraceHandler;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class ShowOcelotlHandler extends ShowTraceHandler {

	@Override
	public String getViewId() {
		return OcelotlView.ID;
	}

}
