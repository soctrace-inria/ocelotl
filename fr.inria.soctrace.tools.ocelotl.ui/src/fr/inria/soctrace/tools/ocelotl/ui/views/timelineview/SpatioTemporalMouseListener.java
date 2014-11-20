package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.util.ArrayList;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.SpaceTimeAggregation2Manager;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView.MouseState;

public class SpatioTemporalMouseListener extends OcelotlMouseListener {

	private static final long	Threshold	= 5;
	MouseState					state		= MouseState.RELEASED;
	MouseState					previous	= MouseState.RELEASED;
	Point						currentPoint;
	Display						display		= Display.getCurrent();
	Shell						shell		= display.getActiveShell();
	long						fixed;
	AggregatedView				aggregatedView;
	int							originY, cornerY;
	boolean						closeToVStart, closeToVEnd, closeToHStart, closeToHEnd;

	public SpatioTemporalMouseListener(AggregatedView theview) {
		super();
		display = Display.getCurrent();
		shell = display.getActiveShell();
		aggregatedView = theview;
	}

	@Override
	public void mouseDoubleClicked(final MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseDragged(final MouseEvent arg0) {
		// If we are in the current dragging state and we have make move greater than 10 pixels
		if ((state == MouseState.PRESSED_LEFT || state == MouseState.DRAG_LEFT || state == MouseState.DRAG_LEFT_HORIZONTAL || state == MouseState.DRAG_LEFT_VERTICAL)
				&& arg0.getLocation().getDistance(currentPoint) > 10) {
			long moved = (long) ((double) ((arg0.x - aggregatedView.aBorder) * aggregatedView.resetTime.getTimeDuration()) / (aggregatedView.root.getSize().width() - 2 * aggregatedView.aBorder)) + aggregatedView.resetTime.getTimeStampStart();
			
			// If we are not performing a vertical drag
			if (state != MouseState.DRAG_LEFT_VERTICAL) {
				// Update x the coordinates
				moved = Math.max(moved, aggregatedView.resetTime.getTimeStampStart());
				moved = Math.min(moved, aggregatedView.resetTime.getTimeStampEnd());
				fixed = Math.max(fixed, aggregatedView.resetTime.getTimeStampStart());
				fixed = Math.min(fixed, aggregatedView.resetTime.getTimeStampEnd());
				if (fixed < moved)
					aggregatedView.selectTime = new TimeRegion(fixed, moved);
				else
					aggregatedView.selectTime = new TimeRegion(moved, fixed);
			}
			
			// If we are not performing an horizontal drag 
			if (!(state == MouseState.DRAG_LEFT_HORIZONTAL)) {
				// Update the height coordinate with the current one of the mouse
				cornerY = arg0.y;
			}
			
			aggregatedView.ocelotlView.setTimeRegion(aggregatedView.selectTime);
			aggregatedView.ocelotlView.getTimeAxisView().select(aggregatedView.selectTime, false);
			
			aggregatedView.selectFigure.draw(aggregatedView.selectTime, false, originY, cornerY);
			aggregatedView.ocelotlView.getOverView().updateSelection(aggregatedView.selectTime);
		}

	}

	@Override
	public void mouseEntered(final MouseEvent arg0) {
	}

	@Override
	public void mouseExited(final MouseEvent arg0) {
		if (state != MouseState.RELEASED && state != MouseState.H_MOVE_START && state != MouseState.H_MOVE_END && state != MouseState.V_MOVE_END && state != MouseState.V_MOVE_START && state != MouseState.EXITED) {
			previous = state;
			state = MouseState.EXITED;
			mouseReleased(arg0);
		}
	}

	@Override
	public void mouseHover(final MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(final MouseEvent arg0) {
		// If there is a current selection 
		if (aggregatedView.selectFigure != null && aggregatedView.root.getChildren().contains(aggregatedView.selectFigure))
			closeToSelectionEdges(arg0);
			
			// If we are close to one vertical edge
			if (closeToVStart) {
				state = MouseState.H_MOVE_START;
				shell.setCursor(new Cursor(display, SWT.CURSOR_SIZEWE));
				// Or the other vertical edge
			} else if (closeToVEnd) {
				state = MouseState.H_MOVE_END;
				shell.setCursor(new Cursor(display, SWT.CURSOR_SIZEWE));
			} else if (closeToHStart) {
				state = MouseState.V_MOVE_START;
				shell.setCursor(new Cursor(display, SWT.CURSOR_SIZENS));
			} else if (closeToHEnd) {
				state = MouseState.V_MOVE_END;
				shell.setCursor(new Cursor(display, SWT.CURSOR_SIZENS));
			} else {
				state = MouseState.RELEASED;
				shell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));
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
		int xBound = aggregatedView.selectFigure.getBounds().x();
		int yBound = aggregatedView.selectFigure.getBounds().y();
		int height = aggregatedView.selectFigure.getBounds().height();
		int width = aggregatedView.selectFigure.getBounds().width();

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
		if (arg0.button == 1 && aggregatedView.resetTime != null) {
			currentPoint = arg0.getLocation();
			long p3 = (long) ((double) ((arg0.x - aggregatedView.aBorder) * aggregatedView.resetTime.getTimeDuration()) / (aggregatedView.root.getSize().width() - 2 * aggregatedView.aBorder)) + aggregatedView.resetTime.getTimeStampStart();
			// We are dragging horizontally by the left side
			if (state == MouseState.H_MOVE_START) {
				originY = aggregatedView.selectFigure.getBounds().y();
				p3 = aggregatedView.selectTime.getTimeStampStart();
				fixed = aggregatedView.selectTime.getTimeStampEnd();
				state = MouseState.DRAG_LEFT_HORIZONTAL;	
			} else if (state == MouseState.H_MOVE_END) {// We are dragging horizontally by the right side
				p3 = aggregatedView.selectTime.getTimeStampEnd();
				fixed = aggregatedView.selectTime.getTimeStampStart();
				state = MouseState.DRAG_LEFT_HORIZONTAL;
			} else if (state == MouseState.V_MOVE_START) {// We are dragging vertically by the up side
				originY = cornerY;
				cornerY = arg0.y;
				state = MouseState.DRAG_LEFT_VERTICAL;
			} else if (state == MouseState.V_MOVE_END) {// We are dragging vertically by the bottom side
				originY = aggregatedView.selectFigure.getBounds().y();
				cornerY = arg0.y;
				state = MouseState.DRAG_LEFT_VERTICAL;
			} else {// We are starting a new selection
				state = MouseState.PRESSED_LEFT;
				originY = arg0.getLocation().y();
				cornerY = originY;
				p3 = Math.max(p3, aggregatedView.resetTime.getTimeStampStart());
				p3 = Math.min(p3, aggregatedView.resetTime.getTimeStampEnd());
				aggregatedView.selectTime = new TimeRegion(aggregatedView.resetTime);
				aggregatedView.selectTime.setTimeStampStart(p3);
				aggregatedView.selectTime.setTimeStampEnd(p3);
				fixed = p3;
			}
			
			aggregatedView.ocelotlView.setTimeRegion(aggregatedView.selectTime);
			aggregatedView.ocelotlView.getTimeAxisView().select(aggregatedView.selectTime, false);
			aggregatedView.selectFigure.draw(aggregatedView.selectTime, false, originY, cornerY);
			aggregatedView.ocelotlView.getOverView().updateSelection(aggregatedView.selectTime);
		}
		
		// If right click
		if(arg0.button == 3 && aggregatedView.resetTime != null) {
			Point clickCoord = new Point(arg0.x, arg0.y);
			SpatioTemporalAggregateView selectedAggregate = null;
			
			// Find the corresponding aggregate
			for (SpatioTemporalAggregateView aggreg : aggregatedView.getAggregates()) {
				if (aggreg.getAggregateZone().contains(clickCoord)) {
					selectedAggregate = aggreg;
				}
			}

			// If none was found
			if(selectedAggregate == null)
				return;

			// Trigger the display
			selectedAggregate.display(aggregatedView.ocelotlView);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent arg0) {

		// If left click or arriving through an exit event
		// and if the released correspond to an action actually started in the view
		if ((arg0.button == 1 || state == MouseState.EXITED) && state != MouseState.RELEASED) {
			// Reset to normal cursor
			shell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));

			if (state == MouseState.DRAG_LEFT_VERTICAL || state == MouseState.DRAG_LEFT_HORIZONTAL)
				mouseDragged(arg0);

			// If we get here through a mouse exited event, do not update the previous state
			if (state != MouseState.EXITED)
				previous = state;
			
			state = MouseState.RELEASED;

			if (aggregatedView.time == null)
				return;

			// Get time slice numbers from the time slice manager
			int startingSlice = (int) aggregatedView.ocelotlView.getOcelotlCore().getMicroModel().getTimeSliceManager().getTimeSlice(aggregatedView.selectTime.getTimeStampStart());
			int endingSlice = (int) aggregatedView.ocelotlView.getOcelotlCore().getMicroModel().getTimeSliceManager().getTimeSlice(aggregatedView.selectTime.getTimeStampEnd());

			// Since the timestamp of the last time slice goes further than the
			// max timestamp of the trace, we must check that we are not over it
			long endTimeStamp = Math.min(aggregatedView.resetTime.getTimeStampEnd(), aggregatedView.ocelotlView.getOcelotlCore().getMicroModel().getTimeSliceManager().getTimeSlices().get(endingSlice).getTimeRegion().getTimeStampEnd());

			aggregatedView.selectTime.setTimeStampStart(aggregatedView.ocelotlView.getOcelotlCore().getMicroModel().getTimeSliceManager().getTimeSlices().get(startingSlice).getTimeRegion().getTimeStampStart());
			aggregatedView.selectTime.setTimeStampEnd(endTimeStamp);

			// If we are performing an horizontal drag then don't change the
			// selected hierarchy
			if (previous != MouseState.DRAG_LEFT_HORIZONTAL) {

				// Get the event producer hierarchy
				SpaceTimeAggregation2Manager STManager = (SpaceTimeAggregation2Manager) aggregatedView.ocelotlView.getOcelotlCore().getLpaggregManager();
				EventProducerHierarchy hierarchy = STManager.getHierarchy();

				// Compute the selected spatiotemporal region
				int y0, y1;
				y0 = Math.min(originY, arg0.y);
				y1 = Math.max(originY, arg0.y);

				// Compute various height values
				int rootHeight = aggregatedView.root.getSize().height;
				int height = rootHeight - (2 * aggregatedView.aBorder);
				double accurateLogicHeight = height / (double) hierarchy.getRoot().getWeight();

				// Compute the boundaries of the event producer
				// Round up to the nearest integer (multiply by 2.0, round then
				// divide by 2)
				double rounding = (((double) (y0 - aggregatedView.aBorder)) / accurateLogicHeight) * 2.0;
				int startingHeight = (int) (Math.round(rounding) / 2);
				int endingHeight = (int) (((double) (y1 - aggregatedView.aBorder)) / accurateLogicHeight);
				// Might happen when selecting a unique point
				if (startingHeight > endingHeight) {
					endingHeight = startingHeight;
				}

				// Find the event producer node containing all the selected node
				ArrayList<EventProducerNode> currentProducers = hierarchy.findNodeWithin(startingHeight, endingHeight);
				EventProducerNode selectedNode = hierarchy.findSmallestContainingNode(currentProducers);

				// Compute the selection bound for drawing (cf.
				// SpatioTemporalModeView)
				originY = (int) (selectedNode.getIndex() * accurateLogicHeight + aggregatedView.aBorder);
				cornerY = originY + (int) ((selectedNode.getWeight()) * accurateLogicHeight) - 1;

				ArrayList<EventProducer> selectedProducers = selectedNode.getContainedProducers();

				// If only one producer is selected, then also add the parent
				// producer to avoid that the building of the micro model fails
				if (selectedNode.getChildrenNodes().isEmpty()) {
					selectedProducers.add(selectedNode.getParentNode().getMe());
				}
				aggregatedView.ocelotlView.getOcelotlParameters().setSpatialSelection(true);
				aggregatedView.ocelotlView.getOcelotlParameters().setSpatiallySelectedProducers(selectedProducers);
			}

			aggregatedView.ocelotlView.getTimeAxisView().select(aggregatedView.selectTime, true);
			aggregatedView.ocelotlView.setTimeRegion(aggregatedView.selectTime);
			aggregatedView.selectFigure.draw(aggregatedView.selectTime, true, originY, cornerY);
			aggregatedView.ocelotlView.getOverView().updateSelection(aggregatedView.selectTime);
			aggregatedView.ocelotlView.getStatView().updateData();
		}
	}

}
