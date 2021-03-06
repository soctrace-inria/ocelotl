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

package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import org.eclipse.draw2d.Figure;
import org.eclipse.swt.graphics.Color;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public interface IAggregatedView {

	void createDiagram(IDataAggregManager iMicroDescManager, TimeRegion time, IVisuOperator visuOperator);

	public void deleteDiagram();

	public long getEnd();

	public long getStart();
	
	public Figure getRoot();

	public void init(TimeLineViewWrapper wrapper);

	public void resizeDiagram();
	
	public void drawSelection();
	
	public abstract void setActiveColorBG(Color activeColorBG);
	public abstract void setActiveColorFG(Color activeColorFG);
	public abstract void setActiveColorAlpha(int anAlphaValue);
	public abstract void setPotentialColorBG(Color selectedColorBG);
	public abstract void setPotentialColorFG(Color selectedColorFG);
	public abstract void setPotentialColorAlpha(int anAlphaValue);
		
}
