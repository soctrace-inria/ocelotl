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

package fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public class SpaceAggregationOperatorManager {

	HashMap<String, SpaceAggregationOperatorResource>	List;
	ISpaceAggregationOperator							selectedOperator;
	String												selectedOperatorName;
	ITraceTypeConfig									selectedConfig;
	OcelotlParameters									parameters;
	OcelotlCore											ocelotlCore;
	
	
	private static final String							POINT_ID					= "fr.inria.soctrace.tools.ocelotl.core.spaceaggregopext";	//$NON-NLS-1$
	private static final String							OP_NAME						= "operator";												//$NON-NLS-1$
	private static final String							OP_CLASS					= "class";													//$NON-NLS-1$
	private static final String							OP_VISUALIZATION			= "visualization";											//$NON-NLS-1$
//	private static final String							OP_PARAM_WIN				= "param_win";												//$NON-NLS-1$
//	private static final String							OP_PARAM_CONFIG				= "param_config";											//$NON-NLS-1$
	private static final String							OP_TIME_COMPATIBILITY		= "time_compatibility";										//$NON-NLS-1$


	public SpaceAggregationOperatorManager(final OcelotlCore ocelotlCore) {
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
		selectedOperator.setOcelotlCore(ocelotlCore);
	}
	
	public List<String> getOperators(final List<String> compatibility) {
		System.out.println("Comparing Space Operator trace format with " + compatibility);
		final List<String> op = new ArrayList<String>();
		for (final SpaceAggregationOperatorResource r : List.values()) {
			System.out.println(r.getTimeCompatibility());
			for (String comp : compatibility)
				if (r.getTimeCompatibility().contains(comp)){
					op.add(r.getName());
					break;
				}
		}
		Collections.sort(op, new Comparator<String>() {

			@Override
			public int compare(final String arg0, final String arg1) {
				return arg0.compareTo(arg1);
			}

		});
		return op;
	}
	
	public ISpaceAggregationOperator getSelectedOperator() {
		return selectedOperator;
	}
	
	public SpaceAggregationOperatorResource getSelectedOperatorResource() {
		return List.get(selectedOperatorName);
	}

//	private void init() throws SoCTraceException {
//		List = new HashMap<String, ISpaceAggregationOperator>();
//		List.put(NoAggregation.descriptor, new NoAggregation());
//		List.put(StateDistribution.descriptor, new StateDistribution());
//
//	}

	private void init() throws SoCTraceException {
		List = new HashMap<String, SpaceAggregationOperatorResource>();

		final IExtensionRegistry reg = Platform.getExtensionRegistry();
		final IConfigurationElement[] config = reg.getConfigurationElementsFor(POINT_ID);
		System.out.println(config.length+ " Space aggregation operators detected:");

		for (final IConfigurationElement e : config) {
			final SpaceAggregationOperatorResource resource = new SpaceAggregationOperatorResource();
			resource.setOperatorClass(e.getAttribute(OP_CLASS));
			resource.setName(e.getAttribute(OP_NAME));
			resource.setTimeCompatibility(e.getAttribute(OP_TIME_COMPATIBILITY));
//			resource.setParamWinClass(e.getAttribute(OP_PARAM_WIN));
//			resource.setParamConfig(e.getAttribute(OP_PARAM_CONFIG));
			resource.setVisualization(e.getAttribute(OP_VISUALIZATION));
			resource.setBundle(e.getContributor().getName());
			List.put(resource.getName(), resource);
			System.out.println("    "+ resource.getName() + " "+resource.getTimeCompatibility());
		}
	}
	
	public void setSelectedOperator(final String name) {
		final Bundle mybundle = Platform.getBundle(List.get(name).getBundle());
		try {

			selectedOperator = (ISpaceAggregationOperator) mybundle.loadClass(List.get(name).getOperatorClass()).newInstance();
			selectedOperatorName = name;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}
