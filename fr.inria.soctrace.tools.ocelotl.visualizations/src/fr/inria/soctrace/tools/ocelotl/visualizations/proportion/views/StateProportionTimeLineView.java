package fr.inria.soctrace.tools.ocelotl.visualizations.proportion.views;

import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.ISpaceTAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.Proportion;

public class StateProportionTimeLineView extends TimeLineView {

	public StateProportionTimeLineView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void computeDiagram() {
			for (int i = 0; i < ((ISpaceTAggregationOperator) ocelotlView.getCore().getSpaceOperator()).getPartNumber(); i++) {
				// TODO manage parts
				final MultiState part = new MultiState(i, (Proportion) ocelotlView.getCore().getSpaceOperator(), root, space);
				part.init();
			}
	}

}
