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

package fr.inria.soctrace.tools.ocelotl.core;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.time.PartManager;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.idataaggregop.IDataAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.idataaggregop.DataAggregationOperatorManager;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.VisuOperatorManager;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescriptionTypeManager;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.statistics.IStatisticOperator;
import fr.inria.soctrace.tools.ocelotl.core.statistics.StatisticOperatorManager;

public class OcelotlCore {

	private static final boolean DEBUG = true;
	private static final boolean TEST = true;

	public static boolean isDebug() {
		return DEBUG;
	}

	public static boolean isTest() {
		return TEST;
	}

	OcelotlParameters ocelotlParameters;
	IDataAggregManager lpaggregManager;
	PartManager partManager;
	MicroscopicDescriptionTypeManager microModelTypeManager;
	MicroscopicDescription microModel;
	DataAggregationOperatorManager aggregOperators;
	IDataAggregationOperator aggregOperator;
	VisuOperatorManager visuOperators;
	IVisuOperator visuOperator;
	StatisticOperatorManager statOperators;
	IStatisticOperator statOperator;

	public OcelotlCore() {
		super();
	}

	public OcelotlCore(final OcelotlParameters ocelotlParameters)
			throws SoCTraceException {
		super();
		init(ocelotlParameters);
	}

	public void initAggregOperator(IProgressMonitor monitor)
			throws OcelotlException {
		setMicroModel(monitor);
		setAggregOperator(monitor);
		if (monitor.isCanceled())
			return;
		try {
			// Build microscopic description
			microModel.setOcelotlParameters(ocelotlParameters, monitor);
			
			if (monitor.isCanceled())
				return;
			
			// Instantiate the aggregation operator
			lpaggregManager = aggregOperator.createManager(microModel, monitor);
		} catch (UnsatisfiedLinkError e) {
			throw new OcelotlException(OcelotlException.JNI);
		} catch (SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void computeQualities() {
		lpaggregManager.computeQualities();
	}

	public void computeDichotomy() throws OcelotlException {
		lpaggregManager.computeDichotomy();
		lpaggregManager.printParameters();
	}

	public void computeParts() {
		lpaggregManager.computeParts(ocelotlParameters.getParameter());
		// lpaggregManager.printParts();
		setVisuOperator();
		lpaggregManager.print(this);
	}
	
	/**
	 * Instantiate the managers for the elements of the core (Microscopic
	 * description, data aggregation and visualization operator)
	 * 
	 * @param ocelotlParameters
	 *            the parameters
	 * @throws SoCTraceException
	 */
	public void init(final OcelotlParameters ocelotlParameters)
			throws SoCTraceException {
		setOcelotlParameters(ocelotlParameters);
		microModelTypeManager = new MicroscopicDescriptionTypeManager();
		aggregOperators = new DataAggregationOperatorManager(ocelotlParameters);
		visuOperators = new VisuOperatorManager(this);
		statOperators = new StatisticOperatorManager(this);
	}

	public IDataAggregManager getLpaggregManager() {
		return lpaggregManager;
	}

	public OcelotlParameters getOcelotlParameters() {
		return ocelotlParameters;
	}

	public PartManager getPartManager() {
		return partManager;
	}

	public MicroscopicDescription getMicroModel() {
		return microModel;
	}

	public void setMicroModel(MicroscopicDescription microModel) {
		this.microModel = microModel;
	}

	public IVisuOperator getVisuOperator() {
		return visuOperator;
	}

	public VisuOperatorManager getVisuOperators() {
		return visuOperators;
	}

	public StatisticOperatorManager getStatOperators() {
		return statOperators;
	}

	public IStatisticOperator getStatOperator() {
		return statOperator;
	}

	public IDataAggregationOperator getAggregOperator() {
		return aggregOperator;
	}

	public DataAggregationOperatorManager getAggregOperators() {
		return aggregOperators;
	}

	public MicroscopicDescriptionTypeManager getMicromodelTypes() {
		return microModelTypeManager;
	}

	public void setOcelotlParameters(final OcelotlParameters ocelotlParameters) {
		this.ocelotlParameters = ocelotlParameters;
	}

	public void setVisuOperator() {
		visuOperators.activateSelectedOperator();
		visuOperator = visuOperators.getSelectedOperator();
	}
	
	public void setStatOperator() {
		statOperators.activateSelectedOperator();
		statOperator = statOperators.getSelectedOperator();
	}

	public void setAggregOperator(IProgressMonitor monitor)
			throws OcelotlException {
		aggregOperators.activateSelectedOperator(monitor);
		aggregOperator = aggregOperators.getSelectedOperator();
	}

	public void setMicroModel(IProgressMonitor monitor) throws OcelotlException {
		microModelTypeManager.activateSelectedMicroModel(ocelotlParameters);
		microModel = microModelTypeManager.getSelectedMicroModel();
	}
}
