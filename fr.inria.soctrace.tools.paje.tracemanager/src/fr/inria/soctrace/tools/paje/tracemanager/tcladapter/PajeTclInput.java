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

import fr.inria.soctrace.framesoc.ui.tcl.ITChartsInput;
import fr.inria.soctrace.framesoc.ui.tcl.ITChartsRow;
import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType;
import fr.inria.soctrace.lib.model.AnalysisResultSearchData;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.ISearchable;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.lib.storage.TraceDBObject;

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

	private List<Event> getPresentationList(final Trace trace) {

		try {
			final ITraceSearch search = new TraceSearch().initialize();
			final IntervalDesc desc = new IntervalDesc(100, 10000000000L);
			final List<Event> elist = search.getEventsByInterval(trace, desc);
			search.uninitialize();
			return elist;
		} catch (final SoCTraceException e) {
			e.printStackTrace();
		}
		return null;
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
	public void loadPage(final Trace trace, List<Event> elist) {

		// XXX Hard coded result: used for the article
		// Ignore the passed list, and use the result one.
		//		elist.clear();
		//		elist = getArticleList(trace);

		elist.clear();
		elist = getPresentationList(trace);

		// load all producers
		final Map<Integer, EventProducer> eps = new HashMap<Integer, EventProducer>();
		try {
			final TraceDBObject traceDB = TraceDBObject.openNewIstance(trace.getDbName());
			final EventProducerQuery query = new EventProducerQuery(traceDB);
			final List<EventProducer> producers = query.getList();
			for (final EventProducer ep : producers)
				eps.put(ep.getId(), ep);
			traceDB.close();
		} catch (final SoCTraceException e) {
			e.printStackTrace();
		}

		//  CPU          EP id
		final Map<Integer, Map<Integer, ITChartsRow>> rows = new HashMap<Integer, Map<Integer, ITChartsRow>>();
		//  CPU      row
		final Map<Integer, ITChartsRow> main = new HashMap<Integer, ITChartsRow>();

		// iterate over all page events 
		Event lastEvent = null;
		ITChartsRow lastProducerRow = null;

		for (final Event e : elist) {
			// get the map containing all the rows for this CPU
			if (!rows.containsKey(e.getCpu())) {
				rows.put(e.getCpu(), new HashMap<Integer, ITChartsRow>());
				main.put(e.getCpu(), new PajeTclRow("CPU " + e.getCpu(), null, null, e.getCpu()));
				addTChartsRow(main.get(e.getCpu()));
			}
			final Map<Integer, ITChartsRow> cpuMap = rows.get(e.getCpu());

			// get the row for the given producer
			if (!cpuMap.containsKey(e.getEventProducer().getId()))
				cpuMap.put(e.getEventProducer().getId(), getNewEventProducerRow(e.getEventProducer(), eps, cpuMap, main.get(e.getCpu())));
			final ITChartsRow producerRow = cpuMap.get(e.getEventProducer().getId());

			// finally add the event if I'm on the end
			//final long OFFSET = 298400000000L;
			final long OFFSET = 0;
			if (lastEvent != null)
				lastProducerRow.addEvent(new PajeTclEvent(lastEvent.getTimestamp() - OFFSET, e.getTimestamp() - OFFSET));

			lastEvent = e;
			lastProducerRow = producerRow;

		}
	}

}