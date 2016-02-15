package fr.inria.soctrace.tools.ocelotl.core.monitor;

public class MonitorMessages {

	/*  */
	public final static String ReadingTrace = "Reading Trace...";
	public final static String ReadingCache = "Building Trace Abstraction from Cache...";
	public final static String ComputingAggregatedView = "Computing Aggregated View...";
	public final static String Initialization = "Initializing Aggregation Operator...";
	public final static String AggregationProcess = "Aggregation Process...";
	public final static String Rendering = "Rendering...";
	
	public final static String BuildAbstraction = "Building Trace Abstraction";
	public final static String LoadData = "Gathering Data from Trace...";
	public final static String LoadCache = "Reading Data from File...";
	
	public final static String subQualities = "Computing Measures from Trace Abstraction";
	public final static String subDichotomy = "Getting Relevant Partitions of Trace Abstraction and Building Quality Curves";
	public final static String subParts = "Computing Partition";
	
	public final static String subDiagram = "Drawing Aggregated View";
	public final static String subCurves = "Drawing Quality Curves";
	public final static String subStats = "Updating Statistics Views";
	public final static String subY = "Drawing Y Axis";
	public final static String subOverview = "Computing Overview";
	
	public final static String subDBQuery = "Querying Database";
	public final static String subDBReading = "Reading Database Result Set";
	
	public final static String subCacheSaving = "Saving Trace Abstraction in Cache";
	public final static String subCacheLoading = "Building Trace Abstraction from Newly Generated Cache";
	
	public final static String subInitializing = "Initializing";
	public final static String subLoading = "Loading";
	public final static String subPartial = "Loading Incomplete Data from Database";
	
	public final static String subConfiguringProducers = "Configuring Event Producers";
}
