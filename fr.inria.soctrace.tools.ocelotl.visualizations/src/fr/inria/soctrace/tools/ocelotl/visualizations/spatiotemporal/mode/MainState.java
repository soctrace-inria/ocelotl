package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode;

public class MainState {

	private String state;
	private double amplitude;
	private final static double Offset = 100.0;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public double getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}

	public int getAmplitude255() {
		return (int) (amplitude * 255.0);
	}

	public int getAmplitude255Shifted() {
		return (int) (Offset+(Math.min(amplitude, 1.0)*(255.0-Offset)));
	}

	public int getAmplitude100() {
		return (int) (amplitude * 100.0);
	}

	public MainState(String state, double amplitude) {
		super();
		this.state = state;
		this.amplitude = amplitude;
	}

}
