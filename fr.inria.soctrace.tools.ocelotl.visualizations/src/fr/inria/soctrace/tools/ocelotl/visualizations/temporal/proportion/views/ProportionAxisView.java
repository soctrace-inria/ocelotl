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
import org.eclipse.swt.graphics.GC;
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

	// Maximal value of the aggregation
	protected double maxValue;
	protected int axisHeight;
	// Height of the drawing of the axis
	protected double drawingHeight;
	// Number of graduations to display
	protected double gradNumber = 10.0;
	// Value of a graduation
	protected double gradDuration = 10.0;
	// Width of a graduation
	protected int gradWidth = 10;
	// Height of a graduation
	protected double gradHeight = 20;
	// Width of a text
	protected int textWidth = 35;
	// Width if the drawing area
	protected int areaWidth = 0;
	// X coordinate of the main axis line
	protected int mainLineXPosition;
	// Max size of a label
	protected int labelMaxWidth;
	
	// Margin from the frame
	protected final static int Border = 10;
	// Minimal height of a graduation
	protected final static long GradHeightMin = 20;
	// Height of the text
	protected final static int TextHeight = 20;
	// Width of a subgraduation
	protected final static int MiniGradWidth = 4;
	// Offset on X to draw the text 
	protected final static int TextPositionOffset = 15;
	// Margin ratio for drawing (let a space on top of the drawing (coordination
	// with proportion view)
	private static final double DrawingMarginRatio = OcelotlConstants.TemporalProportionDrawingMarginRatio;
	// The number of sub graduations per graduation
	private static final int NumberOfSubGraduation = 5;


	public ProportionAxisView() {
		super();
		mouse = new YAxisMouseListener(this);
	}

	public void createDiagram(final double maxValue) {
		root.removeAll();
		this.maxValue = maxValue;
		if (maxValue > 0.0) {
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
		if (maxValue > 0.0) {
			drawMainLine();
			drawGrads();
			resizeAxis();
		}
		canvas.update();
	}

	/**
	 * Draw the graduations and their labels
	 */
	public void drawGrads() {
		NumberFormat formatter = null;
		formatter = java.text.NumberFormat.getInstance(java.util.Locale.US);
		formatter = new DecimalFormat("0.00E0");
		formatter.setMaximumIntegerDigits(3);

		computeMeasurements();

		// Draw the graduations
		for (int i = 0; i < (int) gradNumber + 1; i++) {
			// Draw the main graduation line
			final PolylineConnection line = new PolylineConnection();
			line.setForegroundColor(SWTResourceManager
					.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			line.setLineWidth(2);
			line.setEndpoints(new Point(mainLineXPosition, (int) axisHeight
					- (int) (i * gradHeight)), new Point(mainLineXPosition
					+ gradWidth, (int) axisHeight - (int) (i * gradHeight)));
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
			
			// Compute label width
			GC gc = new GC(canvas);
			gc.setFont(label.getFont());
			int labelWidth = gc.textExtent(label.getText()).x;
			if (labelWidth > labelMaxWidth)
				labelMaxWidth = labelWidth;

			root.add(label, new Rectangle(new Point(mainLineXPosition
					- TextPositionOffset - textWidth, axisHeight
					- (int) (i * gradHeight) + TextHeight - Border), new Point(
					new Point(mainLineXPosition - TextPositionOffset,
							axisHeight - (int) (i * gradHeight) - Border))));

			// Draw the mini graduations
			for (int j = 1; j < NumberOfSubGraduation; j++) {
				final PolylineConnection line2 = new PolylineConnection();
				if (axisHeight - (int) (i * gradHeight) - Border
						+ (int) (j * gradHeight / NumberOfSubGraduation) > root.getSize()
						.height() - Border)
					break;

				line2.setForegroundColor(SWTResourceManager
						.getColor(SWT.COLOR_WIDGET_FOREGROUND));
				line2.setLineWidth(1);
				line2.setEndpoints(new Point(
						mainLineXPosition,
						axisHeight - (int) (i * gradHeight)
								+ (int) (j * gradHeight / NumberOfSubGraduation)),
						new Point(new Point(mainLineXPosition + MiniGradWidth,
								axisHeight - (int) (i * gradHeight)
										+ (int) (j * gradHeight / NumberOfSubGraduation))));
				root.add(line2);
			}
		}
		
		// Draw additional subgraduations, if necessary
		int yPositionOfLastGraduation = (int) ((int) (gradNumber) * gradHeight);
		if (drawingHeight > yPositionOfLastGraduation) {
			int j = 0;
			while (yPositionOfLastGraduation
					+ (int) (j * gradHeight / NumberOfSubGraduation) < drawingHeight) {
				final PolylineConnection line2 = new PolylineConnection();
				line2.setForegroundColor(SWTResourceManager
						.getColor(SWT.COLOR_WIDGET_FOREGROUND));
				line2.setLineWidth(1);
				line2.setEndpoints(new Point(mainLineXPosition,
						(int) axisHeight
								- (yPositionOfLastGraduation + (int) (j
										* gradHeight / NumberOfSubGraduation))),
						new Point(new Point(mainLineXPosition + MiniGradWidth,
								axisHeight
										- (yPositionOfLastGraduation + (int) (j
												* gradHeight / NumberOfSubGraduation)))));
				root.add(line2);
				j++;
			}
		}
		
		// Add legend
		String unit = ocelotlView.getOcelotlCore().getUnit(ocelotlView.getOcelotlCore().getMicromodelTypes().getSelectedOperatorResource().getUnit());
		final Label label = new Label(unit);
		label.setLabelAlignment(PositionConstants.RIGHT);
		label.setForegroundColor(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		label.setFont(SWTResourceManager.getFont("Cantarell",
				6, SWT.NORMAL));
		label.setToolTip(new Label(unit));
		label.setSize(textWidth, TextHeight);
		
		// Compute label width
		GC gc = new GC(canvas);
		gc.setFont(label.getFont());
		int labelWidth = gc.textExtent(label.getText()).x;
		if (labelWidth > labelMaxWidth)
			labelMaxWidth = labelWidth;

		root.add(label, new Rectangle(new Point(0, 0), 
				new Point(mainLineXPosition - TextPositionOffset,
						TextHeight)));
	}

	/**
	 * Draw the main line of the axis
	 */
	public void drawMainLine() {		
		mainLineXPosition = root.getClientArea().width() - Border - gradWidth;
		final PolylineConnection line = new PolylineConnection();
		line.setForegroundColor(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		line.setLineWidth(2);
		line.setEndpoints(new Point(mainLineXPosition, root.getSize().height()
				- Border), new Point(mainLineXPosition,
				(int) ((1 - DrawingMarginRatio) * root.getSize().height())));
		root.add(line);
	}

	/**
	 * Compute the number and size of graduation to draw
	 */
	public void computeMeasurements() {
		axisHeight = (int) (root.getSize().height() - Border);
		drawingHeight = DrawingMarginRatio * axisHeight;

		areaWidth = root.getClientArea().width();
		textWidth = areaWidth - (areaWidth - mainLineXPosition - 2)
				- TextPositionOffset;

		// Try to divide the axis to have round number
		double temp = maxValue;
		int i;
		for (i = 1; temp > 10; i++)
			temp /= 10;
		final double factor = temp < 6.0 ? 10.0 : temp;
		for (int j = 1; j < i; j++)
			temp *= 10;
		gradDuration = temp / factor;
		gradNumber = maxValue / gradDuration;
		gradHeight = drawingHeight / gradNumber;

		// If the graduation height is smaller than the minimum, reduce the
		// number of graduations
		while (gradHeight < GradHeightMin && gradNumber > 6) {
			gradNumber /= 2;
			gradHeight *= 2;
			gradDuration *= 2;
		}
	}

	/**
	 * After the first run, resize the axis to display the labels
	 */
	protected void resizeAxis() {
		int minSize = labelMaxWidth + TextPositionOffset + (areaWidth - mainLineXPosition);
		if (minSize > areaWidth) {
			ocelotlView.getMainViewTopSashform().setWeights(
					new int[] {
							minSize,
							ocelotlView.getMainViewTopSashform().getSize().y
									- minSize });
		}
	}

	@Override
	public void initDiagram() {
		// Modify the width only if the it is currently 0
		if (ocelotlView.getMainViewTopSashform().getWeights()[0] == 0) {
			ocelotlView.getMainViewTopSashform().setWeights(
					OcelotlConstants.yAxisDefaultWeight);
			ocelotlView.getMainViewTopSashform().layout();
		}
	}

	@Override
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
