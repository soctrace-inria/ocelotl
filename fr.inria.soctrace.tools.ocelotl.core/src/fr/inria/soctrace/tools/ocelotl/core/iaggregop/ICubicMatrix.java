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

package fr.inria.soctrace.tools.ocelotl.core.iaggregop;

import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.MLPAggregManager;

public interface ICubicMatrix extends IAggregationOperator {

	@Override
	public MLPAggregManager createManager();

	public List<HashMap<String, HashMap<String, Long>>> getMatrix();
	
	public void addKey(String key);
	
	public List<String> getKeys();
}
