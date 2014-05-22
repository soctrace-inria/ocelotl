/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
 * overview by using aggregation techniques
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

package fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop;

public class Part {
	private int startPart;
	private int endPart;
	private IPartData data;

	public Part() {
		super();
	}

	public Part(final int startPart, final int endPart, final IPartData data) {
		super();
		this.startPart = startPart;
		this.endPart = endPart;
		this.data = data;
	}

	public Part(final int startPart, final IPartData data) {
		super();
		this.startPart = startPart;
		this.endPart = startPart;
		this.data = data;
	}

	public IPartData getData() {
		return data;
	}

	public int getEndPart() {
		return endPart;
	}

	public int getPartSize() {
		return endPart - startPart;

	}

	public int getStartPart() {
		return startPart;
	}

	public void setData(final IPartData data) {
		this.data = data;
	}

	public void setEndPart(final int endPart) {
		this.endPart = endPart;
	}

	public void setStartPart(final int startPart) {
		this.startPart = startPart;
	}

	public void incrSize() {
		endPart++;
	}

	public boolean compare(Part p2) {
		return (this.startPart == p2.getStartPart())
				&& (this.endPart == p2.getEndPart());
	}

	@Override
	public String toString() {
		String s = "start: " + startPart + ", end: " + endPart;
		return s;
	}

}
