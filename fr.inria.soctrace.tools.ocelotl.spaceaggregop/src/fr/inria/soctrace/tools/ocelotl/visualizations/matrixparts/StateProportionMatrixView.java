package fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.MatrixView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.parts.views.PartColorManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.stateproportion.StateProportion;

public class StateProportionMatrixView extends MatrixView {

	private PartColorManager colors;

	public StateProportionMatrixView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		colors = new PartColorManager();
	}

	@Override
	protected void computeDiagram() {
		if (!hierarchy.getRoot().getParts().isEmpty()) {
			HierarchyStateProportion hp=new HierarchyStateProportion(hierarchy, root, colors);
			hp.draw();

			

		}
	}

}
