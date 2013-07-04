/* ===========================================================
 * LPAggreg UI module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to visualize a Paje
 * trace across an aggregated representation.
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

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.PolylineConnection;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.Quality;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class QualityView {

	private Figure				root;
	private Canvas				canvas;
	private final static int	Border				= 50;
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
	private List<Quality>		qualities;
	private float				currentParameter;
	private final OcelotlView	ocelotlView;
	private final static float	paramLineWidth		= 1.8F;

	public QualityView(final OcelotlView lpaggregView) {
		super();
		ocelotlView = lpaggregView;
	}

	public void createDiagram() {
		root.removeAll();
		if (ocelotlView.getCore().getLpaggregManager() != null) {
			qualities = ocelotlView.getCore().getLpaggregManager().getQualities();
			currentParameter = ocelotlView.getCore().getOcelotlParameters().getParameter();
			if (qualities != null) {
				drawXGrads();
				drawYGrads();
				drawXYLines();
				drawQualities();
				drawParam();
			}
			canvas.update();
		}
	}

	public void drawParam() {
		if (currentParameter == -1)
			return;
		final int width = root.getSize().width - 2 * Border;
		final int yOff = root.getSize().height() - Border;
		final PolylineConnection line = new PolylineConnection();
		line.setEndpoints(new Point((int) (Border + width * (1 - currentParameter)), yOff), new Point((int) (Border + width * (1 - currentParameter)), Border));
		line.setForegroundColor(ColorConstants.blue);
		line.setLineWidthFloat(paramLineWidth);
		root.add(line);
	}

	public void drawQualities() {
		int i;
		final double maxValue = Math.max(qualities.get(qualities.size() - 1).getGain(), qualities.get(qualities.size() - 1).getLoss());
		final int width = root.getSize().width - 2 * Border;
		final int yOff = root.getSize().height() - Border;
		final int height = root.getSize().height() - 2 * Border;
		qualities.add(new Quality(qualities.get(qualities.size() - 1).getGain(), qualities.get(qualities.size() - 1).getLoss(), 1));
		for (i = 1; i < qualities.size(); i++) {
			final float cParam = 1 - qualities.get(i).getParameter();
			final float nParam = 1 - qualities.get(i - 1).getParameter();
			final double cgain = qualities.get(i).getGain();
			final double ngain = qualities.get(i - 1).getGain();
			final double closs = qualities.get(i).getLoss();
			final double nloss = qualities.get(i - 1).getLoss();
			final PolylineConnection lineGain1 = new PolylineConnection();
			lineGain1.setEndpoints(new Point((int) (Border + width * cParam), (int) (yOff - height * cgain / maxValue)), new Point((int) (Border + width * cParam), (int) (yOff - height * ngain / maxValue)));
			final PolylineConnection lineLoss1 = new PolylineConnection();
			lineLoss1.setEndpoints(new Point((int) (Border + width * cParam), (int) (yOff - height * closs / maxValue)), new Point((int) (Border + width * cParam), (int) (yOff - height * nloss / maxValue)));
			final PolylineConnection lineGain2 = new PolylineConnection();
			lineGain2.setEndpoints(new Point((int) (Border + width * cParam), (int) (yOff - height * ngain / maxValue)), new Point((int) (Border + width * nParam), (int) (yOff - height * ngain / maxValue)));
			final PolylineConnection lineLoss2 = new PolylineConnection();
			lineLoss2.setEndpoints(new Point((int) (Border + width * cParam), (int) (yOff - height * nloss / maxValue)), new Point((int) (Border + width * nParam), (int) (yOff - height * nloss / maxValue)));
			lineGain1.setForegroundColor(ColorConstants.green);
			lineLoss1.setForegroundColor(ColorConstants.red);
			lineGain1.setLineWidthFloat(2);
			lineLoss1.setLineWidthFloat(2);
			root.add(lineGain1);
			root.add(lineLoss1);
			lineGain2.setForegroundColor(ColorConstants.green);
			lineLoss2.setForegroundColor(ColorConstants.red);
			lineGain2.setLineWidthFloat(2);
			lineLoss2.setLineWidthFloat(2);
			root.add(lineGain2);
			root.add(lineLoss2);
		}
		qualities.remove(qualities.size() - 1);
	}

	public void drawXGrads() {
		final double width = (root.getSize().width - 2 * Border) / XGradNumber;
		for (int i = 0; i < XGradNumber + 1; i++) {
			final PolylineConnection line = new PolylineConnection();
			line.setEndpoints(new Point((int) (i * width + Border), root.getSize().height() - Border), new Point(new Point((int) (i * width + Border), Border)));
			line.setBackgroundColor(ColorConstants.lightGray);
			line.setForegroundColor(ColorConstants.lightGray);
			line.setLineWidth(1);
			root.add(line);
			final RectangleFigure rectangleText = new RectangleFigure();
			root.add(rectangleText, new Rectangle(new Point((int) (i * width + Border - TextWidth / 2), root.getSize().height() - Border + TextOffset), new Point(new Point((int) (i * width + Border + AxisWidth + TextWidth / 2), root.getSize().height()
					- Border - AxisWidth + TextHeight + TextOffset))));
			final float value = (float) ((10 - i) * 0.1);
			final Label label = new Label("" + value);
			label.setLabelAlignment(SWT.CENTER);
			label.setForegroundColor(ColorConstants.darkGray);
			rectangleText.setFont(SWTResourceManager.getFont("Cantarell", TextHeight / 2, SWT.NORMAL));
			rectangleText.setLineWidth(1);
			rectangleText.add(label);
			rectangleText.setBackgroundColor(root.getBackgroundColor());
			rectangleText.setForegroundColor(root.getBackgroundColor());
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			rectangleText.setLayoutManager(layout);
			if (i != (int) XGradNumber && width / MiniDivide > YGradWidthMin / 2)
				for (int j = 1; j < 5; j++) {
					final PolylineConnection lineDash = new PolylineConnection();
					lineDash.setEndpoints(new Point((int) (i * width + Border) + (int) (j * width / MiniDivide), root.getSize().height() - Border), new Point(new Point((int) (i * width + Border) + (int) (j * width / MiniDivide), Border)));
					lineDash.setBackgroundColor(ColorConstants.lightGray);
					lineDash.setForegroundColor(ColorConstants.lightGray);
					lineDash.setLineWidth(1);
					lineDash.setLineStyle(SWT.LINE_DASH);
					root.add(lineDash);
				}
		}
	}

	public void drawXYLines() {
		final RectangleFigure rectangleX = new RectangleFigure();
		final RectangleFigure rectangleY = new RectangleFigure();
		root.add(rectangleX, new Rectangle(new Point(Border, root.getSize().height() - Border - AxisWidth), new Point(root.getSize().width() - Border, root.getSize().height() - Border)));
		rectangleX.setBackgroundColor(ColorConstants.darkGray);
		rectangleX.setForegroundColor(ColorConstants.darkGray);
		root.add(rectangleY, new Rectangle(new Point(Border - AxisWidth, Border), new Point(Border, root.getSize().height() - Border)));
		rectangleY.setBackgroundColor(ColorConstants.darkGray);
		rectangleY.setForegroundColor(ColorConstants.darkGray);
		rectangleX.setLineWidth(1);
		rectangleY.setLineWidth(1);
	}

	public void drawYGrads() {
		YGrads();
		final double width = root.getSize().width - 2 * Border;
		final double height = root.getSize().height - 2 * Border;
		for (int i = 0; i < (int) yGradNumber + 1; i++) {
			final PolylineConnection line = new PolylineConnection();
			line.setEndpoints(new Point(Border, (int) (height + Border - i * yGradWidth)), new Point((int) (width + Border), (int) (height + Border - i * yGradWidth)));
			line.setBackgroundColor(ColorConstants.lightGray);
			line.setForegroundColor(ColorConstants.lightGray);
			line.setLineWidth(1);
			root.add(line);
			final RectangleFigure rectangleText = new RectangleFigure();
			root.add(rectangleText, new Rectangle(new Point(Border - TextWidth - TextOffset, (int) (height + Border - i * yGradWidth) + TextHeight / 3), new Point(new Point(Border - TextOffset, (int) (height + Border - i * yGradWidth - TextHeight / 1.5)))));
			rectangleText.setBackgroundColor(ColorConstants.white);
			rectangleText.setForegroundColor(ColorConstants.white);
			final double max = Math.max(qualities.get(qualities.size() - 1).getGain(), qualities.get(qualities.size() - 1).getLoss());
			final double value = i == yGradNumber ? max : (double) ((long) (i * qualityWidth * 10) / 10.0);
			final Label label = new Label("" + value);
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

		root.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClicked(final MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(final MouseEvent arg0) {
				if (qualities != null)
					if (arg0.x > Border && arg0.x < root.getSize().width() - Border) {
						final float param = 1 - (float) (arg0.x - Border) / (root.getSize().width() - 2 * Border);
						ocelotlView.getParam().setText(String.valueOf(param));
						createDiagram();
						ocelotlView.setConfiguration();
					}
			}

			@Override
			public void mouseReleased(final MouseEvent arg0) {
				// TODO Auto-generated method stub
				ocelotlView.getBtnRun().notifyListeners(SWT.Selection, new Event());

			}

		});

		return canvas;
	}

	public void resizeDiagram() {
		createDiagram();
		root.repaint();
	}

	public void YGrads() {
		final double maxValue = Math.max(qualities.get(qualities.size() - 1).getGain(), qualities.get(qualities.size() - 1).getLoss());
		long temp = (long) maxValue;
		int i;
		for (i = 1; temp > 10; i++)
			temp /= 10;
		final long factor = temp < 6 ? YGradDefaultNumber : temp;
		for (int j = 1; j < i; j++)
			temp *= 10;
		qualityWidth = (double) temp / (double) factor;
		yGradNumber = maxValue / qualityWidth;
		yGradWidth = (root.getSize().height - 2 * Border - 1) / yGradNumber;
		while (yGradWidth < YGradWidthMin && yGradNumber > 6) {
			yGradNumber /= 2;
			yGradWidth *= 2;
			qualityWidth *= 2;
		}
	}

}
