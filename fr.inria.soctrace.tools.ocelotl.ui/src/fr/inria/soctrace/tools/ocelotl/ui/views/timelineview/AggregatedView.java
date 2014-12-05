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

package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
abstract public class AggregatedView implements IAggregatedView {

	protected Figure							root;
	protected Canvas							canvas;
	protected final List<RectangleFigure>		figures				= new ArrayList<RectangleFigure>();
	protected TimeRegion						time;
	protected TimeRegion						selectTime;
	protected TimeRegion						potentialSelectTime;
	protected TimeRegion						resetTime;
	protected int								aBorder				= 10;
	protected final int							space				= 3;
	protected final OcelotlView					ocelotlView;
	protected SelectFigure						selectFigure;
	protected SelectFigure						highLightAggregateFigure;
	protected SelectFigure						potentialSelectFigure;
	protected EventProducerNode					currentlySelectedNode;
	protected IVisuOperator						visuOperator		= null;
	protected OcelotlMouseListener				mouse;
	protected List<SpatioTemporalAggregateView>	aggregates;
	public final static Color					selectColorFG		= ColorConstants.white;
	public final static Color					selectColorBG		= ColorConstants.blue;
	public final static Color					potentialColorFG	= ColorConstants.darkBlue;
	public final static Color					potentialColorBG	= ColorConstants.darkBlue;
	public final static Color					activeColorFG		= ColorConstants.black;
	public final static Color					activeColorBG		= ColorConstants.black;
	
	class SelectFigure extends RectangleFigure {

		private SelectFigure() {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			setForegroundColor(selectColorFG);
			setBackgroundColor(selectColorBG);
			setAlpha(120);
		}
		
		private SelectFigure(Color foreGround, Color backGround) {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			setForegroundColor(foreGround);
			setBackgroundColor(backGround);
		}

		/**
		 * Draw the current select figure
		 * 
		 * @param timeRegion
		 *            selected time region
		 * @param active
		 *            Is the selection currently active
		 * @param y0
		 *            origin height
		 * @param y1
		 *            corner height
		 */
		public void draw(final TimeRegion timeRegion, final boolean active, int y0, int y1) {
			if (active) {
				setForegroundColor(activeColorFG);
				setBackgroundColor(activeColorBG);
				setFill(true);
				setAlpha(120);
			} else {
				setForegroundColor(selectColorFG);
				setBackgroundColor(selectColorBG);
				setFill(false);
				setAlpha(250);
			}
			
			if (getParent() != root)
				root.add(this);

			// Default values for selecting the height of the graph
			if (y0 == -1)
				y0 = root.getSize().height - 1;

			if (y1 == -1)
				y1 = 2;
			
			root.setConstraint(this, new Rectangle(new Point((int) ((timeRegion.getTimeStampStart() - time.getTimeStampStart()) * (root.getSize().width - 2 * aBorder) / time.getTimeDuration() + aBorder), y0), new Point(
					((int) ((timeRegion.getTimeStampEnd() - time.getTimeStampStart()) * (root.getSize().width - 2 * aBorder) / time.getTimeDuration() + aBorder)) - space, y1)));
			root.repaint();
		}
		
		public void draw(final TimeRegion timeRegion, int y0, int y1) {
			if (getParent() != root)
				root.add(this);
			
			// Default values for selecting the height of the graph
			if (y0 == -1)
				y0 = root.getSize().height - 1;

			if (y1 == -1)
				y1 = 2;
					
			root.setConstraint(this, new Rectangle(new Point((int) ((timeRegion.getTimeStampStart() - time.getTimeStampStart()) * (root.getSize().width - 2 * aBorder) / time.getTimeDuration() + aBorder), y0), new Point(
					((int) ((timeRegion.getTimeStampEnd() - time.getTimeStampStart()) * (root.getSize().width - 2 * aBorder) / time.getTimeDuration() + aBorder)) - space, y1)));
			root.repaint();
		}
		
		/**
		 * Remove the selection from display
		 */
		public void delete() {
			if (getParent() != null)
				root.remove(this);
		}
	}
	
	/**
	 * States of the mouse
	 * 
	 * PRESSED_LEFT: Left button pressed
	 * DRAG_LEFT_VERTICAL/DRAG_LEFT_HORIZONTAL: dragging of the mouse with the left button on the vertical axis (horizontal resp.)
	 * RELEASED: Button is released
	 * EXITED: Cursor out of the zone
	 * H/V_MOVE_START/END: starting a horizontal/vertical (H/V) dragging move from one side (left, right, up or bottom)
	 */
	static public enum MouseState {
		PRESSED_D, DRAG_D, PRESSED_LEFT, DRAG_LEFT, DRAG_LEFT_START, 
		DRAG_LEFT_VERTICAL, DRAG_LEFT_HORIZONTAL,
		RELEASED, H_MOVE_START, H_MOVE_END, V_MOVE_START, V_MOVE_END, EXITED;
	}

