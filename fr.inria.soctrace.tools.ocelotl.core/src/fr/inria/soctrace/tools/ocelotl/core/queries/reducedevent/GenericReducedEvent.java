/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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

package fr.inria.soctrace.tools.ocelotl.core.queries.reducedevent;

import fr.inria.soctrace.tools.ocelotl.core.queries.eventproxy.EventProxy;

public class GenericReducedEvent extends EventProxy {
	public String TYPE;
	public long TS;

	public GenericReducedEvent(final int id, final int ep, final int page,
			final long ts, final String type) {
		super(id, ep, page);
		TS = ts;
		TYPE = type;
	}

}
