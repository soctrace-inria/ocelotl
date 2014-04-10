package fr.inria.soctrace.tools.ocelotl.visualizations.matrixproportion;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.MatrixView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.matrixparts.HierarchyPart;
import fr.inria.soctrace.tools.ocelotl.visualizations.parts.views.PartColorManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.Proportion;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.config.ProportionConfig;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.views.StateColorManager;

public class ProportionMatrixView extends MatrixView {

	private MatrixProportion proportion;
	
	public ProportionMatrixView(final OcelotlView ocelotlView) {
		super(ocelotlView);
	}

	@Override
	protected void computeDiagram() {
		if (!hierarchy.getRoot().getParts().isEmpty()) {
			proportion=(MatrixProportion) ocelotlView.getCore().getSpaceOperator();
			HierarchyProportion hp=new HierarchyProportion(proportion, hierarchy, root, space);
			hp.draw();

			

		}
	}

}
