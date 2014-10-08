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

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.TimeAggregation3Manager;

public class _3DMicroDescription implements I3DMicroDescription {
	
	@Override
	public TimeAggregation3Manager createManager(MultiThreadTimeAggregationOperator microMod, IProgressMonitor monitor) {
		return new TimeAggregation3Manager(microMod, monitor);
	}

	@Override
	public OcelotlParameters getOcelotlParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVectorSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getVectorNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void print() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOcelotlParameters(OcelotlParameters parameters,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<HashMap<EventProducer, HashMap<String, Double>>> getMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

}
