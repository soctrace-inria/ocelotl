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

package fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.queries.reducedevent2;

import fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.queries.reducedevent1.PajeReducedEvent1;

public class PajeReducedEvent2 extends PajeReducedEvent1 {
	public String	TYPE;

	public PajeReducedEvent2(final int id, final int ep, final int page, final long ts, final String type, final String value) {
		super(id, ep, page, ts, value);
		TYPE = type;
		// TODO Auto-generated constructor stub
	}

}
