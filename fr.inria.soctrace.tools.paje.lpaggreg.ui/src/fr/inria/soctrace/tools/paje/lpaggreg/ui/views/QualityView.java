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

package fr.inria.soctrace.tools.paje.lpaggreg.ui.views;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.paje.lpaggreg.core.Quality;
import fr.inria.soctrace.tools.paje.lpaggreg.core.TimeRegion;

/** 
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>" 
 */
public class QualityView {

	Figure				root;
	Canvas				canvas;
	TimeRegion 			time;
	final static int	Height	= 100;
	final static int	AxisBorder	= 30;
	final static int	TimeAxisWidth = 1;
	final static long	Divide = 10;
	double 				GradNumber = 10.0;
	double				GradDuration = 10.0;
	final static long	GradWidthMin=50;
	double				GradWidth = 50;
	final static int	GradHeight = 8;
	final static int	TextWidth = 50;
	final static int	TextHeight = 20;
	final static long	MiniDivide = 5;
	final static int	MiniGradHeight = 4;
	int					Space	= 6;
	List<Quality> 		qualities;
	float 				currentParameter;

	final ColorManager	colors	= new ColorManager();

	public void drawXYLines() {
		RectangleFigure rectangleX = new RectangleFigure();
		RectangleFigure rectangleY = new RectangleFigure();
		root.add(rectangleX, new Rectangle(new Point(AxisBorder, root.getSize().height()-AxisBorder), new Point(root.getSize().width()-AxisBorder, root.getSize().height()-AxisBorder)));
		rectangleX.setBackgroundColor(ColorConstants.darkGray);
		rectangleX.setForegroundColor(ColorConstants.darkGray);
		root.add(rectangleY, new Rectangle(new Point(AxisBorder, AxisBorder), new Point(AxisBorder, root.getSize().height()-AxisBorder)));
		rectangleY.setBackgroundColor(ColorConstants.darkGray);
		rectangleY.setForegroundColor(ColorConstants.darkGray);
		rectangleX.setLineWidth(1);
		rectangleY.setLineWidth(1);	
	}
	
	public void drawQualities() {
		int i;
		double maxValue = Math.max(qualities.get(qualities.size()-1).getGain(), qualities.get(qualities.size()-1).getLoss());
		int width=root.getSize().width-2*AxisBorder;
		int yOff=root.getSize().height()-AxisBorder;
		int height=root.getSize().height()-2*AxisBorder;
		qualities.add(new Quality(qualities.get(qualities.size()-1).getGain(), qualities.get(qualities.size()-1).getLoss(), 1));
		for (i=0; i< qualities.size()-1; i++){
			float cParam=1-qualities.get(i).getParameter();
			float nParam=1-qualities.get(i+1).getParameter();
			double cgain=qualities.get(i).getGain();
			double ngain=qualities.get(i+1).getGain();
			double closs=qualities.get(i).getLoss();
			double nloss=qualities.get(i+1).getLoss();
			PolylineConnection lineGain = new PolylineConnection();
			lineGain.setEndpoints(new Point((int)(AxisBorder+(width*cParam)), (int)(yOff-(height*cgain)/maxValue)), new Point((int)(AxisBorder+(width*nParam)), (int)(yOff-(height*ngain)/maxValue)));
			PolylineConnection lineLoss = new PolylineConnection();
			lineLoss.setEndpoints(new Point((int)(AxisBorder+(width*cParam)), (int)(yOff-(height*closs)/maxValue)), new Point((int)(AxisBorder+(width*nParam)), (int)(yOff-(height*nloss)/maxValue)));
			lineGain.setForegroundColor(ColorConstants.green);
			lineLoss.setForegroundColor(ColorConstants.red);
			root.add(lineGain);
			root.add(lineLoss);
		}
		qualities.remove(qualities.size()-1);
	}
	
