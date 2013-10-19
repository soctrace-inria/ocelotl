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

package fr.inria.soctrace.tools.ocelotl.core;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlConstants.HasChanged;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop.AggregationOperators;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop.IAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.ILPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.MLPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.PartManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.VLPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeNormalizedStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeStateTypeSum;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;

public class OcelotlCore {

	private static final boolean	DEBUG	= true;
	private static final boolean	TEST	= true;

	public static boolean isDebug() {
		return DEBUG;
	}

	public static boolean isTest() {
		return TEST;
	}

	OcelotlParameters	ocelotlParameters;
	Query				query;
	ILPAggregManager	lpaggregManager;
	PartManager			partManager;
	AggregationOperators operators;
	IAggregationOperator operator;

	public OcelotlCore() {
		super();
	}
	
	public void setOperator(){
		try {
			operator = operators.getOperator(ocelotlParameters.getAggOperator());
		} catch (SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public OcelotlCore(final OcelotlParameters ocelotlParameters) throws SoCTraceException{
		super();
		init(ocelotlParameters);
		operators = new AggregationOperators(query);
	}

	public void compute(final HasChanged hasChanged) throws SoCTraceException {
		if (hasChanged == HasChanged.ALL){
			setOperator();
			lpaggregManager = operator.createManager();
		}
		// vectors.print();
		if (hasChanged == HasChanged.ALL || hasChanged == HasChanged.NORMALIZE)
			lpaggregManager.computeQualities();
		// TODO clean dicho & parts

	}

	public AggregationOperators getOperators() {
		return operators;
	}

	public void computeDichotomy(final HasChanged hasChanged) throws SoCTraceException {
		compute(hasChanged);
		if (hasChanged == HasChanged.ALL || hasChanged == HasChanged.NORMALIZE || hasChanged == HasChanged.THRESHOLD) {
			lpaggregManager.computeDichotomy();
			lpaggregManager.printParameters();
		}

	}

	public void computeParts(final HasChanged hasChanged) throws SoCTraceException {
		compute(hasChanged);
		if (hasChanged == HasChanged.ALL || hasChanged == HasChanged.NORMALIZE || hasChanged == HasChanged.PARAMETER) {
			lpaggregManager.computeParts();
			lpaggregManager.printParts();
			partManager = new PartManager(this);
			partManager.print();
		}

	}

	public ILPAggregManager getLpaggregManager() {
		return lpaggregManager;
	}

	public OcelotlParameters getOcelotlParameters() {
		return ocelotlParameters;
	}

	public PartManager getPartsManager() {
		return partManager;
	}

	public Query getQueries() {
		return query;
	}

	public void init(final OcelotlParameters ocelotlParameters) throws SoCTraceException {
		setOcelotlParameters(ocelotlParameters);
		query = new Query(ocelotlParameters);
	}

	public void setOcelotlParameters(final OcelotlParameters ocelotlParameters) {
		this.ocelotlParameters = ocelotlParameters;
	}

}
