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

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.TimeAggregation3Manager;

public abstract class _3DMicroDescription extends _3DMatrixMicroDescription
		implements I3DMicroDescription {
	
	public _3DMicroDescription() {
		super();
	}

	public _3DMicroDescription(final OcelotlParameters parameters, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		super();
		try {
			setOcelotlParameters(parameters, monitor);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public TimeAggregation3Manager createManager(IProgressMonitor monitor) {
		return new TimeAggregation3Manager(this, monitor);

	}

	public void matrixPushType(final int incr, final EventProducer ep,
			final String key) {
		matrix.get(incr).get(ep).put(key, 0.0);
	}
	
}
