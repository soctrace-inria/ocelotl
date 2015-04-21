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

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.MainEvent;

public class ModeFigure extends RectangleFigure {

	public ModeFigure() {
		super();
	}

	// Draw the part visualization of the aggregates
	public void draw(MainEvent mainEvent) {
		removeAll();
		setPreferredSize(1000, 1000);
		final Label label = new Label(" " + mainEvent.getState() + ": "
				+ mainEvent.getAmplitude100() + "% ");
		setToolTip(label);
		final ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
		setLayoutManager(layout);
		setLineWidth(0);

		setForegroundColor(FramesocColorManager.getInstance()
				.getEventTypeColor(mainEvent.getState()).getSwtColor());
		setBackgroundColor(FramesocColorManager.getInstance()
				.getEventTypeColor(mainEvent.getState()).getSwtColor());
		setAlpha(mainEvent.getAmplitude255Shifted());
	}
}
