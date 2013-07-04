/* ===========================================================
 * LPAggreg core module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to visualize a Paje
 * trace across an aggregated representation.
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
 */

package fr.inria.soctrace.tools.ocelotl.core;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlConstants.HasChanged;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.ILPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.MLPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.PartManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.VLPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators.ActivityTimeCubicMatrix;
import fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators.ActivityTimeMatrix;
import fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators.ActivityTimeProbabilityDistributionMatrix;
import fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators.AggregationOperators;

public class OcelotlCore {

	private static final boolean	DEBUG	= true;
	private static final boolean	TEST	= true;

	public static boolean isDebug() {
		return DEBUG;
	}

	public static boolean isTest() {
		return TEST;
	}

	OcelotlParameters	lpaggregParameters;
	Query				query;
	ILPAggregManager	lpaggregManager;
	PartManager		partManager;

	public OcelotlCore() {
		super();
	}

	public OcelotlCore(OcelotlParameters lpaggregParameters) throws SoCTraceException {
		super();
		init(lpaggregParameters);
	}

	public void compute(HasChanged hasChanged) throws SoCTraceException {
		if (hasChanged == HasChanged.ALL){
			if (lpaggregParameters.getAggOperator().equals(AggregationOperators.ActivityTime))
				lpaggregManager = new VLPAggregManager(new ActivityTimeMatrix(query));
			else if (lpaggregParameters.getAggOperator().equals(AggregationOperators.ActivityTimeProbabilityDistribution))
				lpaggregManager = new VLPAggregManager(new ActivityTimeProbabilityDistributionMatrix(query));
			else if (lpaggregParameters.getAggOperator().equals(AggregationOperators.ActivityTimeByStateType))
				lpaggregManager = new MLPAggregManager(new ActivityTimeCubicMatrix(query));
			else
				lpaggregManager = new VLPAggregManager(new ActivityTimeMatrix(query)); //default
			//vectors.print();
		}
		if (hasChanged == HasChanged.ALL || hasChanged == HasChanged.NORMALIZE)
			lpaggregManager.computeQualities();
		//TODO clean dicho & parts

	}

	public void computeDichotomy(HasChanged hasChanged) throws SoCTraceException {
		compute(hasChanged);
		if (hasChanged == HasChanged.ALL || hasChanged == HasChanged.NORMALIZE || hasChanged == HasChanged.THRESHOLD) {
			lpaggregManager.computeDichotomy();
			lpaggregManager.printParameters();
		}

	}

//	public void computeEqParts(HasChanged hasChanged) throws SoCTraceException {
//		compute(hasChanged);
//		if (hasChanged == HasChanged.ALL || hasChanged == HasChanged.NORMALIZE || hasChanged == HasChanged.PARAMETER || hasChanged == HasChanged.EQ) {
//			lpaggregManager.computeParts();
//			lpaggregManager.printParts();
//			//partManager = new PartManager(lpaggregManager);
//		}
//		lpaggregManager.computeEqParts();
//		lpaggregManager.printParts();
//	}

	public void computeParts(HasChanged hasChanged) throws SoCTraceException {
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

	public OcelotlParameters getLpaggregParameters() {
		return lpaggregParameters;
	}

	public PartManager getPartsManager() {
		return partManager;
	}

	public Query getQueries() {
		return query;
	}

//	public ITimeSliceMatrix getMatrix() {
//		return lpaggregManager.get;
//	}

	public void init(OcelotlParameters lpaggregParameters) throws SoCTraceException {
		setLpaggregParameters(lpaggregParameters);
		query = new Query(lpaggregParameters);
	}

	public void setLpaggregParameters(OcelotlParameters lpaggregParameters) {
		this.lpaggregParameters = lpaggregParameters;
	}

}
