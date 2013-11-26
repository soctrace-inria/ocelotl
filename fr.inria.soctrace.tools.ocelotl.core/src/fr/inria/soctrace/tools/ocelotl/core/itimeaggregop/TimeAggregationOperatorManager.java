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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public class TimeAggregationOperatorManager {

	HashMap<String, TimeAggregationOperatorResource>	List;
	ITimeAggregationOperator							selectedOperator;
	String												selectedOperatorName;
	ITraceTypeConfig									selectedConfig;
	OcelotlParameters									parameters;

	private static final String							POINT_ID					= "fr.inria.soctrace.tools.ocelotl.core.timeaggregopext";	//$NON-NLS-1$
	private static final String							OP_NAME						= "operator";												//$NON-NLS-1$
	private static final String							OP_CLASS					= "class";													//$NON-NLS-1$
	private static final String							OP_TRACE_FORMATS			= "trace_formats";											//$NON-NLS-1$
	private static final String							OP_PARAM_WIN				= "param_win";												//$NON-NLS-1$
	private static final String							OP_SPATIAL_COMPATIBILITY	= "spatial_compatibility";									//$NON-NLS-1$
	private static final String							OP_PARAM_CONFIG				= "param_config";											//$NON-NLS-1$
	private static final String							OP_GENERIC					= "generic";												//$NON-NLS-1$

	public TimeAggregationOperatorManager(final OcelotlParameters parameters) {
		super();
		this.parameters = parameters;
		try {
			init();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void activateSelectedOperator() {
		try {
			selectedOperator.setOcelotlParameters(parameters);
		} catch (SoCTraceException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<String> getOperators(final String traceType) {
		System.out.println("Comparing Operator trace format with " + traceType);
		final List<String> op = new ArrayList<String>();
		for (final TimeAggregationOperatorResource r : List.values()) {
			System.out.println(r.getTraceFormats());
			if (r.isGeneric())
				op.add(r.getName());
			else if (r.getTraceFormats().contains(traceType))
				op.add(r.getName());
		}
		Collections.sort(op, new Comparator<String>() {

			@Override
			public int compare(final String arg0, final String arg1) {
				return arg0.compareTo(arg1);
			}

		});
		return op;
	}

	public ITraceTypeConfig getSelectedConfig() {
		return selectedConfig;
	}

	public ITimeAggregationOperator getSelectedOperator() {
		return selectedOperator;
	}

	public TimeAggregationOperatorResource getSelectedOperatorResource() {
		return List.get(selectedOperatorName);
	}

	private void init() throws SoCTraceException {
		List = new HashMap<String, TimeAggregationOperatorResource>();

		final IExtensionRegistry reg = Platform.getExtensionRegistry();
		final IConfigurationElement[] config = reg.getConfigurationElementsFor(POINT_ID);
		System.out.println(config.length+ " Time aggregation operators detected:");

		for (final IConfigurationElement e : config) {
			final TimeAggregationOperatorResource resource = new TimeAggregationOperatorResource();
			resource.setOperatorClass(e.getAttribute(OP_CLASS));
			resource.setName(e.getAttribute(OP_NAME));
			resource.setGeneric(e.getAttribute(OP_GENERIC).contains("true"));
			resource.setTraceFormats(e.getAttribute(OP_TRACE_FORMATS));
			resource.setSpaceCompatibility(e.getAttribute(OP_SPATIAL_COMPATIBILITY));
			resource.setParamWinClass(e.getAttribute(OP_PARAM_WIN));
			resource.setParamConfig(e.getAttribute(OP_PARAM_CONFIG));
			resource.setBundle(e.getContributor().getName());
			List.put(resource.getName(), resource);
			System.out.println("    "+ resource.getName() + " "+resource.getTraceFormats());
		}
	}

	public void setSelectedOperator(final String name) {
		final Bundle mybundle = Platform.getBundle(List.get(name).getBundle());
		try {

			selectedOperator = (ITimeAggregationOperator) mybundle.loadClass(List.get(name).getOperatorClass()).newInstance();
			selectedOperatorName = name;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			selectedConfig = (ITraceTypeConfig) mybundle.loadClass(List.get(name).getParamConfig()).newInstance();
			parameters.setTraceTypeConfig(selectedConfig);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
