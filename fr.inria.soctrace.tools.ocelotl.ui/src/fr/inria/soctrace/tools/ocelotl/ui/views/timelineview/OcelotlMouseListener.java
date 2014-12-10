package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView.MouseState;

public abstract class OcelotlMouseListener implements MouseListener, MouseMotionListener {

	protected static final long	Threshold	= 5;
	protected MouseState		state		= MouseState.RELEASED;
	protected MouseState		previous	= MouseState.RELEASED;
	protected Point				currentPoint;
	protected AggregatedView	aggregatedView;
	protected Display			display		= Display.getCurrent();
	protected Shell				shell		= display.getActiveShell();
	protected long				fixed;

	public abstract void drawSelection();
	
	public void setSpatialSelection(EventProducerNode selectedNode) {}
}
