package fr.inria.soctrace.tools.ocelotl.spaceaggregop.operators.stateproportion.views;

import fr.inria.soctrace.tools.ocelotl.spaceaggregop.operators.stateproportion.StateProportion;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;

public class StateProportionTimeLineView extends TimeLineView{

	public StateProportionTimeLineView(OcelotlView ocelotlView) {
		super(ocelotlView);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void computeDiagram() {
		if (parts != null) {
			while ((root.getSize().width - 2 * Border) / parts.size() - 2 < Space && Space != 0)
				Space = Space - 1;
			for (int i = 0; i < ocelotlView.getCore().getSpaceOperator().getPartNumber(); i++) {
				// TODO manage parts
				final MultiState part = new MultiState(i, (StateProportion) ocelotlView.getCore().getSpaceOperator(), root, Space);
				part.init();
			}
			
		}		
	}
	
}
