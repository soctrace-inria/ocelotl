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

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.quality.DLPQuality;
import fr.inria.dlpaggreg.spacetime.ISpaceTimeAggregation;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.jni.DLPAggregWrapper;

public abstract class JNISpaceTimeAggregation implements ISpaceTimeAggregation {
	
	protected DLPAggregWrapper jniWrapper;
	List<Double> parameters = new ArrayList<Double>();
	List<DLPQuality> qualities = new ArrayList<DLPQuality>();

	
	public JNISpaceTimeAggregation() {
		super();
	}

	@Override
	public void computeBestQualities(double threshold, double min, double max) {
		jniWrapper.computeDichotomy((float) threshold);
		parameters.clear();
		qualities.clear();
		for (int i=0; i<jniWrapper.getParameterNumber(); i++){
			parameters.add((double) jniWrapper.getParameter(i));
			qualities.add(new DLPQuality(jniWrapper.getGainByIndex(i), jniWrapper.getLossByIndex(i)));
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
	public void computeParts(double parameter) {
		jniWrapper.computeParts(parameter);
	}

	@Override
	public List<DLPQuality> getQualityList() {
		return qualities;
	}

	@Override
	public int getSize() {
		return jniWrapper.getPartNumber();
	}
	
	@Override
	public List<Integer> getParts(int id){
		ArrayList<Integer> parts = new ArrayList<Integer>();
		for (int i=0; i<getSize(); i++)
			parts.add(jniWrapper.getPart(id, i));
		return parts;
	}
	
	@Override
	public void validate(){
		jniWrapper.validate();
	}

}
