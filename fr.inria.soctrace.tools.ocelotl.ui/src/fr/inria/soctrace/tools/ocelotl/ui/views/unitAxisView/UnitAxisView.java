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

package fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView;

import java.util.HashMap;

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
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;

/**
 * Unit Axis View : show a Y axis on the side
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
abstract public class UnitAxisView {

	protected Figure				root;
	protected Canvas				canvas;
	protected OcelotlView			ocelotlView;
	protected YAxisMouseListener	mouse;
	protected SelectFigure			highLightDisplayedProducer;
	protected HashMap<Rectangle, EventProducerNode>	figures;

	public UnitAxisView() {
		super();
		figures = new HashMap<Rectangle, EventProducerNode>();
	}

	public HashMap<Rectangle, EventProducerNode> getFigures() {
		return figures;
	}

	public void setFigures(HashMap<Rectangle, EventProducerNode> figures) {
		this.figures = figures;
	}

	public OcelotlView getOcelotlView() {
		return ocelotlView;
	}

	public void setOcelotlView(OcelotlView ocelotlView) {
		this.ocelotlView = ocelotlView;
	}

	public SelectFigure getHighLightDisplayedProducer() {
		return highLightDisplayedProducer;
	}

	public void setHighLightDisplayedProducer(SelectFigure highLightDisplayedProducer) {
		this.highLightDisplayedProducer = highLightDisplayedProducer;
	}

	public abstract void createDiagram(final IVisuOperator manager);

	public abstract void resizeDiagram();

	public abstract void initDiagram();
	
	public abstract void select(final int y0, final int y1, final boolean active);

	public abstract void unselect();

	public void init(final UnitAxisViewWrapper wrapper) {
		root = wrapper.getRoot();
		canvas = wrapper.getCanvas();
		ocelotlView = wrapper.getOcelotlView();
		initDiagram();
		
		highLightDisplayedProducer = new SelectFigure(ColorConstants.black, ColorConstants.white, 110);
		highLightDisplayedProducer.setLineWidth(2);
		highLightDisplayedProducer.setAlpha(255);
		highLightDisplayedProducer.setFill(false);
		
		wrapper.cleanMouseListeners();
		wrapper.cleanMouseMotionListeners();
		wrapper.addMouseListener(mouse);
		wrapper.addMouseMotionListener(mouse);
		
		wrapper.cleanControlListeners();
		wrapper.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(final ControlEvent arg0) {
				canvas.redraw();
				root.repaint();
				resizeDiagram();
			}

			@Override
			public void controlResized(final ControlEvent arg0) {
				canvas.redraw();
				root.repaint();
				resizeDiagram();
			}
		});
	}
	
	public void deleteDiagram() {
		root.removeAll();
		root.repaint();
	}

	protected class SelectFigure extends RectangleFigure {

		public SelectFigure() {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			setAlpha(50);
		}

		public SelectFigure(Color foreGround, Color backGround, int alphaValue) {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			setForegroundColor(foreGround);
			setBackgroundColor(backGround);
			setAlpha(alphaValue);
		}

		public void draw(int originY, int cornerY, final boolean active) {
			if (active) {
				setForegroundColor(AggregatedView.activeColorFG);
				setBackgroundColor(AggregatedView.activeColorBG);
			} else {
				setForegroundColor(AggregatedView.selectColorBG);
				setBackgroundColor(AggregatedView.selectColorBG);
			}
			root.add(this,
					new Rectangle(new Point(0, originY), new Point(root.getClientArea().width,
							cornerY)));
		}

		public void draw(int originX, int originY, int cornerX, int cornerY) {
			root.add(this, new Rectangle(new Point(originX, originY),
					new Point(cornerX, cornerY)));
		}
		
		/**
		 * Remove the selection from display
		 */
		public void delete() {
			if (getParent() != null)
				root.remove(this);
		}
	}
}
