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

package fr.inria.soctrace.tools.ocelotl.core.itimeaggregop;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.tools.ocelotl.core.micromodel.MicroscopicModel;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.TimeAggregation3Manager;

public class TemporalAggregation implements IAggregationOperator {

	@Override
	public TimeAggregation3Manager createManager(MicroscopicModel microMod, IProgressMonitor monitor) {
		return new TimeAggregation3Manager(microMod, monitor);
	}

}
