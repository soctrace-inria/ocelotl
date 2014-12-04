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

import org.eclipse.draw2d.Figure;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
abstract public class UnitAxisView {

	protected Figure	root;
	protected Canvas	canvas;

	public UnitAxisView() {
		super();
	}

	public abstract void createDiagram(final IVisuOperator manager);

	public abstract void resizeDiagram();

	public abstract Canvas initDiagram(Composite compositeUnitAxisView);

	public void init(final UnitAxisViewWrapper wrapper) {
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
	}
	
	public void deleteDiagram() {
		root.removeAll();
		root.repaint();
	}

}
