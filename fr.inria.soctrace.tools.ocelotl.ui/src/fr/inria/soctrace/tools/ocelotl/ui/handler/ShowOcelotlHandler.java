/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.ui.handler;

import fr.inria.soctrace.framesoc.ui.handlers.ShowTraceHandler;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class ShowOcelotlHandler extends ShowTraceHandler {

	private final static String VIEW_NAME = "Ocelotl";
	
	@Override
	public String getViewId() {
		return OcelotlView.ID;
	}

	@Override
	public String getViewName() {
		return VIEW_NAME;
	}

}
