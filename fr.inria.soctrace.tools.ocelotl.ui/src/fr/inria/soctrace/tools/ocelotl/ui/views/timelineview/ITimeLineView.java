package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public interface ITimeLineView {

	void createDiagram(List<Integer> parts, TimeRegion time);

	public void deleteDiagram();

	public void init(TimeLineViewWrapper wrapper);

	public void resizeDiagram();
}
