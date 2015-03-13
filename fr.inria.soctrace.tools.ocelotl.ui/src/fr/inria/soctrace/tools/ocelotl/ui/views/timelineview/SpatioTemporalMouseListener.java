/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.util.ArrayList;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.SpaceTimeAggregation2Manager;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView.MouseState;

public class SpatioTemporalMouseListener extends TemporalMouseListener {

	protected Boolean						clickOnView			= false;
	protected int							minDrawThreshold	= OcelotlConstants.MinimalHeightDrawingThreshold;
	protected int							originY, cornerY, originX;
	protected boolean						closeToVStart, closeToVEnd, closeToHStart, closeToHEnd;

	protected double						rootHeight;
	protected double						height;
	protected double						accurateLogicHeight;
	protected double						logicHeight;
	protected SpaceTimeAggregation2Manager	spatioTemporalManager;
	protected EventProducerHierarchy		hierarchy;
	protected EventProducerNode				selectedNode;
	
	public SpatioTemporalMouseListener(AggregatedView theView) {
		super(theView);
	}

	public int getOriginY() {
		return originY;
	}

	public void setOriginY(int originY) {
		this.originY = originY;
	}

	public int getCornerY() {
		return cornerY;
	}

	public void setCornerY(int cornerY) {
		this.cornerY = cornerY;
	}

	@Override
	public void mouseDragged(final MouseEvent arg0) {
		// If we are in the current dragging state and we have make move greater than 10 pixels
		if ((state == MouseState.PRESSED_LEFT || state == MouseState.DRAG_LEFT || state == MouseState.DRAG_LEFT_HORIZONTAL || state == MouseState.DRAG_LEFT_VERTICAL)
				&& arg0.getLocation().getDistance(currentPoint) > 10) {
			long moved = (long) ((double) ((arg0.x - aggregatedView.getBorder()) * aggregatedView.getResetTime().getTimeDuration()) / (aggregatedView.getRoot().getSize().width() - 2 * aggregatedView.getBorder())) + aggregatedView.getResetTime().getTimeStampStart();
			
			// If we are not performing a vertical drag
			if (state != MouseState.DRAG_LEFT_VERTICAL) {
				// Update x the coordinates
				moved = Math.max(moved, aggregatedView.getResetTime().getTimeStampStart());
				moved = Math.min(moved, aggregatedView.getResetTime().getTimeStampEnd());
				fixed = Math.max(fixed, aggregatedView.getResetTime().getTimeStampStart());
				fixed = Math.min(fixed, aggregatedView.getResetTime().getTimeStampEnd());
				if (fixed < moved)
					aggregatedView.setSelectTime(new TimeRegion(fixed, moved));
				else
					aggregatedView.setSelectTime(new TimeRegion(moved, fixed));
			}
			
			// If we are not performing an horizontal drag
			if (!(state == MouseState.DRAG_LEFT_HORIZONTAL)) {
				// Update the height coordinate with the current one of the
				// mouse
				cornerY = arg0.y;
			}
			
			// Compute current potential selection
			Point timeslices = getTimeSlices(aggregatedView.getSelectTime());
			aggregatedView.setPotentialSelectTime(setTemporalSelection(timeslices.x(), timeslices.y()));
			EventProducerNode foundNode = findEventProducerNode(cornerY);
			Point heights = getSpatialSelectionCoordinates(foundNode);

			updateEverything(heights.x(), heights.y(), false, aggregatedView.getPotentialSelectTime());
			aggregatedView.getPotentialSelectFigure().draw(aggregatedView.getPotentialSelectTime(), heights.x(), heights.y());
		}
	}

	@Override
	public void mouseExited(final MouseEvent arg0) {
		setShellCursor(SWT.CURSOR_ARROW);
		
		if (state != MouseState.RELEASED && state != MouseState.H_MOVE_START && state != MouseState.H_MOVE_END && state != MouseState.V_MOVE_END && state != MouseState.V_MOVE_START && state != MouseState.EXITED) {
			previous = state;
			state = MouseState.EXITED;
			mouseReleased(arg0);
		}
	}

