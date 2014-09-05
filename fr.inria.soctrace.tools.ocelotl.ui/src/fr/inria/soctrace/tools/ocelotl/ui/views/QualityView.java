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

package fr.inria.soctrace.tools.ocelotl.ui.views;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.lpaggreg.quality.DLPQuality;

/**
 * _2DCacheMicroDescription View : part representation, according to LP
 * algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class QualityView {

	class ParamMouseListener implements MouseListener {

		State	state	= State.RELEASED;

		@Override
		public void mouseDoubleClicked(final MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(final MouseEvent arg0) {
			state = State.PRESSED;
			if (qualities != null)
				if (arg0.x > XBorder && arg0.x < root.getSize().width() - Border) {
					final float param = 1 - (float) (arg0.x - XBorder) / (root.getSize().width() - XBorder - Border);
					ocelotlView.getParam().setText(String.valueOf(param));
					ocelotlView.setConfiguration();
					createDiagram();
				}
		}

		@Override
		public void mouseReleased(final MouseEvent arg0) {
			state = State.RELEASED;
			ocelotlView.getBtnRun().notifyListeners(SWT.Selection, new Event());
		}

	}

	static public enum State {
		PRESSED, RELEASED;
	}

	private Figure				root;
	private Canvas				canvas;
	private final static int	XBorder				= 50;
	private final static int	YBorder				= 25;
	private final static int	Border				= 15;
	private final static int	AxisWidth			= 1;
	private final static int	YGradDefaultNumber	= 10;
	private double				yGradNumber			= 10.0;
	private double				qualityWidth		= 10.0;
	private final static long	YGradWidthMin		= 20;
	private double				yGradWidth			= 50;
	private final static int	TextWidth			= 50;
	private final static int	TextHeight			= 16;
	private final static int	TextOffset			= 2;
	private final static long	MiniDivide			= 5;
	private final static double	XGradNumber			= 10;
	private List<DLPQuality>	qualities;
	private List<Double>		parameterList;
	private double				currentParameter;
	private final OcelotlView	ocelotlView;
	private double				maxValue;
	private double				minValue;

	private final static float	ParamLineWidth		= 1.8F;

	private final static float	QualityLineWidth	= 2.0F;
	private static final Logger	logger				= LoggerFactory.getLogger(QualityView.class);

	public QualityView(final OcelotlView lpaggregView) {
		super();
		ocelotlView = lpaggregView;
	}
	
	public Figure getRoot() {
		return root;
	}

	public void createDiagram() {
		root.removeAll();
		if (ocelotlView.getCore().getLpaggregManager() != null) {
			qualities = ocelotlView.getCore().getLpaggregManager().getQualities();
			parameterList = new ArrayList<Double>(ocelotlView.getCore().getLpaggregManager().getParameters());
			currentParameter = ocelotlView.getCore().getOcelotlParameters().getParameter();
			if (qualities != null && (root.getSize().width() > (XBorder + Border) * 1.5)) {
				maxValue = Math.max(qualities.get(qualities.size() - 1).getGain(), qualities.get(qualities.size() - 1).getLoss());
				minValue = Math.min(0, Math.min(qualities.get(0).getGain(), qualities.get(0).getLoss()));
				if (maxValue == 0.0 && minValue == 0.0) {
					drawError();
				} else {
					drawYGrads();
					drawXGrads();
					drawXYLines();
					drawQualities();
					drawParam();
				}
			}
			canvas.update();
		}
	}

	private void drawError() {
		final RectangleFigure rectangleText = new RectangleFigure();
		root.add(rectangleText, new Rectangle(new Point(XBorder, (int) ((root.getSize().height() - YBorder - Border) / 3)), new Point(new Point(root.getSize().width() - XBorder, (int) ((root.getSize().height() - YBorder - Border) / 2)))));
		rectangleText.setBackgroundColor(ColorConstants.white);
		rectangleText.setForegroundColor(ColorConstants.white);
		final Label label = new Label("Error: unable to get several aggregations");
		label.setLabelAlignment(SWT.CENTER);
		label.setForegroundColor(ColorConstants.darkGray);
		rectangleText.setFont(SWTResourceManager.getFont("Cantarell", TextHeight / 2, SWT.NORMAL));
		rectangleText.setLineWidth(1);
		rectangleText.add(label);
		final ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
		rectangleText.setLayoutManager(layout);
		rectangleText.setBackgroundColor(root.getBackgroundColor());
		rectangleText.setForegroundColor(root.getBackgroundColor());
	}

	public void drawParam() {
		if (currentParameter == -1)
			return;
		final int width = root.getSize().width - XBorder - Border;
		final int yOff = root.getSize().height() - YBorder;
		final PolylineConnection line = new PolylineConnection();
		line.setEndpoints(new Point((int) (XBorder + width * (1 - currentParameter)), yOff), new Point((int) (XBorder + width * (1 - currentParameter)), Border));
		line.setForegroundColor(ColorConstants.blue);
		line.setLineWidthFloat(ParamLineWidth);
		root.add(line);
	}

	public void drawQualities() {
		int i;
		final int width = root.getSize().width - XBorder - Border;
		final int yOff = root.getSize().height() - YBorder;
		final int height = root.getSize().height() - YBorder - Border;
		qualities.add(new DLPQuality(qualities.get(qualities.size() - 1)));
		parameterList.add(1.0);
		for (i = 1; i < qualities.size(); i++) {
			final double cParam = 1 - parameterList.get(i);
			final double nParam = 1 - parameterList.get(i - 1);
			double cgain = qualities.get(qualities.size() - 1).getGain() - qualities.get(i).getGain();
			double ngain = qualities.get(qualities.size() - 1).getGain() - qualities.get(i - 1).getGain();
			double closs = qualities.get(qualities.size() - 1).getLoss() - qualities.get(i).getLoss();
			double nloss = qualities.get(qualities.size() - 1).getLoss() - qualities.get(i - 1).getLoss();
			if (!ocelotlView.getParams().isGrowingQualities()) {
				cgain = qualities.get(i).getGain();
				ngain = qualities.get(i - 1).getGain();
				closs = qualities.get(i).getLoss();
				nloss = qualities.get(i - 1).getLoss();
			}

			final PolylineConnection lineGain1 = new PolylineConnection();
			lineGain1.setEndpoints(new Point((int) (XBorder + width * cParam), (int) (yOff - height * cgain / (maxValue - minValue))), new Point((int) (XBorder + width * cParam), (int) (yOff - height * ngain / (maxValue - minValue))));
			final PolylineConnection lineLoss1 = new PolylineConnection();
			lineLoss1.setEndpoints(new Point((int) (XBorder + width * cParam), (int) (yOff - height * closs / (maxValue - minValue))), new Point((int) (XBorder + width * cParam), (int) (yOff - height * nloss / (maxValue - minValue))));
			final PolylineConnection lineGain2 = new PolylineConnection();
			lineGain2.setEndpoints(new Point((int) (XBorder + width * cParam), (int) (yOff - height * ngain / (maxValue - minValue))), new Point((int) (XBorder + width * nParam), (int) (yOff - height * ngain / (maxValue - minValue))));
			final PolylineConnection lineLoss2 = new PolylineConnection();
			lineLoss2.setEndpoints(new Point((int) (XBorder + width * cParam), (int) (yOff - height * nloss / (maxValue - minValue))), new Point((int) (XBorder + width * nParam), (int) (yOff - height * nloss / (maxValue - minValue))));
			lineGain1.setForegroundColor(ColorConstants.green);
			lineLoss1.setForegroundColor(ColorConstants.red);
			lineGain1.setLineWidthFloat(QualityLineWidth);
			lineLoss1.setLineWidthFloat(QualityLineWidth);
			root.add(lineGain1);
			root.add(lineLoss1);
			lineGain2.setForegroundColor(ColorConstants.green);
			lineLoss2.setForegroundColor(ColorConstants.red);
			lineGain2.setLineWidthFloat(QualityLineWidth);
			lineLoss2.setLineWidthFloat(QualityLineWidth);
			root.add(lineGain2);
			root.add(lineLoss2);
		}
		qualities.remove(qualities.size() - 1);
	}

	public void drawXGrads() {
		double width = (root.getSize().width - XBorder - Border) / XGradNumber;
		double xGradNumber = XGradNumber;
		while (width < TextWidth && width > 1) {
			xGradNumber /= 2;
			width = (root.getSize().width - XBorder - Border) / xGradNumber;
		}
		final PolylineConnection lineEnd = new PolylineConnection();
		lineEnd.setEndpoints(new Point((int) (xGradNumber * width + XBorder), root.getSize().height() - YBorder), new Point(new Point((int) (xGradNumber * width + XBorder), Border)));
		lineEnd.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lineEnd.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		lineEnd.setLineWidth(1);
		root.add(lineEnd);
		for (int i = 0; i < xGradNumber + 1; i++) {
			final PolylineConnection line = new PolylineConnection();
			line.setEndpoints(new Point((int) (i * width + XBorder), root.getSize().height() - YBorder), new Point(new Point((int) (i * width + XBorder), Border)));
			line.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			line.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			line.setLineWidth(1);
			root.add(line);
			final RectangleFigure rectangleText = new RectangleFigure();
			root.add(rectangleText, new Rectangle(new Point((int) (i * width + XBorder - width / 2), root.getSize().height() - YBorder + TextOffset), new Point(new Point((int) ((i + 1) * width + XBorder - width / 2), root.getSize().height() - YBorder
					- AxisWidth + TextHeight + TextOffset))));
			final float value = (float) ((10 - (i * XGradNumber / xGradNumber)) * 0.1);
			final Label label = new Label("" + value);
			label.setLabelAlignment(SWT.CENTER);
			label.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			rectangleText.setFont(SWTResourceManager.getFont("Cantarell", TextHeight / 2, SWT.NORMAL));
			rectangleText.setLineWidth(1);
			rectangleText.add(label);
			rectangleText.setBackgroundColor(root.getBackgroundColor());
			rectangleText.setForegroundColor(root.getBackgroundColor());
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			rectangleText.setLayoutManager(layout);
			if (i * (XGradNumber / xGradNumber) != (int) XGradNumber && width / MiniDivide > YGradWidthMin / 2)
				for (int j = 1; j < 5; j++) {
					if (((i * width + XBorder) + (int) (j * width / MiniDivide)) < xGradNumber * width + XBorder) {
						final PolylineConnection lineDash = new PolylineConnection();
						lineDash.setEndpoints(new Point((int) (i * width + XBorder) + (int) (j * width / MiniDivide), root.getSize().height() - YBorder), new Point(new Point((int) (i * width + XBorder) + (int) (j * width / MiniDivide), Border)));
						lineDash.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
						lineDash.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
						lineDash.setLineWidth(1);
						lineDash.setLineStyle(SWT.LINE_DASH);
						root.add(lineDash);
					}
				}
		}
	}

	public void drawXYLines() {
		final RectangleFigure rectangleX = new RectangleFigure();
		final RectangleFigure rectangleY = new RectangleFigure();
		root.add(rectangleX, new Rectangle(new Point(XBorder, root.getSize().height() - YBorder + AxisWidth), new Point(root.getSize().width() - Border, root.getSize().height() - YBorder)));
		rectangleX.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		rectangleX.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		root.add(rectangleY, new Rectangle(new Point(XBorder - AxisWidth, Border), new Point(XBorder, root.getSize().height() - YBorder)));
		rectangleY.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		rectangleY.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		rectangleX.setLineWidth(1);
		rectangleY.setLineWidth(1);
	}

	public void drawYGrads() {
		YGrads();
		NumberFormat formatter = null;
		formatter = java.text.NumberFormat.getInstance(java.util.Locale.US);
		formatter = new DecimalFormat("0.0E0");
		final double width = root.getSize().width - XBorder - Border;
		final double height = root.getSize().height - YBorder;
		final PolylineConnection lineEnd = new PolylineConnection();
		lineEnd.setEndpoints(new Point(XBorder, (int) (height - yGradNumber * yGradWidth)), new Point((int) (width + XBorder), (int) (height - yGradNumber * yGradWidth)));
		lineEnd.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lineEnd.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		lineEnd.setLineWidth(1);
		root.add(lineEnd);
		for (int i = 0; i < (int) yGradNumber + 1; i++) {
			final PolylineConnection line = new PolylineConnection();
			line.setEndpoints(new Point(XBorder, (int) (height - i * yGradWidth)), new Point((int) (width + XBorder), (int) (height - i * yGradWidth)));
			line.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			line.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			line.setLineWidth(1);
			root.add(line);
			final RectangleFigure rectangleText = new RectangleFigure();
			root.add(rectangleText, new Rectangle(new Point(XBorder - TextWidth - TextOffset, (int) (height - i * yGradWidth) + TextHeight / 3), new Point(new Point(XBorder - TextOffset, (int) (height - i * yGradWidth - TextHeight / 1.5)))));
			rectangleText.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			rectangleText.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			final double value = i == yGradNumber ? maxValue - minValue : (double) ((long) (i * qualityWidth * 10) / 10.0);
			String text = formatter.format(value);
			if (value < 1000)
				text = String.valueOf(value);
			final Label label = new Label(text);
			label.setLabelAlignment(SWT.CENTER);
			label.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			rectangleText.setFont(SWTResourceManager.getFont("Cantarell", TextHeight / 2, SWT.NORMAL));
			rectangleText.setLineWidth(1);
			rectangleText.add(label);
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			rectangleText.setLayoutManager(layout);
			rectangleText.setBackgroundColor(root.getBackgroundColor());
			rectangleText.setForegroundColor(root.getBackgroundColor());
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

		root.addMouseListener(new ParamMouseListener());

		return canvas;
	}

	public void resizeDiagram() {
		createDiagram();
		root.repaint();
	}

	public void YGrads() {
		final double maxValue = Math.max(qualities.get(qualities.size() - 1).getGain(), qualities.get(qualities.size() - 1).getLoss());
		final double minValue = Math.min(0, Math.min(qualities.get(0).getGain(), qualities.get(0).getLoss()));
		long temp = (long) (maxValue - minValue);
		int i;
		for (i = 1; temp > 10; i++)
			temp /= 10;
		final long factor = temp < 6 ? YGradDefaultNumber : temp;
		for (int j = 1; j < i; j++)
			temp *= 10;
		qualityWidth = (double) temp / (double) factor;
		yGradNumber = (maxValue - minValue) / qualityWidth;
		yGradWidth = (root.getSize().height - YBorder - Border) / yGradNumber;
		while (yGradWidth < YGradWidthMin && yGradNumber > 6) {
			yGradNumber /= 2;
			yGradWidth *= 2;
			qualityWidth *= 2;
		}
	}
	
}