	public void drawParam() {
		if (currentParameter==-1)
			return;
		int i;
		//double maxValue = Math.max(qualities.get(qualities.size()-1).getGain(), qualities.get(qualities.size()-1).getLoss());
		int width=root.getSize().width-2*AxisBorder;
		int yOff=root.getSize().height()-AxisBorder;
		//int height=root.getSize().height()-2*AxisBorder;
		//for (i=0; i< qualities.size()-1; i++){
			//if (currentParameter==qualities.get(i).getParameter()){
				//float cParam=1-qualities.get(i).getParameter();
			//double cgain=qualities.get(i).getGain();
			//double closs=qualities.get(i).getLoss();
				PolylineConnection line = new PolylineConnection();
				line.setEndpoints(new Point((int)(AxisBorder+(width*(1-currentParameter))), (int)(yOff)), new Point((int)(AxisBorder+(width*(1-currentParameter))), AxisBorder));
				line.setForegroundColor(ColorConstants.blue);
				root.add(line);
			//}
		//}
		//qualities.remove(qualities.size()-1);
	}
	
//	public void grads() {
//		long duration = time.getTimeDuration();
//		long temp=duration;
//		int i;
//		for (i=1; temp>10; i++)
//			temp/=10;
//		long factor = temp<6?Divide:temp;
//		for (int j=1; j<i; j++)
//			temp*=10;
//		GradDuration=(double)temp/(double)factor;
//		GradNumber=duration/GradDuration;
//		GradWidth=(root.getSize().width-(2*AxisBorder)-1)/GradNumber;
//		while (GradWidth<GradWidthMin&&GradNumber>6){
//			GradNumber/=2;
//			GradWidth*=2;
//			GradDuration*=2;
//		}	
//	}
//	
//	public void drawGrads() {
//		grads();
//		NumberFormat formatter = null;
//		formatter=java.text.NumberFormat.getInstance(java.util.Locale.US);
//		formatter = new DecimalFormat("0.00E0");
//		for (int i=0; i<(int)GradNumber+1; i++){
//			RectangleFigure rectangle = new RectangleFigure();
//			root.add(rectangle, new Rectangle(new Point((int)(i*GradWidth)+AxisBorder, root.getSize().height()/3), new Point(new Point((int)(i*GradWidth)+AxisBorder+TimeAxisWidth, root.getSize().height()/3-(int)GradHeight))));
//			rectangle.setBackgroundColor(ColorConstants.darkGray);
//			rectangle.setForegroundColor(ColorConstants.darkGray);
//			rectangle.setLineWidth(1);	
//			RectangleFigure rectangleText = new RectangleFigure();
//			if (i!=(int)GradNumber)
//				root.add(rectangleText, new Rectangle(new Point((int)(i*GradWidth), root.getSize().height()/3+2), new Point(new Point((int)(i*GradWidth)+TimeAxisWidth+TextWidth, root.getSize().height()/3+2+TextHeight))));
//			else
//				root.add(rectangleText, new Rectangle(new Point((int)(i*GradWidth)-AxisBorder*3, root.getSize().height()/3+2), new Point(new Point((int)(i*GradWidth)+TimeAxisWidth+TextWidth, root.getSize().height()/3+2+TextHeight))));
//			rectangleText.setBackgroundColor(ColorConstants.white);
//			rectangleText.setForegroundColor(ColorConstants.white);
//			long value = (long)(i*GradDuration+time.getTimeStampStart());
//			String text = formatter.format(value);
//			//text.replace(',', '.');
//			Label label = new Label(text);
//			label.setLabelAlignment(SWT.CENTER);
//			label.setForegroundColor(ColorConstants.darkGray);
//			rectangleText.setFont(SWTResourceManager.getFont("Cantarell", TextHeight/2, SWT.NORMAL));
//			rectangleText.setLineWidth(1);
//			rectangleText.add(label);
//			ToolbarLayout layout = new ToolbarLayout();
//			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
//			//rectangleText.setConstraint(rectangleText, rectangleText.getBounds());
//			rectangleText.setLayoutManager(layout);
//			//if (i!=(int)GradNumber)
//			for (int j=1; j<5; j++){
//				RectangleFigure rectangle2 = new RectangleFigure();
//				if ((int)(i*GradWidth)+AxisBorder+(int)((j*GradWidth)/MiniDivide)>root.getSize().width()-AxisBorder)
//					break;
//				root.add(rectangle2, new Rectangle(new Point((int)(i*GradWidth)+AxisBorder+(int)((j*GradWidth)/MiniDivide), root.getSize().height()/3), new Point(new Point((int)(i*GradWidth)+AxisBorder+(int)((j*GradWidth)/MiniDivide), root.getSize().height()/3-(int)MiniGradHeight))));
//				rectangle2.setBackgroundColor(ColorConstants.gray);
//				rectangle2.setForegroundColor(ColorConstants.gray);
//				rectangle2.setLineWidth(1);
//			}
//		}
//	}
	
	
	public void createDiagram(List<Quality> qualities, float currentParameter) {
		root.removeAll();
		this.qualities=qualities;
		this.currentParameter=currentParameter;
		if (qualities!=null){
			drawXYLines();
			drawQualities();
			drawParam();
		}
		canvas.update();
	}

	public Canvas initDiagram(Composite parent) {
		root = new Figure();
		root.setFont(parent.getFont());
		XYLayout layout = new XYLayout();
		root.setLayoutManager(layout);
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.gray);
		canvas.setSize(parent.getSize());
		LightweightSystem lws = new LightweightSystem(canvas);
		lws.setContents(root);
		lws.setControl(canvas);
		root.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		root.setSize(parent.getSize().x, parent.getSize().y);
		canvas.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(ControlEvent arg0) {
				// TODO Auto-generated method stub
				canvas.redraw();
			 root.repaint();
				resizeDiagram();

			}

			@Override
			public void controlResized(ControlEvent arg0) {
				canvas.redraw();
				root.repaint();
				resizeDiagram();
			}
		});

		return canvas;
	}

	public void resizeDiagram() {
		createDiagram(qualities, currentParameter);
		root.repaint();
	}

}
