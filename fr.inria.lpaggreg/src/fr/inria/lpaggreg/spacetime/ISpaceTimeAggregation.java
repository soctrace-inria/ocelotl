package fr.inria.lpaggreg.spacetime;

import java.util.List;

import fr.inria.lpaggreg.quality.DLPQuality;

public interface ISpaceTimeAggregation {

	public void computeBestQualities(double threshold, double min, double max);

	public void computeQualities(boolean normalization);

	public List<Double> getParameters();

	public void computeParts(double parameter);
	
	public List<Integer> getParts(int id);

	public List<DLPQuality> getQualityList();

	public int getSize();

	public void validate();

	public void addNode(int id, int parentID);

	public void addRoot(int id);

	public void addLeaf(int id, int parentID, Object values, int weight);
}
