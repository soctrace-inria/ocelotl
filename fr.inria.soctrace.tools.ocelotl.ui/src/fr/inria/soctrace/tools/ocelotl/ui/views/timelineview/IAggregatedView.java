package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.IMicroDescManager;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public interface IAggregatedView {

	void createDiagram(IMicroDescManager iMicroDescManager, TimeRegion time);

	public void deleteDiagram();

	public void init(TimeLineViewWrapper wrapper);

	public void resizeDiagram();
	
	public long getStart();
	
	public long getEnd();

}
