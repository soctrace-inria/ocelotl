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

import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.time.TimeAggregationManager;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;


/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
abstract public class TimeLineView extends AggregatedView implements IAggregatedView {

	protected List<Integer>	parts	= null;

	public TimeLineView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		mouse = new TemporalMouseListener(this);
	}

	@Override
	abstract protected void computeDiagram();

	public void createDiagram(IDataAggregManager manager, TimeRegion time, IVisuOperator aVisuOperator) {
		setVisuOperator(aVisuOperator);
		createDiagram(((TimeAggregationManager) manager).getParts(), time);
	}
	
	public void createDiagram(final List<Integer> parts, final TimeRegion time) {
		root.removeAll();
		figures.clear();
		canvas.update();
		this.parts = parts;
		this.time = time;
		if (time != null) {
			resetTime = new TimeRegion(time);
			selectTime = new TimeRegion(time);
		}
		if (parts != null) {
			computeDiagram();
		}
	}

	public List<Integer> getParts() {
		return parts;
	}

	@Override
	public void resizeDiagram() {
		root.removeAll();
		figures.clear();
		canvas.update();
		if (parts != null) {
			computeDiagram();
		}
		root.repaint();
		drawSelection();
	}

}
