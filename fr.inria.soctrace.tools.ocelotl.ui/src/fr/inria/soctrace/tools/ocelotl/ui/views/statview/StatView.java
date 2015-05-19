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
package fr.inria.soctrace.tools.ocelotl.ui.views.statview;

import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public abstract class StatView implements IStatView,IFramesocBusListener{

	protected OcelotlView	ocelotlView;
	protected StatViewWrapper wrapper;
	protected boolean	dispose;

	public StatView(OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
		dispose=false;
	}


	@Override
	public void init(StatViewWrapper wrapper) {
		this.wrapper=wrapper;
	}

	@Override
	public void resizeDiagram() {
		createDiagram();                                                             
	}
	
	@Override
	public String getStatDataToCSV() {
           return "";                                              
	}

}
