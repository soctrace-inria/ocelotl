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

package fr.inria.soctrace.tools.ocelotl.core.constants;

public class OcelotlConstants {

	/**
	 * States of the state machine
	 * ALL: 
	 * TS: number of time slices has changed
	 * NORMALIZE: quality curve normalization has changed 
	 * PARAMETER: parameter has changed
	 * EQ: 
	 * NOTHING: nothing has changed
	 *
	 */
	static public enum HasChanged {
		ALL, TS, NORMALIZE, THRESHOLD, PARAMETER, EQ, NOTHING
	}

}
