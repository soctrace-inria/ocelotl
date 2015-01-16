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
	public static Color							selectColorFG		= ColorConstants.white;
	public static Color							selectColorBG		= ColorConstants.blue;
	public static Color							potentialColorFG	= ColorConstants.darkBlue;
	public static Color							potentialColorBG	= ColorConstants.darkBlue;
	public static Color							activeColorFG		= ColorConstants.black;
	public static Color							activeColorBG		= ColorConstants.black;
	public static int							potentialColorAlpha	= 120;
	public static int							activeColorAlpha	= 120;
	public static int							selectColorAlpha	= 250;
		
	class SelectFigure extends RectangleFigure {

		private SelectFigure() {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			setForegroundColor(selectColorFG);
			setBackgroundColor(selectColorBG);
			setAlpha(potentialColorAlpha);
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
				setAlpha(activeColorAlpha);
			} else {
				setForegroundColor(selectColorFG);
				setBackgroundColor(selectColorBG);
				setFill(false);
				setAlpha(selectColorAlpha);
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

		// Set colors according to the settings
		activeColorFG = ocelotlView.getOcelotlParameters().getOcelotlSettings().getMainDisplayFgColor();
		activeColorBG = ocelotlView.getOcelotlParameters().getOcelotlSettings().getMainDisplayBgColor();
		activeColorAlpha = ocelotlView.getOcelotlParameters().getOcelotlSettings().getMainDisplayAlphaValue();
		potentialColorFG = ocelotlView.getOcelotlParameters().getOcelotlSettings().getMainSelectionFgColor();
		potentialColorBG = ocelotlView.getOcelotlParameters().getOcelotlSettings().getMainSelectionBgColor();
		potentialColorAlpha = ocelotlView.getOcelotlParameters().getOcelotlSettings().getMainSelectionAlphaValue();
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
	
	public void setSpatialSelection(EventProducerNode epn) {
		mouse.setSpatialSelection(epn);
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

	public OcelotlMouseListener getMouse() {
		return mouse;
	}

	public void setMouse(OcelotlMouseListener mouse) {
		this.mouse = mouse;
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
		potentialSelectFigure.setAlpha(potentialColorAlpha);
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

	public static Color getSelectColorFG() {
		return selectColorFG;
	}

	public void setSelectColorFG(Color selectColorFG) {
		AggregatedView.selectColorFG = selectColorFG;
	}

	public static Color getSelectColorBG() {
		return selectColorBG;
	}

	public void setSelectColorBG(Color selectColorBG) {
		AggregatedView.selectColorBG = selectColorBG;
	}

	public static Color getPotentialColorFG() {
		return potentialColorFG;
	}

	public void setPotentialColorFG(Color aPotentialColorFG) {
		AggregatedView.potentialColorFG = aPotentialColorFG;
		if (potentialSelectFigure != null) {
			potentialSelectFigure.setForegroundColor(potentialColorFG);
		}
	}

	public static Color getPotentialColorBG() {
		return potentialColorBG;
	}

	public void setPotentialColorBG(Color aPotentialColorBG) {
		AggregatedView.potentialColorBG = aPotentialColorBG;
		if (potentialSelectFigure != null) {
			potentialSelectFigure.setBackgroundColor(potentialColorBG);
		}
	}

	public static Color getActiveColorFG() {
		return activeColorFG;
	}

	public void setActiveColorFG(Color anActiveColorFG) {
		AggregatedView.activeColorFG = anActiveColorFG;
		if (selectFigure != null) {
			selectFigure.setForegroundColor(activeColorFG);
		}
	}

	public static Color getActiveColorBG() {
		return activeColorBG;
	}

	public void setActiveColorBG(Color anActiveColorBG) {
		AggregatedView.activeColorBG = anActiveColorBG;
		if (selectFigure != null) {
			selectFigure.setBackgroundColor(activeColorBG);
		}
	}

	public static int getPotentialColorAlpha() {
		return potentialColorAlpha;
	}

	public void setPotentialColorAlpha(int aPotentialColorAlpha) {
		AggregatedView.potentialColorAlpha = aPotentialColorAlpha;
		if (potentialSelectFigure != null) {
			potentialSelectFigure.setAlpha(aPotentialColorAlpha);
		}
	}

	public static int getActiveColorAlpha() {
		return activeColorAlpha;
	}

	public void setActiveColorAlpha(int anActiveColorAlpha) {
		AggregatedView.activeColorAlpha = anActiveColorAlpha;
	}

	public static int getSelectColorAlpha() {
		return selectColorAlpha;
	}

	public void setSelectColorAlpha(int aSelectColorAlpha) {
		AggregatedView.selectColorAlpha = aSelectColorAlpha;
		if (selectFigure != null) {
			selectFigure.setAlpha(selectColorAlpha);
		}
	}

}
