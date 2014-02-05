package fr.inria.dlpaggreg.time;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.quality.DLPQuality;

public abstract class TimeAggregation implements ITimeAggregation {

	protected int			size;
	List<List<DLPQuality>>	qualities;
	List<Integer>			bestCuts;
	List<Integer>			bestPartitions;
	List<Double>			parameters;
	List<DLPQuality>		qualityList;

	private void addBestQualities(final double min, final double max, final DLPQuality bestQualityParamMin, final DLPQuality bestQualityParamMax, final double threshold) {
		if (!(bestQualityParamMin.compare(bestQualityParamMax) || max - min <= threshold)) {
			final double parameter = min + (max - min) / 2;
			final DLPQuality bestQuality = new DLPQuality();
			computeBestCuts(parameter);
			computeBestQuality(bestQuality);
			addBestQualities(min, parameter, bestQualityParamMin, bestQuality, threshold);
			parameters.add(parameter);
			qualityList.add(bestQuality);
			addBestQualities(parameter, max, bestQuality, bestQualityParamMax, threshold);
		}
	}

	private void computeBestCuts(final double parameter) {
		bestCuts = new ArrayList<Integer>();
		final List<Double> bestQuality = new ArrayList<Double>();
		for (int init = 0; init < size; init++) {
			bestCuts.add(0);
			bestQuality.add(0.0);
		}
		for (int j = 1; j < size; j++) {
			int currentCut = 0;
			double currentQuality = pIC(parameter, 0, j);
			for (int i = 1; i < j + 1; i++) {
				final double quality = bestQuality.get(i - 1) + pIC(parameter, i, j - i);
				if (quality >= currentQuality) {
					currentCut = i;
					currentQuality = quality;
				}
				bestCuts.set(j, currentCut);
				bestQuality.set(j, currentQuality);

			}

		}
	}

	private void computeBestPartitions() {
		bestPartitions = new ArrayList<Integer>();
		for (int init = 0; init < size; init++)
			bestPartitions.add(0);
		fillPartition(size - 1, 0);

	}

	@Override
	public void computeBestQualities(final double threshold, final double min, final double max) {
		parameters = new ArrayList<Double>();
		qualityList = new ArrayList<DLPQuality>();
		final DLPQuality bestQualityParamMin = new DLPQuality();
		final DLPQuality bestQualityParamMax = new DLPQuality();
		computeBestCuts(min);
		computeBestQuality(bestQualityParamMin);
		parameters.add(min);
		qualityList.add(bestQualityParamMin);
		computeBestCuts(max);
		computeBestQuality(bestQualityParamMax);
		addBestQualities(min, max, bestQualityParamMin, bestQualityParamMax, threshold);
		parameters.add(max);
		qualityList.add(bestQualityParamMax);
		for (int i = qualityList.size() - 1; i > 0; i--)
			if (qualityList.get(i).compare(qualityList.get(i - 1))) {
				qualityList.remove(i);
				parameters.remove(i);
			}
	}

	private void computeBestQuality(final DLPQuality bestQuality) {
		bestQuality.setQuality(0, 0);
		fillQuality(size - 1, bestQuality);

	}

	protected abstract void computeQualities();

	@Override
	public void computeQualities(final boolean normalization) {
		computeQualities();
		if (normalization)
			normalize();
	}

	private int fillPartition(final int i, int p) {
		final int c = bestCuts.get(i);
		if (c > 0)
			p = fillPartition(c - 1, p);
		for (int k = c; k < i + 1; k++)
			bestPartitions.set(k, p);
		return p + 1;
	}

	private void fillQuality(final int i, final DLPQuality bestQuality) {
		final int c = bestCuts.get(i);
		if (c > 0)
			fillQuality(c - 1, bestQuality);
		bestQuality.addToQuality(qualities.get(c).get(i - c));

	}

	@Override
	public List<Double> getParameters() {
		return parameters;
	}

	@Override
	public List<Integer> getParts(final double parameter) {
		computeBestCuts(parameter);
		computeBestPartitions();
		return bestPartitions;
	}

	@Override
	public List<DLPQuality> getQualityList() {
		return qualityList;
	}

	@Override
	public int getSize() {
		return size;
	}

	private void normalize() {
		final DLPQuality maxQuality = new DLPQuality(qualities.get(0).get(size - 1));
		for (int j = 0; j < size; j++)
			for (int i = 0; i < size - j; i++)
				qualities.get(i).get(j).normalize(maxQuality);

	}

	private double pIC(final double parameter, final int i, final int j) {
		return parameter * qualities.get(i).get(j).getGain() - (1.0 - parameter) * qualities.get(i).get(j).getLoss();
	}

	protected void setSize(final int size) {
		this.size = size;
	}
}
