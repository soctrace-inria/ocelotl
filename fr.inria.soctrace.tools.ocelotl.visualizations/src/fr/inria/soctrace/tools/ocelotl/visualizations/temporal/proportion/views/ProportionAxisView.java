/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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
import java.util.Locale;

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
	// Height of a mini-graduation
	protected double minGradHeight;
	
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
			resizeAxis();
		}
		canvas.update();
		root.validate();
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
		root.validate();
	}

	/**
	 * Draw the graduations and their labels
	 */
	public void drawGrads() {
		NumberFormat formatter = null;
		formatter = NumberFormat.getInstance(Locale.US);
		formatter = new DecimalFormat("0.00E0");
		formatter.setMaximumIntegerDigits(3);

		computeMeasurements();

		// Draw the graduations, starting from the bottom (reversed Y
		// coordinates)
		for (int i = 0; i < (int) gradNumber + 1; i++) {
			// Draw the main graduation lines
			final PolylineConnection line = new PolylineConnection();
			line.setForegroundColor(SWTResourceManager
					.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			line.setLineWidth(2);
			
			int gradYpos = (int) axisHeight - (int) (i * gradHeight);
			line.setEndpoints(new Point(mainLineXPosition, gradYpos),
					new Point(mainLineXPosition + gradWidth, gradYpos));
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
	
			// Do not draw the sub-grads under the first grad
			if(i == 0)
				continue;
			
			// Draw the sub graduations
			for (int j = 1; j < NumberOfSubGraduation; j++) {
				final PolylineConnection line2 = new PolylineConnection();
				
				int minGradYposition = (int) axisHeight
						- (int) (i * gradHeight) + (int) (j * minGradHeight);
				
				if (minGradYposition - Border > root.getSize().height()
						- Border)
					break;

				line2.setForegroundColor(SWTResourceManager
						.getColor(SWT.COLOR_WIDGET_FOREGROUND));
				line2.setLineWidth(1);
				line2.setEndpoints(new Point(mainLineXPosition,
						minGradYposition), new Point(new Point(
						mainLineXPosition + MiniGradWidth, minGradYposition)));
				root.add(line2);
			}
		}
		
		// Draw additional subgraduations, if necessary
		int yPositionOfLastGraduation = (int) ((int) (gradNumber) * gradHeight);
		if (drawingHeight > yPositionOfLastGraduation) {
			int j = 0;
			while (yPositionOfLastGraduation
					+ (int) (j * minGradHeight) < drawingHeight) {
				final PolylineConnection line2 = new PolylineConnection();
				line2.setForegroundColor(SWTResourceManager
						.getColor(SWT.COLOR_WIDGET_FOREGROUND));
				line2.setLineWidth(1);
				line2.setEndpoints(new Point(mainLineXPosition,
						(int) axisHeight
								- (yPositionOfLastGraduation + (int) (j
										* minGradHeight))),
						new Point(new Point(mainLineXPosition + MiniGradWidth,
								axisHeight
										- (yPositionOfLastGraduation + (int) (j
												* minGradHeight)))));
				root.add(line2);
				j++;
			}
		}
		
		// Add legend
		String unit = ocelotlView.getCore().getMicromodelTypes().getSelectedOperatorResource().getUnitDescription();
		final Label label = new Label(" " + unit + " ");
		label.setLabelAlignment(PositionConstants.RIGHT);
		label.setForegroundColor(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		label.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		label.setToolTip(new Label(" " + unit + " "));
		label.setSize(textWidth, TextHeight);

		// Compute label width
		GC gc = new GC(canvas);
		gc.setFont(label.getFont());
		int labelWidth = gc.textExtent(label.getText()).x;
		// Since the label is not align with the other labels, remove the difference
		if (labelWidth - (TextPositionOffset + (areaWidth - mainLineXPosition)) > labelMaxWidth)
			labelMaxWidth = labelWidth - (TextPositionOffset + (areaWidth - mainLineXPosition));

		root.add(label, new Rectangle(new Point(areaWidth - labelWidth, 0), new Point(
				areaWidth, TextHeight)));
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

		gradDuration = computeGradDuration();
		gradNumber = maxValue / gradDuration;
		gradHeight = drawingHeight / gradNumber;

		// If the graduation height is smaller than the minimum, reduce the
		// number of graduations
		while (gradHeight < GradHeightMin && gradNumber > 6) {
			gradNumber /= 2;
			gradHeight *= 2;
			gradDuration *= 2;
		}
		
		minGradHeight = gradHeight / NumberOfSubGraduation;
	}

	/**
	 * Compute a round grad duration
	 * 
	 * @return the computed grad duration
	 */
	private double computeGradDuration() {
		// Try to divide the axis to have round number
		if (maxValue > 1) {
			long temp = (long) maxValue;
			int i;
			// Divide max value until there is only one significant number
			// before the dot
			for (i = 1; temp > 10; i++)
				temp /= 10;
			// Number of graduations (min: 6; max: 10)
			final double numberOfGrads = temp < 6.0 ? 10.0 : temp;
			// Get temp back to its original value
			for (int j = 1; j < i; j++)
				temp *= 10;
			
			// Have a round grad duration
			return temp / numberOfGrads;
		} else {
			double temp = maxValue;
			int i;
			// Multiply max value until there is just one significant number
			// before the dot
			for (i = 1; temp < 1; i++)
				temp *= 10;
			// Number of graduations (min: 6; max: 10)
			final double numberOfGrads = temp < 6.0 ? 10.0 : temp;
			// Round the number to its integer part
			double tempBis = (double) Math.round(temp);
			// Get temp back to its original value
			for (int j = 1; j < i; j++)
				tempBis /= 10;
			
			// Have a round grad duration
			return tempBis / numberOfGrads;
		}
	}

	/**
	 * After the first run, resize the axis to display the labels
	 */
	protected void resizeAxis() {
		canvas.update();
		root.validate();
		
		int minSize = labelMaxWidth + TextPositionOffset
				+ (areaWidth - mainLineXPosition);

		if((areaWidth < minSize && (ocelotlView.getMainViewTopSashform().getSize().x
				- minSize > 0)) || areaWidth > minSize + 10)
		ocelotlView.getMainViewTopSashform().setWeights(
				new int[] {
						minSize,
						ocelotlView.getMainViewTopSashform().getSize().x
								- minSize });
	}

	@Override
	public void initDiagram() {
		// Modify the width only if it is currently 0
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
