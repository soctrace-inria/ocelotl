/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.core.iaggregop2;

public class Part {
	int startPart;
	int endPart;
	IPartData data;
	

	
	public Part(int startPart, int endPart, IPartData data) {
		super();
		this.startPart = startPart;
		this.endPart = endPart;
		this.data = data;
	}

	public Part() {
		super();
	}

	public int getStartPart() {
		return startPart;
	}

	public void setStartPart(int startPart) {
		this.startPart = startPart;
	}

	public int getEndPart() {
		return endPart;
	}

	public void setEndPart(int endPart) {
		this.endPart = endPart;
	}

	public IPartData getData() {
		return data;
	}

	public void setData(IPartData data) {
		this.data = data;
	}
	
	public int getPartSize(){
		return endPart-startPart;
		
	}
	
	
	
}
