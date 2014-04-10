package fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts.views;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.MatrixView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts.HierarchyPart;
import fr.inria.soctrace.tools.ocelotl.visualizations.parts.views.PartColorManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.Proportion;

public class PartMatrixView extends MatrixView {

	private PartColorManager colors;

	public PartMatrixView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		colors = new PartColorManager();
	}

	@Override
	protected void computeDiagram() {
		if (!hierarchy.getRoot().getParts().isEmpty()) {
			HierarchyPart hp=new HierarchyPart(hierarchy, root, colors, space);
			hp.draw();

			

		}
	}

}
