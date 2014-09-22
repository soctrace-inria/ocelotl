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

package fr.inria.soctrace.tools.ocelotl.visualizations.parts;

import java.util.ArrayList;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.ISpaceAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceTAggregationOperator;

public class Parts extends SpaceTAggregationOperator {

	public Parts() {
		super();
	}

	public Parts(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
	}

	@Override
	protected void computeParts() {
		initParts();
	}

	@Override
	public ISpaceAggregationOperator copy() {
		Parts newParts = new Parts();
		newParts.ocelotlCore = ocelotlCore;
		newParts.timeSliceNumber = timeSliceNumber;
		newParts.timeSliceDuration = timeSliceDuration;
		newParts.lpaggregManager = lpaggregManager;

		newParts.parts = new ArrayList<Part>();
		int i;
		for (i = 0; i < parts.size(); i++)
			newParts.parts.add(parts.get(i));

		return newParts;
	}

	
}
