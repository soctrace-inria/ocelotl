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

package fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.AggregatedData;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceSTAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceTAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;

public class MatrixParts extends SpaceSTAggregationOperator {

	public MatrixParts() {
		super();
	}

	public MatrixParts(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
	}

	@Override
	protected void computeParts() {
	}


	@Override
	protected void initParts() {
		// TODO Auto-generated method stub
		
	}

}
