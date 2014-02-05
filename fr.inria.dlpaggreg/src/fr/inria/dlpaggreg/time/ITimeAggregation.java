package fr.inria.dlpaggreg.time;

import java.util.List;

import fr.inria.dlpaggreg.quality.DLPQuality;

public interface ITimeAggregation {

	public void computeBestQualities(double threshold, double min, double max);

	public void computeQualities(boolean normalization);

	public List<Double> getParameters();

	public List<Integer> getParts(double parameter);

	public List<DLPQuality> getQualityList();

	public int getSize();
}
