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

package fr.inria.soctrace.tools.ocelotl.core.paje.query;

import fr.inria.soctrace.tools.ocelotl.core.generic.query.EventProxy;

public class PajeReducedEvent1 extends EventProxy {

	public String	VALUE;
	public int		PAGE;
	public long		TS;

	public PajeReducedEvent1(final int id, final int ep, final int page, final long ts, final String value) {
		super(id, ep);
		VALUE = value;
		PAGE = page;
		TS = ts;
		// TODO Auto-generated constructor stub
	}

}
