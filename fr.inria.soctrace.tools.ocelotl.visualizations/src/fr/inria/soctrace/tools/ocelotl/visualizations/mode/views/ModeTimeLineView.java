package fr.inria.soctrace.tools.ocelotl.visualizations.mode.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.IMicroDescManager;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.SpaceTimeAggregationManager;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.TimeAggregationManager;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.matrixproportion.MajState;
import fr.inria.soctrace.tools.ocelotl.visualizations.mode.Mode;
import fr.inria.soctrace.tools.ocelotl.visualizations.mode.SpaceTimeMode;
import fr.inria.soctrace.tools.ocelotl.visualizations.mode.TimeMode;

public class ModeTimeLineView extends TimeLineView {

	public ModeTimeLineView(OcelotlView ocelotlView) {
		super(ocelotlView);
		 space = 1;
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
					* (root.getSize().width - 2 * Border) / parts.size()
					+ Border, root.getSize().height - Border), new Point(
					(j + aggParts.get(i)) * (root.getSize().width - 2 * Border)
							/ parts.size() - space + Border, Border)));
			j = j + aggParts.get(i);
			part.getUpdateManager().performUpdate();
			MajState mState = ((Mode) spaceOperator).getMajStates().get(i);
			part.draw(mState);
		}
	}

	@Override
	public void createDiagram(final IMicroDescManager manager, final TimeRegion time) {
		if (SpaceTimeAggregationManager.class.isAssignableFrom(manager.getClass())) {
			createDiagram(((SpaceTimeAggregationManager) manager).getHierarchy().getRoot().getParts(), time);
		} else if (TimeAggregationManager.class.isAssignableFrom(manager.getClass())) {
			createDiagram(((TimeAggregationManager) manager).getParts(), time);
		}
	}
	
}
