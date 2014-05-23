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

public class HierarchyStateProportion {

	private int					index;
	private static final int	Border		= PartMatrixView.Border;
	private static final int	Space		= 2;
	private EventProducerHierarchy hierarchy;
	private IFigure				root;
	private double rootHeight;
	private double height;
	private double width;
	private double logicWidth;
	private double logicHeight;
	private PartColorManager colors;

	public HierarchyStateProportion(EventProducerHierarchy hierarchy, final IFigure root, PartColorManager colors) {
		super();
		setIndex(index);
		this.root = root;
		this.hierarchy = hierarchy;
		this.colors = colors;
	}

	public void draw() {
		rootHeight = root.getSize().height;
		height = 9.0 / 10.0 * rootHeight;
		width = root.getSize().width - 2 * Border;
		logicWidth = hierarchy.getRoot().getParts().size();
		logicHeight = hierarchy.getRoot().getWeight();
		print(hierarchy.getRoot().getID(), 0, (int) logicWidth);
	}
	
	public void print(int id, int start, int end){
		EventProducerNode epn=hierarchy.getEventProducerNodes().get(id);
		int size=1;
		List<Part> parts = new ArrayList<Part>();
		
		int oldPart = epn.getParts().get(start);
		parts.add(new Part(start, start+size, null));
		for (int i = start; i < end; i++){
			if (epn.getParts().get(i) == oldPart){
				parts.get(parts.size() - 1).setEndPart(i + 1);
				parts.get(parts.size() - 1).setData(new AggregatedData(epn.getParts().get(i)!=-1, epn.getParts().get(i)));
			}
			else {
				oldPart = epn.getParts().get(i);
				parts.add(new Part(i, i + 1, null));
				parts.get(parts.size() - 1).setData(new AggregatedData(epn.getParts().get(i)!=-1, epn.getParts().get(i)));

			}
		}
		
		for (Part p:parts){
			if (((AggregatedData) p.getData()).isAggregated())
				addRectangle(p.getStartPart(), epn.getIndex(), p.getEndPart(), epn.getWeight(), ((AggregatedData) p.getData()).getValue(), epn.getMe().getName());
			else
				printChildren(id, p.getStartPart(), p.getEndPart());
		}	
		
	}
	
	public void printChildren(int id, int start, int end){
		for (EventProducerNode ep: hierarchy.getEventProducerNodes().get(id).getChildrenNodes())
			print(ep.getID(), start, end);
	}
	
	public void addRectangle(int logicX, int logicY, int sizeX, int sizeY, int number, String name){
		final RectangleFigure rectangle = new RectangleFigure();
		rectangle.setBackgroundColor(colors.getColors().get(number).getBg());
		rectangle.setLineWidth(2);
		rectangle.setForegroundColor(colors.getColors().get(number).getBg());
		rectangle.setToolTip(new Label(" "+name+" "));
		int xa=(int) (((double) logicX * (double) (width / logicWidth)) + Border);
		int ya=(int) (rootHeight - height + height * logicY / logicHeight);
		int xb=xa+(int) ((double) sizeX * ( double) (width / logicWidth) + Border)-Space;
		int yb=(int) (ya + height * sizeY / logicHeight);
		root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb, yb)));
	}

	public void setIndex(final int index) {
		this.index = index;
	}

}