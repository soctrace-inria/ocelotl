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

package fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts;

import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.AggregatedData;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceTAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;

public class MatrixStateProportion extends SpaceAggregationOperator {

	final public static String	descriptor	= "Matrix Parts";
	
	public MatrixStateProportion() {
		super();
	}

	public MatrixStateProportion(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
	}

	@Override
	protected void computeParts() {
	}
	

	

	@Override
	public String descriptor() {
		return descriptor;
	}

}
