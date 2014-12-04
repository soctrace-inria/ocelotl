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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class UnitAxisViewWrapper {

	private Figure							root;
	private Canvas							canvas;
	private final OcelotlView				ocelotlView;
	private UnitAxisView					view;
	private final List<ControlListener>		controlListeners		= new ArrayList<ControlListener>();
	
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
