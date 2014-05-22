package fr.inria.soctrace.tools.ocelotl.visualizations.matrixproportion;

public class MajState {
	
	private String state;
	private double amplitude;
	
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
	
	public int getAmplitude255(){
		return (int) (amplitude*255.0);
	} 
	
	public int getAmplitude255M(){
		return (int) ((Math.min(amplitude*255.0, 255.0)));
	} 
	
	public int getAmplitude100(){
		return (int) (amplitude*100.0);
	}
	public MajState(String state, double amplitude) {
		super();
		this.state = state;
		this.amplitude = amplitude;
	}
	
	

}
