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

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.SpaceTimeAggregationManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
abstract public class MatrixView extends AggregatedView implements IAggregatedView {

	protected EventProducerHierarchy	hierarchy;

	public MatrixView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		mouse = new SpatioTemporalMouseListener(this);
	}

	@Override
	abstract protected void computeDiagram();

	public void createDiagram(final EventProducerHierarchy hierarchy, final TimeRegion time) {
		root.removeAll();
		figures.clear();
		canvas.update();
		this.hierarchy = hierarchy;
		this.time = time;
		if (time != null) {
			resetTime = new TimeRegion(time);
			selectTime = new TimeRegion(time);
		}
		if (hierarchy != null)
			if (hierarchy.getRoot().getParts() != null) {
				computeDiagram();
			}
	}
	
	@Override
	public void createDiagram(final IDataAggregManager manager, final TimeRegion time, IVisuOperator aVisuOperator) {
		setVisuOperator(aVisuOperator);
		createDiagram(((SpaceTimeAggregationManager) manager).getHierarchy(), time);
	}

	public EventProducerHierarchy getHierarchy() {
		return hierarchy;
	}

	@Override
	public void resizeDiagram() {
		createDiagram(hierarchy, time);
		root.repaint();
	}

	public void setHierarchy(final EventProducerHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	protected abstract void computeDiagram(EventProducerNode aHierarchy, int start, int end);
}
