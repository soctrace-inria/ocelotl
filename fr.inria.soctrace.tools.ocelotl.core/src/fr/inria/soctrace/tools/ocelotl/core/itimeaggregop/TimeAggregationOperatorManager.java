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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IAggregateWorkingSet;

import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.generic.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.paje.config.PajeConfig;
import fr.inria.soctrace.tools.ocelotl.core.paje.timeaggregop.PajeNormalizedStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.timeaggregop.PajePushPopStateTypeSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.timeaggregop.PajeStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.timeaggregop.PajeStateTypeSum;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;

public class TimeAggregationOperatorManager {

	HashMap<String, ITimeAggregationOperator>	List;
	HashMap<String, ITraceTypeConfig>			Config;
	ArrayList<String>							Names;
	OcelotlParameters							parameters;
	
	private static final String POINT_ID = "timeaggregopext"; //$NON-NLS-1$
	private static final String OP_NAME = "name"; //$NON-NLS-1$
	private static final String OP_TYPE = "class"; //$NON-NLS-1$
	private static final String EP_TOOL_DOC = "doc"; //$NON-NLS-1$
	private static final String EP_TOOL_CLASS = "class"; //$NON-NLS-1$

	public TimeAggregationOperatorManager(final OcelotlParameters parameters) {
		super();
		this.parameters = parameters;
		try {
			init();
			initConfig();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ITraceTypeConfig config(final ITimeAggregationOperator op) {
		return Config.get(op.traceType());
	}

	public ITraceTypeConfig config(final String op) {
		return Config.get(List.get(op).traceType());
	}

	public Collection<ITimeAggregationOperator> getList() {
		final List<ITimeAggregationOperator> val = new ArrayList<ITimeAggregationOperator>();
		val.addAll(List.values());
		Collections.sort(val, new Comparator<ITimeAggregationOperator>() {

			@Override
			public int compare(final ITimeAggregationOperator o1, final ITimeAggregationOperator o2) {
				return o1.descriptor().compareTo(o2.descriptor());
			}

		});
		return val;
	}

	public ITimeAggregationOperator getOperator(final String name) throws SoCTraceException {
		final ITimeAggregationOperator op = List.get(name);
		try {
			op.setOcelotlParameters(parameters);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return op;
	}

	public String getType(final String op) {
		return List.get(op).traceType();
	}

	private void init() throws SoCTraceException {
		List = new HashMap<String, ITimeAggregationOperator>();
		
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] config = reg.getConfigurationElementsFor(POINT_ID);

		for (IConfigurationElement e : config) {
			ITimeAggregationOperator operator=(ITimeAggregationOperator) Class.forName(e.getAttribute("class")).newInstance();
			Tool tmp = new Tool(reverseIdManager.getNextId());
			tmp.setPlugin(true);
			tmp.setCommand("plugin:"+e.getNamespaceIdentifier());
			tmp.setName(e.getAttribute(EP_TOOL_NAME));
			tmp.setType(e.getAttribute(EP_TOOL_TYPE));
			if (e.getAttribute(EP_TOOL_DOC)!=null)
				tmp.setDoc(e.getAttribute(EP_TOOL_DOC));
			tools.add(tmp);
		}
		
		return tools;

	}

	private void initConfig() throws SoCTraceException {
		Config = new HashMap<String, ITraceTypeConfig>();
		Config.put(PajeConstants.PajeFormatName, new PajeConfig());
	}

}
