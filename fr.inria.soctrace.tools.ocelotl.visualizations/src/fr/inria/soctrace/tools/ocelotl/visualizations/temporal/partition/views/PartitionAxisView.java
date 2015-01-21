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
package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.partition.views;

import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.UnitAxisView;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.YAxisMouseListener;

public class PartitionAxisView extends UnitAxisView {
	
	public PartitionAxisView() {
		super();
		mouse = new YAxisMouseListener(this);
	}
	
	@Override
	public void createDiagram(IVisuOperator manager) {
	}

	@Override
	public void resizeDiagram() {
	
	}

	@Override
	public void initDiagram() {
		ocelotlView.getMainViewTopSashform().setWeights(new int[] { 0, 100 });
	}

	@Override
	public void select(int y0, int y1, boolean active) {

	}

	@Override
	public void unselect() {

	}

}
