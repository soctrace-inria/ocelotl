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

package fr.inria.soctrace.tools.ocelotl.ui.loaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultSearchData;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.ISearchable;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.AnalysisResultQuery;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.search.utils.Printer;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Convenience class to load Trace data related to LPAggreg configuration.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class ConfDataLoader {

	private Trace					currentTrace	= null;
	private List<Trace>				traces;
	private List<EventProducer>		producers;
	private List<EventType>			types;
	private List<List<EventType> >	typesByCat;
	private long					minTimestamp;
	private long					maxTimestamp;
	private List<AnalysisResult>	results;
	public final static String		STATE			= "STATE";
	public final static String		PUNCTUAL_EVENT	= "EVENT";
	public final static String		LINK			= "LINK";
	public final static String		VARIABLE		= "VARIABLE";
	public final static String		ALL				= "ALL";
	public final static int[] category = {EventCategory.PUNCTUAL_EVENT, EventCategory.STATE, EventCategory.LINK, EventCategory.VARIABLE};
	
	private static final Logger		logger			= LoggerFactory.getLogger(ConfDataLoader.class);

	/** The constructor. */
	public ConfDataLoader() {
		clean();
	}

	private void clean() {
		currentTrace = null;
		producers = null;
		types = null;
		minTimestamp = maxTimestamp = 0;
	}

	public Trace getCurrentTrace() {
		return currentTrace;
	}

	public long getMaxTimestamp() {
		return Math.max(1, maxTimestamp);
	}

	public long getMinTimestamp() {
		return minTimestamp;
	}

	public List<EventProducer> getProducers() {
		return producers;
	}

	public List<EventProducer> getProducersFromResult(final AnalysisResult result) throws SoCTraceException {
		final ITraceSearch traceSearch = new TraceSearch().initialize();
		traceSearch.getAnalysisResultData(currentTrace, result);
		traceSearch.uninitialize();
		final AnalysisResultSearchData data = (AnalysisResultSearchData) result.getData();
		final List<ISearchable> search = data.getElements();
		final List<Integer> id = new ArrayList<Integer>();
		final List<EventProducer> prodFromResult = new ArrayList<EventProducer>();
		for (final ISearchable s : search)
			id.add(s.getId());
		for (final EventProducer ep : producers)
			if (id.contains(ep.getId()))
				prodFromResult.add(ep);
		return prodFromResult;
		// TODO test with cast, please
	}

	public List<AnalysisResult> getResults() {
		return results;
	}

	public List<Trace> getTraces() {
		return traces;
	}

	public List<EventType> getTypes() {
		return types;
	}

	/**
	 * Load the information related to the new Trace.
	 * 
	 * @param trace
	 *            trace to consider
	 * @throws SoCTraceException
	 */
	public void load(final Trace trace) throws SoCTraceException {
		clean();
		currentTrace = trace;
		final TraceDBObject traceDB = new TraceDBObject(trace.getDbName(), DBMode.DB_OPEN);
		final EventProducerQuery pQuery = new EventProducerQuery(traceDB);
		producers = pQuery.getList();
		final EventTypeQuery tQuery = new EventTypeQuery(traceDB);
		types = tQuery.getList();
		setSubTypes();
		minTimestamp = currentTrace.getMinTimestamp();
		maxTimestamp = currentTrace.getMaxTimestamp();

		final AnalysisResultQuery aQuery = new AnalysisResultQuery(traceDB);
		results = aQuery.getList();
		traceDB.close();
	}

	public List<Trace> loadTraces() throws SoCTraceException {
		clean();
		final SystemDBObject sysDB = FramesocManager.getInstance().getSystemDB();
		final TraceQuery tQuery = new TraceQuery(sysDB);
		traces = tQuery.getList();
		sysDB.close();

		// Sort alphabetically
		Collections.sort(traces, new Comparator<Trace>() {
			@Override
			public int compare(final Trace arg0, final Trace arg1) {
				return arg0.getAlias().compareTo(arg1.getAlias());
			}
		});
		return traces;
	}

	/**
	 * Check if the loaded trace has events of a specific category
	 * 
	 * @param aCategory
	 *            must be one of the constant defining a category in
	 *            {@link EventCategory} (i.e. STATE, LINK, VARIABLE,
	 *            PUNCTUAL_EVENT)
	 * @return true if the trace contains at least one event type belonging to
	 *         the category
	 */
	public boolean hasEventOfCategory(int aCategory) {
		for (EventType anEventType : getTypes()) {
			if (anEventType.getCategory() == aCategory) {
				return true;
			}
		}

		return false;
	}

	/** Debug print method */
	public void print() {
		Printer.printTraceList(traces);
		Printer.printIModelElementsList(producers);
		Printer.printIModelElementsList(types);
		logger.debug("min ts: " + minTimestamp);
		logger.debug("max ts: " + maxTimestamp);
	}

	public List<String> getCategories() {
		ArrayList<String> category = new ArrayList<String>();
		if (hasEventOfCategory(EventCategory.STATE))
			category.add(STATE);
		if (hasEventOfCategory(EventCategory.PUNCTUAL_EVENT))
			category.add(PUNCTUAL_EVENT);
		if (hasEventOfCategory(EventCategory.LINK))
			category.add(LINK);
		if (hasEventOfCategory(EventCategory.VARIABLE))
			category.add(VARIABLE);
		if (!category.isEmpty())
			category.add(ALL);
		return category;
	}
	
	private void setSubTypes(){
		typesByCat=new ArrayList<List<EventType>>();
		for (int i=0; i<category.length; i++){
			typesByCat.add(new ArrayList<EventType>());
		}
		for (EventType et: types){
			typesByCat.get(et.getCategory()).add(et);
		}
	}

	public List<EventType> getTypes(List<String> eventCategory) {

		List<EventType> nlist = new ArrayList<EventType>();
		for (String c :eventCategory){
			if (c.equals(ALL)){
				nlist.addAll(getTypes());
				return nlist;
			}
		}
		for (String c :eventCategory){
			if (c.equals(PUNCTUAL_EVENT))
				nlist.addAll(typesByCat.get(EventCategory.PUNCTUAL_EVENT));
			if (c.equals(STATE))
				nlist.addAll(typesByCat.get(EventCategory.STATE));
			if (c.equals(LINK))
				nlist.addAll(typesByCat.get(EventCategory.LINK));
			if (c.equals(VARIABLE))
				nlist.addAll(typesByCat.get(EventCategory.VARIABLE));
		}
		return nlist;
	}
	
	public List<EventType> getTypes (int cat){
		return typesByCat.get(cat);
	}
	
	public List<EventType> getTypes (String cat){
		ArrayList<String> str = new ArrayList<String>();
		str.add(cat);
		return getTypes(str);
	}

	public List<List<EventType>> getTypesByCat() {
		return typesByCat;
	}

	
	
}
