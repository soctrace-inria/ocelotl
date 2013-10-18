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
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.TraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.paje.PajeConfig;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeNormalizedStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeStateTypeSum;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;


public class AggregationOperators {
	
ArrayList<IAggregationOperator> List;
HashMap<String, TraceTypeConfig> Config;
ArrayList<String> Names;
Query query;

public AggregationOperators(Query query) {
	super();
	this.query=query;
	try {
		init();
		initConfig();
	} catch (SoCTraceException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

}

private void init() throws SoCTraceException{
	List = new ArrayList<IAggregationOperator>();
	List.add(new PajeStateSum());
	List.add(new PajeNormalizedStateSum());
	List.add(new PajeStateTypeSum());
	
}

private void initConfig() throws SoCTraceException{
	Config = new HashMap<String, TraceTypeConfig>();
	Config.put(PajeConstants.PajeFormatName, new PajeConfig());
	
	
}

public List<IAggregationOperator> getList(){
	return List;
}

public IAggregationOperator createOperator(String name) throws SoCTraceException{
	init();
	for (IAggregationOperator op: List){
		if (op.descriptor().equals(name)){
			op.setQueries(query);
			return op;
		}
	}
	return null;
	
}

public TraceTypeConfig config(IAggregationOperator op){
		return Config.get(op.traceType());
	
}


 
}


