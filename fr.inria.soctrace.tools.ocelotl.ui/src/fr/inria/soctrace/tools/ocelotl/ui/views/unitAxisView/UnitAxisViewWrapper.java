/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Unit Axis View: display an axis alongside the main view
 *  
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class UnitAxisViewWrapper {

	private Figure							root;
	private Canvas							canvas;
	private final OcelotlView				ocelotlView;
	private UnitAxisView					view;
	private final List<ControlListener>		controlListeners		= new ArrayList<ControlListener>();
	private final List<MouseListener>		mouseListeners			= new ArrayList<MouseListener>();
	private final List<MouseMotionListener>	mouseMotionListeners	= new ArrayList<MouseMotionListener>();

	public UnitAxisViewWrapper(final OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	public void addControlListener(final ControlListener controlListener) {
		controlListeners.add(controlListener);
		canvas.addControlListener(controlListener);
	}
	
	public void cleanControlListeners() {
		for (final ControlListener c : controlListeners)
			canvas.removeControlListener(c);
		controlListeners.clear();
	}
	
	public Canvas getCanvas() {
		return canvas;
	}

	public OcelotlView getOcelotlView() {
		return ocelotlView;
	}

	public Figure getRoot() {
		return root;
	}

	public UnitAxisView getView() {
		return view;
	}
	
	public void addMouseListener(final MouseListener mouse) {
		mouseListeners.add(mouse);
		root.addMouseListener(mouse);
	}

	public void addMouseMotionListener(final MouseMotionListener mouse) {
		mouseMotionListeners.add(mouse);
		root.addMouseMotionListener(mouse);
	}
	
	public void cleanMouseListeners() {
		for (final MouseListener m : mouseListeners)
			root.removeMouseListener(m);
		mouseListeners.clear();
	}

	public void cleanMouseMotionListeners() {
		for (final MouseMotionListener m : mouseMotionListeners)
			root.removeMouseMotionListener(m);
		mouseMotionListeners.clear();
	}

	public Canvas init(final Composite parent) {
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
		return canvas;
	}

	public void setView(final UnitAxisView view) {
		this.view = view;
		view.init(this);
	}

}
