package fr.inria.soctrace.tools.ocelotl.visualizations.operators.parts.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.operators.parts.config.PartsConfig;

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
		final int partHeight = (int) (root.getSize().height / 1.1 - Border);
		if (parts != null) {
			while ((root.getSize().width - 2 * Border) / parts.size() - 2 < Space && Space != 0)
				Space = Space - 1;
			if (!config.isAggregated())
				for (int i = 0; i < parts.size(); i++) {
					final PartFigure part = new PartFigure(i, parts.get(i), colors.getColors().get(parts.get(i) % colors.getColors().size()), config.isNumbers());
					figures.add(part);
					root.add(part, new Rectangle(new Point(i * (root.getSize().width - 2 * Border) / parts.size() + Border, root.getSize().height / 2 - partHeight / 2), new Point((i + 1) * (root.getSize().width - 2 * Border) / parts.size() + Border - Space,
							root.getSize().height / 2 + partHeight / 2)));
					part.getUpdateManager().performUpdate();
					part.init();
				}
			else {
				final List<Integer> aggParts = new ArrayList<Integer>();
				for (int i = 0; i <= parts.get(parts.size() - 1); i++)
					aggParts.add(0);
				for (int i = 0; i < parts.size(); i++)
					aggParts.set(parts.get(i), aggParts.get(parts.get(i)) + 1);
				int j = 0;
				for (int i = 0; i < aggParts.size(); i++) {
					// TODO manage parts
					final PartFigure part = new PartFigure(i, i, colors.getColors().get(j % colors.getColors().size()), config.isNumbers());
					figures.add(part);
					root.add(
							part,
							new Rectangle(new Point(j * (root.getSize().width - 2 * Border) / parts.size() + Border, root.getSize().height), new Point((j + aggParts.get(i)) * (root.getSize().width - 2 * Border) / parts.size() - Space + Border, 0 + root
									.getSize().height / 10)));
					j = j + aggParts.get(i);
					part.getUpdateManager().performUpdate();
					part.init();
				}
			}
		}

	}

}
