package fr.inria.soctrace.tools.paje.lpaggreg.core;

public class Quality {
	
	private double gain, loss;
	private float parameter;

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

	public Quality(double gain, double loss, float parameter) {
		super();
		this.gain = gain;
		this.loss = loss;
		this.setParameter(parameter);
	}
	
	public Quality() {
		super();
		this.gain = 0;
		this.loss = 0;
	}

	public float getParameter() {
		return parameter;
	}

	public void setParameter(float parameter) {
		this.parameter = parameter;
	}

}
