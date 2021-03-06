/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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

package fr.inria.soctrace.tools.ocelotl.ui;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * The main tool class
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class OcelotlTool extends FramesocTool {

	private static final Logger logger = LoggerFactory.getLogger(FramesocTool.class);

	public OcelotlTool() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void launch(IFramesocToolInput input) {
		logger.debug("Arguments");

		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			window.getActivePage().showView(OcelotlView.ID);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}


}
