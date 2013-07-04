package fr.inria.soctrace.tools.ocelotl.core.lpaggreg;

public class Quality {

	private double	gain, loss;
	private float	parameter;

	public Quality() {
		super();
		gain = 0;
		loss = 0;
	}

	public Quality(final double gain, final double loss, final float parameter) {
		super();
		this.gain = gain;
		this.loss = loss;
		setParameter(parameter);
	}

	public double getGain() {
		return gain;
	}

	public double getLoss() {
		return loss;
	}

	public float getParameter() {
		return parameter;
	}

	public void setGain(final double gain) {
		this.gain = gain;
	}

	public void setLoss(final double loss) {
		this.loss = loss;
	}

	public void setParameter(final float parameter) {
		this.parameter = parameter;
	}

}
