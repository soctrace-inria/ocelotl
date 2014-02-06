package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.quality.DLPQuality;
import fr.inria.dlpaggreg.time.ITimeAggregation;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.jni.LPAggregWrapper;

public class JNITimeAggregation implements ITimeAggregation {
	
	protected LPAggregWrapper jniWrapper;
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
	public List<Integer> getParts(double parameter) {
		jniWrapper.computeParts((float) parameter);
		List<Integer> parts = new ArrayList<Integer>();
		for (int i=0; i<jniWrapper.getPartNumber(); i++)
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
