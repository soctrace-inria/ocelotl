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
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Canvas;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

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
	protected List<HierarchyView>	subHierarchies;
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

	public List<HierarchyView> getSubHierarchies() {
		return subHierarchies;
	}

	public void setSubHierarchies(List<HierarchyView> subHierarchies) {
		this.subHierarchies = subHierarchies;
	}

	public OcelotlView getOcelotlView() {
		return ocelotlView;
	}

	public void setOcelotlView(OcelotlView ocelotlView) {
		this.ocelotlView = ocelotlView;
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

}
