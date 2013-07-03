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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
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
	final static int	AxisBorder	= 50;
	final static int	AxisWidth = 1;
	final static int	YGrads = 10;
	double 				yGrad = 10.0;
	double				yGradRealWidth = 10.0;
	final static long	YGradWidthMin=20;
	double				yGradWidth = 50;
	final static int	GradHeight = 8;
	final static int	TextWidth = 50;
	final static int	TextHeight = 16;
	final static long	MiniDivide = 5;
	final static int	MiniGradHeight = 4;
	int					Space	= 6;
	final static double	XGrads = 10;
	List<Quality> 		qualities;
	float 				currentParameter;

	final ColorManager	colors	= new ColorManager();

	public void drawXYLines() {
		RectangleFigure rectangleX = new RectangleFigure();
		RectangleFigure rectangleY = new RectangleFigure();
		root.add(rectangleX, new Rectangle(new Point(AxisBorder, root.getSize().height()-AxisBorder-AxisWidth), new Point(root.getSize().width()-AxisBorder, root.getSize().height()-AxisBorder)));
		rectangleX.setBackgroundColor(ColorConstants.darkGray);
		rectangleX.setForegroundColor(ColorConstants.darkGray);
		root.add(rectangleY, new Rectangle(new Point(AxisBorder-AxisWidth, AxisBorder), new Point(AxisBorder, root.getSize().height()-AxisBorder)));
		rectangleY.setBackgroundColor(ColorConstants.darkGray);
		rectangleY.setForegroundColor(ColorConstants.darkGray);
		rectangleX.setLineWidth(1);
		rectangleY.setLineWidth(1);	
	}

	public void YGrads() {
		double maxValue = Math.max(qualities.get(qualities.size()-1).getGain(), qualities.get(qualities.size()-1).getLoss());
		long temp=(long)maxValue;
		int i;
		for (i=1; temp>10; i++)
			temp/=10;
		long factor =  temp<6?YGrads:temp;
		for (int j=1; j<i; j++)
			temp*=10;
		yGradRealWidth=(double)temp/(double)factor;
		yGrad=maxValue/yGradRealWidth;
		yGradWidth=(root.getSize().height-2*AxisBorder-1)/yGrad;
		while (yGradWidth<YGradWidthMin&&yGrad>6){
			yGrad/=2;
			yGradWidth*=2;
			yGradRealWidth*=2;
		}	
	}

	public void drawXGrads() {
		double width=(double)(root.getSize().width-2*AxisBorder)/XGrads;
		for (int i=0; i<XGrads+1; i++){
			PolylineConnection line = new PolylineConnection();
			line.setEndpoints(new Point((int)(i*width+AxisBorder), root.getSize().height()-AxisBorder), new Point(new Point((int)(i*width+AxisBorder), AxisBorder)));
			line.setBackgroundColor(ColorConstants.lightGray);
			line.setForegroundColor(ColorConstants.lightGray);
			line.setLineWidth(1);
			root.add(line);
			RectangleFigure rectangleText = new RectangleFigure();
			//if (i!=(int)yGrad)
			root.add(rectangleText, new Rectangle(new Point((int)(i*width+AxisBorder- TextWidth/2), root.getSize().height()-AxisBorder+2), new Point(new Point((int)(i*width+AxisBorder+AxisWidth + TextWidth/2), root.getSize().height()-AxisBorder-AxisWidth +TextHeight+2))));
			//else
			//root.add(rectangleText, new Rectangle(new Point((int)(i*yGradWidth)-Border*3, root.getSize().height()/3+2), new Point(new Point((int)(i*yGradWidth)+TimeAxisWidth+TextWidth, root.getSize().height()/3+2+TextHeight))));
			rectangleText.setBackgroundColor(ColorConstants.white);
			rectangleText.setForegroundColor(ColorConstants.white);
			//long value = (long)(i*yGradRealWidth+time.getTimeStampStart());
			//text.replace(',', '.');
			float value = (float) ((10-(int)i)*0.1);
			Label label = new Label(""+value);
			label.setLabelAlignment(SWT.CENTER);
			label.setForegroundColor(ColorConstants.darkGray);
			rectangleText.setFont(SWTResourceManager.getFont("Cantarell", TextHeight/2, SWT.NORMAL));
			rectangleText.setLineWidth(1);
			rectangleText.add(label);
			ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			//rectangleText.setConstraint(rectangleText, rectangleText.getBounds());
			rectangleText.setLayoutManager(layout);
			if (i!=(int)XGrads&&(width/MiniDivide)>yGradWidth/10)
				for (int j=1; j<5; j++){
					PolylineConnection lineDash = new PolylineConnection();
					lineDash.setEndpoints(new Point((int)(i*width+AxisBorder)+(int)((j*width)/MiniDivide), root.getSize().height()-AxisBorder), new Point(new Point((int)(i*width+AxisBorder)+(int)((j*width)/MiniDivide), AxisBorder)));
					lineDash.setBackgroundColor(ColorConstants.lightGray);
					lineDash.setForegroundColor(ColorConstants.lightGray);
					lineDash.setLineWidth(1);
					lineDash.setLineStyle(SWT.LINE_DASH);
					root.add(lineDash);
				}
		}
	}
	
	public void drawYGrads() {
		YGrads();
		double width=(double)(root.getSize().width-2*AxisBorder);
		double height=(double)(root.getSize().height-2*AxisBorder);
		for (int i=0; i<(int)yGrad+1; i++){
			PolylineConnection line = new PolylineConnection();
			line.setEndpoints(new Point((int)(AxisBorder),(int)((height+AxisBorder)-i*yGradWidth)), new Point((int)(width+AxisBorder), (int)((height+AxisBorder)-i*yGradWidth)));
			line.setBackgroundColor(ColorConstants.lightGray);
			line.setForegroundColor(ColorConstants.lightGray);
			line.setLineWidth(1);
			root.add(line);
			RectangleFigure rectangleText = new RectangleFigure();
			//if (i!=(int)yGrad)
			root.add(rectangleText, new Rectangle(new Point((int)(AxisBorder- TextWidth-2), (int)((height+AxisBorder)-i*yGradWidth)+TextHeight/3), new Point(new Point((int)(AxisBorder-2), (int)((height+AxisBorder)-i*yGradWidth-TextHeight/1.5)))));
			//else
			//root.add(rectangleText, new Rectangle(new Point((int)(i*yGradWidth)-Border*3, root.getSize().height()/3+2), new Point(new Point((int)(i*yGradWidth)+TimeAxisWidth+TextWidth, root.getSize().height()/3+2+TextHeight))));
			rectangleText.setBackgroundColor(ColorConstants.white);
			rectangleText.setForegroundColor(ColorConstants.white);
			//long value = (long)(i*yGradRealWidth+time.getTimeStampStart());
			//text.replace(',', '.');
			double max = Math.max(qualities.get(qualities.size()-1).getGain(), qualities.get(qualities.size()-1).getLoss());
			double value = i==yGrad?max: (double)((long)(i*yGradRealWidth*10)/10.0);
			Label label = new Label(""+value);
			label.setLabelAlignment(SWT.CENTER);
			label.setForegroundColor(ColorConstants.darkGray);
			rectangleText.setFont(SWTResourceManager.getFont("Cantarell", TextHeight/2, SWT.NORMAL));
			rectangleText.setLineWidth(1);
			rectangleText.add(label);
			ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			//rectangleText.setConstraint(rectangleText, rectangleText.getBounds());
			rectangleText.setLayoutManager(layout);
//			if (i!=(int)XGrads)
//				for (int j=1; j<5; j++){
//					PolylineConnection lineDash = new PolylineConnection();
//					lineDash.setEndpoints(new Point((int)(i*width+AxisBorder)+(int)((j*width)/MiniDivide), root.getSize().height()-AxisBorder), new Point(new Point((int)(i*width+AxisBorder)+(int)((j*width)/MiniDivide), AxisBorder)));
//					lineDash.setBackgroundColor(ColorConstants.lightGray);
//					lineDash.setForegroundColor(ColorConstants.lightGray);
//					lineDash.setLineWidth(1);
//					lineDash.setLineStyle(SWT.LINE_DASH);
//					root.add(lineDash);
//				}
		}
	}


	public void drawQualities() {
		int i;
		double maxValue = Math.max(qualities.get(qualities.size()-1).getGain(), qualities.get(qualities.size()-1).getLoss());
		int width=root.getSize().width-2*AxisBorder;
		int yOff=root.getSize().height()-AxisBorder;
		int height=root.getSize().height()-2*AxisBorder;
		qualities.add(new Quality(qualities.get(qualities.size()-1).getGain(), qualities.get(qualities.size()-1).getLoss(), 1));
		for (i=1; i< qualities.size(); i++){
			float cParam=1-qualities.get(i).getParameter();
			float nParam=1-qualities.get(i-1).getParameter();
			double cgain=qualities.get(i).getGain();
			double ngain=qualities.get(i-1).getGain();
			double closs=qualities.get(i).getLoss();
			double nloss=qualities.get(i-1).getLoss();
			PolylineConnection lineGain1 = new PolylineConnection();
			lineGain1.setEndpoints(new Point((int)(AxisBorder+(width*cParam)), (int)(yOff-(height*cgain)/maxValue)), new Point((int)(AxisBorder+(width*cParam)), (int)(yOff-(height*ngain)/maxValue)));
			PolylineConnection lineLoss1 = new PolylineConnection();
			lineLoss1.setEndpoints(new Point((int)(AxisBorder+(width*cParam)), (int)(yOff-(height*closs)/maxValue)), new Point((int)(AxisBorder+(width*cParam)), (int)(yOff-(height*nloss)/maxValue)));
			PolylineConnection lineGain2 = new PolylineConnection();
			lineGain2.setEndpoints(new Point((int)(AxisBorder+(width*cParam)), (int)(yOff-(height*ngain)/maxValue)), new Point((int)(AxisBorder+(width*nParam)), (int)(yOff-(height*ngain)/maxValue)));
			PolylineConnection lineLoss2 = new PolylineConnection();
			lineLoss2.setEndpoints(new Point((int)(AxisBorder+(width*cParam)), (int)(yOff-(height*nloss)/maxValue)), new Point((int)(AxisBorder+(width*nParam)), (int)(yOff-(height*nloss)/maxValue)));
			lineGain1.setForegroundColor(ColorConstants.green);
			lineLoss1.setForegroundColor(ColorConstants.red);
			lineGain1.setLineWidthFloat((float) 1.7);
			lineLoss1.setLineWidthFloat((float) 1.7);
			root.add(lineGain1);
			root.add(lineLoss1);
			lineGain2.setForegroundColor(ColorConstants.green);
			lineLoss2.setForegroundColor(ColorConstants.red);
			lineGain2.setLineWidthFloat((float) 1.7);
			lineLoss2.setLineWidthFloat((float) 1.7);
			root.add(lineGain2);
			root.add(lineLoss2);
		}
		qualities.remove(qualities.size()-1);
	}

	public void drawParam() {
		if (currentParameter==-1)
			return;
		//int i;
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
		line.setLineWidthFloat((float) 1.7);
		root.add(line);
		//}
		//}
		//qualities.remove(qualities.size()-1);
	}



	public void createDiagram(List<Quality> qualities, float currentParameter) {
		root.removeAll();
		this.qualities=qualities;
		this.currentParameter=currentParameter;
		if (qualities!=null){
			drawXGrads();
			drawYGrads();
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
