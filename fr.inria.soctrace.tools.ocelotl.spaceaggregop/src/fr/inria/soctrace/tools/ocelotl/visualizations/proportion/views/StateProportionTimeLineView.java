package fr.inria.soctrace.tools.ocelotl.visualizations.proportion.views;

import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.ISpaceAggregationOperator;
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
		if (parts != null) {
			while ((root.getSize().width - 2 * Border) / parts.size() - 2 < Space && Space != 0)
				Space = Space - 1;
			for (int i = 0; i < ((ISpaceTAggregationOperator) ocelotlView.getCore().getSpaceOperator()).getPartNumber(); i++) {
				// TODO manage parts
				final MultiState part = new MultiState(i, (Proportion) ocelotlView.getCore().getSpaceOperator(), root, Space);
				part.init();
			}

		}
	}

}
