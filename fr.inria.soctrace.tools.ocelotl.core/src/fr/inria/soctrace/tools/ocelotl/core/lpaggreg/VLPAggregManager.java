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

import fr.inria.soctrace.tools.ocelotl.core.iaggregop.ITimeSliceMatrix;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.jni.LPAggregWrapper;

public class VLPAggregManager extends LPAggregManager {

	ITimeSliceMatrix	timeSliceMatrix;

	public VLPAggregManager(final ITimeSliceMatrix timeSliceMatrix) {
		super(timeSliceMatrix.getQueries().getOcelotlParameters());
		this.timeSliceMatrix = timeSliceMatrix;
		lpaggregWrapper = new LPAggregWrapper(2);
		fillVectors();
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
				final LPAggregWrapper lpaggregWrapperTemp = new LPAggregWrapper(2);
				for (int k = 0; k < parts.size(); k++)
					if (parts.get(k) == i || parts.get(k) == j) {
						lpaggregWrapperTemp.addVector();
						for (final String key : timeSliceMatrix.getMatrix().get(k).keySet())
							lpaggregWrapperTemp.push_back(timeSliceMatrix.getMatrix().get(k).get(key));
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
	public void fillVectors() {
		for (int i = 0; i < timeSliceMatrix.getVectorsNumber(); i++) {
			lpaggregWrapper.addVector();
			for (final String key : timeSliceMatrix.getMatrix().get(i).keySet())
				lpaggregWrapper.push_back(timeSliceMatrix.getMatrix().get(i).get(key));
		}

	}

	public ITimeSliceMatrix getTimeSliceMatrix() {
		return timeSliceMatrix;
	}

	@Override
	public void reset() {
		lpaggregWrapper = new LPAggregWrapper(2);
		fillVectors();
	}

}