	@Override
	public void mouseMoved(final MouseEvent arg0) {
		// If there is a current selection
		if (aggregatedView.getSelectFigure() != null && aggregatedView.getRoot().getChildren().contains(aggregatedView.getSelectFigure()))
			closeToSelectionEdges(arg0);

		// If we are close to one vertical edge
		if (closeToVStart) {
			state = MouseState.H_MOVE_START;
			setShellCursor(SWT.CURSOR_SIZEWE);
			// Or the other vertical edge
		} else if (closeToVEnd) {
			state = MouseState.H_MOVE_END;
			setShellCursor(SWT.CURSOR_SIZEWE);
		} else if (closeToHStart) {
			state = MouseState.V_MOVE_START;
			setShellCursor(SWT.CURSOR_SIZENS);
		} else if (closeToHEnd) {
			state = MouseState.V_MOVE_END;
			setShellCursor(SWT.CURSOR_SIZENS);
		} else {
			if (state != MouseState.PRESSED_LEFT) {
				state = MouseState.RELEASED;
			}
			
			setShellCursor(SWT.CURSOR_ARROW);
		}
	}
	
	/**
	 * Check if we are close to an edge of the selection
	 * 
	 * @param arg0
	 *            mouse parameters
	 */
	public void closeToSelectionEdges(MouseEvent arg0) {
		closeToVStart = closeToVEnd = closeToHStart = closeToHEnd = false;
		boolean withinX = false, withinY = false;
		int xBound = aggregatedView.getSelectFigure().getBounds().x();
		int yBound = aggregatedView.getSelectFigure().getBounds().y();
		int height = aggregatedView.getSelectFigure().getBounds().height();
		int width = aggregatedView.getSelectFigure().getBounds().width();

		// Are we within the vertical or horizontal boundaries of the selection
		if (xBound <= arg0.x && (xBound + width) >= arg0.x)
			withinX = true;
		if (yBound <= arg0.y && (yBound + height) >= arg0.y)
			withinY = true;

		// Check each edge
		if (Math.abs(xBound - arg0.x) < Threshold && withinY) {
			closeToVStart = true;
		}
		if (Math.abs((xBound + width) - arg0.x) < Threshold && withinY) {
			closeToVEnd = true;
		}
		if (Math.abs(yBound - arg0.y) < Threshold && withinX) {
			closeToHStart = true;
		}
		if (Math.abs((yBound + height) - arg0.y) < Threshold && withinX) {
			closeToHEnd = true;
		}
	}

	@Override
	public void mousePressed(final MouseEvent arg0) {
		if (arg0.button == 1 && aggregatedView.getResetTime() != null) {
			clickOnView = true;
			currentPoint = arg0.getLocation();
			// Compute the timestamp on which we clicked
			long p3 = (long) ((double) ((arg0.x - aggregatedView.getBorder()) * aggregatedView.getResetTime().getTimeDuration()) / (aggregatedView.getRoot().getSize().width() - 2 * aggregatedView.getBorder())) + aggregatedView.getResetTime().getTimeStampStart();
			originX = arg0.x;

			// We are dragging horizontally by the left side
			if (state == MouseState.H_MOVE_START) {
				originY = aggregatedView.getSelectFigure().getBounds().y();
				p3 = aggregatedView.getSelectTime().getTimeStampStart();
				fixed = aggregatedView.getSelectTime().getTimeStampEnd();
				state = MouseState.DRAG_LEFT_HORIZONTAL;	
			} else if (state == MouseState.H_MOVE_END) {// We are dragging horizontally by the right side
				p3 = aggregatedView.getSelectTime().getTimeStampEnd();
				fixed = aggregatedView.getSelectTime().getTimeStampStart();
				state = MouseState.DRAG_LEFT_HORIZONTAL;
			} else if (state == MouseState.V_MOVE_START) {// We are dragging vertically by the up side
				originY = cornerY;
				cornerY = arg0.y;
				state = MouseState.DRAG_LEFT_VERTICAL;
			} else if (state == MouseState.V_MOVE_END) {// We are dragging vertically by the bottom side
				originY = aggregatedView.getSelectFigure().getBounds().y();
				cornerY = arg0.y;
				state = MouseState.DRAG_LEFT_VERTICAL;
			} else {// We are starting a new selection
				state = MouseState.PRESSED_LEFT;
				originY = arg0.getLocation().y();
				cornerY = originY;
				p3 = Math.max(p3, aggregatedView.getResetTime().getTimeStampStart());
				p3 = Math.min(p3, aggregatedView.getResetTime().getTimeStampEnd());
				aggregatedView.setSelectTime(new TimeRegion(aggregatedView.getResetTime()));
				aggregatedView.getSelectTime().setTimeStampStart(p3);
				aggregatedView.getSelectTime().setTimeStampEnd(p3);
				fixed = p3;
			}

			// If there was a selected event producer in the hierarchy, delete it
			aggregatedView.getOcelotlView().getUnitAxisView().setCurrentlySelectedEpn(null);
			
			updateEverything(originY, cornerY, false, aggregatedView.getSelectTime());
		}
		
		// If right click
		if(arg0.button == 3 && aggregatedView.getResetTime() != null) {
			SpatioTemporalAggregateView selectedAggregate = findAggregate(arg0.x, arg0.y);

			// If none was found or if not a visual aggregate
			if (selectedAggregate == null || (!selectedAggregate.isVisualAggregate()))
				return;
			
			// Compute highlight selection
			Point heights = getSpatialSelectionCoordinates(selectedAggregate.getEventProducerNode());
			aggregatedView.getHighLightAggregateFigure().draw(setTemporalSelection(selectedAggregate.getStartingTimeSlice(), selectedAggregate.getEndingTimeSlice() - 1), heights.x(), heights.y());

			// Trigger the display
			selectedAggregate.display(aggregatedView.getOcelotlView());
		}
		
		// If middle click, cancel selection
		if (arg0.button == 2 && aggregatedView.getResetTime() != null) {
			aggregatedView.getOcelotlView().cancelSelection();
		}
	}
	
