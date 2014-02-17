package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.quality.DLPQuality;
import fr.inria.dlpaggreg.spacetime.ISpaceTimeAggregation;
import fr.inria.dlpaggreg.time.ITimeAggregation;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.jni.DLPAggregWrapper;

public class JNISpaceTimeAggregation implements ISpaceTimeAggregation {
	
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
		jniWrapper.computeParts((float) parameter);
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
	public List<Integer> getParts(int id) {
		List<Integer> parts = new ArrayList<Integer>();
		for (int i=0; i<jniWrapper.getPartNumber(); i++)
			parts.add(jniWrapper.getPart(id, i));
		return parts;
	}

}
