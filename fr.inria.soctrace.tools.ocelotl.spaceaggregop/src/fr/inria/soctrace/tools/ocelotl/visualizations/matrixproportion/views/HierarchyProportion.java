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

package fr.inria.soctrace.tools.ocelotl.visualizations.matrixproportion.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.AggregatedData;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy.Aggregation;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.ui.com.eclipse.wb.swt.SWTResourceManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts.VisualAggregatedData;
import fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts.views.PartMatrixView;
import fr.inria.soctrace.tools.ocelotl.visualizations.matrixproportion.MajState;
import fr.inria.soctrace.tools.ocelotl.visualizations.matrixproportion.MatrixProportion;
import fr.inria.soctrace.tools.ocelotl.visualizations.parts.views.PartColorManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.Proportion;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.views.IconManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.views.StateColorManager;

public class HierarchyProportion {

	private int					index;
	private static final int	Border		= PartMatrixView.Border;
	private static final int ColorThreshold = 175;
	private static final int AlphaThreshold = 190;
	private int	space		= 3;
	private int rectangleBorder = 1;
	private EventProducerHierarchy hierarchy;
	private IFigure				root;
	private double rootHeight;
	private double height;
	private double width;
	private double logicWidth;
	private double logicHeight;
	private int minLogicWeight = 1;
	private List<Integer> xendlist;
	private List<Integer> yendlist;
	private MatrixProportion proportion;

	public HierarchyProportion(MatrixProportion proportion, EventProducerHierarchy hierarchy, final IFigure root, int space) {
		super();
		setIndex(index);
		this.root = root;
		this.hierarchy = hierarchy;
		xendlist=new ArrayList<Integer>();
		yendlist=new ArrayList<Integer>();
		this.space=space;
		this.proportion=proportion;
	}

	private void initX(){
		xendlist.clear();
		for (int i=0; i<=hierarchy.getRoot().getParts().size(); i++)
			xendlist.add((int)(i*logicWidth+Border-space));
	}
	
	private void initY(){
		yendlist.clear();
		for (int i=0; i<=hierarchy.getRoot().getWeight(); i++)
			yendlist.add((int)(rootHeight - height + i * logicHeight -space - Border));
	}
	
	public void draw() {
		rootHeight = root.getSize().height;
		height = rootHeight - (2* Border);
		width = root.getSize().width - (2 * Border);
		logicWidth = (double) width/(double) (hierarchy.getRoot().getParts().size());
		logicHeight = (double) height/(double) hierarchy.getRoot().getWeight();
		initX();
		initY();
		print(hierarchy.getRoot().getID(), 0, hierarchy.getRoot().getParts().size());
	}
	
	private List<Part> computeParts(EventProducerNode epn, int start, int end){
		List<Part> parts = new ArrayList<Part>();
		int oldPart = epn.getParts().get(start);
		parts.add(new Part(start, start+1, new VisualAggregatedData(false, epn.getParts().get(start)!=-1, epn.getParts().get(start), true)));
		for (int i = start + 1; i < end; i++){
			if (epn.getParts().get(i) == oldPart){
				parts.get(parts.size() - 1).incrSize();

			}
			else {
				oldPart = epn.getParts().get(i);
				parts.add(new Part(i, i+1, null));
				parts.get(parts.size() - 1).setData(new VisualAggregatedData(false, epn.getParts().get(i)!=-1, epn.getParts().get(i), true));

			}
		}
		return parts;
	}
	
	private void print(int id, int start, int end){
		EventProducerNode epn=hierarchy.getEventProducerNodes().get(id);
		List<Part> parts = computeParts(epn, start, end);
		for (Part p:parts){
			if (((VisualAggregatedData) p.getData()).isAggregated())			
				drawStandardAggregate(p.getStartPart(), epn.getIndex(), p.getEndPart(), epn.getWeight(), ((VisualAggregatedData) p.getData()).getValue(), epn);
			else{
				boolean aggy=false;
				for (EventProducerNode ep: epn.getChildrenNodes()){
					if (((double) ep.getWeight()*logicHeight-(double) space)<minLogicWeight){
						aggy=true;
						break;
					}
				}
				if (aggy==false)
					printChildren(id, p.getStartPart(), p.getEndPart());
				else{
					List<Part> aggParts = computeCommonCuts(epn, p.getStartPart(), p.getEndPart());
					for (Part pagg: aggParts){
					if (((VisualAggregatedData) pagg.getData()).isNoCutInside())	
						drawCleanVisualAggregate(pagg.getStartPart(), epn.getIndex(), pagg.getEndPart(), epn.getWeight(), ((VisualAggregatedData) p.getData()).getValue(), epn);
					else
						drawNotCleanVisualAggregate(pagg.getStartPart(), epn.getIndex(), pagg.getEndPart(), epn.getWeight(), ((VisualAggregatedData) p.getData()).getValue(), epn);

					}	
				}
			}
		}	
		
	}
	
