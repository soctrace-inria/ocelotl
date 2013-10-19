package fr.inria.soctrace.tools.ocelotl.ui.views;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import fr.inria.soctrace.tools.ocelotl.core.iaggregop.AggregationOperatorManager;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop.IAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.ui.paje.PajeView;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;
import fr.inria.soctrace.tools.ocelotl.core.paje.config.PajeConfig;

public class ConfigViewManager {

	OcelotlView	ocelotlView;

	public ConfigViewManager(OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	public void openConfigWindows() {
		ApplicationWindow window = null;
		if (ocelotlView.getCore().getOperators().getType(ocelotlView.getComboAggregationOperator().getText()).equals(PajeConstants.PajeFormatName))
			window = new PajeView(ocelotlView, (PajeConfig) ocelotlView.getCore().getOcelotlParameters().getTraceTypeConfig());
		window.setBlockOnOpen(true);
		window.open();

	}

}
