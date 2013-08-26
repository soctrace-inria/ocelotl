/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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

import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.jni.LPAggregWrapper;
import fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators.ITimeSliceCubicMatrix;

public class MLPAggregManager extends LPAggregManager {

	ITimeSliceCubicMatrix	timeSliceMatrix;

	public MLPAggregManager(final ITimeSliceCubicMatrix timeSliceMatrix) {
		super(timeSliceMatrix.getQueries().getOcelotlParameters());
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
					lpaggregWrapper.push_back(timeSliceMatrix.getMatrix().get(i).get(key).get(key2));
			}
		}

	}

	public ITimeSliceCubicMatrix getTimeSliceMatrix() {
		return timeSliceMatrix;
	}

	@Override
	public void reset() {
		lpaggregWrapper = new LPAggregWrapper(3);
		fillVectors();
	}

}