	@Override
	public void mouseDoubleClicked(final MouseEvent arg0) {
		SpatioTemporalAggregateView selectedAggregate = findAggregate(arg0.x, arg0.y);
		
		// If none was found
		if(selectedAggregate == null)
			return;

		// Select the aggregate
		aggregatedView.setSelectTime(setTemporalSelection(selectedAggregate.getStartingTimeSlice(), selectedAggregate.getEndingTimeSlice() - 1));
		Point heights = getSpatialSelectionCoordinates(selectedAggregate.getEventProducerNode());
		
		// Set the height points
		originY = heights.x();
		cornerY = heights.y();
		setSpatialSelection(selectedAggregate.getEventProducerNode());

		// Avoid triggering the mouse released event
		clickOnView = false;

		updateEverything(selectedAggregate.getAggregateZone().x, selectedAggregate.getAggregateZone().x + selectedAggregate.getAggregateZone().width, selectedAggregate.getAggregateZone().y, selectedAggregate.getAggregateZone().y + selectedAggregate.getAggregateZone().height, true, aggregatedView.getSelectTime());
		aggregatedView.getOcelotlView().getStatView().updateData();
	}

	@Override
	public void mouseReleased(final MouseEvent arg0) {
		
		// If left click or arriving through an exit event
		// and if the released correspond to an action actually started in the view
		if ((arg0.button == 1 || state == MouseState.EXITED) && clickOnView) {
			
			// Reset to normal cursor
			setShellCursor(SWT.CURSOR_ARROW);
			
			clickOnView = false;
			
			// Remove the potential selection figure
			aggregatedView.getPotentialSelectFigure().delete();
			
			// If we get here through a mouse exited event, do not update the previous state
			if (state != MouseState.EXITED)
				previous = state;
			
			state = MouseState.RELEASED;

			if (aggregatedView.time == null)
				return;

			// If we are performing a vertical drag then don't change the
			// selected time slices
			if (previous != MouseState.DRAG_LEFT_VERTICAL) {
				// Get time slice numbers from the time slice manager
				int startingSlice = (int) aggregatedView.getOcelotlView().getCore().getMicroModel().getTimeSliceManager().getTimeSlice(aggregatedView.getSelectTime().getTimeStampStart());
				int endingSlice = (int) aggregatedView.getOcelotlView().getCore().getMicroModel().getTimeSliceManager().getTimeSlice(aggregatedView.getSelectTime().getTimeStampEnd());

				aggregatedView.setSelectTime(setTemporalSelection(startingSlice, endingSlice));
			}

			// If we are performing an horizontal drag then don't change the
			// selected hierarchy
			if (previous != MouseState.DRAG_LEFT_HORIZONTAL) {
				SpatioTemporalAggregateView selectedAggregate = findAggregate(arg0.x, arg0.y, originX, originY);
				if (selectedAggregate == null || !selectedAggregate.isVisualAggregate()) {
					selectedNode = findEventProducerNode(arg0.y);

					Point heights = getSpatialSelectionCoordinates(selectedNode);

					// Set the height points
					originY = heights.x();
					cornerY = heights.y();
				} else {
					selectedNode = selectedAggregate.getEventProducerNode();

					originY = selectedAggregate.getAggregateZone().y();
					cornerY = selectedAggregate.getAggregateZone().y() + selectedAggregate.getAggregateZone().height();
				}

				// Select producers
				setSpatialSelection(selectedNode);
			}
			
			updateEverything(originY, cornerY, true, aggregatedView.getSelectTime());
			aggregatedView.getOcelotlView().getStatView().updateData();
		}
	}
	
