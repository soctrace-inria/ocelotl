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
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.spaceaggregop.StateDistribution;
import fr.inria.soctrace.tools.ocelotl.ui.color.ColorManager;
import fr.inria.soctrace.tools.ocelotl.ui.com.eclipse.wb.swt.SWTResourceManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
abstract public class TimeLineView implements ITimeLineView{

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

	public final static Color					selectColorFG	= ColorConstants.blue;

	public final static Color					selectColorBG	= ColorConstants.lightGray;

	public final static Color					activeColorFG	= ColorConstants.black;
	public final static Color					activeColorBG	= ColorConstants.darkBlue;
	protected Figure						root;
	protected Canvas						canvas;
	protected final List<RectangleFigure>	figures			= new ArrayList<RectangleFigure>();
	protected List<Integer>				parts			= null;
	protected TimeRegion					time;
	protected TimeRegion					selectTime;
	protected TimeRegion					resetTime;
	protected boolean						aggregated		= false;
	protected boolean						numbers			= true;
	protected final static int			Border			= 10;
	protected int							Space			= 6;
	protected final ColorManager			colors			= new ColorManager();

	protected final OcelotlView			ocelotlView;

	private SelectFigure				selectFigure;

	public TimeLineView(final OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;

	}

	public void createDiagram(final List<Integer> parts, final TimeRegion time, final boolean aggregated, final boolean numbers) {
		root.removeAll();
		figures.clear();
		canvas.update();
		this.aggregated = aggregated;
		this.parts = parts;
		this.time = time;
		if (time != null) {
			resetTime = new TimeRegion(time);
			selectTime = new TimeRegion(time);
		}
		this.numbers = numbers;
		Space = 6;
		computeDiagram();		
//		final int partHeight = (int) (root.getSize().height / 1.1 - Border);
//		if (parts != null) {
//			while ((root.getSize().width - 2 * Border) / parts.size() - 2 < Space && Space != 0)
//				Space = Space - 1;
//			if (!aggregated)
//				for (int i = 0; i < parts.size(); i++) {
//					final PartFigure part = new PartFigure(i, parts.get(i), colors.getColors().get(parts.get(i) % colors.getColors().size()), numbers);
//					figures.add(part);
//					root.add(part, new Rectangle(new Point(i * (root.getSize().width - 2 * Border) / parts.size() + Border, root.getSize().height / 2 - partHeight / 2), new Point((i + 1) * (root.getSize().width - 2 * Border) / parts.size() + Border - Space,
//							root.getSize().height / 2 + partHeight / 2)));
//					part.getUpdateManager().performUpdate();
//					part.init();
//				}
//			else if (ocelotlView.getParams().getSpaceAggOperator().equals("No Aggregation")) {
//				final List<Integer> aggParts = new ArrayList<Integer>();
//				for (int i = 0; i <= parts.get(parts.size() - 1); i++)
//					aggParts.add(0);
//				for (int i = 0; i < parts.size(); i++)
//					aggParts.set(parts.get(i), aggParts.get(parts.get(i)) + 1);
//				int j = 0;
//				for (int i = 0; i < aggParts.size(); i++) {
//					// TODO manage parts
//					final PartFigure part = new PartFigure(i, i, colors.getColors().get(j % colors.getColors().size()), numbers);
//					figures.add(part);
//					root.add(
//							part,
//							new Rectangle(new Point(j * (root.getSize().width - 2 * Border) / parts.size() + Border, root.getSize().height), new Point((j + aggParts.get(i)) * (root.getSize().width - 2 * Border) / parts.size() - Space + Border, 0 + root
//									.getSize().height / 10)));
//					j = j + aggParts.get(i);
//					part.getUpdateManager().performUpdate();
//					part.init();
//				}
//			} else
//				for (int i = 0; i < ocelotlView.getCore().getSpaceOperator().getPartNumber(); i++) {
//					// TODO manage parts
//					final MultiState part = new MultiState(i, (StateDistribution) ocelotlView.getCore().getSpaceOperator(), root, Space);
//					// figures.add(part);
//					part.init();
//					// part.getUpdateManager().performUpdate();
//				}
//		}
	}

	abstract protected void computeDiagram();

	@SuppressWarnings("unused")
	private IFigure createPart() {
		final RectangleFigure rectangleFigure = new RectangleFigure();
		rectangleFigure.setBackgroundColor(ColorConstants.lightGray);
		rectangleFigure.setLayoutManager(new ToolbarLayout());
		rectangleFigure.setForegroundColor(ColorConstants.white);
		return rectangleFigure;
	}

	public void deleteDiagram() {
		root.removeAll();
		figures.clear();
		root.repaint();
	}

	public Canvas initDiagram(final Composite parent) {
		root = new Figure();
		root.setFont(parent.getFont());
		final XYLayout layout = new XYLayout();
		root.setLayoutManager(layout);
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
		canvas.setSize(parent.getSize());
		final LightweightSystem lws = new LightweightSystem(canvas);
		lws.setContents(root);
		lws.setControl(canvas);
		root.setFont(SWTResourceManager.getFont("Cantarell", 24, SWT.NORMAL));
		root.setSize(parent.getSize().x, parent.getSize().y);
		canvas.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(final ControlEvent arg0) {
				// TODO Auto-generated method stub
				canvas.redraw();
				// root.repaint();
				resizeDiagram();

			}

			@Override
			public void controlResized(final ControlEvent arg0) {
				canvas.redraw();
				// root.repaint();
				resizeDiagram();
			}
		});

		final TimeMouseListener mouse = new TimeMouseListener();
		root.addMouseListener(mouse);
		root.addMouseMotionListener(mouse);
		selectFigure = new SelectFigure();

		return canvas;
	}

	public void resizeDiagram() {
		createDiagram(parts, time, aggregated, numbers);
		root.repaint();
	}

}
