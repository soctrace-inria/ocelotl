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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
abstract public class AggregatedView implements IAggregatedView {
	
	protected Figure						root;
	protected Canvas						canvas;
	protected final List<RectangleFigure>	figures			= new ArrayList<RectangleFigure>();
	protected TimeRegion					time;
	protected TimeRegion					selectTime;
	protected TimeRegion					resetTime;
	protected int							aBorder			= 10;
	protected final int						space			= 3;
	protected final OcelotlView				ocelotlView;
	private SelectFigure					selectFigure;
	protected IVisuOperator					visuOperator	= null;
	public final static Color				selectColorFG	= ColorConstants.blue;
	public final static Color				selectColorBG	= ColorConstants.lightGray;
	public final static Color				activeColorFG	= ColorConstants.black;
	public final static Color				activeColorBG	= ColorConstants.darkBlue;

	private class SelectFigure extends RectangleFigure {

		private SelectFigure() {
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
					new Rectangle(new Point((int) ((timeRegion.getTimeStampStart() - time.getTimeStampStart()) * (root.getSize().width - 2 * aBorder) / time.getTimeDuration() + aBorder), root.getSize().height), new Point(
							(int) ((timeRegion.getTimeStampEnd() - time.getTimeStampStart()) * (root.getSize().width - 2 * aBorder) / time.getTimeDuration() + aBorder), 2)));
			root.repaint();
		}
	}

	static public enum State {
		PRESSED_D, DRAG_D, PRESSED_G, DRAG_G, DRAG_G_START, RELEASED, MOVE_START, MOVE_END, EXITED;
	}

	class TimeMouseListener implements MouseListener, MouseMotionListener {

		private static final long	Threshold	= 5;
		State						state		= State.RELEASED;
		State						previous	= State.RELEASED;
		Point						currentPoint;
		Display						display		= Display.getCurrent();
		Shell						shell		= display.getActiveShell();
		long						fixed;

		public TimeMouseListener() {
			super();
			display = Display.getCurrent();
			shell = display.getActiveShell();

		}

		@Override
		public void mouseDoubleClicked(final MouseEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseDragged(final MouseEvent arg0) {
			if ((state == State.PRESSED_G || state == State.DRAG_G || state == State.DRAG_G_START) && arg0.getLocation().getDistance(currentPoint) > 10) {
				long moved = (long) ((double) ((arg0.x - aBorder) * resetTime.getTimeDuration()) / (root.getSize().width() - 2 * aBorder)) + resetTime.getTimeStampStart();
				if (state != State.DRAG_G_START)
					state = State.DRAG_G;
				moved = Math.max(moved, resetTime.getTimeStampStart());
				moved = Math.min(moved, resetTime.getTimeStampEnd());
				fixed = Math.max(fixed, resetTime.getTimeStampStart());
				fixed = Math.min(fixed, resetTime.getTimeStampEnd());
				if (fixed < moved)
					selectTime = new TimeRegion(fixed, moved);
				else
					selectTime = new TimeRegion(moved, fixed);
				ocelotlView.setTimeRegion(selectTime);
				ocelotlView.getTimeAxisView().select(selectTime, false);
				selectFigure.draw(selectTime, false);
				ocelotlView.getOverView().updateSelection(selectTime);
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
			if (selectFigure != null && root.getChildren().contains(selectFigure))
				if (Math.abs(selectFigure.getBounds().x - arg0.x) < Threshold) {
					state = State.MOVE_START;
					shell.setCursor(new Cursor(display, SWT.CURSOR_SIZEWE));
				} else if (Math.abs(selectFigure.getBounds().x + selectFigure.getBounds().width - arg0.x) < Threshold) {
					state = State.MOVE_END;
					shell.setCursor(new Cursor(display, SWT.CURSOR_SIZEWE));
				} else {
					state = State.RELEASED;
					shell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));
				}

		}

		@Override
		public void mousePressed(final MouseEvent arg0) {
			if (arg0.button == 1 && resetTime != null) {
				currentPoint = arg0.getLocation();
				long p3 = (long) ((double) ((arg0.x - aBorder) * resetTime.getTimeDuration()) / (root.getSize().width() - 2 * aBorder)) + resetTime.getTimeStampStart();
				if (state == State.MOVE_START) {
					p3 = selectTime.getTimeStampStart();
					fixed = selectTime.getTimeStampEnd();
					state = State.DRAG_G_START;
				} else if (state == State.MOVE_END) {
					p3 = selectTime.getTimeStampEnd();
					fixed = selectTime.getTimeStampStart();
					state = State.DRAG_G;
				} else {
					state = State.PRESSED_G;
					p3 = Math.max(p3, resetTime.getTimeStampStart());
					p3 = Math.min(p3, resetTime.getTimeStampEnd());
					selectTime = new TimeRegion(resetTime);
					selectTime.setTimeStampStart(p3);
					selectTime.setTimeStampEnd(p3);
					fixed = p3;
				}
				ocelotlView.setTimeRegion(selectTime);
				ocelotlView.getTimeAxisView().select(selectTime, false);
				selectFigure.draw(selectTime, false);
				ocelotlView.getOverView().updateSelection(selectTime);
			}
		}

		@Override
		public void mouseReleased(final MouseEvent arg0) {

			shell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));
			if (state == State.DRAG_G || state == State.DRAG_G_START)
				mouseDragged(arg0);
			state = State.RELEASED;
			if (time == null)
				return;

			// If the selection is different than the whole region
			if (!ocelotlView.getTimeRegion().compareTimeRegion(time)) {
				// Get time slice numbers from the time slice manager
				int startingSlice = (int) ocelotlView.getOcelotlCore().getMicroModel().getTimeSliceManager()
						.getTimeSlice(selectTime.getTimeStampStart());
				int endingSlice = (int) ocelotlView.getOcelotlCore().getMicroModel().getTimeSliceManager().getTimeSlice(
						selectTime.getTimeStampEnd());
				
				// Since the timestamp of the last time slice goes further than the max timestamp of the trace, we must check that we are not over it
				long endTimeStamp = Math.min(resetTime.getTimeStampEnd(), ocelotlView.getOcelotlCore().getMicroModel().getTimeSliceManager().getTimeSlices().get(endingSlice).getTimeRegion().getTimeStampEnd());
				
				selectTime.setTimeStampStart(ocelotlView.getOcelotlCore().getMicroModel().getTimeSliceManager().getTimeSlices().get(startingSlice).getTimeRegion().getTimeStampStart());
				selectTime.setTimeStampEnd(endTimeStamp);
				ocelotlView.getTimeAxisView().select(selectTime, true);
				ocelotlView.setTimeRegion(selectTime);
				selectFigure.draw(selectTime, true);
				ocelotlView.getOverView().updateSelection(selectTime);
				ocelotlView.getStatView().updateData();
			} else {
				ocelotlView.getTimeAxisView().resizeDiagram();
				ocelotlView.getOverView().deleteSelection();
				if (selectFigure.getParent() != null)
					root.remove(selectFigure);
				root.repaint();
			}

		}
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

	private byte[] createImage(final Figure figure, final int format) {

		final Device device = Display.getCurrent();
		final Rectangle r = figure.getBounds();

		final ByteArrayOutputStream result = new ByteArrayOutputStream();

		Image image = null;
		GC gc = null;
		Graphics g = null;
		try {
			image = new Image(device, r.width, r.height);
			gc = new GC(image);
			g = new SWTGraphics(gc);
			g.translate(r.x * -1, r.y * -1);

			figure.paint(g);

			final ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] { image.getImageData() };
			imageLoader.save(result, format);
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (g != null)
				g.dispose();
			if (gc != null)
				gc.dispose();
			if (image != null)
				image.dispose();
		}
		return result.toByteArray();
	}

	// TODO take resolution in to account (given as a parameter)
	@Override
	public void createSnapshotFor(final String fileName) {
		final byte[] imageBytes = createImage(root, SWT.IMAGE_PNG);

		try {
			final FileOutputStream out = new FileOutputStream(fileName);
			out.write(imageBytes);
			out.flush();
			out.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteDiagram() {
		root.removeAll();
		figures.clear();
		root.repaint();
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
				ocelotlView.getOverView().resizeDiagram();
			}

			@Override
			public void controlResized(final ControlEvent arg0) {
				canvas.redraw();
				resizeDiagram();
				ocelotlView.getOverView().resizeDiagram();
			}
		});

		final TimeMouseListener mouse = new TimeMouseListener();
		wrapper.cleanMouseListeners();
		wrapper.cleanMouseMotionListeners();
		wrapper.addMouseListener(mouse);
		wrapper.addMouseMotionListener(mouse);
		selectFigure = new SelectFigure();
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

}
