package fr.inria.soctrace.tools.filters.timefilter;

import java.util.List;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType;
import fr.inria.soctrace.lib.model.AnalysisResultSearchData;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.IdManager;

public class ResultManager {

	private Queries					queries;
	private TimeFilterParameters	timeFilterParameters;
	private IdManager				idManager	= new IdManager();

	public ResultManager() {
		super();
	}

	public ResultManager(Queries queries, TimeFilterParameters timeFilterParameters) {
		super();
		this.queries = queries;
		this.timeFilterParameters = timeFilterParameters;
	}

	public Queries getQueries() {
		return queries;
	}

	public void setQueries(Queries queries) {
		this.queries = queries;
	}

	public TimeFilterParameters getTimeFilterParameters() {
		return timeFilterParameters;
	}

	public void setTimeFilterParameters(TimeFilterParameters timeFilterParameters) {
		this.timeFilterParameters = timeFilterParameters;
	}

	public void saveEventProducerSearchResult() throws SoCTraceException {

		ITraceSearch traceSearch = new TraceSearch().initialize();
		Tool tool = traceSearch.getToolByName(TimeFilterConstants.TOOL_NAME);
		traceSearch.uninitialize();
		// Tool tool =
		// ToolContributionManager.getTool("fr.inria.soctrace.tools.filters.ui");
		// // XXX experimental

		AnalysisResultSearchData searchData = new AnalysisResultSearchData(EventProducer.class);
		List<EventProducer> list = queries.getEventProducers();
		searchData.setElements(list);
		searchData.setSearchCommand(timeFilterParameters.getLabel() + "_cmd");
		AnalysisResult result = new AnalysisResult(searchData, tool);
		result.setDescription(timeFilterParameters.getLabel());
		result.setId(idManager.getNextId());
		result.setType("SEARCH");
		saveResult(timeFilterParameters.getTrace(), result, AnalysisResultType.TYPE_SEARCH);
	}

	public void saveEventSearchResult() throws SoCTraceException {

		ITraceSearch traceSearch = new TraceSearch().initialize();
		Tool tool = traceSearch.getToolByName(TimeFilterConstants.TOOL_NAME);
		traceSearch.uninitialize();
		// Tool tool =
		// ToolContributionManager.getTool("fr.inria.soctrace.tools.filters.ui");
		// // XXX experimental

		AnalysisResultSearchData searchData = new AnalysisResultSearchData(Event.class);
		List<Event> list = queries.getEvent();
		searchData.setElements(list);
		searchData.setSearchCommand(timeFilterParameters.getLabel() + "_cmd");
		AnalysisResult result = new AnalysisResult(searchData, tool);
		result.setDescription(timeFilterParameters.getLabel());
		result.setId(idManager.getNextId());
		result.setType("SEARCH");
		saveResult(timeFilterParameters.getTrace(), result, AnalysisResultType.TYPE_SEARCH);
	}

	private static void saveResult(Trace trace, AnalysisResult result, AnalysisResultType type) throws SoCTraceException {
		TraceDBObject traceDB = new TraceDBObject(trace.getDbName(), DBMode.DB_OPEN);
		traceDB.save(result);
		traceDB.close();
	}
}
