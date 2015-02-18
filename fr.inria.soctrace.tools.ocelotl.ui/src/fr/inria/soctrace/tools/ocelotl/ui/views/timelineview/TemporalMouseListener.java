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

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView.MouseState;

public class TemporalMouseListener extends OcelotlMouseListener {


	public TemporalMouseListener(AggregatedView theview) {
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
		if ((state == MouseState.PRESSED_LEFT || state == MouseState.DRAG_LEFT || state == MouseState.DRAG_LEFT_START) && arg0.getLocation().getDistance(currentPoint) > 10) {
			long moved = (long) ((double) ((arg0.x - aggregatedView.getBorder()) * aggregatedView.getResetTime().getTimeDuration()) / (aggregatedView.getRoot().getSize().width() - 2 * aggregatedView.getBorder())) + aggregatedView.getResetTime().getTimeStampStart();
			if (state != MouseState.DRAG_LEFT_START)
				state = MouseState.DRAG_LEFT;
			
			moved = Math.max(moved, aggregatedView.getResetTime().getTimeStampStart());
			moved = Math.min(moved, aggregatedView.getResetTime().getTimeStampEnd());
			fixed = Math.max(fixed, aggregatedView.getResetTime().getTimeStampStart());
			fixed = Math.min(fixed, aggregatedView.getResetTime().getTimeStampEnd());
			
			if (fixed < moved)
				aggregatedView.setSelectTime(new TimeRegion(fixed, moved));
			else
				aggregatedView.setSelectTime(new TimeRegion(moved, fixed));
			
			// Compute current potential selection
			Point timeslices = getTimeSlices(aggregatedView.getSelectTime());
			aggregatedView.setPotentialSelectTime(setTemporalSelection(timeslices.x(), timeslices.y()));
			
			aggregatedView.getOcelotlView().setTimeRegion(aggregatedView.getPotentialSelectTime());
			aggregatedView.getOcelotlView().getTimeAxisView().select(aggregatedView.getPotentialSelectTime(), false);	
			aggregatedView.getPotentialSelectFigure().draw(aggregatedView.getPotentialSelectTime(), -1, -1);
			aggregatedView.getSelectFigure().draw(aggregatedView.getSelectTime(), false, -1, -1);
			aggregatedView.getOcelotlView().getOverView().updateSelection(aggregatedView.getPotentialSelectTime());
			
			if (aggregatedView.getOcelotlView().getTimeRegion().compareTimeRegion(aggregatedView.time)) {
				aggregatedView.getOcelotlView().getTimeAxisView().unselect();
				aggregatedView.getOcelotlView().getUnitAxisView().unselect();
				if (aggregatedView.getSelectFigure().getParent() != null)
					aggregatedView.getRoot().remove(aggregatedView.getSelectFigure());
				aggregatedView.getRoot().repaint();
			}
		}
	}

	@Override
	public void mouseEntered(final MouseEvent arg0) {
	}

