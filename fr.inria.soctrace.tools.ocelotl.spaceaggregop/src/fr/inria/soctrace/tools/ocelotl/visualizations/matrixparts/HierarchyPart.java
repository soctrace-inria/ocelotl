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
import fr.inria.soctrace.tools.ocelotl.visualizations.stateproportion.StateProportion;
import fr.inria.soctrace.tools.ocelotl.visualizations.stateproportion.views.IconManager;

public class HierarchyPart {

	private int					index;
	private static final int	Border		= StateProportionMatrixView.Border;
	private static final int	Space		= 2;
	private EventProducerHierarchy hierarchy;
	private IFigure				root;
	private double rootHeight;
	private double height;
	private double width;
	private double logicWidth;
	private double logicHeight;
	private PartColorManager colors;

	public HierarchyPart(EventProducerHierarchy hierarchy, final IFigure root, PartColorManager colors) {
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
//		
//	
//		for (final String state : states) {
//			final double value = ((PartMap) distribution.getPart(index).getData()).getElements().get(state);
//			if (value > 0) {
//				// System.out.println("Part " + index + " " + state + " " +
//				// value);
//				final RectangleFigure rect = new RectangleFigure();
//				rect.setBackgroundColor(ColorConstants.white);
//				rect.setBackgroundColor(FramesocColorManager.getInstance().getEventTypeColor(state).getSwtColor());
//				rect.setForegroundColor(ColorConstants.white);
//				final Label label = new Label(" " + state + " ");
//				rect.setToolTip(label);
//				if (y1 * value / m > MinHeight) {
//					root.add(rect, new Rectangle(new Point((int) (distribution.getPart(index).getStartPart() * width / logicWidth + Border), (int) (rootHeight - y1 * total / m)), new Point((int) (distribution.getPart(index).getEndPart() * width / logicWidth - space + 1 + Border),
//							(int) (rootHeight + 1 - y1 * (total + value) / m))));
//					total += value;
//				} else {
//					agg += value;
//					aggList.add(state);
//				}
//				label.getUpdateManager().performUpdate();
//				rect.getUpdateManager().performUpdate();
//			}
//		}
//		if (agg != 0) {
//			// System.out.println("Part " + index + " " + "Aggregate" + " " +
//			// agg);
//			final ImageFigure icon = new ImageFigure();
//			final RectangleFigure rectangle = new RectangleFigure();
//			icon.setBackgroundColor(ColorConstants.black);
//			icon.setForegroundColor(ColorConstants.white);
//			rectangle.setBackgroundColor(ColorConstants.black);
//			rectangle.setForegroundColor(ColorConstants.white);
//			
//			
//			String aggString = " ";
//			for (int i = 0; i < aggList.size() - 1; i++)
//				aggString = aggString + aggList.get(i) + " + ";
//			aggString = aggString + aggList.get(aggList.size() - 1) + " ";
//			final Label label = new Label(aggString);
//			icon.setToolTip(label);
//			rectangle.setToolTip(label);
//			final PolylineConnection lineDash = new PolylineConnection();
//			lineDash.setBackgroundColor(ColorConstants.lightGray);
//			lineDash.setForegroundColor(ColorConstants.lightGray);
//			lineDash.setLineWidth(2);
//			lineDash.setLineStyle(SWT.LINE_DASH);
//				if (y1 * agg /m > MinHeight){
//					root.add(rectangle, new Rectangle(new Point((int) (distribution.getPart(index).getStartPart() * width / logicWidth + Border), (int) (rootHeight - y1 * total / m)), new Point((int) (distribution.getPart(index).getEndPart() * width / logicWidth - space + 1 + Border),
//							(int) (rootHeight + 1 - y1 * (total + agg) / m))));
//				}
//				else{
//					int size = (int) Math.min(IconMax,Math.min(width/logicWidth-2*space, (rootHeight - y1 * total / m)));
//				if (size>IconMin){
//					icon.setImage(iconManager.getImage(size));
//
//					lineDash.setEndpoints(new Point((int) (distribution.getPart(index).getStartPart() * width / logicWidth + Border + 1), (int) (rootHeight - y1 * total / m) - 1), new Point((int) (distribution.getPart(index).getEndPart() * width / logicWidth - space + Border), (int) (rootHeight - y1
//							* (total) / m)- 1));
//					root.add(lineDash);
//				root.add(icon, new Rectangle(new Point((int) (distribution.getPart(index).getStartPart() * width / logicWidth + Border), (int) (rootHeight - y1 * total / m) - 2), new Point((int) (distribution.getPart(index).getEndPart() * width / logicWidth - space + 1 + Border), (int) (rootHeight - y1
//					* (total) / m)- size - 2)));
//				}
//				else{
//					lineDash.setEndpoints(new Point((int) (distribution.getPart(index).getStartPart() * width / logicWidth + Border + 1), (int) (rootHeight - y1 * total / m) - 1), new Point((int) (distribution.getPart(index).getEndPart() * width / logicWidth - space + Border), (int) (rootHeight - y1
//							* (total) / m)- 1));
//					root.add(lineDash);
//				}
//			
//				}
//			label.getUpdateManager().performUpdate();
//			icon.getUpdateManager().performUpdate();
//				
//		}
//	}
	
	public void print(int id, int start, int end){
		EventProducerNode epn=hierarchy.getEventProducerNodes().get(id);
		int s=start;
		int e=end;
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
				addRectangle(p.getStartPart(), epn.getIndex(), p.getEndPart(), epn.getWeight(), ((AggregatedData) p.getData()).getValue());
			else
				printChildren(id, p.getStartPart(), p.getEndPart());
		}	
		
	}
	
	public void printChildren(int id, int start, int end){
		for (EventProducerNode ep: hierarchy.getEventProducerNodes().get(id).getChildrenNodes())
			print(ep.getID(), start, end);
	}
	
	public void addRectangle(int logicX, int logicY, int sizeX, int sizeY, int number){
		final RectangleFigure rectangle = new RectangleFigure();
		rectangle.setBackgroundColor(colors.getColors().get(number).getBg());
		rectangle.setLineWidth(2);
		rectangle.setForegroundColor(colors.getColors().get(number).getBg());
		rectangle.setToolTip(new Label(String.valueOf(number)));
		int xa=(int) (logicX * width / logicWidth + Border);
		int ya=(int) (rootHeight - height + height * logicY / logicHeight);
		int xb=xa+(int) (sizeX * width / logicWidth + Border);
		int yb=(int) (ya + height * sizeY / logicHeight);
		root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb, yb)));
	}

	public void setIndex(final int index) {
		this.index = index;
	}

}