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

package fr.inria.soctrace.tools.ocelotl.core.lpaggreg;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.I3DMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.jni.LPAggregWrapper;

public class MLPAggregManager extends LPAggregManager {

	I3DMicroDescription	timeSliceMatrix;

	public MLPAggregManager(final I3DMicroDescription timeSliceMatrix) {
		super(timeSliceMatrix.getOcelotlParameters());
		this.timeSliceMatrix = timeSliceMatrix;
		lpaggregWrapper = new LPAggregWrapper(3);
		fillVectors();
	}

	@Override
	public void fillVectors() {
		for (int i = 0; i < timeSliceMatrix.getMatrix().size(); i++) {
			lpaggregWrapper.addMatrix();
			for (final String key : timeSliceMatrix.getMatrix().get(i).keySet()) {
				lpaggregWrapper.addVector();
				for (final String key2 : timeSliceMatrix.getMatrix().get(i).get(key).keySet())
					lpaggregWrapper.push_back(timeSliceMatrix.getMatrix().get(i).get(key).get(key2).doubleValue());
			}
		}

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
		lpaggregWrapper = new LPAggregWrapper(3);
		fillVectors();
	}

}
