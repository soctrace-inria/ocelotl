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
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.tools.ocelotl.ui.com.eclipse.wb.swt.SWTResourceManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class TimeLineViewWrapper{

	private Figure						root;
	private Canvas						canvas;
	private final OcelotlView			ocelotlView;
	private ITimeLineView view;
	private List<ControlListener> controlListeners = new ArrayList<ControlListener>();
	private List<MouseListener> mouseListeners = new ArrayList<MouseListener>();
	private List<MouseMotionListener> mouseMotionListeners = new ArrayList<MouseMotionListener>();
	
	
	
	public Figure getRoot() {
		return root;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public TimeLineViewWrapper(final OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
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

	public OcelotlView getOcelotlView() {
		return ocelotlView;
	}

	public ITimeLineView getView() {
		return view;
	}

	public void setView(ITimeLineView view) {
		this.view = view;
		view.init(this);
	}

	public void cleanControlListeners() {
		for (ControlListener c: controlListeners){
			canvas.removeControlListener(c);
		}
		controlListeners.clear();	
	}

	public void addControlListener(ControlListener controlListener) {
		controlListeners.add(controlListener);
		canvas.addControlListener(controlListener);
		
	}

	public void cleanMouseListeners() {
		for (MouseListener m: mouseListeners){
			root.removeMouseListener(m);
		}
		mouseListeners.clear();	
		
	}

	public void cleanMouseMotionListeners() {
		for (MouseMotionListener m: mouseMotionListeners){
			root.removeMouseMotionListener(m);
		}
		mouseMotionListeners.clear();	
		
	}

	public void addMouseListener(MouseListener mouse) {
		mouseListeners.add(mouse);
		root.addMouseListener(mouse);
		
	}

	public void addMouseMotionListener(MouseMotionListener mouse) {
		mouseMotionListeners.add(mouse);
		root.addMouseMotionListener(mouse);
		
	}

}