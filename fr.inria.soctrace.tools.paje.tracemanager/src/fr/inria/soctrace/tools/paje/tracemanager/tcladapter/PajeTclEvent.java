/* ===========================================================
 * Paje Trace Manager module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to import, export and
 * process Pajé trace files
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

package fr.inria.soctrace.tools.paje.tracemanager.tcladapter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.graphics.Color;

import fr.inria.soctrace.framesoc.ui.tcl.ITChartsEvent;

/**
 * Paje implementation of Tcl Event
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PajeTclEvent implements ITChartsEvent {

	private final static int	DEFAULT_EVENT_HEIGHT	= 30;

	private final IFigure		figure;
	private final long			startTime;
	private final long			endTime;
	private List<ITChartsEvent>	outlinkedEvents;

	public PajeTclEvent(final long startTime, final long endTime) {
		figure = createRect(ColorConstants.red, DEFAULT_EVENT_HEIGHT);
		this.startTime = startTime;
		this.endTime = endTime;
		outlinkedEvents = new ArrayList<ITChartsEvent>();
	}

	@Override
	public void addOutlinkedEvent(final ITChartsEvent event) {
		outlinkedEvents.add(event);
	}

	private RectangleFigure createRect(final Color color, final int height) {
		final RectangleFigure rectangle = new RectangleFigure();
		rectangle.setLayoutManager(new BorderLayout());
		rectangle.setBackgroundColor(color);
		rectangle.setOpaque(true);
		rectangle.setSize(0, height);
		return rectangle;
	}

	@Override
	public long getEndTime() {
		return endTime;
	}

	@Override
	public IFigure getFigure() {
		return figure;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public List<ITChartsEvent> getOutlinkedEvents() {
		return outlinkedEvents;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public void setOutlinkedEvents(final List<ITChartsEvent> outlinkedEvents) {
		this.outlinkedEvents = outlinkedEvents;
	}

}