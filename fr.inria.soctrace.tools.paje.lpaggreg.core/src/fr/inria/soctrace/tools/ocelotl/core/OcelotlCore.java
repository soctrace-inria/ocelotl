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

	OcelotlParameters	ocelotlParameters;
	Query				query;
	ILPAggregManager	lpaggregManager;
	PartManager			partManager;

	public OcelotlCore() {
		super();
	}

	public OcelotlCore(final OcelotlParameters ocelotlParameters) throws SoCTraceException {
		super();
		init(ocelotlParameters);
	}

	public void compute(final HasChanged hasChanged) throws SoCTraceException {
		if (hasChanged == HasChanged.ALL)
			if (ocelotlParameters.getAggOperator().equals(AggregationOperators.ActivityTime))
				lpaggregManager = new VLPAggregManager(new ActivityTimeMatrix(query));
			else if (ocelotlParameters.getAggOperator().equals(AggregationOperators.ActivityTimeProbabilityDistribution))
				lpaggregManager = new VLPAggregManager(new ActivityTimeProbabilityDistributionMatrix(query));
			else if (ocelotlParameters.getAggOperator().equals(AggregationOperators.ActivityTimeByStateType))
				lpaggregManager = new MLPAggregManager(new ActivityTimeCubicMatrix(query));
			else
				lpaggregManager = new VLPAggregManager(new ActivityTimeMatrix(query)); // default
		// vectors.print();
		if (hasChanged == HasChanged.ALL || hasChanged == HasChanged.NORMALIZE)
			lpaggregManager.computeQualities();
		// TODO clean dicho & parts

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
