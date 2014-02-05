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

import fr.inria.dlpaggreg.time.TimeAggregation3;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.I3DMicroDescription;

public class TimeAggregation3Manager extends TimeAggregationManager {

	I3DMicroDescription			timeSliceMatrix;
	List<List<List<Double>>>	values;

	public TimeAggregation3Manager(final I3DMicroDescription timeSliceMatrix) {
		super(timeSliceMatrix.getOcelotlParameters());
		this.timeSliceMatrix = timeSliceMatrix;
		reset();
	}

	@Override
	public void fillVectors() {
		values = new ArrayList<List<List<Double>>>();
		for (int i = 0; i < timeSliceMatrix.getMatrix().size(); i++) {
			values.add(new ArrayList<List<Double>>());
			int j = 0;
			for (final String key : timeSliceMatrix.getMatrix().get(i).keySet()) {
				values.get(i).add(new ArrayList<Double>());
				for (final String key2 : timeSliceMatrix.getMatrix().get(i).get(key).keySet())
					values.get(i).get(j).add(timeSliceMatrix.getMatrix().get(i).get(key).get(key2).doubleValue());
				j++;
			}

		}
		((TimeAggregation3) timeAggregation).setValues(values);

	}

	@Override
	public List<String> getEventProducers() {
		return new ArrayList<String>(timeSliceMatrix.getMatrix().get(0).keySet());
	}

	public List<String> getKeys() {
		return new ArrayList<String>(timeSliceMatrix.getMatrix().get(0).get(getEventProducers().get(0)).keySet());
	}

	public I3DMicroDescription getTimeSliceMatrix() {
		return timeSliceMatrix;
	}

	@Override
	public void reset() {
		timeAggregation = new TimeAggregation3();
		fillVectors();
	}

}
