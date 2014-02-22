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
abstract public class TimeLineView extends AggregatedView implements IAggregatedView {

	
	protected List<Integer>					parts	= null;

	public TimeLineView(final OcelotlView ocelotlView) {
		super(ocelotlView);

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
	

	public List<Integer> getParts() {
		return parts;
	}
	
	@Override
	public void resizeDiagram() {
		createDiagram(parts, time);
		root.repaint();
	}

}
