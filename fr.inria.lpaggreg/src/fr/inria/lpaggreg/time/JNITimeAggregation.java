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

package fr.inria.lpaggreg.time;

import java.util.ArrayList;
import java.util.List;

import fr.inria.lpaggreg.jni.OLPAggregWrapper;
import fr.inria.lpaggreg.quality.DLPQuality;

public class JNITimeAggregation implements ITimeAggregation {

	protected OLPAggregWrapper jniWrapper;
	List<Double> parameters = new ArrayList<Double>();
	List<DLPQuality> qualities = new ArrayList<DLPQuality>();

	public JNITimeAggregation() {
		super();
	}

	@Override
	public void computeBestQualities(double threshold, double min, double max) {
		jniWrapper.computeDichotomy((float) threshold);
		parameters.clear();
		qualities.clear();
		for (int i = 0; i < jniWrapper.getParameterNumber(); i++) {
			parameters.add((double) jniWrapper.getParameter(i));
			qualities.add(new DLPQuality(jniWrapper.getGainByIndex(i),
					jniWrapper.getLossByIndex(i)));
		}
	}

	@Override
	public void computeQualities(boolean normalization) {
		jniWrapper.computeQualities(normalization);
	}

	@Override
	public List<Double> getParameters() {
		return parameters;
	}

	@Override
	public List<Integer> getParts(double parameter) {
		jniWrapper.computeParts((float) parameter);
		List<Integer> parts = new ArrayList<Integer>();
		for (int i = 0; i < jniWrapper.getPartNumber(); i++)
			parts.add(jniWrapper.getPart(i));
		return parts;
	}

	@Override
	public List<DLPQuality> getQualityList() {
		return qualities;
	}

	@Override
	public int getSize() {
		return jniWrapper.getPartNumber();
	}

}
