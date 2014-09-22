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

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import fr.inria.lpaggreg.time.JNITimeAggregation3;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.I3DMicroDescription;

public class TimeAggregation3Manager extends TimeAggregationManager {

	I3DMicroDescription matrix;
	List<List<List<Double>>> values;

	public TimeAggregation3Manager(final I3DMicroDescription timeSliceMatrix, IProgressMonitor monitor) {
		super(timeSliceMatrix.getOcelotlParameters());
		this.matrix = timeSliceMatrix;
		reset(monitor);
	}

	@Override
	protected void fillVectors(IProgressMonitor monitor) {
		for (int i = 0; i < matrix.getMatrix().size(); i++) {
			((JNITimeAggregation3) timeAggregation).addMatrix();
			if (monitor.isCanceled()) {
				return;
			}
			for (final EventProducer key : matrix.getMatrix().get(i).keySet()) {
				((JNITimeAggregation3) timeAggregation).addVector();
				for (final String key2 : matrix.getMatrix().get(i).get(key)
						.keySet())
					((JNITimeAggregation3) timeAggregation).push_back(matrix
							.getMatrix().get(i).get(key).get(key2));
			}
		}
	}

	@Override
	public List<EventProducer> getEventProducers() {
		return new ArrayList<EventProducer>(matrix.getMatrix().get(0).keySet());
	}

	public List<String> getKeys() {
		return new ArrayList<String>(matrix.getMatrix().get(0)
				.get(getEventProducers().get(0)).keySet());
	}

	public I3DMicroDescription getTimeSliceMatrix() {
		return matrix;
	}

	@Override
	public void reset(IProgressMonitor monitor) {
		timeAggregation = new JNITimeAggregation3();
		fillVectors(monitor);
	}

	
	public TimeAggregation3Manager copy()
	{
		TimeAggregation3Manager aNewManager = new TimeAggregation3Manager(this.matrix,  new NullProgressMonitor());
		aNewManager.matrix = (I3DMicroDescription) matrix.copy();
		aNewManager.timeAggregation = timeAggregation.copy();
		aNewManager.qualities = timeAggregation.getQualityList();
				
		return aNewManager;
	}
}
