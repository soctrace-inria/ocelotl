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

package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.HierarchyAxisMouseListener;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.UnitAxisView;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.VisuSTOperator;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;

/**
 * Hierarchy Axis View : Show the hierarchy of the event producer nodes as
 * colored rectangles
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class HierarchyAxisView extends UnitAxisView {

	protected EventProducerHierarchy hierarchy;
	protected ArrayList<EventProducerNode> producers;

	// Level of hierarchy to display
	protected int hierarchyLevel;
	protected int smallestDisplayableHierarchyLevel;
	HashMap<Integer, Integer> sameHierarchy = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> newHierarchy = new HashMap<Integer, Integer>();
	protected int spaceDirty = 10;
	protected int spaceDirty2 = 1;
	protected int iterationDirty = 3;
	
	protected int levelWidth = 15;
	final protected int MinLevelWidth = 10;
	protected int areaWidth = 0;
	protected int verticalBorder = 10;
	protected int horizontalBorder = 5;
	protected double rootHeight;
	protected double height;
	protected double logicHeight;
	protected List<Integer> yendlist;
	protected List<Integer> xendlist;
	protected final int verticalSpace = 3;
	protected final int horizontalSpace = 5;
	protected int minLogicWeight = OcelotlConstants.MinimalHeightDrawingThreshold;
	
	protected SelectFigure selectFigure;
	protected SelectFigure highLightAggregateFigure;
	protected int originY;
	protected int cornerY;
	
	public HierarchyAxisView() {
		super();
		selectFigure = new SelectFigure();
		mouse = new HierarchyAxisMouseListener(this);
		xendlist = new ArrayList<Integer>();
		yendlist = new ArrayList<Integer>();
	}

	public void createDiagram(EventProducerHierarchy hierarchy,
			boolean activeSelection) {
		root.removeAll();
		this.hierarchy = hierarchy;
		if (hierarchy != null && !hierarchy.getEventProducerNodes().isEmpty()) {
			drawHierarchy();
			if (originY != -1 && cornerY != -1)
				selectFigure.draw(originY, cornerY, activeSelection);
			if(currentlySelectedEpn != null)
				highLightSelectedProducer.draw(currentlySelectedEpn);
		}
	}

	@Override
	public void createDiagram(IVisuOperator manager) {
		root.removeAll();
		VisuSTOperator propOperator = (VisuSTOperator) manager;
		hierarchy = propOperator.getHierarchy();
		if (!hierarchy.getEventProducerNodes().isEmpty()) {
			drawHierarchy();
		}
	}

	/**
	 * Print the hierarchy for a given eventproducerNode
	 * 
	 * @param epn
	 *            eventProducerNode
	 */
	protected void print(EventProducerNode epn) {
		if (epn.getHierarchyLevel() > smallestDisplayableHierarchyLevel)
			return;
		
		if (newHierarchy.containsKey(epn.getHierarchyLevel())) 
			drawProd(epn, newHierarchy.get(epn.getHierarchyLevel()), false);

		boolean tooSmall = false;
		// Check for each child that we have enough vertical space
		// to display them
		for (EventProducerNode ep : epn.getChildrenNodes()) {
			if ((ep.getWeight() * logicHeight - verticalSpace) < minLogicWeight) {
				// Too small
				tooSmall = true;
				break;
			}
		}
		// If enough space
		// recursively call print() on the children node
		if (!tooSmall) {
			printChildren(epn);
		} else if (epn.getHierarchyLevel() + 1 <= smallestDisplayableHierarchyLevel) {
			// draw with dirty texture
			drawProd(epn, newHierarchy.get(epn.getHierarchyLevel() + 1), true);
		}
	}

	/**
	 * Draw an event producer node
	 * 
	 * @param epn
	 *            the drawn epn
	 * @param currentHierarchyLevel
	 *            the current hierarchy level
	 * @param dirty
	 *            is it an aggregate of epn ?
	 */
	public void drawProd(EventProducerNode epn, int currentHierarchyLevel, boolean dirty) {
		String text = epn.getMe().getName();
		
		// If it is similar to another hierarchy
		if(sameHierarchy.containsValue(epn.getHierarchyLevel()))
			for(Integer level: sameHierarchy.keySet())
				if(sameHierarchy.get(level) == epn.getHierarchyLevel())
					// Add the name of the similar level to the label text
					text = text + " / " + hierarchy.getEventProducerNodesFromHierarchyLevel(level).get(0).getMe().getName();		
		
		final Label label = new Label(" " + text + " ");
		label.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		RectangleFigure rectangle = new RectangleFigure();
		rectangle.setBackgroundColor(FramesocColorManager.getInstance()
				.getEventProducerColor(epn.getMe().getName()).getSwtColor());
		rectangle.setForegroundColor(FramesocColorManager.getInstance()
				.getEventProducerColor(epn.getMe().getName()).getSwtColor());
		rectangle.setToolTip(label);

		int xa = (int) (areaWidth - horizontalBorder - levelWidth * (hierarchyLevel - currentHierarchyLevel + 1));
		int ya = (int) (rootHeight - height + epn.getIndex() * logicHeight - verticalBorder);
		int xb = xendlist.get(currentHierarchyLevel);
		int yb = yendlist.get(epn.getIndex() + epn.getWeight());

		Rectangle boundrect =  new Rectangle(new Point(xa, ya), new Point(xb, yb));
		root.add(rectangle, boundrect);

		if (dirty) 
			drawTextureDirty(xa, xb, ya, yb, label.getText());
				
		// Save the rectangle in a map
		eventProdToFigures.put(epn, boundrect);
		figuresToEventProd.put(boundrect, epn);
	}
	
	/**
	 * Draw the graduation and the name of the event producers
	 */
	public void drawHierarchy() {
		eventProdToFigures.clear();
		figuresToEventProd.clear();
		hierarchyLevel = 0;
		smallestDisplayableHierarchyLevel = 0;
		areaWidth = root.getClientArea().width();
		
		computeGradMeasure();
	
		print(hierarchy.getRoot());
	}
	
	/**
	 * If all the event producer node of a hierarchy level are too small to be
	 * represented, then do not show them
	 * 
	 * @param epn
	 *            the tested event producer node
	 */
	public void findSmallestDisplayableHierarchy(EventProducerNode epn) {
		// Check for each child that we have enough vertical space
		// to display them
		for (EventProducerNode ep : epn.getChildrenNodes()) {
			if ((ep.getWeight() * logicHeight - verticalSpace) >= minLogicWeight) {
				// Too small
				if (ep.getHierarchyLevel() > smallestDisplayableHierarchyLevel)
					smallestDisplayableHierarchyLevel = ep.getHierarchyLevel();
			}
			findSmallestDisplayableHierarchy(ep);
		}
	}

	/**
	 * Compute the number and size of the graduations to draw
	 */
	public void computeGradMeasure() {
		rootHeight = root.getClientArea().height;
		height = rootHeight - (2 * verticalBorder);
		logicHeight = height / hierarchy.getRoot().getWeight();
		
		findSmallestDisplayableHierarchy(hierarchy.getRoot());
		findSameHierarchy();
		hierarchyLevel = smallestDisplayableHierarchyLevel - sameHierarchy.keySet().size();
		
		rebuildHierarchy();
		
		initX(hierarchy.getRoot());
		initY(hierarchy.getRoot());
	}
	
	/**
	 * Build an index with contiguous values to access the displayed hierarchy
	 * levels
	 */
	private void rebuildHierarchy() {
		newHierarchy = new HashMap<Integer, Integer>();

		// Shift the hierarchy level in order for the displayed ones to be
		// contiguous
		int cpt = 0;
		for (int i = 0; i <= smallestDisplayableHierarchyLevel; i++) {
			if (!sameHierarchy.containsKey(i)) {
				newHierarchy.put(i, cpt);
				cpt++;
			}
		}

		// Compute the width of a hierarchy level
		levelWidth = (areaWidth - horizontalBorder * 2)
				/ newHierarchy.keySet().size();

		// If smaller than the min
		if (levelWidth < MinLevelWidth && newHierarchy.keySet().size() > 1) {
			// Remove root
			newHierarchy.remove(0);
			hierarchyLevel--;

			// Update hierarchies
			cpt = 0;
			for (int i = 0; i <= smallestDisplayableHierarchyLevel; i++) {
				if (newHierarchy.containsKey(i)) {
					newHierarchy.put(i, cpt);
					cpt++;
				}
			}
			
			// Recompute the width
			levelWidth = (areaWidth - horizontalBorder * 2)
					/ newHierarchy.keySet().size();

			if (levelWidth < MinLevelWidth && newHierarchy.keySet().size() > 1) {

				// If the leaves are displayed
				if (smallestDisplayableHierarchyLevel == hierarchy
						.getMaxHierarchyLevel()) {

					// Remove leaves
					newHierarchy.remove(smallestDisplayableHierarchyLevel);
					hierarchyLevel--;
					
					// Update hierarchies
					cpt = 0;
					for (int i = 0; i <= smallestDisplayableHierarchyLevel; i++) {
						if (newHierarchy.containsKey(i)) {
							newHierarchy.put(i, cpt);
							cpt++;
						}
					}
				}
			}
		}
		
		// Recompute width
		levelWidth = (areaWidth - horizontalBorder * 2)
				/ newHierarchy.keySet().size();
		
		//Still too small ?
		if (levelWidth < MinLevelWidth) {
			// Put min width
			levelWidth = MinLevelWidth;
		}
	}

	/**
	 * Recursively print all the children of the event producer with the
	 * given id
	 * 
	 * @param epn
	 *            the event producer node
	 */
	protected void printChildren(EventProducerNode epn) {
		for (EventProducerNode epnChild : epn.getChildrenNodes())
			print(epnChild);
	}
	
	/**
	 * Compute the list of Y values according to the weight of the node
	 */
	protected void initY(EventProducerNode epn) {
		yendlist.clear();
		for (int i = 0; i <= epn.getWeight(); i++)
			yendlist.add((int) (rootHeight - height + i * logicHeight
					- verticalSpace - verticalBorder));
	}
	
	/**
	 * Compute the list of X values according to the hierarchy level
	 */
	protected void initX(EventProducerNode epn) {
		xendlist.clear();
		for (int i = 0; i <= hierarchyLevel; i++)
			xendlist.add((int) (areaWidth - horizontalBorder - horizontalSpace - levelWidth
					* (hierarchyLevel - i)));
	}

	/**
	 * Sort event producer based on their id
	 */
	public void sortPorducers() {
		Collections.sort(producers,
				new Comparator<EventProducerHierarchy.EventProducerNode>() {

					@Override
					public int compare(final EventProducerNode arg0,
							final EventProducerNode arg1) {
						int diff = arg1.getMe().getId() - arg0.getMe().getId();

						return diff;
					}
				});
	}

	/**
	 * Search the hierarchy level that are similar
	 */
	public void findSameHierarchy() {
		sameHierarchy = new HashMap<Integer, Integer>();
		int i;

		for (i = 0; i < smallestDisplayableHierarchyLevel; i++) {
			List<EventProducerNode> producers = hierarchy
					.getEventProducerNodesFromHierarchyLevel(i);
			boolean different = false;
			// If one of the node has more than one child (or none), then it is
			// different
			for (EventProducerNode epn : producers) {
				if (epn.getChildrenNodes().isEmpty()
						|| epn.getChildrenNodes().size() != 1) {
					different = true;
					break;
				}
			}
			if (!different) {
				// If similar, associate it with the similar level
				if (sameHierarchy.containsKey(i)) {
					sameHierarchy.put(i + 1, sameHierarchy.get(i));
				} else {
					sameHierarchy.put(i + 1, i);
				}
			}
		}
	}
	
	/**
	 * Draw vertical stripes to signal that event producers were aggregated 
	 * @param xa
	 * @param xb
	 * @param ya
	 * @param yb
	 * @param label
	 */
	protected void drawTextureDirty(int xa, int xb, int ya, int yb, String label) {
		int i = 0;
		for (int x = xa + spaceDirty2; x < (xb + yb - ya); x = x + spaceDirty2
				+ 1) {
			i++;

			if (i > iterationDirty - 1) {
				i = 0;
				x += spaceDirty;
			}

			if (x >= (xb + yb - ya)) {
				break;
			}

			final PolylineConnection line = new PolylineConnection();
			int xinit = x;
			int yinit = ya;
			int xfinal = Math.max(xa, (xinit - (yb - ya)));
			int yfinal = Math.min(yb, ya + xinit - xfinal);

			if (xb < xinit) {
				yinit = Math.min(yb, ya + xinit - xb);
				xinit = xb;
			}

			line.setBackgroundColor(ColorConstants.white);
			line.setForegroundColor(ColorConstants.white);
			line.setEndpoints(new Point(xinit, yinit),
					new Point(xfinal, yfinal));
			line.setLineWidth(1);
			line.setAntialias(SWT.ON);
			line.setToolTip(new Label(label));
			root.add(line);
		}
	}

	public void initDiagram() {
		if (ocelotlView.getMainViewTopSashform().getWeights()[0] == 0) {
			ocelotlView.getMainViewTopSashform().setWeights(
					OcelotlConstants.yAxisDefaultWeight);
			ocelotlView.getMainViewTopSashform().layout();
		}
	}

	public void resizeDiagram() {
		root.removeAll();
		createDiagram(hierarchy, true);
		root.repaint();
	}
	
	public void resizeDiagram(boolean activeSelection) {
		root.removeAll();
		createDiagram(hierarchy, activeSelection);
		root.repaint();
	}

	public void unselect() {
		originY = -1;
		cornerY = -1;
		currentlySelectedEpn = null;
		
		if (highLightSelectedProducer != null) {
			highLightSelectedProducer.delete();
		}
			
		resizeDiagram();
	}

	@Override
	public void select(int y0, int y1, boolean active) {
		originY = y0;
		cornerY = y1;

		resizeDiagram(active);
	}
}
