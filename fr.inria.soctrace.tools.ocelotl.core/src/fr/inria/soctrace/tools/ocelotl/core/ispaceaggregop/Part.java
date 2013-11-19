/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
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
	int			startPart;
	int			endPart;
	IPartData	data;

	public Part() {
		super();
	}

	public Part(final int startPart, final int endPart, final IPartData data) {
		super();
		this.startPart = startPart;
		this.endPart = endPart;
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

}
