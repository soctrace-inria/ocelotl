package fr.inria.dlpaggreg.time;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.complexity.Complexity;
import fr.inria.dlpaggreg.quality.DLPQuality;

public class TimeAggregation1 extends TimeAggregation {

	List<Double>	values;

	public TimeAggregation1() {
		super();
	}

	public TimeAggregation1(final List<Double> values) {
		super();
		this.values = values;
		setSize(values.size());
	}

	@Override
	protected void computeQualities() {
		final List<List<Double>> sumValues = new ArrayList<List<Double>>();
		final List<List<Double>> entValues = new ArrayList<List<Double>>();
		qualities = new ArrayList<List<DLPQuality>>();
		for (int i = 0; i < size; i++) {
			sumValues.add(new ArrayList<Double>());
			entValues.add(new ArrayList<Double>());
			qualities.add(new ArrayList<DLPQuality>());
			for (int j = 0; j < size - i; j++) {// TODO verificar
				sumValues.get(i).add(0.0);
				entValues.get(i).add(0.0);
				qualities.get(i).add(new DLPQuality());
			}
		}
		for (int i = 0; i < size; i++) {
			sumValues.get(i).set(0, values.get(i));
			entValues.get(i).set(0, Complexity.entropyReduction(values.get(i), 0.0));
		}

		for (int j = 1; j < size; j++)
			for (int i = 0; i < size - j; i++) {
				sumValues.get(i).set(j, sumValues.get(i).get(j - 1) + sumValues.get(i + j).get(0));
				entValues.get(i).set(j, entValues.get(i).get(j - 1) + entValues.get(i + j).get(0));
				qualities.get(i).get(j).setGain(Complexity.entropyReduction(sumValues.get(i).get(j), entValues.get(i).get(j)));
				qualities.get(i).get(j).setLoss(Complexity.divergence(j + 1, sumValues.get(i).get(j), entValues.get(i).get(j)));
			}
	}

	public List<Double> getValues() {
		return values;
	}

	public void setValues(final List<Double> values) {
		this.values = values;
		setSize(values.size());
	}

}
