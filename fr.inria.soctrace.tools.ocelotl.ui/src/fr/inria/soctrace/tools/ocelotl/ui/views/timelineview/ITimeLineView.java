package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.util.List;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public interface ITimeLineView {
	
	public void createDiagram(final List<Integer> parts, final TimeRegion time, final boolean aggregated, final boolean numbers);
	public Canvas initDiagram(final Composite parent);
	public void deleteDiagram();
	public void resizeDiagram();
}
