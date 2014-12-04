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

package fr.inria.soctrace.tools.ocelotl.core.ivisuop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.config.ISpaceConfig;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public class VisuOperatorManager {

	private static final Logger logger = LoggerFactory
			.getLogger(VisuOperatorManager.class);
	
	HashMap<String, VisuOperatorResource> operatorList;
	IVisuOperator selectedOperator;
	String selectedOperatorName;
	ISpaceConfig selectedConfig;
	OcelotlParameters parameters;
	OcelotlCore ocelotlCore;

	private static final String POINT_ID = "fr.inria.soctrace.tools.ocelotl.core.visualization"; //$NON-NLS-1$
	private static final String OP_NAME = "operator"; //$NON-NLS-1$
	private static final String OP_CLASS = "class"; //$NON-NLS-1$
	private static final String OP_VISUALIZATION = "visualization"; //$NON-NLS-1$
	private static final String OP_PARAM_WIN = "param_win"; //$NON-NLS-1$
	private static final String OP_PARAM_CONFIG = "param_config";//$NON-NLS-1$							
	private static final String OP_METRIC_COMPATIBILITY = "metric_compatibility"; //$NON-NLS-1$
	private static final String OP_SELECTION_PRIORITY = "selection_priority"; //$NON-NLS-1$
	private static final String OP_AGGREGATOR_COMPATIBILITY = "aggregator_compatibility"; //$NON-NLS-1$
	private static final String OP_OVERVIEW_VISUALIZATION = "overview_visualization"; //$NON-NLS-1$
	private static final String OP_Y_AXIS_VIEW = "unit_axis_view"; //$NON-NLS-1$
	
	public VisuOperatorManager(final OcelotlCore ocelotlCore) {
		super();
		this.ocelotlCore = ocelotlCore;
		parameters = this.ocelotlCore.getOcelotlParameters();
		try {
			init();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void activateSelectedOperator() {
		final Bundle mybundle = Platform.getBundle(operatorList.get(
				selectedOperatorName).getBundle());
		try {
			selectedOperator = (IVisuOperator) mybundle.loadClass(
					operatorList.get(selectedOperatorName).getOperatorClass())
					.newInstance();

		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		selectedOperator.setOcelotlCore(ocelotlCore);
	}

	/**
	 * Instantiate the visualization operator
	 * 
	 * @param name
	 *            Name of the operator to instantiate
	 * @return the instantiated operator
	 */
	public IVisuOperator instantiateOperator(String name) {
		IVisuOperator instantiatedOperator = null;

		final Bundle mybundle = Platform.getBundle(operatorList.get(
				selectedOperatorName).getBundle());
		try {
			instantiatedOperator = (IVisuOperator) mybundle.loadClass(
					operatorList.get(name).getOperatorClass()).newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return instantiatedOperator;
	}
	public List<String> getOperators(final List<String> metricCompatibility, final List<String> aggregCompatibility) {
		logger.debug("Comparing Visualization Operator trace format with "
				+ metricCompatibility);
		final List<String> op = new ArrayList<String>();
		for (final VisuOperatorResource resource : operatorList.values()) {
			StringBuffer buff = new StringBuffer();
			buff.append(resource.getTimeCompatibility());
			logger.debug(buff.toString());
			// Check metric compatibility
			for (final String metricComp : metricCompatibility)
				for (final String ownTimeComp : resource.getTimeCompatibility())
					if (ownTimeComp.equals(metricComp))
						// Check aggregation compatiblity
						for (final String aggComp : aggregCompatibility)
							for (String ownAggComp : resource
									.getAggregatorCompatibility())
								if (ownAggComp.equals(aggComp))
									if (!op.contains(resource.getName()))
										op.add(resource.getName());

		}
		// Sort according to the selection priority level
		Collections.sort(op, new Comparator<String>() {

			@Override
			public int compare(final String arg0, final String arg1) {
				int diff = operatorList.get(arg0).getSelectionPriority()
						- operatorList.get(arg1).getSelectionPriority();

				// If the two operators have the same priority
				if (diff == 0) {
					// Sort them alphabetically
					return arg0.compareTo(arg1);
				}

				return diff;
			}
		});
		return op;
	}

	public IVisuOperator getSelectedOperator() {
		return selectedOperator;
	}

	public VisuOperatorResource getSelectedOperatorResource() {
		return operatorList.get(selectedOperatorName);
	}

	public VisuOperatorResource getOperatorResource(String anOperatorName) {
		return operatorList.get(anOperatorName);
	}

	// private void init() throws SoCTraceException {
	// List = new HashMap<String, ISpaceAggregationOperator>();
	// List.put(NoAggregation.descriptor, new NoAggregation());
	// List.put(StateDistribution.descriptor, new StateDistribution());
	//
	// }

	private void init() throws SoCTraceException {
		operatorList = new HashMap<String, VisuOperatorResource>();

		final IExtensionRegistry reg = Platform.getExtensionRegistry();
		final IConfigurationElement[] config = reg
				.getConfigurationElementsFor(POINT_ID);
		logger.debug(config.length + " Visualizations detected:");

		for (final IConfigurationElement e : config) {
			final VisuOperatorResource resource = new VisuOperatorResource();
			resource.setOperatorClass(e.getAttribute(OP_CLASS));
			resource.setName(e.getAttribute(OP_NAME));
			resource.setTimeCompatibility(e.getAttribute(OP_METRIC_COMPATIBILITY));
			resource.setAggregatorCompatibility(e.getAttribute(OP_AGGREGATOR_COMPATIBILITY));
			resource.setParamWinClass(e.getAttribute(OP_PARAM_WIN));
			resource.setParamConfig(e.getAttribute(OP_PARAM_CONFIG));
			resource.setVisualization(e.getAttribute(OP_VISUALIZATION));
			resource.setOverviewVisualization(e.getAttribute(OP_OVERVIEW_VISUALIZATION));
			resource.setYAxisView(e.getAttribute(OP_Y_AXIS_VIEW));
			resource.setSelectionPriority(Integer.parseInt(e
					.getAttribute(OP_SELECTION_PRIORITY)));
			resource.setBundle(e.getContributor().getName());
			operatorList.put(resource.getName(), resource);
			logger.debug("    " + resource.getName() + " "
					+ resource.getTimeCompatibility() + " " + resource.getAggregatorCompatibility());
		}
	}

	public void setSelectedOperator(final String name) {
		selectedOperatorName = name;
		final Bundle mybundle = Platform.getBundle(operatorList.get(
				selectedOperatorName).getBundle());

		String paramClassName = operatorList.get(selectedOperatorName)
				.getParamConfig();

		// Check if a configuration class was provided
		if (paramClassName.isEmpty())
			return;

		try {
			selectedConfig = (ISpaceConfig) mybundle.loadClass(paramClassName)
					.newInstance();
			parameters.setSpaceConfig(selectedConfig);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | NullPointerException e) {
		}
	}

	public HashMap<String, VisuOperatorResource> getOperatorList() {
		return operatorList;
	}

}
