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

package fr.inria.soctrace.tools.ocelotl.visualizations.parts.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.parts.config.PartsConfig;

public class PartTimeLineView extends TimeLineView {

	private final PartColorManager	colors	= new PartColorManager();
	private final PartsConfig config;

	public PartTimeLineView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		this.config=(PartsConfig) ocelotlView.getParams().getSpaceConfig();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void computeDiagram() {
		// Height of a part (roughly 90% percent of the display zone)
		final int partHeight = (int) (root.getSize().height / 1.1 - 2* Border);
		int i;
		// if no aggregation
		if (!config.isAggregated())	{
			for (i = 0; i < parts.size(); i++) {
				final PartFigure part = new PartFigure(i, parts.get(i), colors.getColors().get(parts.get(i) % colors.getColors().size()), config.isNumbers());
				figures.add(part);
				root.add(part, new Rectangle(new Point(i * (root.getSize().width - 2 * Border) / parts.size() + Border, root.getSize().height / 2 - partHeight / 2), new Point((i + 1) * (root.getSize().width - 2 * Border) / parts.size() + Border - space,
						root.getSize().height / 2 + partHeight / 2)));
				part.getUpdateManager().performUpdate();
				part.init();
			}
		}
		else {
			final List<Integer> aggParts = new ArrayList<Integer>();
			for (i = 0; i <= parts.get(parts.size() - 1); i++)
				aggParts.add(0);
			for (i = 0; i < parts.size(); i++)
				aggParts.set(parts.get(i), aggParts.get(parts.get(i)) + 1);
			int j = 0;
			for (i = 0; i < aggParts.size(); i++) {
				// TODO manage parts
				final PartFigure part = new PartFigure(i, i, colors.getColors().get(j % colors.getColors().size()), config.isNumbers());
				figures.add(part);
				root.add(
						part,
						new Rectangle(new Point(j * (root.getSize().width - 2 * Border) / parts.size() + Border, root.getSize().height - Border), new Point((j + aggParts.get(i)) * (root.getSize().width - 2 * Border) / parts.size() - space + Border, Border)));
				j = j + aggParts.get(i);
				part.getUpdateManager().performUpdate();
				part.init();
				}
			}
		}

}
