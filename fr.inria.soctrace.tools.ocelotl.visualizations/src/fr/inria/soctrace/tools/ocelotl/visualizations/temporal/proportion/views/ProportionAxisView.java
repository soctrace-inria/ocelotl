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
import java.util.Collection;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.UnitAxisView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;
import fr.inria.soctrace.tools.ocelotl.visualizations.temporal.proportion.TemporalProportion;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;

/**
 * Time Axis View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class ProportionAxisView extends UnitAxisView {

	private class SelectFigure extends RectangleFigure {

		public SelectFigure() {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			setAlpha(50);
		}

		public void draw(final boolean active) {
			if (active) {
				setForegroundColor(AggregatedView.activeColorFG);
				setBackgroundColor(AggregatedView.activeColorBG);
			} else {
				setForegroundColor(AggregatedView.selectColorFG);
				setBackgroundColor(AggregatedView.selectColorBG);
			}
			root.add(this,
					new Rectangle(new Point(0, 0), new Point(20,
							root.getSize().height - 2)));
		}
	}

	protected double maxValue;
	protected int axisHeight;
	protected final static int width = 30;
	// Margin from the frame
	protected final static int border = 10;
	protected final static int unitAxisWidth = 1;
	protected final static long divide = 10;
	// Number of graduations to display
	protected double gradNumber = 10.0;
	// Value of a grad
	protected double gradDuration = 10.0;
	// Width of a graduation
	protected int gradWidth = 10;

	protected final static long gradHeightMin = 20;
	protected double gradHeight = 20;
	protected int textWidth = 35;
	protected final static int textHeight = 20;
	protected final static long miniDivide = 5;
	// Width of a subgraduation
	protected final static int miniGradWidth = 4;
	protected final static int textPositionOffset = 15;
	protected int areaWidth = 0;
	protected int mainLinePosition;
	protected SelectFigure selectFigure;

	public ProportionAxisView() {
		super();
		selectFigure = new SelectFigure();
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
		
		axisHeight = root.getSize().height() - 2* border ;
		areaWidth = root.getClientArea().width();
		textWidth = areaWidth - (areaWidth - mainLinePosition - 2)
				- textPositionOffset;
		computeGradMeasure();

		// Draw the graduations
		for (int i = 0; i < (int) gradNumber + 1; i++) {
			// Draw the main graduation line
			final PolylineConnection line = new PolylineConnection();
			line.setForegroundColor(SWTResourceManager
					.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			line.setLineWidth(2);
			line.setEndpoints(new Point(mainLinePosition,
					axisHeight - (int) (i * gradHeight) + border), new Point(mainLinePosition + gradWidth,
							axisHeight - (int) (i * gradHeight) + border));
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
			label.setSize(textWidth, textHeight);

			root.add(label,
					new Rectangle(new Point(mainLinePosition - textPositionOffset
							- textWidth, axisHeight - (int) (i * gradHeight) + unitAxisWidth
							+ textHeight), new Point(new Point(mainLinePosition
							- textPositionOffset, axisHeight - (int) (i * gradHeight)))));

			// Draw the mini graduations
			for (int j = 1; j < 5; j++) {
				final PolylineConnection line2 = new PolylineConnection();
				if ((int) (i * gradHeight) + border
						+ (int) (j * gradHeight / miniDivide) > root.getSize()
						.height() - border)
					break;

				line2.setForegroundColor(SWTResourceManager
						.getColor(SWT.COLOR_WIDGET_FOREGROUND));
				line2.setLineWidth(1);
				line2.setEndpoints(new Point(
						mainLinePosition,
						axisHeight - (int) (i * gradHeight) + border
								+ (int) (j * gradHeight / miniDivide)),
						new Point(new Point(mainLinePosition + miniGradWidth,
								axisHeight -	(int) (i * gradHeight) + border
										+ (int) (j * gradHeight / miniDivide))));
				root.add(line2);
			}
		}
	}

	/**
	 * Draw the main line of the axis
	 */
	public void drawMainLine() {
		mainLinePosition = root.getClientArea().width() - border - gradWidth;
		final PolylineConnection line = new PolylineConnection();
		line.setForegroundColor(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		line.setLineWidth(2);
		line.setEndpoints(new Point(mainLinePosition, root.getSize().height()
				- border), new Point(mainLinePosition, border));
		root.add(line);
	}

	/**
	 * Compute the number and size of graduation to draw
	 */
	public void computeGradMeasure() {
		final double duration = maxValue;
		double temp = duration;
		int i;

		// Get the number of power of ten in the duration
		for (i = 1; temp > 10; i++)
			temp /= 10;
		final double factor = temp < 6 ? divide : temp;
		for (int j = 1; j < i; j++)
			temp *= 10;
		gradDuration = duration / 8; //(double) temp / (double) factor;
		gradNumber = duration / gradDuration;
		gradHeight = (root.getClientArea().height - 2 * border - 1) / gradNumber;
		while (gradHeight < gradHeightMin && gradNumber > 6) {
			gradNumber /= 2;
			gradHeight *= 2;
			gradDuration *= 2;
		}
	}

	public Canvas initDiagram(final Composite parent) {
		root = new Figure();
		root.setFont(parent.getFont());
		final XYLayout layout = new XYLayout();
		root.setLayoutManager(layout);
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.gray);
		canvas.setSize(parent.getSize());
		final LightweightSystem lws = new LightweightSystem(canvas);
		lws.setContents(root);
		lws.setControl(canvas);
		root.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		root.setSize(parent.getSize().x, parent.getSize().y);
		return canvas;
	}

	public void resizeDiagram() {
		createDiagram(maxValue);
		root.repaint();
	}

	public void select(final boolean active) {
		selectFigure.draw(active);
	}

	public void unselect() {
		resizeDiagram();
	}
}
