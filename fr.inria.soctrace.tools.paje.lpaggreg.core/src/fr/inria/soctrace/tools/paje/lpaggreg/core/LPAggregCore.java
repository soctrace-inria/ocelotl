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

package fr.inria.soctrace.tools.paje.lpaggreg.core;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.paje.lpaggreg.core.LPAggregConstants.HasChanged;
import fr.inria.soctrace.tools.paje.lpaggreg.core.tsaggregoperators.ActivityTimeCubicMatrix;
import fr.inria.soctrace.tools.paje.lpaggreg.core.tsaggregoperators.ActivityTimeMatrix;
import fr.inria.soctrace.tools.paje.lpaggreg.core.tsaggregoperators.ActivityTimeProbabilityDistributionMatrix;
import fr.inria.soctrace.tools.paje.lpaggreg.core.tsaggregoperators.AggregationOperators;
import fr.inria.soctrace.tools.paje.lpaggreg.core.tsaggregoperators.ITimeSliceMatrix;

public class LPAggregCore {

	private static final boolean	DEBUG	= true;
	private static final boolean	TEST	= true;

	public static boolean isDebug() {
		return DEBUG;
	}

	public static boolean isTest() {
		return TEST;
	}

	LPAggregParameters	lpaggregParameters;
	Queries				queries;
	ILPAggregManager	lpaggregManager;
	PartManager		partManager;

	public LPAggregCore() {
		super();
	}

	public LPAggregCore(LPAggregParameters lpaggregParameters) throws SoCTraceException {
		super();
		init(lpaggregParameters);
	}

	public void compute(HasChanged hasChanged) throws SoCTraceException {
		if (hasChanged == HasChanged.ALL){
			if (lpaggregParameters.getAggOperator().equals(AggregationOperators.ActivityTime))
				lpaggregManager = new VLPAggregManager(new ActivityTimeMatrix(queries));
			else if (lpaggregParameters.getAggOperator().equals(AggregationOperators.ActivityTimeProbabilityDistribution))
				lpaggregManager = new VLPAggregManager(new ActivityTimeProbabilityDistributionMatrix(queries));
			else if (lpaggregParameters.getAggOperator().equals(AggregationOperators.ActivityTimeByStateType))
				lpaggregManager = new MLPAggregManager(new ActivityTimeCubicMatrix(queries));
			else
				lpaggregManager = new VLPAggregManager(new ActivityTimeMatrix(queries)); //default
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

	public LPAggregParameters getLpaggregParameters() {
		return lpaggregParameters;
	}

	public PartManager getPartsManager() {
		return partManager;
	}

	public Queries getQueries() {
		return queries;
	}

//	public ITimeSliceMatrix getMatrix() {
//		return lpaggregManager.get;
//	}

	public void init(LPAggregParameters lpaggregParameters) throws SoCTraceException {
		setLpaggregParameters(lpaggregParameters);
		queries = new Queries(lpaggregParameters);
	}

	public void setLpaggregParameters(LPAggregParameters lpaggregParameters) {
		this.lpaggregParameters = lpaggregParameters;
	}

}
