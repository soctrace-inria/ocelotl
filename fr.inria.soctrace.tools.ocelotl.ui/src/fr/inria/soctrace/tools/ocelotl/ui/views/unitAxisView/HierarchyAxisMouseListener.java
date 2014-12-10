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
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// If left click
		if (arg0.button == 1) {
			Rectangle selectedZone = findProducer(arg0.x, arg0.y);
			
			// If none was found
			if (selectedZone == null)
				return;
			
			EventProducerNode selectedEpn = unitAxisView.getFigures().get(selectedZone);

			// Set the spatial selection to the selected epn
			((AggregatedView) unitAxisView.ocelotlView.getTimeLineView()).setSpatialSelection(selectedEpn);
			((AggregatedView) unitAxisView.ocelotlView.getTimeLineView()).setSelectTime(unitAxisView.ocelotlView.getOcelotlParameters().getTimeRegion());
			((AggregatedView) unitAxisView.ocelotlView.getTimeLineView()).drawSelection();
		}

		// If right click
		if (arg0.button == 3) {
			Rectangle selectedZone = findProducer(arg0.x, arg0.y);
			
			// If none was found
			if (selectedZone == null)
				return;
			
			EventProducerNode selectedEpn = unitAxisView.getFigures().get(selectedZone);
			
			// Draw highlight selection
			unitAxisView.getHighLightDisplayedProducer().draw(selectedZone.x, selectedZone.y, selectedZone.x() + selectedZone.width(), selectedZone.y() + selectedZone.height());

			// Trigger the display of the hierarchy
			new HierarchyView(selectedEpn).display(unitAxisView.getOcelotlView());
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * Find the clicked producer
	 * 
	 * @param x
	 *            the x coordinate value of the click
	 * @param y
	 *            the y coordinate value of the click
	 * @return the clicked zone
	 */
	protected Rectangle findProducer(int x, int y) {
		Point clickCoord = new Point(x, y);
		Rectangle selectedZone = null;
		// Find the corresponding epn
		for (Rectangle aZone : unitAxisView.getFigures().keySet()) {
			if (aZone.contains(clickCoord)) {
				selectedZone = aZone;
				break;
			}
		}

		return selectedZone;
	}

}