	/**
	 * Set a bunch of variables necessary for computing spatial selection
	 */
	protected void updateMeasurements() {
		// Get the event producer hierarchy
		spatioTemporalManager = (SpaceTimeAggregation2Manager) aggregatedView.getOcelotlView().getCore().getLpaggregManager();
		hierarchy = spatioTemporalManager.getHierarchy();

		// Compute various height values
		rootHeight = aggregatedView.getRoot().getSize().height;
		height = rootHeight - (2 * aggregatedView.getBorder());
		accurateLogicHeight = height / (double) hierarchy.getRoot().getWeight();
		logicHeight = height / hierarchy.getRoot().getWeight();
	}
	
	/**
	 * Set the spatial selection to the event producer node given in parameter
	 * 
	 * @param selectedNode
	 *            the selected even producer node
	 */
	public void setSpatialSelection(EventProducerNode selectedNode) {
		updateMeasurements();
		ArrayList<EventProducer> selectedProducers = selectedNode.getContainedProducers();

		// If only one producer is selected, then also add the parent
		// producer to avoid that the building of the micro model fails
		if (selectedNode.getChildrenNodes().isEmpty()) {
			selectedProducers.add(selectedNode.getParentNode().getMe());
		}

		aggregatedView.setCurrentlySelectedNode(selectedNode);
		
		// Find the event producer node containing all the selected node
		ArrayList<EventProducerNode> currentProducers = hierarchy.getLeaves(selectedNode);
		aggregatedView.getOcelotlView().getOcelotlParameters().setSelectedEventProducerNodes(currentProducers);
		aggregatedView.getOcelotlView().getOcelotlParameters().setDisplayedSubselection(true);
		aggregatedView.getOcelotlView().getOcelotlParameters().setSpatialSelection(true);
		
		while (selectedNode.getParentNode() != null) {
			if (selectedNode.getParentNode().getWeight() == selectedNode.getWeight()) {
				selectedNode = selectedNode.getParentNode();
				selectedProducers.add(selectedNode.getMe());
			} else {
				break;
			}
		}
		
		aggregatedView.getOcelotlView().getOcelotlParameters().setSpatiallySelectedProducers(selectedProducers);
	}
	
	/**
	 * For a given producer node, compute the starting and ending vertical
	 * coordinates
	 * 
	 * @param theSelectedNode
	 *            the selected producer node
	 * @return a Point containing the coordinate (x = yStart, y = yEnd)
	 */
	protected Point getSpatialSelectionCoordinates(EventProducerNode theSelectedNode) {
		updateMeasurements();

		int y0 = (int) (theSelectedNode.getIndex() * logicHeight + aggregatedView.getBorder());
		int y1 = y0 + (int) ((theSelectedNode.getWeight()) * logicHeight) - aggregatedView.getSpace();
		
		// If the selected producer is too small to be represented, take the
		// parent node until the size is superior to the threshold
		while ((theSelectedNode.getWeight() * logicHeight - aggregatedView.getSpace()) < minDrawThreshold) {
			if (theSelectedNode.getParentNode() != null)
				theSelectedNode = theSelectedNode.getParentNode();
			else
				break;

			y0 = (int) (theSelectedNode.getIndex() * logicHeight + aggregatedView.getBorder());
			y1 = y0 + (int) (theSelectedNode.getWeight() * logicHeight) - aggregatedView.getSpace();
		}
		
		// Try to get the same coordinates as the hierarchy axis view
		Rectangle rect = aggregatedView.getOcelotlView().getUnitAxisView().getEventProdToFigures().get(theSelectedNode);
		
		if (rect != null) {
			y0 = rect.y();
			y1 = y0 + rect.height();
		}
		
		selectedNode = theSelectedNode;

		return new Point(y0, y1);
	}

