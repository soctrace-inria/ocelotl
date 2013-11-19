/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

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