	private List<Part> computeCommonCuts(EventProducerNode epn, int start, int end) {
		HashMap<EventProducerNode, List<Part>> hm = new HashMap<EventProducerNode, List<Part> >();
		List<Part> commonParts = new ArrayList<Part>();
		List<Part> parts = new ArrayList<Part>();
		for (EventProducerNode child:epn.getChildrenNodes()){
			if (child.isAggregated()==Aggregation.FULL)
				hm.put(child, computeParts(child, start, end));
			else
				hm.put(child, computeCommonCuts(child, start, end));
		}
		List<Part> testPart=hm.get(epn.getChildrenNodes().get(0));
		for (Part p:testPart){
				boolean commonCut=false;
				boolean cleanCut= true;
				for (EventProducerNode child:epn.getChildrenNodes()) {
					commonCut=false;
					for (Part p2: hm.get(child)){
						if (p.compare(p2)){
							commonCut=true;
							if (((VisualAggregatedData) p2.getData()).isVisualAggregate()&&!((VisualAggregatedData) p2.getData()).isNoCutInside())
								cleanCut=false;
							break;
						}
							
					}
					if (commonCut==false)
						break;
				}
			if (commonCut){
				commonParts.add(new Part(p.getStartPart(), p.getEndPart(), new VisualAggregatedData(true, false, -1, cleanCut)));
			}
		}
		if (commonParts.isEmpty()){
			parts.add(new Part(start, end, new VisualAggregatedData(true, false, -1, false)));
			return parts;
		}
		if (commonParts.get(0).getStartPart()!=start)
			parts.add(new Part(start, commonParts.get(0).getStartPart(), new VisualAggregatedData(true, false, -1, false)));
		for (Part ptemp:commonParts){
			if (parts.size()>0&&parts.get(parts.size()-1).getEndPart()!=ptemp.getStartPart())
				parts.add(new Part(parts.get(parts.size()-1).getEndPart(), ptemp.getStartPart(), new VisualAggregatedData(true, false, -1, false)));
			parts.add(ptemp);
		}
		if (parts.get(parts.size()-1).getEndPart()!=end)
			parts.add(new Part(parts.get(parts.size()-1).getEndPart(), end, new VisualAggregatedData(true, false, -1, false)));	
		return parts;
	}

	private void printChildren(int id, int start, int end){
		for (EventProducerNode ep: hierarchy.getEventProducerNodes().get(id).getChildrenNodes())
			print(ep.getID(), start, end);
	}
	
