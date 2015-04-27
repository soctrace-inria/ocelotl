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

package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.partition.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.config.temporalpartition.PartitionConfig;

public class TemporalPartitionView extends TimeLineView {

	private final PartitionColorManager colors = new PartitionColorManager();
	private final PartitionConfig config;

	public TemporalPartitionView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		this.config = (PartitionConfig) ocelotlView.getOcelotlParameters().getVisuConfig();
	}

	@Override
	protected void computeDiagram() {
		// Height of a part (roughly 90% percent of the display zone)
		final int partHeight = (int) (root.getSize().height / 1.1 - 2 * aBorder);
		final int drawingAreaWidth = root.getSize().width - 2 * aBorder;
		int i;
		// If no aggregation
		if (!config.isAggregated()) {
			for (i = 0; i < parts.size(); i++) {
				final PartFigure part = new PartFigure(i, parts.get(i), colors
						.getColors().get(
								parts.get(i) % colors.getColors().size()),
						config.isNumbers());
				figures.add(part);
				root.add(part, new Rectangle(new Point((i * drawingAreaWidth)
						/ parts.size() + aBorder, root.getSize().height / 2
						- partHeight / 2), new Point(((i + 1) * drawingAreaWidth)
						/ parts.size() + aBorder - space, root.getSize().height
						/ 2 + partHeight / 2)));
				part.getUpdateManager().performUpdate();
				part.init();
			}
		} else {
			final List<Integer> aggParts = new ArrayList<Integer>();
			for (i = 0; i <= parts.get(parts.size() - 1); i++)
				aggParts.add(0);
			for (i = 0; i < parts.size(); i++)
				aggParts.set(parts.get(i), aggParts.get(parts.get(i)) + 1);
			int j = 0;
			for (i = 0; i < aggParts.size(); i++) {
				// TODO manage parts
				final PartFigure part = new PartFigure(i, i, colors.getColors()
						.get(j % colors.getColors().size()), config.isNumbers());
				figures.add(part);
				root.add(part, new Rectangle(new Point((j * drawingAreaWidth)
						/ parts.size() + aBorder, root.getSize().height
						- aBorder), new Point(((j + aggParts.get(i))
						* drawingAreaWidth) / parts.size() - space + aBorder,
						aBorder)));
				j = j + aggParts.get(i);
				part.getUpdateManager().performUpdate();
				part.init();
			}
		}
	}

}
