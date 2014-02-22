package fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.parts.views.PartColorManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.stateproportion.StateProportion;

public class StateProportionMatrixView extends TimeLineView {

	public StateProportionMatrixView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		PartColorManager colors = new PartColorManager();
	}

	@Override
	protected void computeDiagram() {
		if (parts != null) {
			while ((root.getSize().width - 2 * Border) / parts.size() - 2 < Space && Space != 0)
				Space = Space - 1;

			

		}
	}

}
