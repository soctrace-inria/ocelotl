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

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public abstract class StatView implements IStatView {

	protected OcelotlView	ocelotlView;

	public StatView(OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	@Override
	public void createDiagram() {

	}

	@Override
	public void deleteDiagram() {
	}

	@Override
	public void init(StatViewWrapper wrapper) {

	}

	@Override
	public void resizeDiagram() {
		createDiagram();                                                             
	}

}
