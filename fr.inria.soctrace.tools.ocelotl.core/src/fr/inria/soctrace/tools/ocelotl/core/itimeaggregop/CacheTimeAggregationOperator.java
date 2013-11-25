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

import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

public abstract class CacheTimeAggregationOperator extends MultiThreadTimeAggregationOperator {

	@Override
	abstract protected void computeMatrix() throws SoCTraceException, InterruptedException;

	@Override
	protected void computeSubMatrix(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		if (parameters.isCache())
			computeSubMatrixCached(eventProducers);
		else
			computeSubMatrixNonCached(eventProducers);
	}

	protected abstract void computeSubMatrixCached(List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException;

	protected abstract void computeSubMatrixNonCached(List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException;

	@Override
	abstract protected void initVectors() throws SoCTraceException;

}
