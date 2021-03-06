/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode;

public class MainEvent {

	private String event;
	private double amplitude;
	private final static double Offset = 30.0;

	public String getState() {
		return event;
	}

	public void setState(String state) {
		this.event = state;
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
		return (int) (Offset + (Math.min(amplitude, 1.0) * (255.0 - Offset)));
	}

	public int getAmplitude100() {
		return (int) (amplitude * 100.0);
	}

	public MainEvent(String state, double amplitude) {
		super();
		this.event = state;
		this.amplitude = amplitude;
	}

}
