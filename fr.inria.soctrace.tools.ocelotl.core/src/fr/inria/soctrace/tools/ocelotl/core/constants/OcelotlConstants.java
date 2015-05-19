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

package fr.inria.soctrace.tools.ocelotl.core.constants;

public class OcelotlConstants {

	/**
	 * States of the state machine
	 * ALL: need to recompute everything
	 * NORMALIZE: quality curve normalization has changed 
	 * PARAMETER: parameter has changed
	 * NOTHING: nothing has changed
	 */
	static public enum HasChanged {
		ALL, TS, NORMALIZE, THRESHOLD, PARAMETER, NOTHING
	}
	
	/**
	 * Token used for the CSV file format
	 */
	static public String CSVDelimiter = ";";
	
	/**
	 * Number of field in the header of a cache file 
	 */
	static public final int DATACACHE_HEADER_NORMAL_SIZE = 9;
	static public final int DICHOTOMYCACHE_HEADER_NORMAL_SIZE = 11;
	
	/**
	 * Maximum size of the data cache in bytes.
	 * A value of -1 means no size limit
	 */
	static public int MAX_CACHESIZE = -1;
	
	/**
	 * Minimum ratio value for the datacache
	 */
	static public double MINIMAL_TIMESLICE_RATIO = 1.0;
	
	/**
	 * Maximal ratio value of dirty time slices in a cache
	 */
	static public double MAXIMAL_DIRTY_RATIO = 1.0;
	
	/**
	 * Possible datacache rebuilding strategies
	 */
	static public enum DatacacheStrategy {
		DATACACHE_PROPORTIONAL, DATACACHE_DATABASE
	}
	
	/**
	 * Possible datacache policies (used to choose strategy)
	 */
	static public enum DatacachePolicy {
		CACHEPOLICY_FAST, CACHEPOLICY_SLOW, CACHEPOLICY_AUTO, CACHEPOLICY_ASK
	}
	
	/**
	 * Possible parameter p chose strategy
	 */
	static public enum ParameterPPolicy {
		PARAMETERP_STRATEGY_ONE, PARAMETERP_STRATEGY_ZERO, PARAMETERP_STRATEGY_LARGEST_DIFF, PARAMETERP_STRATEGY_LARGEST_SUM_DIFF,
		PARAMETERP_STRATEGY_LARGEST_SUM_DIFF2
	}
	
	public static final String MultipleValueExtensionSeparator = ", ";
	
	public static final int MinimalHeightDrawingThreshold = 1;
	
	public static final int[] yAxisDefaultWeight = {30, 388};
	
	public static final double TemporalProportionDrawingMarginRatio = 0.9;
	

	public static final String DataCacheSuffix = ".otlcache";
	public static final String DichotomyCacheSuffix = ".otldicho";
	
	/**
	 * JSON Config constants
	 */
	public static final String JSONCacheActivated = "cacheActivated";
	public static final String JSONDichoCacheActivated = "dichoCacheActivated";
	public static final String JSONCacheDirectory = "cacheDirectory";
	public static final String JSONCacheSize = "cacheSize";
	public static final String JSONSnapShotDirectory = "snapShotDirectory";
	public static final String JSONCachePolicy = "cachePolicy";
	public static final String JSONCacheTimeSliceNumber = "cacheTimeSliceNumber";
	public static final String JSONEventsPerThread = "eventsPerThread";
	public static final String JSONMaxEventProducersPerQuery = "maxEventProducersPerQuery";
	public static final String JSONNumberOfThread = "numberOfThread";
	public static final String JSONNormalizedCurve = "normalizedCurve";
	public static final String JSONThresholdPrecision = "thresholdPrecision";
	public static final String JSONIncreasingQualities = "increasingQualities";
	public static final String JSONSnapshotXResolution = "snapshotXResolution";
	public static final String JSONSnapshotYResolution = "snapshotYResolution";
	public static final String JSONYAxisXResolution = "yAxisXResolution";
	public static final String JSONXAxisYResolution = "xAxisYResolution";
	public static final String JSONQualCurveXResolution = "qualCurveXResolution";
	public static final String JSONQualCurveYResolution = "qualCurveYResolution";
	public static final String JSONAggregateLeaves = "aggregateLeaves";
	public static final String JSONMaxNumberOfLeaves = "maxNumberOfLeaves";
	public static final String JSONOverviewAggregateLeaves = "overviewAggregateLeaves";
	public static final String JSONOverviewMaxNumberOfLeaves = "overviewMaxNumberOfLeaves";
	
	public static final String JSONEnableOverview = "enableOverview";
	public static final String JSONOverviewAggregOperator = "overviewAggregOperator";
	public static final String JSONOverviewTimesliceNumber = "overviewTimesliceNumber";
	public static final String JSONOverviewSelectionFgColor = "overviewSelectionFgColor";
	public static final String JSONOverviewSelectionBgColor = "overviewSelectionBgColor";
	public static final String JSONOverviewSelectionAlpha = "overviewSelectionAlpha";
	public static final String JSONOverviewDisplayFgColor = "overviewDisplayFgColor";
	public static final String JSONOverviewDisplayBgColor = "overviewDisplayBgColor";
	public static final String JSONOverviewDisplayAlpha = "overviewDisplayAlpha";	
	public static final String JSONParameterPPolicy = "parameterPPolicy";	
	
	public static final String JSONMainSelectionFgColor = "mainSelectionFgColor";
	public static final String JSONMainSelectionBgColor = "mainSelectionBgColor";
	public static final String JSONMainSelectionAlpha = "mainSelectionAlpha";
	public static final String JSONMainDisplayFgColor = "mainDisplayFgColor";
	public static final String JSONMainDisplayBgColor = "mainDisplayBgColor";
	public static final String JSONMainDisplayAlpha = "mainDisplayAlpha";	
	
	public static final String JSONUseVisualAggregate = "useVisualAggregate";
}
