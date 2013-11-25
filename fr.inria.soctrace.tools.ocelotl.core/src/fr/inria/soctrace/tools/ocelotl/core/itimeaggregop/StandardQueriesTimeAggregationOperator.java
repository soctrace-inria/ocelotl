/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
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

package fr.inria.soctrace.tools.ocelotl.core.itimeaggregop;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;

public abstract class StandardQueriesTimeAggregationOperator extends CacheTimeAggregationOperator {

	protected OcelotlQueries	ocelotlQueries;

	public OcelotlQueries getQuery() {
		return ocelotlQueries;
	}

	@Override
	public void initQueries() {
		try {
			ocelotlQueries = new OcelotlQueries(parameters);
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ocelotlQueries.checkTimeStamps();
	}

}
