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

import org.eclipse.swt.graphics.Image;

import fr.inria.soctrace.framesoc.ui.tcl.ITChartsEvent;
import fr.inria.soctrace.framesoc.ui.tcl.ITChartsRow;

/**
 * Paje implementation for Tcl Row.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PajeTclRow implements ITChartsRow {

	private final String			name;
	private final Image				img;
	private final ITChartsRow		parent;
	private ArrayList<ITChartsRow>	items;
	private List<ITChartsEvent>		events;
	private boolean					startTimeSet		= false;
	private long					startTime;
	private boolean					endTimeSet			= false;
	private long					endTime;
	private final int				order;

	private final static int		DEFAULT_ROW_HEIGHT	= 15;

	public PajeTclRow(final String name, final Image img, final ITChartsRow parent, final int order) {
		this.name = name; // XXX just to test we are using the correct model 
		this.img = img;
		this.parent = parent;
		items = new ArrayList<ITChartsRow>();
		events = new ArrayList<ITChartsEvent>();

		if (parent != null)
			getParent().addChildRow(this);

		this.order = order;
	}

	@Override
	public void addChildRow(final ITChartsRow row) {
		items.add(row);
	}

	@Override
	public void addEvent(final ITChartsEvent event) {
		events.add(event);
	}

	@Override
	public ArrayList<ITChartsRow> getChildrenRows() {
		return items;
	}

	@Override
	public long getEndTime() {
		if (!endTimeSet) {
			setEndTime();
			endTimeSet = true;
		}
		return endTime;
	}

	@Override
	public List<ITChartsEvent> getEvents() {
		return events;
	}

	@Override
	public Image getImg() {
		return img;
	}

	@Override
	public int getMaxEventHeight() {
		return DEFAULT_ROW_HEIGHT;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ITChartsRow getParent() {
		return parent;
	}

	@Override
	public int getPosition() {
		return order;
	}

	@Override
	public long getStartTime() {
		if (!startTimeSet) {
			setStartTime();
			startTimeSet = true;
		}
		return startTime;
	}

	@Override
	public void setChildrenRows(final ArrayList<ITChartsRow> rows) {
		items = rows;
		setStartTime();
		setEndTime();
	}

	private void setEndTime() {
		if (!events.isEmpty()) {
			long eventEndTime;
			endTime = events.get(0).getEndTime();

			for (final ITChartsEvent event : events) {
				eventEndTime = event.getEndTime();
				if (eventEndTime > endTime)
					endTime = eventEndTime;
			}
		}

		if (!items.isEmpty()) {
			long itemEndTime;
			if (events.isEmpty())
				endTime = items.get(0).getEndTime();

			for (final ITChartsRow item : items) {
				itemEndTime = item.getEndTime();
				if (itemEndTime > endTime)
					endTime = itemEndTime;
			}
		}
	}

	/*
	 *  utilities
	 */

	@Override
	public void setEvents(final List<ITChartsEvent> events) {
		this.events = events;
	}

	private void setStartTime() {
		if (!events.isEmpty()) {
			long eventStartTime;
			startTime = events.get(0).getStartTime();

			for (final ITChartsEvent event : events) {
				eventStartTime = event.getStartTime();
				if (eventStartTime < startTime)
					startTime = eventStartTime;
			}
		}

		if (!items.isEmpty()) {
			long itemStartTime;
			if (events.isEmpty())
				startTime = items.get(0).getStartTime();

			for (final ITChartsRow item : items) {
				itemStartTime = item.getStartTime();
				if (itemStartTime < startTime)
					startTime = itemStartTime;
			}
		}
	}

}