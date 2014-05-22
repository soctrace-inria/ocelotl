/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
 * overview by using aggregation techniques
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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

public class VisuConfigViewManager {

	OcelotlView	ocelotlView;

	public VisuConfigViewManager(final OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	public void openConfigWindows() {
		IVisualizationWindow window = null;

		try {
			final Bundle mybundle = Platform.getBundle(ocelotlView.getCore().getSpaceOperators().getSelectedOperatorResource().getBundle());
			window = (IVisualizationWindow) mybundle.loadClass(ocelotlView.getCore().getSpaceOperators().getSelectedOperatorResource().getParamWinClass()).getDeclaredConstructor(Shell.class).newInstance(ocelotlView.getSite().getShell());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | NullPointerException e) {
			// e.printStackTrace();
			return;
		}
		window.init(ocelotlView, ocelotlView.getCore().getOcelotlParameters().getSpaceConfig());
		window.open();

	}

}
