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

package fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.AggregatedData;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy.Aggregation;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.visualizations.parts.views.PartColorManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.Proportion;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.views.IconManager;

public class HierarchyPart {

	private int					index;
	private static final int	Border		= PartMatrixView.Border;
	private int	space		= 3;
	private EventProducerHierarchy hierarchy;
	private IFigure				root;
	private double rootHeight;
	private double height;
	private double width;
	private double logicWidth;
	private double logicHeight;
	private int minLogicWeight = 2;
	private PartColorManager colors;
	private List<Integer> xendlist;

	public HierarchyPart(EventProducerHierarchy hierarchy, final IFigure root, PartColorManager colors, int space) {
		super();
		setIndex(index);
		this.root = root;
		this.hierarchy = hierarchy;
		this.colors = colors;
		xendlist=new ArrayList<Integer>();
		this.space=space;
	}

	private void initX(){
		xendlist.clear();
		for (int i=0; i<=hierarchy.getRoot().getParts().size(); i++)
			xendlist.add((int)(i*logicWidth+Border-space));
	}
	
	public void draw() {
		rootHeight = root.getSize().height;
		height = 9.0 / 10.0 * rootHeight;
		width = root.getSize().width - (2 * Border);
		logicWidth = (double) width/(double) (hierarchy.getRoot().getParts().size());
		logicHeight = (double) height/(double) hierarchy.getRoot().getWeight();
		initX();
		print(hierarchy.getRoot().getID(), 0, hierarchy.getRoot().getParts().size());
	}
	
	private List<Part> computeParts(EventProducerNode epn, int start, int end){
		List<Part> parts = new ArrayList<Part>();
		int oldPart = epn.getParts().get(start);
		parts.add(new Part(start, start+1, new AggregatedData(epn.getParts().get(start)!=-1, epn.getParts().get(start))));
		for (int i = start + 1; i < end; i++){
			if (epn.getParts().get(i) == oldPart){
				parts.get(parts.size() - 1).incrSize();

			}
			else {
				oldPart = epn.getParts().get(i);
				parts.add(new Part(i, i+1, null));
				parts.get(parts.size() - 1).setData(new AggregatedData(epn.getParts().get(i)!=-1, epn.getParts().get(i)));

			}
		}
		return parts;
	}
	
	private void print(int id, int start, int end){
		EventProducerNode epn=hierarchy.getEventProducerNodes().get(id);
		List<Part> parts = computeParts(epn, start, end);
		for (Part p:parts){
			if (((AggregatedData) p.getData()).isAggregated())			
				drawStandardAggregate(p.getStartPart(), epn.getIndex(), p.getEndPart(), epn.getWeight(), ((AggregatedData) p.getData()).getValue(), epn.getMe().getName());
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
					for (Part pagg: aggParts)
						drawVisualAggregate(pagg.getStartPart(), epn.getIndex(), pagg.getEndPart(), epn.getWeight(), ((AggregatedData) p.getData()).getValue(), epn.getMe().getName());
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
				for (EventProducerNode child:epn.getChildrenNodes()) {
					commonCut=false;
					for (Part p2: hm.get(child)){
						if (p.compare(p2)){
							commonCut=true;
							break;
						}
							
					}
					if (commonCut==false)
						break;
				}
			if (commonCut){
				commonParts.add(new Part(p.getStartPart(), p.getEndPart(), null));
			}
		}
		if (commonParts.isEmpty()){
			parts.add(new Part(start, end, null));
			return parts;
		}
		if (commonParts.get(0).getStartPart()!=start)
			parts.add(new Part(start, commonParts.get(0).getStartPart(), null));
		for (Part ptemp:commonParts){
			if (parts.size()>0&&parts.get(parts.size()-1).getEndPart()!=ptemp.getStartPart())
				parts.add(new Part(parts.get(parts.size()-1).getEndPart(), ptemp.getStartPart(), null));
			parts.add(ptemp);
		}
		if (parts.get(parts.size()-1).getEndPart()!=end)
			parts.add(new Part(parts.get(parts.size()-1).getEndPart(), end, null));	
		return parts;
	}

	private void printChildren(int id, int start, int end){
		for (EventProducerNode ep: hierarchy.getEventProducerNodes().get(id).getChildrenNodes())
			print(ep.getID(), start, end);
	}
	
	private void drawStandardAggregate(int logicX, int logicY, int logicX2, int sizeY, int number, String name){
		final RectangleFigure rectangle = new RectangleFigure();
		rectangle.setBackgroundColor(colors.getColors().get(number).getBg());
		rectangle.setForegroundColor(colors.getColors().get(number).getBg());
		rectangle.setLineWidth(2);

		rectangle.setToolTip(new Label(" "+name+" "));
		int xa=(int) (((double) logicX * logicWidth + Border));
		int ya=(int) (rootHeight - height + logicY * logicHeight);
		int xb=xendlist.get(logicX2);
		int yb=(int) (ya + sizeY * logicHeight)- space;
		root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb, yb)));
	}
	
	private void drawVisualAggregate(int logicX, int logicY, int logicX2, int sizeY, int number, String name){
		final RectangleFigure rectangle = new RectangleFigure();
		rectangle.setBackgroundColor(ColorConstants.black);
		rectangle.setForegroundColor(ColorConstants.black);
		rectangle.setLineWidth(2);

		rectangle.setToolTip(new Label(" "+name+" "));
		int xa=(int) (((double) logicX * logicWidth + Border));
		int ya=(int) (rootHeight - height + logicY * logicHeight);
		int xb=xendlist.get(logicX2);
		int yb=(int) (ya + sizeY * logicHeight)- space;
		root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb, yb)));
	}

	private void setIndex(final int index) {
		this.index = index;
	}

}