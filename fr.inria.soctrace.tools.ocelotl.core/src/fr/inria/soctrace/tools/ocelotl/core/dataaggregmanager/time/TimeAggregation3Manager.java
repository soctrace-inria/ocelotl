/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.lpaggreg.time.JNITimeAggregation3;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.Microscopic3DDescription;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;

public class TimeAggregation3Manager extends TimeAggregationManager {

	Microscopic3DDescription matrix;
	List<List<List<Double>>> values;

	public TimeAggregation3Manager(final MicroscopicDescription timeSliceMatrix, IProgressMonitor monitor) {
		super(timeSliceMatrix.getOcelotlParameters());
		this.matrix = (Microscopic3DDescription) timeSliceMatrix;
		simplifyMatrix();
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

	public Microscopic3DDescription getTimeSliceMatrix() {
		return matrix;
	}

	@Override
	public void reset(IProgressMonitor monitor) {
		timeAggregation = new JNITimeAggregation3();
		fillVectors(monitor);
	}
	
	/**
	 * Remove event producers that do not produce any event (better
	 * performances)
	 */
	protected void simplifyMatrix() {
		if (matrix.getInactiveProducers().size() == 0)
			return;

		// Remove the inactive producers
		for (final HashMap<EventProducer, HashMap<String, Double>> it : matrix
				.getMatrix()) {
			// For each inactive event producer
			for (final EventProducer ep : matrix.getInactiveProducers()) {
				it.remove(ep);
			}
		}
	}
	
}
