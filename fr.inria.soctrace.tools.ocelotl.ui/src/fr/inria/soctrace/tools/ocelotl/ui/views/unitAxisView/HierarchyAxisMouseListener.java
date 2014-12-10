package fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;


public class HierarchyAxisMouseListener extends YAxisMouseListener {
	
	public HierarchyAxisMouseListener(UnitAxisView aUnitAxisView) {
		super(aUnitAxisView);
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseHover(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseDoubleClicked(MouseEvent arg0) {
			EventProducerNode selectedEpn = findProducer(arg0.x, arg0.y);

			// If none was found
			if (selectedEpn == null)
				return;
			
			
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// If left click
		if (arg0.button == 1) {
			EventProducerNode selectedEpn = findProducer(arg0.x, arg0.y);

			// If none was found
			if (selectedEpn == null)
				return;

			// Set the spatial selection to the selected epn
			((AggregatedView) unitAxisView.ocelotlView.getTimeLineView()).setSpatialSelection(selectedEpn);
			((AggregatedView) unitAxisView.ocelotlView.getTimeLineView()).setSelectTime(unitAxisView.ocelotlView.getOcelotlParameters().getTimeRegion());
			((AggregatedView) unitAxisView.ocelotlView.getTimeLineView()).drawSelection();
		}

		// If right click
		if (arg0.button == 3) {
			HierarchyView seletedView = findHierarchy(arg0.x, arg0.y);

			// If none was found
			if (seletedView == null)
				return;

			// Trigger the display of the hierarchy
			seletedView.display(unitAxisView.getOcelotlView());
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	protected EventProducerNode findProducer(int x, int y) {
		Point clickCoord = new Point(x, y);
		EventProducerNode selectedEpn = null;

		// Find the corresponding epn
		for (Rectangle aZone : unitAxisView.getFigures().keySet()) {
			if (aZone.contains(clickCoord)) {
				selectedEpn = unitAxisView.getFigures().get(aZone);
				break;
			}
		}

		return selectedEpn;
	}
	
	protected HierarchyView findHierarchy(int x, int y) {
		Point clickCoord = new Point(x, y);
		HierarchyView selectedHierarchy = null;

		// Find the corresponding epn
		for (HierarchyView aHierarchy : unitAxisView.getSubHierarchies()) {
			if (aHierarchy.getAggregateZone().contains(clickCoord)) {
				selectedHierarchy = aHierarchy;
				break;
			}
		}

		return selectedHierarchy;
	}

}
