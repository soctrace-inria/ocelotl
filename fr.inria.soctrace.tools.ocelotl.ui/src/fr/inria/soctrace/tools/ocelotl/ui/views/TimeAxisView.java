/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.color.ColorManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.QualityView.State;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class TimeAxisView {

	Figure				root;
	Canvas				canvas;
	TimeRegion			time;
	TimeRegion			selectTime;
	TimeRegion			resetTime;
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
	private OcelotlView	ocelotlView;
	

	public TimeAxisView(OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	public void createDiagram(TimeRegion time) {
		root.removeAll();
		this.time = time;
		if (time != null) {
			this.resetTime = new TimeRegion(time);
			this.selectTime = new TimeRegion(time);
			drawMainLine();
			drawGrads();
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
		TimeMouseListener mouse = new TimeMouseListener();
		root.addMouseListener(mouse);
		root.addMouseMotionListener(mouse);
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
	
	static public enum State {
		PRESSED_D, DRAG_D, PRESSED_G, RELEASED;
	}
	
	class TimeMouseListener implements MouseListener, MouseMotionListener{
		
		State state = State.RELEASED;

	
	public void mouseDoubleClicked(final MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
	public void mousePressed(final MouseEvent arg0) {
		//if (arg0.button==MouseEvent.BUTTON1){
			selectTime = ocelotlView.getTimeRegion();
			state = State.PRESSED_D;
			long temp=((long)((double)((arg0.x - Border)*resetTime.getTimeDuration())/(root.getSize().width()-2*Border))+resetTime.getTimeStampStart());
			if (temp<selectTime.getTimeStampEnd())
				selectTime.setTimeStampStart(temp);
			else
				selectTime.setTimeStampEnd(temp);
			//selectTime.setTimeStampStart(500);
		//}else{
	//		state = State.PRESSED_G;
	//		selectTime=new TimeRegion(resetTime);
	//	}
		ocelotlView.setTimeRegion(selectTime);	
	}

	public void mouseReleased(final MouseEvent arg0) {
		state= State.RELEASED;	
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if (state == State.PRESSED_D || state == State.DRAG_D){
			state = State.DRAG_D;
				long p3= (long)((double)((arg0.x - Border)*resetTime.getTimeDuration())/(root.getSize().width()-2*Border))+resetTime.getTimeStampStart();
				long p1= selectTime.getTimeStampStart();
				long p2= selectTime.getTimeStampEnd();
				if (p3>p1)
					p2=p3;
				else
					p1=p3;
				selectTime=new TimeRegion(p1, p2);
				ocelotlView.setTimeRegion(selectTime);
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseHover(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

		}
		
	}

	

}
