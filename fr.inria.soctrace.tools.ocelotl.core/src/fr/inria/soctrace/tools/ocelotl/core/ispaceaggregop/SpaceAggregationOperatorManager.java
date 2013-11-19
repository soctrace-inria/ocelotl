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

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.generic.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.generic.spaceaggregop.NoAggregation;
import fr.inria.soctrace.tools.ocelotl.core.generic.spaceaggregop.StateDistribution;
import fr.inria.soctrace.tools.ocelotl.core.paje.config.PajeConfig;
import fr.inria.soctrace.tools.ocelotl.core.paje.timeaggregop.PajeNormalizedStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.timeaggregop.PajePushPopStateTypeSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.timeaggregop.PajeStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.timeaggregop.PajeStateTypeSum;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;

public class SpaceAggregationOperatorManager {

	HashMap<String, ISpaceAggregationOperator>	List;
	ArrayList<String>							Names;
	OcelotlCore									ocelotlCore;
	OcelotlParameters							parameters;

	public SpaceAggregationOperatorManager(final OcelotlCore ocelotlCore) {
		super();
		this.ocelotlCore=ocelotlCore;
		this.parameters = this.ocelotlCore.getOcelotlParameters();
		try {
			init();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Collection<ISpaceAggregationOperator> getList() {
		final List<ISpaceAggregationOperator> val = new ArrayList<ISpaceAggregationOperator>();
		val.addAll(List.values());
		Collections.sort(val, new Comparator<ISpaceAggregationOperator>() {

			@Override
			public int compare(final ISpaceAggregationOperator o1, final ISpaceAggregationOperator o2) {
				return o1.descriptor().compareTo(o2.descriptor());
			}

		});
		return val;
	}

	public ISpaceAggregationOperator getOperator(final String name) throws SoCTraceException {
		final ISpaceAggregationOperator op = List.get(name);
		op.setOcelotlCore(ocelotlCore);
		return op;
	}

	private void init() throws SoCTraceException {
		List = new HashMap<String, ISpaceAggregationOperator>();
		List.put(NoAggregation.descriptor, new NoAggregation());
		List.put(StateDistribution.descriptor, new StateDistribution());

	}

}
