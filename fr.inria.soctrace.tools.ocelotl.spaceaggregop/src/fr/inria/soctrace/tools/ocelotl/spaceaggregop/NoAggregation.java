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

package fr.inria.soctrace.tools.ocelotl.spaceaggregop;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceAggregationOperator;

public class NoAggregation extends SpaceAggregationOperator {

	final public static String	descriptor	= "No Aggregation";

	public NoAggregation() {
		super();
	}

	public NoAggregation(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
	}

	@Override
	protected void computeParts() {
		initParts();
	}

	@Override
	public String descriptor() {
		return descriptor;
	}

}
