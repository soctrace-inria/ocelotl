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

package fr.inria.soctrace.tools.ocelotl.ui.views.statview;

import fr.inria.soctrace.tools.ocelotl.core.statistics.IStatisticsProvider;


public interface IStatView {

	void createDiagram();

	public void deleteDiagram();

	public void init(StatViewWrapper wrapper);

	public void resizeDiagram();

	public void updateData();
	
	public void setStatProvider(IStatisticsProvider aStatProvider);
	
}