	@Override
	public void mouseExited(final MouseEvent arg0) {
		setShellCursor(SWT.CURSOR_ARROW);
		
		if (state != MouseState.RELEASED && state != MouseState.H_MOVE_START && state != MouseState.H_MOVE_END && state != MouseState.EXITED) {
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
		if (aggregatedView.getSelectFigure() != null && aggregatedView.getRoot().getChildren().contains(aggregatedView.getSelectFigure()))
			if (Math.abs(aggregatedView.getSelectFigure().getBounds().x - arg0.x) < Threshold) {
				state = MouseState.H_MOVE_START;
				setShellCursor(SWT.CURSOR_SIZEWE);
			} else if (Math.abs(aggregatedView.getSelectFigure().getBounds().x + aggregatedView.getSelectFigure().getBounds().width - arg0.x) < Threshold) {
				state = MouseState.H_MOVE_END;
				setShellCursor(SWT.CURSOR_SIZEWE);
			} else {
				state = MouseState.RELEASED;
				setShellCursor(SWT.CURSOR_ARROW);
			}
	}

	@Override
	public void mousePressed(final MouseEvent arg0) {
		if (arg0.button == 1 && aggregatedView.getResetTime() != null) {
			currentPoint = arg0.getLocation();
			long p3 = (long) ((double) ((arg0.x - aggregatedView.getBorder()) * aggregatedView.getResetTime().getTimeDuration()) / (aggregatedView.getRoot().getSize().width() - 2 * aggregatedView.getBorder())) + aggregatedView.getResetTime().getTimeStampStart();
			if (state == MouseState.H_MOVE_START) {
				p3 = aggregatedView.getSelectTime().getTimeStampStart();
				fixed = aggregatedView.getSelectTime().getTimeStampEnd();
				state = MouseState.DRAG_LEFT_START;
			} else if (state == MouseState.H_MOVE_END) {
				p3 = aggregatedView.getSelectTime().getTimeStampEnd();
				fixed = aggregatedView.getSelectTime().getTimeStampStart();
				state = MouseState.DRAG_LEFT;
			} else {
				state = MouseState.PRESSED_LEFT;
				p3 = Math.max(p3, aggregatedView.getResetTime().getTimeStampStart());
				p3 = Math.min(p3, aggregatedView.getResetTime().getTimeStampEnd());
				aggregatedView.setSelectTime(new TimeRegion(aggregatedView.getResetTime()));
				aggregatedView.getSelectTime().setTimeStampStart(p3);
				aggregatedView.getSelectTime().setTimeStampEnd(p3);
				fixed = p3;
			}
			
			// Compute current potential selection
			Point timeslices = getTimeSlices(aggregatedView.getSelectTime());
			aggregatedView.setPotentialSelectTime(setTemporalSelection(timeslices.x(), timeslices.y()));
						
			aggregatedView.getOcelotlView().setTimeRegion(aggregatedView.getPotentialSelectTime());
			aggregatedView.getOcelotlView().getTimeAxisView().select(aggregatedView.getPotentialSelectTime(), false);	
			aggregatedView.getPotentialSelectFigure().draw(aggregatedView.getPotentialSelectTime(), -1, -1);
			aggregatedView.getSelectFigure().draw(aggregatedView.getSelectTime(), false, -1, -1);
			aggregatedView.getOcelotlView().getOverView().updateSelection(aggregatedView.getPotentialSelectTime());
		}
		
		// If middle click, cancel selection
		if(arg0.button == 2 && aggregatedView.getResetTime() != null) {
			// Reset selected time region to displayed time region
			aggregatedView.getOcelotlView().cancelSelection();
		}
	}

	@Override
	public void mouseReleased(final MouseEvent arg0) {

		setShellCursor(SWT.CURSOR_ARROW);
		
		if (state == MouseState.DRAG_LEFT || state == MouseState.DRAG_LEFT_START)
			mouseDragged(arg0);
		
		state = MouseState.RELEASED;
		aggregatedView.getPotentialSelectFigure().delete();
		
		if (aggregatedView.time == null)
			return;

		// If the selection is different than the whole region
		if (!aggregatedView.getOcelotlView().getTimeRegion().compareTimeRegion(aggregatedView.time)) {
			
			// Get time slice numbers from the time slice manager
			Point timeslices = getTimeSlices(aggregatedView.getSelectTime());
			aggregatedView.setSelectTime(setTemporalSelection(timeslices.x(), timeslices.y()));
			
			aggregatedView.getOcelotlView().getTimeAxisView().select(aggregatedView.getSelectTime(), true);
			aggregatedView.getOcelotlView().setTimeRegion(aggregatedView.getSelectTime());
			aggregatedView.getSelectFigure().draw(aggregatedView.getSelectTime(), true, -1, -1);
			aggregatedView.getOcelotlView().getOverView().updateSelection(aggregatedView.getSelectTime());
			aggregatedView.getOcelotlView().getStatView().updateData();
		} else {
			aggregatedView.getOcelotlView().getTimeAxisView().resizeDiagram();
			aggregatedView.getOcelotlView().getOverView().deleteSelection();
			if (aggregatedView.getSelectFigure().getParent() != null)
				aggregatedView.getRoot().remove(aggregatedView.getSelectFigure());
			aggregatedView.getRoot().repaint();
		}
	}
	
	/**
	 * Given a starting and ending time slices, compute the corresponding
	 * starting and ending time stamps
	 * 
	 * @param startingTimeSlice
	 * @param endingTimeslice
	 * @return a TimeRegion starting at the found starting timeslice, and
	 *         finishing at the ending time slice
	 */
	protected TimeRegion setTemporalSelection(int startingTimeSlice, int endingTimeslice) {
		// Since timestamps start and end of two adjacent time slice overlap,
		// add 1 to the starting timestamp
		long startTimeStamp = aggregatedView.getOcelotlView().getCore().getMicroModel().getTimeSliceManager().getTimeSlices().get(startingTimeSlice).getTimeRegion().getTimeStampStart() + 1;

		// Since the timestamp of the last time slice goes further than the max
		// timestamp of the trace, we must check that we are not over it
		long endTimeStamp = Math.min(aggregatedView.getResetTime().getTimeStampEnd(), aggregatedView.getOcelotlView().getCore().getMicroModel().getTimeSliceManager().getTimeSlices().get(endingTimeslice).getTimeRegion().getTimeStampEnd());

		return new TimeRegion(startTimeStamp, endTimeStamp);
	}
	
	/**
	 * Given a timeRegion, find the starting and ending time slice containing
	 * this region
	 * 
	 * @param aTimeRegion
	 * @return a Point whose X is the starting time slice, and Y is the ending
	 *         time slice
	 */
	protected Point getTimeSlices(TimeRegion aTimeRegion) {
		int startingSlice = (int) aggregatedView.getOcelotlView().getCore().getMicroModel().getTimeSliceManager().getTimeSlice(aTimeRegion.getTimeStampStart());
		int endingSlice = (int) aggregatedView.getOcelotlView().getCore().getMicroModel().getTimeSliceManager().getTimeSlice(aTimeRegion.getTimeStampEnd());

		return new Point(startingSlice, endingSlice);
	}

	@Override
	public void drawSelection() {
		if (!aggregatedView.getOcelotlView().getTimeRegion().compareTimeRegion(aggregatedView.time) && aggregatedView.getSelectTime() != null)
			aggregatedView.getSelectFigure().draw(aggregatedView.getSelectTime(), true, -1, -1);
	}
}
