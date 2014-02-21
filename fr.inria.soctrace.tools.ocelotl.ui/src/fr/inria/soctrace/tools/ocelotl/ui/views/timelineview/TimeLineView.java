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

package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;

import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.IMicroDescManager;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.TimeAggregationManager;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
abstract public class TimeLineView implements IAggregatedView {

	private class SelectFigure extends RectangleFigure {

		public SelectFigure() {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			setForegroundColor(ColorConstants.blue);
			setBackgroundColor(ColorConstants.lightGray);
			setAlpha(50);
		}

		public void draw(final TimeRegion timeRegion, final boolean active) {
			if (active) {
				setForegroundColor(activeColorFG);
				setBackgroundColor(activeColorBG);
			} else {
				setForegroundColor(selectColorFG);
				setBackgroundColor(selectColorBG);
			}
			if (getParent() != root)
				root.add(this);
			root.setConstraint(this,
					new Rectangle(new Point((int) ((timeRegion.getTimeStampStart() - time.getTimeStampStart()) * (root.getSize().width - 2 * Border) / time.getTimeDuration() + Border), root.getSize().height), new Point(
							(int) ((timeRegion.getTimeStampEnd() - time.getTimeStampStart()) * (root.getSize().width - 2 * Border) / time.getTimeDuration() + Border), 2)));
			root.repaint();
		}
	}

	static public enum State {
		PRESSED_D, DRAG_D, PRESSED_G, RELEASED;
	}

	class TimeMouseListener implements MouseListener, MouseMotionListener {

		State	state	= State.RELEASED;
		Point	currentPoint;

		@Override
		public void mouseDoubleClicked(final MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseDragged(final MouseEvent arg0) {
			if ((state == State.PRESSED_D || state == State.DRAG_D) && arg0.getLocation().getDistance(currentPoint) > 10) {
				state = State.DRAG_D;
				long p3 = (long) ((double) ((arg0.x - Border) * resetTime.getTimeDuration()) / (root.getSize().width() - 2 * Border)) + resetTime.getTimeStampStart();
				p3 = Math.max(p3, resetTime.getTimeStampStart());
				p3 = Math.min(p3, resetTime.getTimeStampEnd());
				long p1 = selectTime.getTimeStampStart();
				long p2 = selectTime.getTimeStampEnd();
				if (p3 > p1)
					p2 = p3;
				else if (p3 < p1)
					p1 = p3;
				selectTime = new TimeRegion(p1, p2);
				ocelotlView.setTimeRegion(selectTime);
				ocelotlView.getTimeAxisView().select(selectTime, false);
				selectFigure.draw(selectTime, false);
				if (ocelotlView.getTimeRegion().compareTimeRegion(time)) {
					ocelotlView.getTimeAxisView().unselect();
					if (selectFigure.getParent() != null)
						root.remove(selectFigure);
					root.repaint();
				}
			}

		}

		@Override
		public void mouseEntered(final MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(final MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseHover(final MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseMoved(final MouseEvent arg0) {

		}

		@Override
		public void mousePressed(final MouseEvent arg0) {
			state = State.PRESSED_D;
			long p3 = (long) ((double) ((arg0.x - Border) * resetTime.getTimeDuration()) / (root.getSize().width() - 2 * Border)) + resetTime.getTimeStampStart();
			p3 = Math.max(p3, resetTime.getTimeStampStart());
			p3 = Math.min(p3, resetTime.getTimeStampEnd());
			selectTime.setTimeStampStart(p3);
			selectTime.setTimeStampEnd(p3);
			currentPoint = arg0.getLocation();
			ocelotlView.setTimeRegion(selectTime);
			ocelotlView.getTimeAxisView().select(selectTime, false);
			selectFigure.draw(selectTime, false);
		}

		@Override
		public void mouseReleased(final MouseEvent arg0) {
			state = State.RELEASED;
			if (!ocelotlView.getTimeRegion().compareTimeRegion(time)) {
				ocelotlView.getTimeAxisView().select(selectTime, true);
				selectFigure.draw(selectTime, true);
			} else {
				ocelotlView.getTimeAxisView().resizeDiagram();
				if (selectFigure.getParent() != null)
					root.remove(selectFigure);
				root.repaint();
			}
			selectTime = new TimeRegion(resetTime);
		}

	}

	public final static Color	selectColorFG	= ColorConstants.blue;

	public final static Color	selectColorBG	= ColorConstants.lightGray;

	public final static Color	activeColorFG	= ColorConstants.black;
	public final static Color	activeColorBG	= ColorConstants.darkBlue;

	public static Color getActivecolorbg() {
		return activeColorBG;
	}

	public static Color getActivecolorfg() {
		return activeColorFG;
	}

	public static int getBorder() {
		return Border;
	}

	public static Color getSelectcolorbg() {
		return selectColorBG;
	}

	public static Color getSelectcolorfg() {
		return selectColorFG;
	}

	protected Figure						root;
	protected Canvas						canvas;
	protected final List<RectangleFigure>	figures	= new ArrayList<RectangleFigure>();
	protected List<Integer>					parts	= null;

	protected TimeRegion					time;

	protected TimeRegion					selectTime;

	protected TimeRegion					resetTime;

	public final static int					Border	= 10;

	protected int							Space	= 4;

	protected final OcelotlView				ocelotlView;

	private SelectFigure					selectFigure;

	public TimeLineView(final OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;

	}

	abstract protected void computeDiagram();

	@Override
	public void createDiagram(IMicroDescManager manager, final TimeRegion time) {
		createDiagram(((TimeAggregationManager) manager).getParts(), time);
	}
	
	public void createDiagram(List<Integer> parts, final TimeRegion time) {
		root.removeAll();
		figures.clear();
		canvas.update();
		this.parts = parts;
		this.time = time;
		if (time != null) {
			resetTime = new TimeRegion(time);
			selectTime = new TimeRegion(time);
		}
		Space = 6;
		computeDiagram();
	}

	@SuppressWarnings("unused")
	private IFigure createPart() {
		final RectangleFigure rectangleFigure = new RectangleFigure();
		rectangleFigure.setBackgroundColor(ColorConstants.lightGray);
		rectangleFigure.setLayoutManager(new ToolbarLayout());
		rectangleFigure.setForegroundColor(ColorConstants.white);
		return rectangleFigure;
	}

	@Override
	public void deleteDiagram() {
		root.removeAll();
		figures.clear();
		root.repaint();
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public List<RectangleFigure> getFigures() {
		return figures;
	}

	public OcelotlView getOcelotlView() {
		return ocelotlView;
	}

	public List<Integer> getParts() {
		return parts;
	}

	public TimeRegion getResetTime() {
		return resetTime;
	}

	public Figure getRoot() {
		return root;
	}

	public SelectFigure getSelectFigure() {
		return selectFigure;
	}

	public TimeRegion getSelectTime() {
		return selectTime;
	}

	public int getSpace() {
		return Space;
	}

	public TimeRegion getTime() {
		return time;
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

		final TimeMouseListener mouse = new TimeMouseListener();
		wrapper.cleanMouseListeners();
		wrapper.cleanMouseMotionListeners();
		wrapper.addMouseListener(mouse);
		wrapper.addMouseMotionListener(mouse);
		selectFigure = new SelectFigure();
	}

	@Override
	public void resizeDiagram() {
		createDiagram(parts, time);
		root.repaint();
	}
	
	public long getStart(){
			return selectTime.getTimeStampStart();
		
	}

public long getEnd(){
	return selectTime.getTimeStampEnd();
}

}
