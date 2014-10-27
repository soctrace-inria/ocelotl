/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
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
	 * ALL: 
	 * TS: number of time slices has changed
	 * NORMALIZE: quality curve normalization has changed 
	 * PARAMETER: parameter has changed
	 * EQ: 
	 * NOTHING: nothing has changed
	 */
	static public enum HasChanged {
		ALL, TS, NORMALIZE, THRESHOLD, PARAMETER, EQ, NOTHING
	}
	
	/**
	 * Token used for the CSV file format
	 */
	static public String CSVDelimiter = ";";
	
	/**
	 * Number of field in the header of a cache file 
	 */
	static public final int CACHE_HEADER_NORMAL_SIZE = 10;
	
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
	 * Size of the configuration file
	 */
	static final public int CONFIGURATION_NORMAL_SIZE = 9;
}
