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
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public class TimeAggregation2Manager extends TimeAggregationManager {

	I2DMicroDescription	matrix;
	

	public TimeAggregation2Manager(final I2DMicroDescription matrix) {
		super(matrix.getOcelotlParameters());
		this.matrix = matrix;
		reset();
	}
	
	public void fillVectorsJava() {
		List<List<Double>>	values;
		values = new ArrayList<List<Double>>();
		for (int i = 0; i < matrix.getVectorsNumber(); i++) {
			values.add(new ArrayList<Double>());
			for (final String key : matrix.getMatrix().get(i).keySet())
				values.get(i).add(matrix.getMatrix().get(i).get(key).doubleValue());
		}
		((TimeAggregation2) timeAggregation).setValues(values);

	}
	
	public void fillVectorsJNI() {
		for (int i = 0; i < matrix.getVectorsNumber(); i++) {
			((JNITimeAggregation2) timeAggregation).addVector();
			for (final String key : matrix.getMatrix().get(i).keySet())
				((JNITimeAggregation2) timeAggregation).push_back(matrix.getMatrix().get(i).get(key).doubleValue());
		}

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
		if (OcelotlParameters.isJniFlag())
			timeAggregation = new JNITimeAggregation2();
		else
			timeAggregation = new TimeAggregation2();
		fillVectors();
	}

}