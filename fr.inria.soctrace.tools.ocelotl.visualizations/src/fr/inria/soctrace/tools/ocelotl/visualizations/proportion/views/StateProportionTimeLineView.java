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

package fr.inria.soctrace.tools.ocelotl.visualizations.proportion.views;

import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuTOperator;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.Proportion;

public class StateProportionTimeLineView extends TimeLineView {

	public StateProportionTimeLineView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void computeDiagram() {
			for (int i = 0; i < ((IVisuTOperator) ocelotlView.getCore().getVisuOperator()).getPartNumber(); i++) {
				// TODO manage parts
				final MultiState part = new MultiState(i, (Proportion) ocelotlView.getCore().getVisuOperator(), root, Space);
				part.init();
			}
	}

}
