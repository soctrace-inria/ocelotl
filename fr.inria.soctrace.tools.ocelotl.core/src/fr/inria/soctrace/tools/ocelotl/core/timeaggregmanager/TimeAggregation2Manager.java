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

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.time.TimeAggregation2;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.I2DMicroDescription;

public class TimeAggregation2Manager extends TimeAggregationManager {

	I2DMicroDescription	matrix;
	List<List<Double>>	values;

	public TimeAggregation2Manager(final I2DMicroDescription matrix) {
		super(matrix.getOcelotlParameters());
		this.matrix = matrix;
		reset();
	}

	@Override
	public void fillVectors() {
		values = new ArrayList<List<Double>>();
		for (int i = 0; i < matrix.getVectorsNumber(); i++) {
			values.add(new ArrayList<Double>());
			for (final String key : matrix.getMatrix().get(i).keySet())
				values.get(i).add(matrix.getMatrix().get(i).get(key).doubleValue());
		}
		((TimeAggregation2) timeAggregation).setValues(values);

	}

	@Override
	public List<String> getEventProducers() {
		return new ArrayList<String>(matrix.getMatrix().get(0).keySet());
	}

	public I2DMicroDescription getTimeSliceMatrix() {
		return matrix;
	}

	@Override
	public void reset() {
		timeAggregation = new TimeAggregation2();
		fillVectors();
	}

}