	public static Color getActivecolorbg() {
		return activeColorBG;
	}

	public static Color getActivecolorfg() {
		return activeColorFG;
	}

	public static Color getSelectcolorbg() {
		return selectColorBG;
	}

	public static Color getSelectcolorfg() {
		return selectColorFG;
	}

	public AggregatedView(final OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	abstract protected void computeDiagram();

	@Override
	public void deleteDiagram() {
		root.removeAll();
		figures.clear();
		root.repaint();
	}
	
	@Override
	public void drawSelection() {
		if (selectTime != null) {
			mouse.drawSelection();
		}
	}
	
	public void deleteSelectFigure() {
		selectFigure.delete();
		selectTime = null;
		setCurrentlySelectedNode(null);
	}

	public int getBorder() {
		return aBorder;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public long getEnd() {
		return selectTime.getTimeStampEnd();
	}

	public List<RectangleFigure> getFigures() {
		return figures;
	}

	public OcelotlView getOcelotlView() {
		return ocelotlView;
	}

	public TimeRegion getResetTime() {
		return resetTime;
	}

	@Override
	public Figure getRoot() {
		return root;
	}

	public SelectFigure getSelectFigure() {
		return selectFigure;
	}

	public TimeRegion getSelectTime() {
		return selectTime;
	}

	public TimeRegion getPotentialSelectTime() {
		return potentialSelectTime;
	}

	public void setPotentialSelectTime(TimeRegion potentialSelectTime) {
		this.potentialSelectTime = potentialSelectTime;
	}

	public void setSelectTime(TimeRegion selectTime) {
		this.selectTime = selectTime;
	}

	public SelectFigure getHighLightAggregateFigure() {
		return highLightAggregateFigure;
	}

	public int getSpace() {
		return space;
	}

	@Override
	public long getStart() {
		return selectTime.getTimeStampStart();
	}

	public TimeRegion getTime() {
		return time;
	}

	public IVisuOperator getVisuOperator() {
		return visuOperator;
	}

	public List<SpatioTemporalAggregateView> getAggregates() {
		return aggregates;
	}

	public void setAggregates(ArrayList<SpatioTemporalAggregateView> aggregateMapping) {
		this.aggregates = aggregateMapping;
	}

	public SelectFigure getPotentialSelectFigure() {
		return potentialSelectFigure;
	}

	public void setPotentialSelectFigure(SelectFigure potentialSelectFigure) {
		this.potentialSelectFigure = potentialSelectFigure;
	}

	@Override
	public void init(final TimeLineViewWrapper wrapper) {
		root = wrapper.getRoot();
		canvas = wrapper.getCanvas();
		wrapper.cleanControlListeners();
		wrapper.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(final ControlEvent arg0) {
				canvas.redraw();
				resizeDiagram();
			}

			@Override
			public void controlResized(final ControlEvent arg0) {
				canvas.redraw();
				resizeDiagram();
			}
		});

		// Reset the spatial selection flag
		ocelotlView.getOcelotlParameters().setSpatialSelection(false);
		
		wrapper.cleanMouseListeners();
		wrapper.cleanMouseMotionListeners();
		wrapper.addMouseListener(mouse);
		wrapper.addMouseMotionListener(mouse);
		selectFigure = new SelectFigure();
		
		potentialSelectFigure = new SelectFigure(potentialColorFG, potentialColorBG);
		potentialSelectFigure.setLineWidth(1);
		potentialSelectFigure.setAlpha(100);
		potentialSelectFigure.setFill(true);
		
		highLightAggregateFigure = new SelectFigure(ColorConstants.black, ColorConstants.white);
		highLightAggregateFigure.setLineWidth(2);
		highLightAggregateFigure.setAlpha(255);
		highLightAggregateFigure.setFill(false);
	}
	
	public void setBorder(final int border) {
		this.aBorder = border;
	}

	public void setCanvas(final Canvas canvas) {
		this.canvas = canvas;
	}

	public void setRoot(final Figure root) {
		this.root = root;
	}

	public void setVisuOperator(final IVisuOperator visuOperator) {
		this.visuOperator = visuOperator;
	}

	public EventProducerNode getCurrentlySelectedNode() {
		return currentlySelectedNode;
	}

	public void setCurrentlySelectedNode(EventProducerNode currentlySelectedNode) {
		this.currentlySelectedNode = currentlySelectedNode;
	}

}
