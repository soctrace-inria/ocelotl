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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.framesoc.ui.gantt.LoadDescriptor;
import fr.inria.soctrace.framesoc.ui.tcl.ITChartsInput;
import fr.inria.soctrace.framesoc.ui.tcl.ITChartsRow;
import fr.inria.soctrace.framesoc.ui.tcl.TclGanttView.TclViewHandle;
import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType;
import fr.inria.soctrace.lib.model.AnalysisResultSearchData;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.ISearchable;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;

/**
 * Paje implementation of Tcl input.
 * The input contains the Tcl rows.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PajeTclInput implements ITChartsInput {

	private final ArrayList<ITChartsRow>	mainItems		= new ArrayList<ITChartsRow>();
	private boolean							startTimeSet	= false;
	private long							startTime;
	private boolean							endTimeSet		= false;
	private long							endTime;

	/**
	 * Debug flag
	 */
	public final static boolean				DEBUG			= false;

	@Override
	public void addTChartsRow(final ITChartsRow main) {
		mainItems.add(main);
	}

	@Override
	public void clear() {
		mainItems.clear();
		startTimeSet = false;
		endTimeSet = false;
	}

	/**
	 * Print a debug message
	 * @param s message
	 */
	private void debug(final String s) {
		if (DEBUG)
			System.out.println("[Default Input] " + s);
	}

	@SuppressWarnings("unused")
	private List<Event> getArticleList(final Trace trace) {

		// Result label: the first result with this label is loaded. Take care.
		final String FILTER_RESULT_LABEL = "thelast";
		// Filter tool name
		final String FILTER_NAME = "Filter Tool";

		try {
			final ITraceSearch search = new TraceSearch().initialize();
			final Tool filter = search.getToolByName(FILTER_NAME);
			final List<AnalysisResult> alist = search.getAnalysisResultsByToolAndType(trace, filter, AnalysisResultType.TYPE_SEARCH);
			AnalysisResult ar = null;
			for (final AnalysisResult a : alist)
				if (a.getDescription().equals(FILTER_RESULT_LABEL)) {
					ar = a;
					break;
				}
			if (ar == null)
				return null;
			final AnalysisResultSearchData data = (AnalysisResultSearchData) search.getAnalysisResultData(trace, ar);
			@SuppressWarnings("unchecked")
			final List<Event> elist = (List<Event>) (List<? extends ISearchable>) data.getElements();
			search.uninitialize();
			return elist;
		} catch (final SoCTraceException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getEndTime() {
		if (endTimeSet)
			return endTime;

		endTime = mainItems.get(0).getEndTime();
		long time;
		for (final ITChartsRow item : mainItems) {
			time = item.getEndTime();
			if (time > endTime)
				endTime = time;
		}
		endTimeSet = true;
		return endTime;
	}

	private ITChartsRow getNewEventProducerRow(final EventProducer ep, final Map<Integer, EventProducer> eps, final Map<Integer, ITChartsRow> cpuRows, final ITChartsRow cpuRow) {

		debug("Creating event producer row " + ep.getId() + ", parent " + ep.getParentId());

		ITChartsRow parentRow = cpuRow;
		// if there's a parent
		if (ep.getParentId() != EventProducer.NO_PARENT_ID)
			// if there is already its row
			if (cpuRows.containsKey(ep.getParentId()))
				parentRow = cpuRows.get(ep.getParentId());
			else {
				parentRow = getNewEventProducerRow(eps.get(ep.getParentId()), eps, cpuRows, cpuRow);
				cpuRows.put(ep.getParentId(), parentRow);
			}
		return new PajeTclRow(ep.getName(), null, parentRow, ep.getId());
	}

	@Override
	public long getStartTime() {
		if (startTimeSet)
			return startTime;

		startTime = mainItems.get(0).getStartTime();
		long time;
		for (final ITChartsRow item : mainItems) {
			time = item.getStartTime();
			if (time < startTime)
				startTime = time;
		}
		startTimeSet = true;
		return startTime;
	}

	@Override
	public ArrayList<ITChartsRow> getTChartsRows() {
		return mainItems;
	}

	@Override
	public LoadDescriptor loadTimeWindow(Trace trace, long startTimestamp, long endTimestamp, IProgressMonitor monitor, TclViewHandle handle) throws SoCTraceException {
		throw new SoCTraceException("not implemented");	
		
	}

}