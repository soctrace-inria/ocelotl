package fr.inria.dlpaggreg.time;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.complexity.Complexity;
import fr.inria.dlpaggreg.quality.DLPQuality;

public class TimeAggregation2 extends TimeAggregation {

	List<List<Double>> values;
	
	public List<List<Double>> getValues() {
		return values;
	}

	public void setValues(List<List<Double>> values) {
		this.values = values;
		setSize(values.size());
	}

	public TimeAggregation2() {
		super();
	}

	public TimeAggregation2(List<List<Double>> values) {
		super();
		this.values = values;
		setSize(values.size());
	}

	@Override
	protected void computeQualities() {
		List<List<List<Double>>> sumValues = new ArrayList<List<List<Double>>>();
		List<List<List<Double>>> entValues = new ArrayList<List<List<Double>>>();
		qualities = new ArrayList<List<DLPQuality>>();
		int size2=values.get(0).size();
		for (int i=0; i<size; i++){
			sumValues.add(new ArrayList<List<Double>>());
			entValues.add(new ArrayList<List<Double>>());
			qualities.add(new ArrayList<DLPQuality>());
			for (int j=0; j<size-i; j++){//TODO verificar
				sumValues.get(i).add(new ArrayList<Double>());
				entValues.get(i).add(new ArrayList<Double>());
				qualities.get(i).add(new DLPQuality());
				for (int k=0; k<size2; k++){
					sumValues.get(i).get(j).add(0.0);
					entValues.get(i).get(j).add(0.0);
				}
			}
		}
		for (int i=0; i<size; i++){
			for (int k=0; k<size2; k++){
			sumValues.get(i).get(0).set(k,values.get(i).get(k));
			entValues.get(i).get(0).set(k,Complexity.entropyReduction(values.get(i).get(k), 0));
			}
		}
		
		for (int j=1; j<size; j++){
			for (int i=0; i<size-j; i++){
				for (int k=0; k<size2; k++){
				sumValues.get(i).get(j).set(k, sumValues.get(i).get(j-1).get(k)+sumValues.get(i+j).get(0).get(k));
				entValues.get(i).get(j).set(k, entValues.get(i).get(j-1).get(k)+entValues.get(i+j).get(0).get(k));
				qualities.get(i).get(j).addToGain
				(Complexity.entropyReduction(sumValues.get(i).get(j).get(k), entValues.get(i).get(j).get(k)));
				qualities.get(i).get(j).setLoss
				(Complexity.divergence(j+1, sumValues.get(i).get(j).get(k), entValues.get(i).get(j).get(k)));
				}
			}
		}
	}

}
