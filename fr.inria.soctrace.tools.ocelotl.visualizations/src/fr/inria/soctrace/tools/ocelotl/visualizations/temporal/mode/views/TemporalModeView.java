package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.mode.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.MainState;
import fr.inria.soctrace.tools.ocelotl.visualizations.temporal.mode.TemporalMode;

public class TemporalModeView extends TimeLineView {

	public TemporalModeView(OcelotlView ocelotlView) {
		super(ocelotlView);
	}

	@Override
	protected void computeDiagram() {
		int i;
		final List<Integer> aggParts = new ArrayList<Integer>();
		for (i = 0; i <= parts.get(parts.size() - 1); i++)
			aggParts.add(0);
		for (i = 0; i < parts.size(); i++)
			aggParts.set(parts.get(i), aggParts.get(parts.get(i)) + 1);
		int j = 0;

		for (i = 0; i < aggParts.size(); i++) {
			final ModeFigure part = new ModeFigure();
			figures.add(part);
			root.add(part, new Rectangle(new Point(j
					* (root.getSize().width - 2 * aBorder) / parts.size()
					+ aBorder, root.getSize().height - aBorder), new Point(
					(j + aggParts.get(i)) * (root.getSize().width - 2 * aBorder)
							/ parts.size() - space + aBorder, aBorder)));
			j = j + aggParts.get(i);
			part.getUpdateManager().performUpdate();
			MainState mState = ((TemporalMode) visuOperator).getMajStates().get(i);
			part.draw(mState);
		}
	}

}
