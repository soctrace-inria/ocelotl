package fr.inria.soctrace.tools.ocelotl.core.parameters;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacachePolicy;

/*
 * Class for the constants defining the default parameters in Ocelotl
 */

public class OcelotlDefaultParameterConstants {

	/**
	 * Precision Threshold
	 */
	public static final double Threshold = 0.001;

	/**
	 * Starting timestamp
	 */
	public static final long TimestampStart = 0;

	/**
	 * Ending timestamp
	 */
	public static final long TimestampEnd = 1;

	/**
	 * Should the quality be normalized ?
	 */
	public static final boolean Normalize = false;

	/**
	 * Should the curves show increasing quality ?
	 */
	public static final boolean IncreasingQualities = true;

	/**
	 * Number of time slices
	 */
	public static final int TimeSliceNumber = 100;

	/**
	 * Minimal Number of timeslices
	 */
	public static final int minTimeslice = 2;

	/**
	 * Minimal Number of timeslices
	 */
	public static final int maxTimeslice = 10000;

	/**
	 * Gain/Loss ratio
	 */
	public static final double RunParameter = 1.0;

	/**
	 * Number of event producer per query (0 = all)
	 */
	public static final int EventProducersPerQuery = 0;

	/**
	 * Is the cache activated by default
	 */
	public static final boolean DEFAULT_CACHE_ACTIVATION = true;

	/**
	 * Default cache policy
	 */
	public static final DatacachePolicy DEFAULT_CACHE_POLICY = DatacachePolicy.CACHEPOLICY_FAST;

	/**
	 * Default number of time slices used when generating a cache
	 */
	public static final int DEFAULT_CACHE_TS_NUMBER = 1000;

	/**
	 * Number of threads for computation
	 */
	public static final int NUMBER_OF_THREADS = 8;
	
	/**
	 * Visualization operator for the overview
	 */
	public static final String OVERVIEW_VISU_OPERATOR = "Simple Mode";
	
	/**
	 * Aggregation Operator for the overview
	 */
	public static final String OVERVIEW_AGGREG_OPERATOR = "Temporal Aggregation";
	
	/**
	 * Number of events loaded by thread
	 */
	public static final int EVENTS_PER_THREAD = 10000;
	public static final int MIN_EVENTS_PER_THREAD = 100;
	public static final int MAX_EVENTS_PER_THREAD = 10000000;
	
	public static final int MIN_NUMBER_OF_THREAD = 1;
	public static final int MAX_NUMBER_OF_THREAD = 1000000;
	
	public static final int MIN_EVENT_PRODUCERS_PER_QUERY = 0;
	public static final int MAX_EVENT_PRODUCERS_PER_QUERY = 1000000;
	
}
