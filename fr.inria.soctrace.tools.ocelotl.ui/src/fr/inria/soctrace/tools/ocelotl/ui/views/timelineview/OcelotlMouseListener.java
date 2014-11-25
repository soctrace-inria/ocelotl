package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;

public abstract class OcelotlMouseListener implements MouseListener, MouseMotionListener {
	public abstract void drawSelection();
}
