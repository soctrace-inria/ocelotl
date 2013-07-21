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

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.jni.VLPAggregWrapper;
import fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators.ITimeSliceMatrix;

public class VLPAggregManager extends LPAggregManager {

	VLPAggregWrapper	lpaggregWrapper;
	ITimeSliceMatrix	timeSliceMatrix;

	public VLPAggregManager(final ITimeSliceMatrix timeSliceMatrix) {
		super();
		this.timeSliceMatrix = timeSliceMatrix;
		lpaggregWrapper = new VLPAggregWrapper();
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

	public void computeEqMatrix() {
		eqMatrix = new ArrayList<List<Boolean>>();
		final int max = parts.get(parts.size() - 1);
		for (int i = 0; i <= max; i++) {
			eqMatrix.add(new ArrayList<Boolean>());
			for (int j = 0; j < i; j++)
				eqMatrix.get(i).add(false);
		}
		for (int i = 0; i < max - 1; i++)
			for (int j = i + 2; j <= max; j++) {
				final VLPAggregWrapper lpaggregWrapperTemp = new VLPAggregWrapper();
				for (int k = 0; k < parts.size(); k++)
					if (parts.get(k) == i || parts.get(k) == j) {
						lpaggregWrapperTemp.newVector();
						for (final String key : timeSliceMatrix.getMatrix().get(k).keySet())
							lpaggregWrapperTemp.addToVector(timeSliceMatrix.getMatrix().get(k).get(key));
					}
				lpaggregWrapperTemp.computeQualities(timeSliceMatrix.getQueries().getOcelotlParameters().isNormalize());
				lpaggregWrapperTemp.computeParts(timeSliceMatrix.getQueries().getOcelotlParameters().getParameter());
				boolean eq = true;
				for (int l = 0; l < lpaggregWrapperTemp.getPartNumber(); l++)
					if (l > 0 && lpaggregWrapperTemp.getPart(l) != lpaggregWrapperTemp.getPart(l - 1)) {
						eq = false;
						break;
					}
				eqMatrix.get(j).set(i, eq);
			}
	}

	public void computeEqParts() {
		computeEqMatrix();
		for (int i = eqMatrix.size() - 1; i > 0; i--)
			for (int j = eqMatrix.get(i).size() - 1; j >= 0; j--)
				if (eqMatrix.get(i).get(j))
					for (int k = parts.size() - 1; k >= 0; k--)
						if (parts.get(k) == i)
							parts.set(k, j);

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
		for (int i = 0; i < timeSliceMatrix.getVectorsNumber(); i++) {
			lpaggregWrapper.newVector();
			for (final String key : timeSliceMatrix.getMatrix().get(i).keySet())
				lpaggregWrapper.addToVector(timeSliceMatrix.getMatrix().get(i).get(key));
		}

	}

	public ITimeSliceMatrix getTimeSliceMatrix() {
		return timeSliceMatrix;
	}

	@Override
	public void reset() {
		lpaggregWrapper = new VLPAggregWrapper();
		fillVectors();
	}

}