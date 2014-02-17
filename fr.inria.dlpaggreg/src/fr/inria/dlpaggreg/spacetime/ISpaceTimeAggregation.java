package fr.inria.dlpaggreg.spacetime;

import java.util.List;

import fr.inria.dlpaggreg.quality.DLPQuality;

public interface ISpaceTimeAggregation {

	public void computeBestQualities(double threshold, double min, double max);

	public void computeQualities(boolean normalization);

	public List<Double> getParameters();

	public void computeParts(double parameter);
	
	public List<Integer> getParts(int id);

	public List<DLPQuality> getQualityList();

	public int getSize();
}
