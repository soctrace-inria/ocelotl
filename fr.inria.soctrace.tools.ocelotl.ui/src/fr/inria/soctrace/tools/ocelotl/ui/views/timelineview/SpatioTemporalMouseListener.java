package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.util.ArrayList;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.SpaceTimeAggregation2Manager;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView.State;

public class SpatioTemporalMouseListener implements MouseListener, MouseMotionListener {

	private static final long	Threshold	= 5;
	State						state		= State.RELEASED;
	State						previous	= State.RELEASED;
	Point						currentPoint;
	Display						display		= Display.getCurrent();
	Shell						shell		= display.getActiveShell();
	long						fixed;
	AggregatedView				aggregatedView;
	int originY, originX;

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
		if ((state == State.PRESSED_G || state == State.DRAG_G || state == State.DRAG_G_START) && arg0.getLocation().getDistance(currentPoint) > 10) {
			long moved = (long) ((double) ((arg0.x - aggregatedView.aBorder) * aggregatedView.resetTime.getTimeDuration()) / (aggregatedView.root.getSize().width() - 2 * aggregatedView.aBorder)) + aggregatedView.resetTime.getTimeStampStart();
			if (state != State.DRAG_G_START)
				state = State.DRAG_G;
			moved = Math.max(moved, aggregatedView.resetTime.getTimeStampStart());
			moved = Math.min(moved, aggregatedView.resetTime.getTimeStampEnd());
			fixed = Math.max(fixed, aggregatedView.resetTime.getTimeStampStart());
			fixed = Math.min(fixed, aggregatedView.resetTime.getTimeStampEnd());
			if (fixed < moved)
				aggregatedView.selectTime = new TimeRegion(fixed, moved);
			else
				aggregatedView.selectTime = new TimeRegion(moved, fixed);
			aggregatedView.ocelotlView.setTimeRegion(aggregatedView.selectTime);
			aggregatedView.ocelotlView.getTimeAxisView().select(aggregatedView.selectTime, false);
			aggregatedView.selectFigure.draw(aggregatedView.selectTime, false, originY, arg0.y);
			aggregatedView.ocelotlView.getOverView().updateSelection(aggregatedView.selectTime);
			if (aggregatedView.ocelotlView.getTimeRegion().compareTimeRegion(aggregatedView.time)) {
				aggregatedView.ocelotlView.getTimeAxisView().unselect();
				if (aggregatedView.selectFigure.getParent() != null)
					aggregatedView.root.remove(aggregatedView.selectFigure);
				aggregatedView.root.repaint();
			}
		}

	}

	@Override
	public void mouseEntered(final MouseEvent arg0) {
	}

	@Override
	public void mouseExited(final MouseEvent arg0) {
		if (state != State.RELEASED && state != State.MOVE_START && state != State.MOVE_END && state != State.EXITED) {
			state = State.EXITED;
			mouseReleased(arg0);
		}
	}

	@Override
	public void mouseHover(final MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(final MouseEvent arg0) {
		if (aggregatedView.selectFigure != null && aggregatedView.root.getChildren().contains(aggregatedView.selectFigure))
			if (Math.abs(aggregatedView.selectFigure.getBounds().x - arg0.x) < Threshold) {
				state = State.MOVE_START;
				shell.setCursor(new Cursor(display, SWT.CURSOR_SIZEWE));
			} else if (Math.abs(aggregatedView.selectFigure.getBounds().x + aggregatedView.selectFigure.getBounds().width - arg0.x) < Threshold) {
				state = State.MOVE_END;
				shell.setCursor(new Cursor(display, SWT.CURSOR_SIZEWE));
			} else {
				state = State.RELEASED;
				shell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));
			}

	}

	@Override
	public void mousePressed(final MouseEvent arg0) {
		if (arg0.button == 1 && aggregatedView.resetTime != null) {
			currentPoint = arg0.getLocation();
			long p3 = (long) ((double) ((arg0.x - aggregatedView.aBorder) * aggregatedView.resetTime.getTimeDuration()) / (aggregatedView.root.getSize().width() - 2 * aggregatedView.aBorder)) + aggregatedView.resetTime.getTimeStampStart();
			if (state == State.MOVE_START) {
				originX = arg0.getLocation().x();
				originY = arg0.getLocation().y();
				p3 = aggregatedView.selectTime.getTimeStampStart();
				fixed = aggregatedView.selectTime.getTimeStampEnd();
				state = State.DRAG_G_START;
			} else if (state == State.MOVE_END) {
				p3 = aggregatedView.selectTime.getTimeStampEnd();
				fixed = aggregatedView.selectTime.getTimeStampStart();
				state = State.DRAG_G;
			} else {
				state = State.PRESSED_G;
				originX = arg0.getLocation().x();
				originY = arg0.getLocation().y();
				p3 = Math.max(p3, aggregatedView.resetTime.getTimeStampStart());
				p3 = Math.min(p3, aggregatedView.resetTime.getTimeStampEnd());
				aggregatedView.selectTime = new TimeRegion(aggregatedView.resetTime);
				aggregatedView.selectTime.setTimeStampStart(p3);
				aggregatedView.selectTime.setTimeStampEnd(p3);
				fixed = p3;
			}

			int y1 = (arg0.y - aggregatedView.aBorder);
			
			aggregatedView.ocelotlView.setTimeRegion(aggregatedView.selectTime);
			aggregatedView.ocelotlView.getTimeAxisView().select(aggregatedView.selectTime, false);
			aggregatedView.selectFigure.draw(aggregatedView.selectTime, false, originY, y1);
			aggregatedView.ocelotlView.getOverView().updateSelection(aggregatedView.selectTime);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent arg0) {

		shell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));
		if (state == State.DRAG_G || state == State.DRAG_G_START)
			mouseDragged(arg0);
		state = State.RELEASED;
		if (aggregatedView.time == null)
			return;

		// If the selection is different than the whole region
		if (!aggregatedView.ocelotlView.getTimeRegion().compareTimeRegion(aggregatedView.time)) {
			final double sliceSize = (double) aggregatedView.resetTime.getTimeDuration() / (double) aggregatedView.ocelotlView.getTimeSliceNumber();
			int i = 0;
			for (i = 0; i < aggregatedView.ocelotlView.getTimeSliceNumber(); i++)
				if (aggregatedView.selectTime.getTimeStampStart() >= (long) (sliceSize * i + aggregatedView.resetTime.getTimeStampStart())
						&& aggregatedView.selectTime.getTimeStampStart() < (long) (sliceSize * (i + 1) + aggregatedView.resetTime.getTimeStampStart())) {
					aggregatedView.selectTime.setTimeStampStart((long) (sliceSize * i + aggregatedView.resetTime.getTimeStampStart()));
					break;
				}
			for (i = 0; i < aggregatedView.ocelotlView.getTimeSliceNumber(); i++)
				if (aggregatedView.selectTime.getTimeStampEnd() > (long) (sliceSize * i + aggregatedView.resetTime.getTimeStampStart())
						&& aggregatedView.selectTime.getTimeStampEnd() <= (long) (sliceSize * (i + 1) + aggregatedView.resetTime.getTimeStampStart())) {
					aggregatedView.selectTime.setTimeStampEnd((long) (sliceSize * (i + 1) + aggregatedView.resetTime.getTimeStampStart()));
					break;
				}
			
			SpaceTimeAggregation2Manager STManager = (SpaceTimeAggregation2Manager) aggregatedView.ocelotlView.getOcelotlCore().getLpaggregManager();
			EventProducerHierarchy hierarchy = STManager.getHierarchy();
			
			// Compute the selected spatiotemporal region
			int y1 = arg0.y;
			
			Rectangle currentRect = new Rectangle(originX, originY, arg0.x - originX, y1 - originY);
			ArrayList<EventProducerNode> currentProducers = new ArrayList<EventProducerNode>();
			
			// 
			for (Rectangle aRect : aggregatedView.epnMapping.keySet()) {
				if (currentRect.intersects(aRect)) {
					currentProducers.add(aggregatedView.epnMapping.get(aRect));
				}
			}
			
			int embeddedEpn = 0;
			int tempCount = 0;
			EventProducerNode selectedNode = currentProducers.get(0);
			// Find the top epn that encompasses all the selected ones
			for (EventProducerNode anEPN : currentProducers) {
				tempCount = 0;
				for (EventProducerNode otherEPN : currentProducers) {
					if (anEPN.contains(otherEPN))
						tempCount++;
				}
				if (tempCount > embeddedEpn) {
					embeddedEpn = tempCount;
					selectedNode = anEPN;
				}
			}

			if (embeddedEpn != currentProducers.size()) {
				selectedNode = hierarchy.findSmallestContainingNode(currentProducers);
			}

			//Compute the selection (cf. SpatioTemporalModeView) 
			
			//compute height (root.height) + logic height
			int rootHeight = aggregatedView.root.getSize().height;
			int height = rootHeight - aggregatedView.aBorder;
	
			int logicHeight = height / hierarchy.getRoot().getWeight();
		
			// then compute correspondence with the weight of each ep
			for (EventProducerNode aChild : hierarchy.getRoot().getChildrenNodes()) {
				if (aChild.getWeight() > originY) {
					//originY = aChild.getIndex() * logicHeight;
					originY = (int) (rootHeight - height +  aChild.getIndex() * logicHeight - 5);
					break;
				}
			}
			
			for (EventProducerNode aChild : hierarchy.getRoot().getChildrenNodes()) {
				if (aChild.getWeight() > y1) {
					//originY = aChild.getIndex() * logicHeight;
					y1 = originY +  aChild.getWeight();
					break;
				}
			}

			int originWeight;
			int cornerWeight;
			
			aggregatedView.ocelotlView.getTimeAxisView().select(aggregatedView.selectTime, true);
			aggregatedView.ocelotlView.setTimeRegion(aggregatedView.selectTime);
			aggregatedView.selectFigure.draw(aggregatedView.selectTime, true, originY, y1);
			aggregatedView.ocelotlView.getOverView().updateSelection(aggregatedView.selectTime);
			aggregatedView.ocelotlView.getStatView().updateData();
		} else {
			aggregatedView.ocelotlView.getTimeAxisView().resizeDiagram();
			aggregatedView.ocelotlView.getOverView().deleteSelection();
			if (aggregatedView.selectFigure.getParent() != null)
				aggregatedView.root.remove(aggregatedView.selectFigure);
			aggregatedView.root.repaint();
		}
	}
	
}
