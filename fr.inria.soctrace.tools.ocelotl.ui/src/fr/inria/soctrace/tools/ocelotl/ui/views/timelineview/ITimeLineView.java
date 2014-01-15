package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.util.List;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public interface ITimeLineView {

	public void deleteDiagram();

	public Canvas initDiagram(final Composite parent);

	public void resizeDiagram();

	void createDiagram(List<Integer> parts, TimeRegion time, boolean aggregated, boolean numbers);
}
