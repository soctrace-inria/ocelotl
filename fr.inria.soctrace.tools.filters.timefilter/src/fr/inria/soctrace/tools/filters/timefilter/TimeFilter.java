/* ===========================================================
 * Filter Tool module
 * ===========================================================
 *
 * (C) Copyright 2013 Damien Dosimont. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 */

package fr.inria.soctrace.tools.filters.timefilter;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

public class TimeFilter {

	private static final boolean DEBUG = true;
	private static final boolean TEST = true;

	public static boolean isDebug() {
		return DEBUG;
	}

	public static boolean isTest() {
		return TEST;
	}

	TimeFilterParameters params;
	Queries queries;
	ResultManager results;

	public TimeFilter(TimeFilterParameters timefilterParameters)
			throws SoCTraceException {
		super();
		params = timefilterParameters;
		queries = new Queries(params);
		results = new ResultManager(queries, params);
	}

	public void compute() throws SoCTraceException {
		results.saveEventSearchResult();
	}

}
