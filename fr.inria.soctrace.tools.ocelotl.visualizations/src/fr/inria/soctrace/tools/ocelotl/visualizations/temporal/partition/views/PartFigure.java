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

package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.partition.views;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

public class PartFigure extends RectangleFigure {

	private int index;
	private final int value;
	private final PartitionColor color;
	private final static int textSize = 15;
	private final boolean numbers;

	public PartFigure(final int index, final int value,
			final PartitionColor color, final boolean numbers) {
		super();
		setIndex(index);
		this.value = value;
		this.color = color;
		this.numbers = numbers;
	}

	public int getIndex() {
		return index;
	}

	// Draw the part visualization of the aggregates
	public void init() {
		removeAll();
		final RoundedRectangle roundedRectangle = new RoundedRectangle();
		roundedRectangle.setBackgroundColor(color.getBg());
		roundedRectangle.setForegroundColor(color.getBg());
		roundedRectangle.setLineWidth(15);
		final ToolbarLayout roundedLayout = new ToolbarLayout();
		roundedRectangle.setLayoutManager(roundedLayout);
		roundedRectangle.setPreferredSize(1000, 1000);
		this.add(roundedRectangle);
		final int dim = 0;
		roundedRectangle.setCornerDimensions(new Dimension(dim, dim));
		final Label label = new Label("" + value);
		label.setLabelAlignment(SWT.CENTER);
		label.setForegroundColor(color.getFg());
		roundedRectangle.setFont(SWTResourceManager.getFont("Cantarell",
				textSize, SWT.BOLD));
		if (numbers)
			if (getSize().width / 2 - 3 > textSize
					&& getSize().height / 2 - 3 > textSize)
				roundedRectangle.add(label);
		final ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
		setConstraint(roundedRectangle, getBounds());
		setLayoutManager(layout);
	}

	public void setIndex(final int index) {
		this.index = index;
	}

}