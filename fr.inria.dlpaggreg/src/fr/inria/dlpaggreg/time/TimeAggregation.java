package fr.inria.dlpaggreg.time;

import java.util.ArrayList;
import java.util.List;

import fr.inria.dlpaggreg.quality.Quality;

public class TimeAggregation {
	
	private int size;
	List<List<Quality>> qualities;
	List<Integer> bestCuts;
	List<Integer> bestPartitions;
	List<Float> parameters;
	List<Quality> qualityList;
	
	public int getSize(){
		return size;
	}
	
	private void computeBestCuts(double parameter){
		bestCuts=new ArrayList<Integer>();
		List<Double> bestQuality=new ArrayList<Double>();
		bestCuts.add(0);
		bestQuality.add(0.0);
		for (int j=1; j<size; j++){
			int currentCut=0;
			double currentQuality = pIC(parameter, 0, j);
			for (int i=1; i<j+1; i++){
				double quality = bestQuality.get(i-1) + pIC(parameter, i, j-i);
				if (quality>=currentQuality){
					currentCut=i;
					currentQuality=quality;
				}
				if (bestCuts.size()>j){
					bestCuts.set(j, currentCut);
					bestQuality.set(j, currentQuality);
				}
				else if (bestCuts.size()==j){
					bestCuts.add(currentCut);
					bestQuality.add(currentQuality);
				}
				
			}
			
		}
	}
	
	private void computeBestPartitions
	
	private double pIC(double parameter, int i, int j){
		return parameter * qualities.get(i).get(j).getGain() - (1.0-parameter) * qualities.get(i).get(j).getLoss();
	}
}
