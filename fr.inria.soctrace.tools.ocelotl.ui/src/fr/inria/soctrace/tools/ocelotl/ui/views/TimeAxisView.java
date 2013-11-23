/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
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

package fr.inria.soctrace.tools.ocelotl.ui.views;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.com.eclipse.wb.swt.SWTResourceManager;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class TimeAxisView {

	private class SelectFigure extends RectangleFigure {

		public SelectFigure() {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			setAlpha(50);
		}

		public void draw(final TimeRegion timeRegion, final boolean active) {
			if (active) {
				setForegroundColor(MatrixView.activeColorFG);
				setBackgroundColor(MatrixView.activeColorBG);
			} else {
				setForegroundColor(MatrixView.selectColorFG);
				setBackgroundColor(MatrixView.selectColorBG);
			}
			root.add(this,
					new Rectangle(new Point((int) ((timeRegion.getTimeStampStart() - time.getTimeStampStart()) * (root.getSize().width - 2 * Border) / time.getTimeDuration() + Border), root.getSize().height - 2), new Point(
							(int) ((timeRegion.getTimeStampEnd() - time.getTimeStampStart()) * (root.getSize().width - 2 * Border) / time.getTimeDuration() + Border), -1)));
		}

	}

	Figure				root;
	Canvas				canvas;
	TimeRegion			time;
	final static int	Height			= 100;
	final static int	Border			= 10;
	final static int	TimeAxisWidth	= 1;
	final static long	Divide			= 10;
	double				GradNumber		= 10.0;
	double				GradDuration	= 10.0;
	final static long	GradWidthMin	= 50;
	double				GradWidth		= 50;
	final static int	GradHeight		= 8;
	final static int	TextWidth		= 50;
	final static int	TextHeight		= 20;
	final static long	MiniDivide		= 5;
	final static int	MiniGradHeight	= 4;
	int					Space			= 6;
	SelectFigure		selectFigure;

	public TimeAxisView() {
		super();
		selectFigure = new SelectFigure();
	}

	public void createDiagram(final TimeRegion time) {
		root.removeAll();
		this.time = time;
		if (time != null) {
			drawMainLine();
			drawGrads();
		}
		canvas.update();
	}

	public void createDiagram(final TimeRegion time, final TimeRegion timeRegion, final boolean active) {
		root.removeAll();
		this.time = time;
		if (time != null) {
			drawMainLine();
			drawGrads();
			selectFigure.draw(timeRegion, active);
		}
		canvas.update();
	}

	public void drawGrads() {
		grads();
		NumberFormat formatter = null;
		formatter = java.text.NumberFormat.getInstance(java.util.Locale.US);
		formatter = new DecimalFormat("0.00E0");
		for (int i = 0; i < (int) GradNumber + 1; i++) {
			final RectangleFigure rectangle = new RectangleFigure();
			root.add(rectangle, new Rectangle(new Point((int) (i * GradWidth) + Border, root.getSize().height() / 3), new Point(new Point((int) (i * GradWidth) + Border + TimeAxisWidth, root.getSize().height() / 3 - GradHeight))));
			rectangle.setBackgroundColor(ColorConstants.darkGray);
			rectangle.setForegroundColor(ColorConstants.darkGray);
			rectangle.setLineWidth(1);
			final RectangleFigure rectangleText = new RectangleFigure();
			if (i != (int) GradNumber)
				root.add(rectangleText, new Rectangle(new Point((int) (i * GradWidth), root.getSize().height() / 3 + 2), new Point(new Point((int) (i * GradWidth) + TimeAxisWidth + TextWidth, root.getSize().height() / 3 + 2 + TextHeight))));
			else
				root.add(rectangleText, new Rectangle(new Point((int) (i * GradWidth) - Border * 3, root.getSize().height() / 3 + 2), new Point(new Point((int) (i * GradWidth) + TimeAxisWidth + TextWidth, root.getSize().height() / 3 + 2 + TextHeight))));
			rectangleText.setBackgroundColor(root.getBackgroundColor());
			rectangleText.setForegroundColor(root.getBackgroundColor());
			final long value = (long) (i * GradDuration + time.getTimeStampStart());
			final String text = formatter.format(value);
			final Label label = new Label(text);
			label.setLabelAlignment(SWT.CENTER);
			label.setForegroundColor(ColorConstants.darkGray);
			rectangleText.setFont(SWTResourceManager.getFont("Cantarell", TextHeight / 2, SWT.NORMAL));
			rectangleText.setLineWidth(1);
			rectangleText.add(label);
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			rectangleText.setLayoutManager(layout);
			for (int j = 1; j < 5; j++) {
				final RectangleFigure rectangle2 = new RectangleFigure();
				if ((int) (i * GradWidth) + Border + (int) (j * GradWidth / MiniDivide) > root.getSize().width() - Border)
					break;
				root.add(rectangle2, new Rectangle(new Point((int) (i * GradWidth) + Border + (int) (j * GradWidth / MiniDivide), root.getSize().height() / 3), new Point(new Point((int) (i * GradWidth) + Border + (int) (j * GradWidth / MiniDivide), root
						.getSize().height() / 3 - MiniGradHeight))));
				rectangle2.setBackgroundColor(ColorConstants.gray);
				rectangle2.setForegroundColor(ColorConstants.gray);
				rectangle2.setLineWidth(1);
			}
		}
	}

	public void drawMainLine() {
		final RectangleFigure rectangle = new RectangleFigure();
		root.add(rectangle, new Rectangle(new Point(Border, root.getSize().height() / 3 + TimeAxisWidth), new Point(root.getSize().width() - Border, root.getSize().height() / 3)));
		rectangle.setBackgroundColor(ColorConstants.darkGray);
		rectangle.setForegroundColor(ColorConstants.darkGray);
		rectangle.setLineWidth(1);
	}

	public void grads() {
		final long duration = time.getTimeDuration();
		long temp = duration;
		int i;
		for (i = 1; temp > 10; i++)
			temp /= 10;
		final long factor = temp < 6 ? Divide : temp;
		for (int j = 1; j < i; j++)
			temp *= 10;
		GradDuration = (double) temp / (double) factor;
		GradNumber = duration / GradDuration;
		GradWidth = (root.getSize().width - 2 * Border - 1) / GradNumber;
		while (GradWidth < GradWidthMin && GradNumber > 6) {
			GradNumber /= 2;
			GradWidth *= 2;
			GradDuration *= 2;
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
		canvas.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(final ControlEvent arg0) {
				// TODO Auto-generated method stub
				canvas.redraw();
				root.repaint();
				resizeDiagram();

			}

			@Override
			public void controlResized(final ControlEvent arg0) {
				canvas.redraw();
				root.repaint();
				resizeDiagram();
			}
		});

		return canvas;
	}

	public void resizeDiagram() {
		createDiagram(time);
		root.repaint();
	}

	public void select(final TimeRegion timeRegion, final boolean active) {
		createDiagram(time, timeRegion, active);
		root.repaint();
	}

	public void unselect() {
		resizeDiagram();
	}

}
