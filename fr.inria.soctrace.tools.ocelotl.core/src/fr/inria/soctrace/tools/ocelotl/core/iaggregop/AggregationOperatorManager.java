/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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

package fr.inria.soctrace.tools.ocelotl.core.iaggregop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.TraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeNormalizedStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeStateTypeSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.config.PajeConfig;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;

public class AggregationOperatorManager {

	HashMap<String, IAggregationOperator>	List;
	HashMap<String, TraceTypeConfig>		Config;
	ArrayList<String>						Names;
	OcelotlParameters						parameters;

	public AggregationOperatorManager(final OcelotlParameters parameters) {
		super();
		this.parameters = parameters;
		try {
			init();
			initConfig();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public TraceTypeConfig config(final IAggregationOperator op) {
		return Config.get(op.traceType());
	}

	public TraceTypeConfig config(final String op) {
		return Config.get(List.get(op).traceType());
	}

	public Collection<IAggregationOperator> getList() {
		return List.values();
	}

	public IAggregationOperator getOperator(final String name) throws SoCTraceException {
		final IAggregationOperator op = List.get(name);
		op.setOcelotlParameters(parameters);
		return op;
	}

	public String getType(final String op) {
		return List.get(op).traceType();
	}

	private void init() throws SoCTraceException {
		List = new HashMap<String, IAggregationOperator>();
		List.put(PajeStateSum.descriptor, new PajeStateSum());
		List.put(PajeNormalizedStateSum.descriptor, new PajeNormalizedStateSum());
		List.put(PajeStateTypeSum.descriptor, new PajeStateTypeSum());

	}

	private void initConfig() throws SoCTraceException {
		Config = new HashMap<String, TraceTypeConfig>();
		Config.put(PajeConstants.PajeFormatName, new PajeConfig());
	}

}
