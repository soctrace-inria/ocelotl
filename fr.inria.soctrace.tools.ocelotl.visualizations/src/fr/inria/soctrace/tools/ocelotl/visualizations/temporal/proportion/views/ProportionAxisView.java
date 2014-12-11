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

package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.proportion.views;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.UnitAxisView;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.YAxisMouseListener;
import fr.inria.soctrace.tools.ocelotl.visualizations.temporal.proportion.TemporalProportion;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;

/**
 * Time Axis View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class ProportionAxisView extends UnitAxisView {

	protected double maxValue;
	protected int axisHeight;
	// Margin from the frame
	protected final static int Border = 10;
	protected final static int UnitAxisWidth = 1;
	// Number of graduations to display
	protected double gradNumber = 10.0;
	// Value of a grad
	protected double gradDuration = 10.0;
	// Width of a graduation
	protected int gradWidth = 10;

	protected final static long GradHeightMin = 20;
	protected double gradHeight = 20;
	protected int textWidth = 35;
	protected final static int TextHeight = 20;
	protected final static long MiniDivide = 5;
	// Width of a subgraduation
	protected final static int MiniGradWidth = 4;
	protected final static int TextPositionOffset = 15;
	protected int areaWidth = 0;
	protected int mainLinePosition;

	public ProportionAxisView() {
		super();
		mouse = new YAxisMouseListener(this);
	}

	public void createDiagram(final double maxValue) {
		root.removeAll();
		this.maxValue = maxValue;
		if (maxValue >= 0) {
			drawMainLine();
			drawGrads();
		}
		canvas.update();
	}

	@Override
	public void createDiagram(IVisuOperator manager) {
		root.removeAll();
		TemporalProportion propOperator = (TemporalProportion) manager;
		this.maxValue = propOperator.getMax();
		if (maxValue >= 0.0) {
			drawMainLine();
			drawGrads();
		}
		canvas.update();
	}

	public void drawGrads() {
		NumberFormat formatter = null;
		formatter = java.text.NumberFormat.getInstance(java.util.Locale.US);
		formatter = new DecimalFormat("0.00E0");
		formatter.setMaximumIntegerDigits(3);
		
		axisHeight = root.getSize().height() - 2* Border ;
		areaWidth = root.getClientArea().width();
		textWidth = areaWidth - (areaWidth - mainLinePosition - 2)
				- TextPositionOffset;
		computeGradMeasure();

		// Draw the graduations
		for (int i = 0; i < (int) gradNumber + 1; i++) {
			// Draw the main graduation line
			final PolylineConnection line = new PolylineConnection();
			line.setForegroundColor(SWTResourceManager
					.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			line.setLineWidth(2);
			line.setEndpoints(new Point(mainLinePosition,
					axisHeight - (int) (i * gradHeight) + Border), new Point(mainLinePosition + gradWidth,
							axisHeight - (int) (i * gradHeight) + Border));
			root.add(line);

			// Draw the legend
			final double value = (double) (i * gradDuration);
			final String text = formatter.format(value);
			final Label label = new Label(text);
			label.setLabelAlignment(PositionConstants.RIGHT);
			label.setForegroundColor(SWTResourceManager
					.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			label.setFont(SWTResourceManager.getFont("Cantarell",
					8, SWT.NORMAL));
			label.setToolTip(new Label(text));
			label.setSize(textWidth, TextHeight);

			root.add(label,
					new Rectangle(new Point(mainLinePosition - TextPositionOffset
							- textWidth, axisHeight - (int) (i * gradHeight) + UnitAxisWidth
							+ TextHeight), new Point(new Point(mainLinePosition
							- TextPositionOffset, axisHeight - (int) (i * gradHeight)))));

			// Draw the mini graduations
			for (int j = 1; j < 5; j++) {
				final PolylineConnection line2 = new PolylineConnection();
				if (axisHeight - (int) (i * gradHeight) + Border
						+ (int) (j * gradHeight / MiniDivide) > root.getSize()
						.height() - Border)
					break;

				line2.setForegroundColor(SWTResourceManager
						.getColor(SWT.COLOR_WIDGET_FOREGROUND));
				line2.setLineWidth(1);
				line2.setEndpoints(new Point(
						mainLinePosition,
						axisHeight - (int) (i * gradHeight) + Border
								+ (int) (j * gradHeight / MiniDivide)),
						new Point(new Point(mainLinePosition + MiniGradWidth,
								axisHeight -	(int) (i * gradHeight) + Border
										+ (int) (j * gradHeight / MiniDivide))));
				root.add(line2);
			}
		}
	}

	/**
	 * Draw the main line of the axis
	 */
	public void drawMainLine() {
		mainLinePosition = root.getClientArea().width() - Border - gradWidth;
		final PolylineConnection line = new PolylineConnection();
		line.setForegroundColor(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		line.setLineWidth(2);
		line.setEndpoints(new Point(mainLinePosition, root.getSize().height()
				- Border), new Point(mainLinePosition, Border));
		root.add(line);
	}

	/**
	 * Compute the number and size of graduation to draw
	 */
	public void computeGradMeasure() {
		final double duration = maxValue;

		gradDuration = duration / 10.0;
		gradNumber = duration / gradDuration;
		gradHeight = (root.getClientArea().height - 2 * Border - 1) / gradNumber;
		while (gradHeight < GradHeightMin && gradNumber > 6) {
			gradNumber /= 2;
			gradHeight *= 2;
			gradDuration *= 2;
		}
	}

	public void initDiagram() {
		if (ocelotlView.getMainViewTopSashform().getWeights()[0] == 0) {
			ocelotlView.getMainViewTopSashform().setWeights(
					OcelotlConstants.yAxisDefaultWeight);
			ocelotlView.getMainViewTopSashform().layout();
		}
	}

	public void resizeDiagram() {
		createDiagram(maxValue);
		root.repaint();
	}

	@Override
	public void select(int y0, int y1, boolean active) {
	}

	@Override
	public void unselect() {
	}
}
