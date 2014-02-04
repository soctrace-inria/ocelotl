package fr.inria.dlpaggreg.quality;

public class DLPQuality {
	
	private double gain;
	private double loss;
	
	public DLPQuality() {
		super();
		this.gain = 0;
		this.loss = 0;
	}
	
	public DLPQuality(double gain, double loss) {
		super();
		this.gain = gain;
		this.loss = loss;
	}

	public DLPQuality(DLPQuality dLPQuality) {
		super();
		this.gain=dLPQuality.getGain();
		this.loss=dLPQuality.getLoss();
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
	
	public void addToQuality(DLPQuality dLPQuality){
		this.gain += dLPQuality.getGain();
		this.loss += dLPQuality.getLoss();
	}
	
	public boolean compare(DLPQuality dLPQuality){
		return this.getGain()==dLPQuality.getGain()&&this.getLoss()==dLPQuality.getLoss();
	}
	
	public void normalize(DLPQuality dLPQuality){
		this.gain=this.gain/dLPQuality.getGain();
		this.loss=this.loss/dLPQuality.getLoss();
	}

}
