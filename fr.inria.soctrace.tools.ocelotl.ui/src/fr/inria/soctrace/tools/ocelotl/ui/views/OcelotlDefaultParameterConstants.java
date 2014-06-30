package fr.inria.soctrace.tools.ocelotl.ui.views;
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
	public static final boolean Normalize = true;
	
	/**
	 * Should the curves show increasing quality ?
	 */
	public static final boolean GrowingQualities = true;
	
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
}
