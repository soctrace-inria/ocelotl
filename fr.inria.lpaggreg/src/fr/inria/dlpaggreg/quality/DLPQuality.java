package fr.inria.dlpaggreg.quality;

public class DLPQuality {

	private double	gain;
	private double	loss;

	public DLPQuality() {
		super();
		gain = 0;
		loss = 0;
	}

	public DLPQuality(final DLPQuality dLPQuality) {
		super();
		gain = dLPQuality.getGain();
		loss = dLPQuality.getLoss();
	}

	public DLPQuality(final double gain, final double loss) {
		super();
		this.gain = gain;
		this.loss = loss;
	}

	public void addToGain(final double gain) {
		this.gain += gain;
	}

	public void addToLoss(final double loss) {
		this.loss += loss;
	}

	public void addToQuality(final DLPQuality dLPQuality) {
		gain += dLPQuality.getGain();
		loss += dLPQuality.getLoss();
	}

	public void addToQuality(final double gain, final double loss) {
		this.gain += gain;
		this.loss += loss;
	}

	public boolean compare(final DLPQuality dLPQuality) {
		return getGain() == dLPQuality.getGain() && getLoss() == dLPQuality.getLoss();
	}

	public double getGain() {
		return gain;
	}

	public double getLoss() {
		return loss;
	}

	public void normalize(final DLPQuality dLPQuality) {
		gain = gain / dLPQuality.getGain();
		loss = loss / dLPQuality.getLoss();
	}

	public void setGain(final double gain) {
		this.gain = gain;
	}

	public void setLoss(final double loss) {
		this.loss = loss;
	}

	public void setQuality(final double gain, final double loss) {
		this.gain = gain;
		this.loss = loss;
	}

}
