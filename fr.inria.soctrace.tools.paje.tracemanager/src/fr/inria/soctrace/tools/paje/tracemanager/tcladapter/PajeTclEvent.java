package fr.inria.soctrace.tools.paje.tracemanager.tcladapter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.graphics.Color;

import fr.inria.soctrace.framesoc.ui.tcl.ITChartsEvent;

/**
 * Paje implementation of Tcl Event
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PajeTclEvent implements ITChartsEvent {

	private final static int DEFAULT_EVENT_HEIGHT = 30;
	
	private final IFigure figure;
	private final long startTime;
	private final long endTime;
	private List<ITChartsEvent> outlinkedEvents;
	
	public PajeTclEvent(long timestamp) {
		this.figure = createRect(ColorConstants.blue, DEFAULT_EVENT_HEIGHT);
		this.startTime = timestamp;
		this.endTime = timestamp;
		this.outlinkedEvents = new ArrayList<ITChartsEvent>();
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public IFigure getFigure() {
		return figure;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public long getEndTime() {
		return endTime;
	}

	@Override
	public List<ITChartsEvent> getOutlinkedEvents() {
		return outlinkedEvents;
	}

	@Override
	public void setOutlinkedEvents(List<ITChartsEvent> outlinkedEvents) {
		this.outlinkedEvents = outlinkedEvents;
	}
	
	@Override
	public void addOutlinkedEvent(ITChartsEvent event) {
		outlinkedEvents.add(event);
	}
	
	private RectangleFigure createRect(Color color, int height) {
		RectangleFigure rectangle = new RectangleFigure();	
		rectangle.setLayoutManager(new BorderLayout());
		rectangle.setBackgroundColor(color);
		rectangle.setOpaque(true);
		rectangle.setSize(0, height);
		return rectangle;
	}
	
}