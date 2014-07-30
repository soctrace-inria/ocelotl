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

import fr.inria.lpaggreg.time.JNITimeAggregation2;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.I2DMicroDescription;

public class TimeAggregation2Manager extends TimeAggregationManager {

	I2DMicroDescription matrix;

	public TimeAggregation2Manager(final I2DMicroDescription matrix) {
		super(matrix.getOcelotlParameters());
		this.matrix = matrix;
		reset();
	}


	@Override
	public void fillVectors() {
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

	public I2DMicroDescription getTimeSliceMatrix() {
		return matrix;
	}

	@Override
	public void reset() {
		timeAggregation = new JNITimeAggregation2();
		fillVectors();
	}

}
