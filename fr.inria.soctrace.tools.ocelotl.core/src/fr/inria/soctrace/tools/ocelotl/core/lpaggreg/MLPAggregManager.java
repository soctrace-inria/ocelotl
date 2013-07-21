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

import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.jni.MLPAggregWrapper;
import fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators.ITimeSliceCubicMatrix;

public class MLPAggregManager extends LPAggregManager {

	MLPAggregWrapper		lpaggregWrapper;
	ITimeSliceCubicMatrix	timeSliceMatrix;

	public MLPAggregManager(final ITimeSliceCubicMatrix timeSliceMatrix) {
		super();
		this.timeSliceMatrix = timeSliceMatrix;
		lpaggregWrapper = new MLPAggregWrapper();
		fillVectors();
	}

	@Override
	public void computeDichotomy() {
		final DeltaManager dm = new DeltaManager();
		dm.start();
		parameters.clear();
		qualities.clear();
		lpaggregWrapper.computeDichotomy(timeSliceMatrix.getQueries().getOcelotlParameters().getThreshold());
		for (int i = 0; i < lpaggregWrapper.getParameterNumber(); i++) {
			parameters.add(lpaggregWrapper.getParameter(i));
			qualities.add(new Quality(lpaggregWrapper.getGainByIndex(i), lpaggregWrapper.getLossByIndex(i), lpaggregWrapper.getParameter(i)));
		}
		dm.end("LPAGGREG - PARAMETERS LIST");
	}

	@Override
	public void computeParts() {
		parts.clear();
		final DeltaManager dm = new DeltaManager();
		dm.start();
		lpaggregWrapper.computeParts(timeSliceMatrix.getQueries().getOcelotlParameters().getParameter());
		for (int i = 0; i < lpaggregWrapper.getPartNumber(); i++)
			parts.add(lpaggregWrapper.getPart(i));
		dm.end("LPAGGREG - COMPUTE PARTS");
	}

	@Override
	public void computeQualities() {
		final DeltaManager dm = new DeltaManager();
		dm.start();
		lpaggregWrapper.computeQualities(timeSliceMatrix.getQueries().getOcelotlParameters().isNormalize());
		dm.end("LPAGGREG - COMPUTE QUALITIES");
	}

	@Override
	public void fillVectors() {
		for (int i = 0; i < timeSliceMatrix.getMatrix().size(); i++) {
			lpaggregWrapper.newMatrix();
			for (final String key : timeSliceMatrix.getMatrix().get(i).keySet()) {
				lpaggregWrapper.newVector();
				for (final String key2 : timeSliceMatrix.getMatrix().get(i).get(key).keySet())
					lpaggregWrapper.addToVector(timeSliceMatrix.getMatrix().get(i).get(key).get(key2));
			}
		}

	}

	public ITimeSliceCubicMatrix getTimeSliceMatrix() {
		return timeSliceMatrix;
	}

	@Override
	public void reset() {
		lpaggregWrapper = new MLPAggregWrapper();
		fillVectors();
	}

}
