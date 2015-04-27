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
package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.mode.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.MainEvent;
import fr.inria.soctrace.tools.ocelotl.visualizations.temporal.mode.TemporalMode;

public class TemporalModeView extends TimeLineView {

	public TemporalModeView(OcelotlView ocelotlView) {
		super(ocelotlView);
	}

	@Override
	protected void computeDiagram() {
		int i;
		final List<Integer> aggParts = new ArrayList<Integer>();
		final double drawingAreaWidth = root.getSize().width - 2 * aBorder;
		final double partWidth = drawingAreaWidth / parts.size();
		for (i = 0; i <= parts.get(parts.size() - 1); i++)
			aggParts.add(0);
		for (i = 0; i < parts.size(); i++)
			aggParts.set(parts.get(i), aggParts.get(parts.get(i)) + 1);
		int j = 0;
		root.removeAll();
		for (i = 0; i < aggParts.size(); i++) {
			final ModeFigure part = new ModeFigure();
			part.getUpdateManager().performUpdate();
			MainEvent mState = ((TemporalMode) visuOperator).getMajStates()
					.get(i);
			if (!mState.getState().equals("void")) {
				part.draw(mState);
				figures.add(part);
				root.add(part, new Rectangle(new PrecisionPoint((j * partWidth)
						+ aBorder, root.getSize().height - aBorder), new PrecisionPoint(
						((j + aggParts.get(i)) * partWidth) - space
								+ aBorder, aBorder)));
			}
			j = j + aggParts.get(i);
		}
		root.validate();
	}

}
