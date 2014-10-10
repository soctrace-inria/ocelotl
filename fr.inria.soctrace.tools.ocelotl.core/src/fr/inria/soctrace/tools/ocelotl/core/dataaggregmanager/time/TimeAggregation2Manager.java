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

package fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.time;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import fr.inria.lpaggreg.time.JNITimeAggregation2;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.Microscopic2DDescription;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;

public class TimeAggregation2Manager extends TimeAggregationManager {

	Microscopic2DDescription matrix;

	public TimeAggregation2Manager(final MicroscopicDescription matrix, IProgressMonitor monitor) {
		super(matrix.getOcelotlParameters());
		this.matrix = (Microscopic2DDescription) matrix;
		reset(monitor);
	}


	@Override
	public void fillVectors(IProgressMonitor monitor) {
		for (int i = 0; i < matrix.getVectorNumber(); i++) {
			((JNITimeAggregation2) timeAggregation).addVector();
			for (final EventProducer key : matrix.getMatrix().get(i).keySet())
				((JNITimeAggregation2) timeAggregation).push_back(matrix
						.getMatrix().get(i).get(key));
		}

	}

	@Override
	public List<EventProducer> getEventProducers() {
		return new ArrayList<EventProducer>(matrix.getMatrix().get(0).keySet());
	}

	public Microscopic2DDescription getTimeSliceMatrix() {
		return matrix;
	}

	@Override
	public void reset(IProgressMonitor monitor) {
		timeAggregation = new JNITimeAggregation2();
		fillVectors(monitor);
	}

	@Override
	public IDataAggregManager copy() {
		TimeAggregation2Manager aNewManager = null;

		aNewManager = new TimeAggregation2Manager(matrix,  new NullProgressMonitor());
		//aNewManager.matrix = new _2DMicroDescription();//initVectors();
		/*for (int i = 0; i < matrix.getMatrix().size(); i++) {
			for (EventProducer ep : ocelotlParameters.getEventProducers()) {
				aNewManager.matrix.getMatrix().get(i).get(ep)
						.put(this.matrix.getMatrix().get(i));
			}
		}*/
		return aNewManager;
	}


}
