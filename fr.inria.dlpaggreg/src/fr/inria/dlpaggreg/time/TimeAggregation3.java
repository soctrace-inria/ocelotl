package fr.inria.dlpaggreg.time;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.complexity.Complexity;
import fr.inria.dlpaggreg.quality.DLPQuality;

public class TimeAggregation3 extends TimeAggregation {

	List<List<List<Double>>> values;
	
	public List<List<List<Double>>> getValues() {
		return values;
	}

	public void setValues(List<List<List<Double>>> values) {
		this.values = values;
		setSize(values.size());
	}

	public TimeAggregation3() {
		super();
	}

	public TimeAggregation3(List<List<List<Double>>> values) {
		super();
		this.values = values;
		setSize(values.size());
	}

	@Override
	protected void computeQualities() {
		List<List<List<List<Double>>>> sumValues = new ArrayList<List<List<List<Double>>>>();
		List<List<List<List<Double>>>> entValues = new ArrayList<List<List<List<Double>>>>();
		qualities = new ArrayList<List<DLPQuality>>();
		int size2=values.get(0).size();
		int size3=values.get(0).get(0).size();
		for (int i=0; i<size; i++){
			sumValues.add(new ArrayList<List<List<Double>>>());
			entValues.add(new ArrayList<List<List<Double>>>());
			qualities.add(new ArrayList<DLPQuality>());
			for (int j=0; j<size-i; j++){//TODO verificar
				sumValues.get(i).add(new ArrayList<List<Double>>());
				entValues.get(i).add(new ArrayList<List<Double>>());
				qualities.get(i).add(new DLPQuality());
				for (int k=0; k<size2; k++){
					sumValues.get(i).get(j).add(new ArrayList<Double>());
					entValues.get(i).get(j).add(new ArrayList<Double>());
					for (int l=0; l<size3; l++){
						sumValues.get(i).get(j).get(k).add(0.0);
						entValues.get(i).get(j).get(k).add(0.0);
					}
				}
			}
		}
		for (int i=0; i<size; i++){
			for (int k=0; k<size2; k++){
				for (int l=0; l<size3; l++){
			sumValues.get(i).get(0).get(k).set(l,values.get(i).get(k).get(l));
			entValues.get(i).get(0).get(k).set(l,Complexity.entropyReduction(values.get(i).get(k).get(l), 0));
			}
			}
		}
		
		for (int j=1; j<size; j++){
			for (int i=0; i<size-j; i++){
				for (int k=0; k<size2; k++){
					for (int l=0; l<size3; l++){
				sumValues.get(i).get(j).get(k).set(l, sumValues.get(i).get(j-1).get(k).get(l)+sumValues.get(i+j).get(0).get(k).get(l));
				entValues.get(i).get(j).get(k).set(l, entValues.get(i).get(j-1).get(k).get(l)+entValues.get(i+j).get(0).get(k).get(l));
				qualities.get(i).get(j).addToGain
				(Complexity.entropyReduction(sumValues.get(i).get(j).get(k).get(l), entValues.get(i).get(j).get(k).get(l)));
				qualities.get(i).get(j).setLoss
				(Complexity.divergence(j+1, sumValues.get(i).get(j).get(k).get(l), entValues.get(i).get(j).get(k).get(l)));
				}
				}
			}
		}
	}

}