	private void drawStandardAggregate(int logicX, int logicY, int logicX2, int sizeY, int number, EventProducerNode epn){
		final RectangleFigure rectangle = new RectangleFigure();
		MajState state=proportion.getMajState(epn, logicX, logicX2);
		rectangle.setBackgroundColor(FramesocColorManager.getInstance().getEventTypeColor(state.getState()).getSwtColor());
		rectangle.setForegroundColor(FramesocColorManager.getInstance().getEventTypeColor(state.getState()).getSwtColor());
		rectangle.setAlpha(state.getAmplitude255M());
		rectangle.setLineWidth(1);

		rectangle.setToolTip(new Label(" "+epn.getMe().getName()+" ("+state.getState()+", "+state.getAmplitude100()+"%) "));
		int xa=(int) (((double) logicX * logicWidth + Border));
		int ya=(int) (rootHeight - height + logicY * logicHeight - Border);
		int xb=xendlist.get(logicX2);
		int yb=yendlist.get(logicY+sizeY);
		root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb, yb)));
	}
	
	private void drawNotCleanVisualAggregate(int logicX, int logicY, int logicX2, int sizeY, int number, EventProducerNode epn){
		final RectangleFigure rectangle = new RectangleFigure();
		MajState state=proportion.getMajState(epn, logicX, logicX2);
		rectangle.setBackgroundColor(FramesocColorManager.getInstance().getEventTypeColor(state.getState()).getSwtColor());
		rectangle.setForegroundColor(FramesocColorManager.getInstance().getEventTypeColor(state.getState()).getSwtColor());
		rectangle.setAlpha(state.getAmplitude255M());
		rectangle.setLineWidth(1);
		rectangle.setToolTip(new Label(" "+epn.getMe().getName()+" ("+state.getState()+", "+state.getAmplitude100()+"%) "));
		rectangle.setLayoutManager(new BorderLayout());
		rectangle.setPreferredSize(1000, 1000);
		Label lab = new Label("?");
		lab.setTextAlignment(PositionConstants.CENTER);
		lab.setLabelAlignment(SWT.CENTER);
		lab.setForegroundColor(ColorConstants.black);
		rectangle.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.BOLD));
		//rectangle.add(lab, BorderLayout.CENTER);
		int xa=(int) (((double) logicX * logicWidth + Border));
		int ya=(int) (rootHeight - height + logicY * logicHeight - Border);
		int xb=xendlist.get(logicX2);
		int yb=yendlist.get(logicY+sizeY);
		root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb, yb)));
		final PolylineConnection line = new PolylineConnection();
		Color color=ColorConstants.black;
		boolean light=isColorLight(rectangle.getBackgroundColor(), rectangle.getAlpha());
		if (!light){
			color=ColorConstants.white;
		}
		line.setBackgroundColor(color);
		line.setForegroundColor(color);
		line.setEndpoints(new Point(xa, ya), new Point(xb, yb));
		line.setAntialias(SWT.ON);
		line.setLineWidth(1);
		//line.setAlpha(state.getAmplitude255());
		root.add(line);
		final PolylineConnection line2 = new PolylineConnection();
		line2.setBackgroundColor(color);
		line2.setForegroundColor(color);
		line2.setEndpoints(new Point(xa, yb), new Point(xb, ya));
		line2.setAntialias(SWT.ON);
		line2.setLineWidth(1);
		root.add(line2);
		//line2.setAlpha(state.getAmplitude255());
		if (light){
			drawRectangleBorder(xa, xb, ya, yb);
		}

	}
	
	private void drawRectangleBorder(int xa, int xb, int ya, int yb){
		final PolylineConnection rect1 = new PolylineConnection();
		rect1.setBackgroundColor(ColorConstants.black);
		rect1.setForegroundColor(ColorConstants.black);
		rect1.setLineWidth(rectangleBorder);
		rect1.setEndpoints(new Point(xa, ya), new Point(xb, ya));
		final PolylineConnection rect2 = new PolylineConnection();
		rect2.setBackgroundColor(ColorConstants.black);
		rect2.setForegroundColor(ColorConstants.black);
		rect2.setLineWidth(rectangleBorder);
		rect2.setEndpoints(new Point(xa, yb), new Point(xb, yb));
		final PolylineConnection rect3 = new PolylineConnection();
		rect3.setBackgroundColor(ColorConstants.black);
		rect3.setForegroundColor(ColorConstants.black);
		rect3.setLineWidth(rectangleBorder);
		rect3.setEndpoints(new Point(xa, ya), new Point(xa, yb));
		final PolylineConnection rect4 = new PolylineConnection();
		rect4.setBackgroundColor(ColorConstants.black);
		rect4.setForegroundColor(ColorConstants.black);
		rect4.setLineWidth(rectangleBorder);
		rect4.setEndpoints(new Point(xb, ya), new Point(xb, yb));
		root.add(rect1);
		root.add(rect2);
		root.add(rect3);
		root.add(rect4);
	}
	
	private boolean isColorLight(Color color, int alpha){
		return (alpha<AlphaThreshold||((color.getBlue()<ColorThreshold)&&(color.getRed()<ColorThreshold)&&(color.getGreen()<ColorThreshold)));
	}
	
	private void drawCleanVisualAggregate(int logicX, int logicY, int logicX2, int sizeY, int number, EventProducerNode epn){
		final RectangleFigure rectangle = new RectangleFigure();
		MajState state=proportion.getMajState(epn, logicX, logicX2);
		rectangle.setBackgroundColor(FramesocColorManager.getInstance().getEventTypeColor(state.getState()).getSwtColor());
		rectangle.setForegroundColor(FramesocColorManager.getInstance().getEventTypeColor(state.getState()).getSwtColor());
		rectangle.setAlpha(state.getAmplitude255M());
		rectangle.setLineWidth(1);

		rectangle.setToolTip(new Label(" "+epn.getMe().getName()+" ("+state.getState()+", "+state.getAmplitude100()+"%) "));

		rectangle.setLayoutManager(new BorderLayout());
		rectangle.setPreferredSize(1000, 1000);
		Label lab = new Label("?");
		lab.setTextAlignment(PositionConstants.CENTER);
		lab.setLabelAlignment(SWT.CENTER);
		lab.setForegroundColor(ColorConstants.black);
		rectangle.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.BOLD));
		//rectangle.add(lab, BorderLayout.CENTER);
		int xa=(int) (((double) logicX * logicWidth + Border));
		int ya=(int) (rootHeight - height + logicY * logicHeight - Border);
		int xb=xendlist.get(logicX2);
		int yb=yendlist.get(logicY+sizeY);
		root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb, yb)));
		final PolylineConnection line = new PolylineConnection();
		Color color=ColorConstants.black;
		boolean light=isColorLight(rectangle.getBackgroundColor(), rectangle.getAlpha());
		if (!light){
			color=ColorConstants.white;
		}
		line.setBackgroundColor(color);
		line.setForegroundColor(color);
		line.setEndpoints(new Point(xa, yb), new Point(xb, ya));
		line.setLineWidth(1);
		line.setAntialias(SWT.ON);
		root.add(line);
		if (light){
			drawRectangleBorder(xa, xb, ya, yb);
		}
		
	}

	private void setIndex(final int index) {
		this.index = index;
	}

}