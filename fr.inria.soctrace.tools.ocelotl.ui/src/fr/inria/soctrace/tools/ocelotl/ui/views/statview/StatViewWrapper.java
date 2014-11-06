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

package fr.inria.soctrace.tools.ocelotl.ui.views.statview;

import org.eclipse.swt.widgets.Composite;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class StatViewWrapper {

	private final OcelotlView				ocelotlView;
	private IStatView						view;

	public StatViewWrapper(final OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	public OcelotlView getOcelotlView() {
		return ocelotlView;
	}

	public IStatView getView() {
		return view;
	}

	public void init(final Composite parent) {

	}

	public void setView(final IStatView view) {
		this.view = view;
		view.init(this);
	}

}