	/**
	 * Find the currently selected event producer node
	 * 
	 * @param y
	 *            the current y parameter tom compute the height with originY
	 * @return the found event producer
	 */
	protected EventProducerNode findEventProducerNode(int y) {
		updateMeasurements();

		// Compute the selected spatiotemporal region
		int y0, y1;
		y0 = Math.min(originY, y);
		y1 = Math.max(originY, y);

		// Remove the border margin and make sure we are within the boundary
		y0 = Math.min(y0 - aggregatedView.getBorder(), (int) height - 1);
		y1 = Math.min(y1 - aggregatedView.getBorder(), (int) height - 1);

		// Compute the boundaries of the event producer
		// Round up to the nearest integer (multiply by 2.0, round then
		// divide by 2)
		double rounding = (((double) (y0)) / accurateLogicHeight) * 2.0;
		int startingHeight = (int) (Math.round(rounding) / 2);
		int endingHeight = (int) (((double) (y1)) / accurateLogicHeight);
		
		// Might happen when selecting a unique point
		if (startingHeight > endingHeight) {
			startingHeight = endingHeight;
		}

		// Find the event producer node containing all the selected node
		ArrayList<EventProducerNode> currentProducers = hierarchy.findNodeWithin(startingHeight, endingHeight);

		return hierarchy.findSmallestContainingNode(currentProducers);
	}

	/**
	 * Find the aggregate in the corresponding coordinate
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return the found aggregate, or null otherwise
	 */
	protected SpatioTemporalAggregateView findAggregate(int xa, int ya, int xb, int yb) {
		Point clickCoord = new Point(xa, ya);
		Point clickCoord2 = new Point(xb, yb);
		SpatioTemporalAggregateView selectedAggregate = null;

		// Find the corresponding aggregate
		for (SpatioTemporalAggregateView aggreg : aggregatedView.getAggregates()) {
			if (aggreg.getAggregateZone().contains(clickCoord) && 
					aggreg.getAggregateZone().contains(clickCoord2)) {
				selectedAggregate = aggreg;
				break;
			}
		}
	
		return selectedAggregate;
	}
	
	/**
	 * Find the aggregate in the corresponding coordinate
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return the found aggregate, or null otherwise
	 */
	protected SpatioTemporalAggregateView findAggregate(int x, int y) {
		Point clickCoord = new Point(x, y);
		SpatioTemporalAggregateView selectedAggregate = null;

		// Find the corresponding aggregate
		for (SpatioTemporalAggregateView aggreg : aggregatedView.getAggregates()) {
			if (aggreg.getAggregateZone().contains(clickCoord)) {
				selectedAggregate = aggreg;
				break;
			}
		}
	
		return selectedAggregate;
	}

	public void updateEverything(int y0, int y1, boolean active, TimeRegion aTimeRegion) {
		aggregatedView.getOcelotlView().setTimeRegion(aTimeRegion);
		aggregatedView.getOcelotlView().getTimeAxisView().select(aTimeRegion, active);
		aggregatedView.getOcelotlView().getUnitAxisView().select(y0, y1, active);
		aggregatedView.getSelectFigure().draw(aggregatedView.getSelectTime(), active, originY, cornerY);
		aggregatedView.getOcelotlView().getOverView().updateSelection(aTimeRegion);
	}
	
	public void updateEverything(int x0, int x1, int y0, int y1, boolean active, TimeRegion aTimeRegion) {
		aggregatedView.getOcelotlView().setTimeRegion(aTimeRegion);
		aggregatedView.getOcelotlView().getTimeAxisView().select(aTimeRegion, active);
		aggregatedView.getOcelotlView().getUnitAxisView().select(y0, y1, active);
		aggregatedView.getSelectFigure().draw(x0, x1, y0, y1, active);
		aggregatedView.getOcelotlView().getOverView().updateSelection(aTimeRegion);
	}
	
	@Override
	public void drawSelection() {
		if (aggregatedView.getSelectTime() != null && aggregatedView.getCurrentlySelectedNode() != null) {
			Point newCoordinates = getSpatialSelectionCoordinates(aggregatedView.getCurrentlySelectedNode());
			originY = newCoordinates.x();
			cornerY = newCoordinates.y();

			updateEverything(originY, cornerY, true, aggregatedView.getSelectTime());
			aggregatedView.getOcelotlView().getStatView().updateData();
		}
	}
}
