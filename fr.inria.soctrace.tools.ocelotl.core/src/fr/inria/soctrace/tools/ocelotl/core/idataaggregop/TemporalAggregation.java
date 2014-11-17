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

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.time.TimeAggregation3Manager;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;

public class TemporalAggregation implements IDataAggregationOperator {
	
	@Override
	public TimeAggregation3Manager createManager(
			MicroscopicDescription microMod, IProgressMonitor monitor) {
		return new TimeAggregation3Manager(microMod, monitor);
	}
}
