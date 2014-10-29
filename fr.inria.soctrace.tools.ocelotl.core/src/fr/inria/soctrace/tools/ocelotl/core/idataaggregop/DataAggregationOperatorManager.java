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

package fr.inria.soctrace.tools.ocelotl.core.idataaggregop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public class DataAggregationOperatorManager {

	HashMap<String, DataAggregationOperatorResource> operatorList;

	IDataAggregationOperator selectedOperator;
	String selectedOperatorName;
	ITraceTypeConfig selectedConfig;
	OcelotlParameters parameters;

	private static final String POINT_ID = "fr.inria.soctrace.tools.ocelotl.core.timeaggregator"; //$NON-NLS-1$
	private static final String OP_NAME = "operator"; //$NON-NLS-1$
	private static final String OP_CLASS = "class"; //$NON-NLS-1$
	private static final String OP_PARAM_WIN = "param_win"; //$NON-NLS-1$
	private static final String OP_VISUALIZATION_COMPATIBILITY = "visual_compatibility"; //$NON-NLS-1$
	private static final String OP_PARAM_CONFIG = "param_config"; //$NON-NLS-1$
	private static final String OP_TS = "ts_default_number"; //$NON-NLS-1$
	private static final String OP_SELECTION_PRIORITY = "selection_priority"; //$NON-NLS-1$
	private static final String OP_DIMENSION = "dimension"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory
			.getLogger(DataAggregationOperatorManager.class);

	public DataAggregationOperatorManager(final OcelotlParameters parameters) {
		super();
		this.parameters = parameters;
		try {
			init();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void activateSelectedOperator(IProgressMonitor monitor)
			throws OcelotlException {
		/*
		 * try { //selectedOperator.setOcelotlParameters(parameters, monitor); }
		 * catch (SoCTraceException | InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
	}

	public List<String> getOperators(final String traceType,
			final List<String> category) {
		logger.debug("Comparing Time Operator trace format with " + traceType);
		final List<String> op = new ArrayList<String>();
		for (final DataAggregationOperatorResource r : operatorList.values()) {
			op.add(r.getName());
		}
		// Sort in alphabetical order
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

	public ITraceTypeConfig getSelectedConfig() {
		return selectedConfig;
	}

	public IDataAggregationOperator getSelectedOperator() {
		return selectedOperator;
	}

	public DataAggregationOperatorResource getSelectedOperatorResource() {
		return operatorList.get(selectedOperatorName);
	}

	private void init() throws SoCTraceException {
		operatorList = new HashMap<String, DataAggregationOperatorResource>();

		final IExtensionRegistry reg = Platform.getExtensionRegistry();
		final IConfigurationElement[] config = reg
				.getConfigurationElementsFor(POINT_ID);
		logger.debug(config.length + " Time aggregation operators detected:");

		for (final IConfigurationElement e : config) {
			final DataAggregationOperatorResource resource = new DataAggregationOperatorResource();
			resource.setOperatorClass(e.getAttribute(OP_CLASS));
			resource.setName(e.getAttribute(OP_NAME));
			resource.setVisuCompatibility(e
					.getAttribute(OP_VISUALIZATION_COMPATIBILITY));
			resource.setParamWinClass(e.getAttribute(OP_PARAM_WIN));
			resource.setParamConfig(e.getAttribute(OP_PARAM_CONFIG));
			resource.setBundle(e.getContributor().getName());
			resource.setTs(e.getAttribute(OP_TS));
			resource.setDimension(e.getAttribute(OP_DIMENSION));
			resource.setSelectionPriority(e.getAttribute(OP_SELECTION_PRIORITY));
			operatorList.put(resource.getName(), resource);
		}
	}

	public void setSelectedOperator(final String name) {
		final Bundle mybundle = Platform.getBundle(operatorList.get(name)
				.getBundle());
		try {
			selectedOperator = (IDataAggregationOperator) mybundle.loadClass(
					operatorList.get(name).getOperatorClass()).newInstance();
			selectedOperatorName = name;
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			selectedConfig = (ITraceTypeConfig) mybundle.loadClass(
					operatorList.get(name).getParamConfig()).newInstance();
			parameters.setTraceTypeConfig(selectedConfig);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HashMap<String, DataAggregationOperatorResource> getOperatorList() {
		return operatorList;
	}

	public void setOperatorList(
			HashMap<String, DataAggregationOperatorResource> operatorList) {
		this.operatorList = operatorList;
	}

}
