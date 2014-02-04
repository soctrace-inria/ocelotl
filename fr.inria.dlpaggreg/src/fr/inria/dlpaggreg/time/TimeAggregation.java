package fr.inria.dlpaggreg.time;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.quality.DLPQuality;

public abstract class TimeAggregation {
	
	protected int size;
	List<List<DLPQuality>> qualities;
	List<Integer> bestCuts;
	List<Integer> bestPartitions;
	List<Double> parameters;
	List<DLPQuality> qualityList;
	
	public int getSize(){
		return size;
	}
	
	private void computeBestCuts(double parameter){
		bestCuts=new ArrayList<Integer>();
		List<Double> bestQuality=new ArrayList<Double>();
		for (int init=0; init<size; init++){
			bestCuts.add(0);
			bestQuality.add(0.0);
		}
		for (int j=1; j<size; j++){
			int currentCut=0;
			double currentQuality = pIC(parameter, 0, j);
			for (int i=1; i<j+1; i++){
				double quality = bestQuality.get(i-1) + pIC(parameter, i, j-i);
				if (quality>=currentQuality){
					currentCut=i;
					currentQuality=quality;
				}
					bestCuts.set(j, currentCut);
					bestQuality.set(j, currentQuality);
			
				
			}
			
		}
	}
	
	private void computeBestPartitions(){
		bestPartitions= new ArrayList<Integer>();
		for (int init=0; init<size; init++)
			bestPartitions.add(0);
		fillPartition(size-1, 0);
		
	}
	
	private int fillPartition(int i, int p){
		int c = bestCuts.get(i);
		if (c>0)
			p=fillPartition(c-1, p);
		for (int k =c; k<i+1; k++)
			bestPartitions.set(k, p);
		return p+1;
	}
	
	protected void setSize(int size){
		this.size=size;
	}
	
	public void computeBestQualities(double threshold, double min, double max){
		parameters=new ArrayList<Double>();
		qualityList=new ArrayList<DLPQuality>();
		DLPQuality bestQualityParamMin = new DLPQuality();
		DLPQuality bestQualityParamMax = new DLPQuality();
		computeBestCuts(min);
		computeBestQuality(bestQualityParamMin);
		parameters.add(min);
		qualityList.add(bestQualityParamMin);
		computeBestCuts(max);
		computeBestQuality(bestQualityParamMax);
		addBestQualities(min, max, bestQualityParamMin, bestQualityParamMax, threshold);
		parameters.add(max);
		qualityList.add(bestQualityParamMax);
		for (int i= qualityList.size()-1; i>0; i--){
			if ((qualityList.get(i).getGain()==qualityList.get(i-1).getGain())&& (qualityList.get(i).getLoss()==qualityList.get(i-1).getLoss())){
				qualityList.remove(i);
				parameters.remove(i);
			}
		}
	}
		
	
	private void addBestQualities(double min, double max, DLPQuality bestQualityParamMin, DLPQuality bestQualityParamMax, double threshold) {
		if (!(bestQualityParamMin.compare(bestQualityParamMax)||(max-min<=threshold))){
			double parameter = min + ((max - min)/2);
			DLPQuality bestQuality = new DLPQuality();
			computeBestCuts(parameter);
			computeBestQuality(bestQuality);
			addBestQualities(min, parameter, bestQualityParamMin, bestQuality, threshold);
			parameters.add(parameter);
			qualityList.add(bestQuality);
			addBestQualities(parameter, max, bestQuality, bestQualityParamMax, threshold);
		}
	}

	private void computeBestQuality(DLPQuality bestQuality) {
		bestQuality.setQuality(0, 0);
		fillQuality(size-1, bestQuality);
		
	}

	private void fillQuality(int i, DLPQuality bestQuality) {
		int c = bestCuts.get(i);
		if (c>0)
			fillQuality(c-1, bestQuality);
		bestQuality.addToQuality(qualities.get(c).get(i-c));
		
	}

	private double pIC(double parameter, int i, int j){
		return parameter * qualities.get(i).get(j).getGain() - (1.0-parameter) * qualities.get(i).get(j).getLoss();
	}
	
	public List<Integer> getParts(double parameter){
		computeBestCuts(parameter);
		computeBestPartitions();
		return bestPartitions;
	}
	
	public List<DLPQuality> getQualityList(){
		return qualityList;
	}
	
	public void computeQualities(boolean normalization){
		computeQualities();
		if (normalization)
		normalize();
	}

	private void normalize() {
		DLPQuality maxQuality = new DLPQuality(qualities.get(0).get(size-1));
		for (int j=0; j<size; j++){
			for (int i=0; i<size-j; i++){
				qualities.get(i).get(j).normalize(maxQuality);
			}
		}
		
	}

	protected abstract void computeQualities();
}
