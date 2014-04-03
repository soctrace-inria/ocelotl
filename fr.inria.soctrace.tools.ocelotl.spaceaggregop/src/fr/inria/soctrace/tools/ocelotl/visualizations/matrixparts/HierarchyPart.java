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

package fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.visualizations.parts.views.PartColorManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.Proportion;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.views.IconManager;

public class HierarchyPart {

	private int					index;
	private static final int	Border		= PartMatrixView.Border;
	private static final int	Space		= 3;
	private EventProducerHierarchy hierarchy;
	private IFigure				root;
	private double rootHeight;
	private double height;
	private double width;
	private double logicWidth;
	private double logicHeight;
	private int minLogicWeight = 4;
	private PartColorManager colors;
	private List<Integer> xendlist;

	public HierarchyPart(EventProducerHierarchy hierarchy, final IFigure root, PartColorManager colors) {
		super();
		setIndex(index);
		this.root = root;
		this.hierarchy = hierarchy;
		this.colors = colors;
		xendlist=new ArrayList<Integer>();
	}

	private void initX(){
		xendlist.clear();
		for (int i=0; i<=hierarchy.getRoot().getParts().size(); i++)
			xendlist.add((int)(i*logicWidth+Border-Space));
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
	
	private void print(int id, int start, int end){
		EventProducerNode epn=hierarchy.getEventProducerNodes().get(id);
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
		for (Part p:parts){
			if (((AggregatedData) p.getData()).isAggregated())			
				addRectangle(p.getStartPart(), epn.getIndex(), p.getEndPart(), epn.getWeight(), ((AggregatedData) p.getData()).getValue(), epn.getMe().getName(), false);
			else{
				boolean aggy=false;
				for (EventProducerNode ep: epn.getChildrenNodes()){
					if (((double) ep.getWeight()*logicHeight)<minLogicWeight){
						aggy=true;
						break;
					}
				}
				if (aggy==false)
					printChildren(id, p.getStartPart(), p.getEndPart());
				else
					addRectangle(p.getStartPart(), epn.getIndex(), p.getEndPart(), epn.getWeight(), ((AggregatedData) p.getData()).getValue(), epn.getMe().getName(), true);
			}
		}	
		
	}
	
	private void printChildren(int id, int start, int end){
		for (EventProducerNode ep: hierarchy.getEventProducerNodes().get(id).getChildrenNodes())
			print(ep.getID(), start, end);
	}
	
	private void addRectangle(int logicX, int logicY, int logicX2, int sizeY, int number, String name, boolean visualaggregate){
		final RectangleFigure rectangle = new RectangleFigure();
		if (!visualaggregate){
		rectangle.setBackgroundColor(colors.getColors().get(number).getBg());
		rectangle.setForegroundColor(colors.getColors().get(number).getBg());
		}
		else{
			rectangle.setBackgroundColor(ColorConstants.black);
			rectangle.setForegroundColor(ColorConstants.black);
		}
		rectangle.setLineWidth(2);

		rectangle.setToolTip(new Label(" "+name+" "));
		int xa=(int) (((double) logicX * logicWidth + Border));
		int ya=(int) (rootHeight - height + logicY * logicHeight);
		int xb=xendlist.get(logicX2);
		int yb=(int) (ya + sizeY * logicHeight)- Space;
		root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb, yb)));
	}

	private void setIndex(final int index) {
		this.index = index;
	}

}