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

package fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.config;

import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;

public class GenericConfig implements ITraceTypeConfig {

	public final static String		DefaultState	= "PajeSetState";
	public final static String		DefaultIdle		= "IDLE";
	private final List<String>		idles			= new LinkedList<String>();
	private final List<EventType>	types			= new LinkedList<EventType>();

	public GenericConfig() {
		super();
	}

	@Override
	public List<EventType> getTypes() {
		return types;
	}

}
