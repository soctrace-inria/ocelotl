package fr.inria.soctrace.tools.ocelotl.ui.views;

import org.eclipse.jface.window.ApplicationWindow;

import fr.inria.soctrace.tools.ocelotl.core.paje.config.PajeConfig;
import fr.inria.soctrace.tools.ocelotl.ui.paje.PajeView;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;

public class ConfigViewManager {

	OcelotlView	ocelotlView;

	public ConfigViewManager(final OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	public void openConfigWindows() {
		ApplicationWindow window = null;
		if (ocelotlView.getCore().getTimeOperators().getType(ocelotlView.getComboAggregationOperator().getText()).equals(PajeConstants.PajeFormatName))
			window = new PajeView(ocelotlView, (PajeConfig) ocelotlView.getCore().getOcelotlParameters().getTraceTypeConfig());
		window.setBlockOnOpen(true);
		window.open();

	}

}
