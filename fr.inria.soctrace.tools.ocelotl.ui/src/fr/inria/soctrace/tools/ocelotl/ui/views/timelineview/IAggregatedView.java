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

package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IMicroDescManager;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public interface IAggregatedView {

	void createDiagram(IMicroDescManager iMicroDescManager, TimeRegion time);

	public void deleteDiagram();

	public long getEnd();

	public long getStart();

	public void init(TimeLineViewWrapper wrapper);

	public void resizeDiagram();
	
	public void createSnapshotFor(String fileName);

}
