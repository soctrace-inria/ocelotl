package fr.inria.dlpaggreg.quality;

public class Quality {
	
	private double gain;
	private double loss;
	
	public Quality() {
		super();
		this.gain = 0;
		this.loss = 0;
	}
	
	public Quality(double gain, double loss) {
		super();
		this.gain = gain;
		this.loss = loss;
	}

	public double getGain() {
		return gain;
	}

	public void setGain(double gain) {
		this.gain = gain;
	}

	public double getLoss() {
		return loss;
	}

	public void setLoss(double loss) {
		this.loss = loss;
	}
	
	public void setQuality(double gain, double loss){
		this.gain = gain;
		this.loss = loss;
	}
	
	public void addToGain(double gain){
		this.gain+=gain;
	}
	
	public void addToLoss(double loss){
		this.loss+=loss;
	}
	
	public void addToQuality(double gain, double loss){
		this.gain += gain;
		this.loss += loss;
	}

}
