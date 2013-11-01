
package fr.inria.soctrace.tools.paje.tracemanager.tcladapter;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.st.tchartslite.viewer.ITChartsLabelProvider;
import com.st.tchartslite.viewer.figures.BorderAnchor;

import fr.inria.soctrace.framesoc.ui.tcl.ITChartsEvent;
import fr.inria.soctrace.framesoc.ui.tcl.ITChartsRow;

public class PajeTclLabelProvider extends LabelProvider implements ITChartsLabelProvider {

	private final int	MARGIN	= 20;

	@Override
	public void dispose() {
		// Nothing to do
	}

	@Override
	public Connection getConnection(final Object sourceEvent, final Object destEvent) {
		if (sourceEvent instanceof ITChartsEvent && destEvent instanceof ITChartsEvent)
			// If this link actually exists
			if (((ITChartsEvent) sourceEvent).getOutlinkedEvents().contains(destEvent)) {
				// We create the figure (Connection) corresponding to the link
				final PolylineConnection c = new PolylineConnection();
				if (((ITChartsEvent) destEvent).getStartTime() - ((ITChartsEvent) sourceEvent).getEndTime() > 1000) {
					final PolygonDecoration decoration = new PolygonDecoration();
					final PointList decorationPointList = new PointList();
					decorationPointList.addPoint(0, 0);
					decorationPointList.addPoint(-2, 2);
					decorationPointList.addPoint(-4, 0);
					decorationPointList.addPoint(-2, -2);
					decoration.setTemplate(decorationPointList);
					c.setSourceDecoration(decoration);

					c.setSourceAnchor(new BorderAnchor(((ITChartsEvent) sourceEvent).getFigure()));
					c.setTargetAnchor(new BorderAnchor(((ITChartsEvent) destEvent).getFigure()));
				}

				// And we return it
				return c;
			}
		return null;
	}

	@Override
	public IFigure getFigure(final Object event) {
		return ((ITChartsEvent) event).getFigure();
	}

	@Override
	public Image getImage(final Object element) {
		return ((ITChartsRow) element).getImg();
	}

	@Override
	public int getRowHeight(final Object element) {
		if (element instanceof ITChartsRow)
			return ((ITChartsRow) element).getMaxEventHeight() + MARGIN;
		else if (element instanceof ITChartsEvent)
			return ((ITChartsEvent) element).getFigure().getSize().height + MARGIN;
		else
			return MARGIN;
	}

	@Override
	public String getText(final Object element) {
		String name = "";

		if (element instanceof ITChartsRow)
			name = ((ITChartsRow) element).getName();
		else if (element instanceof ITChartsEvent)
			name = ((ITChartsEvent) element).getName();

		return name;
	}

}
