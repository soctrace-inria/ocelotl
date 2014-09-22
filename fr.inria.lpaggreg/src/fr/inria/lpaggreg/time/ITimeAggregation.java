package fr.inria.lpaggreg.time;

import java.util.List;

import fr.inria.lpaggreg.quality.DLPQuality;

public interface ITimeAggregation {

	public void computeBestQualities(double threshold, double min, double max);

	public void computeQualities(boolean normalization);

	public List<Double> getParameters();

	public List<Integer> getParts(double parameter);

	public List<DLPQuality> getQualityList();

	public int getSize();
	
	public ITimeAggregation copy();
}
