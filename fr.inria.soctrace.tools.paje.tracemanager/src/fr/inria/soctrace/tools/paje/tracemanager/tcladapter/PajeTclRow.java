package fr.inria.soctrace.tools.paje.tracemanager.tcladapter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import fr.inria.soctrace.framesoc.ui.tcl.ITChartsEvent;
import fr.inria.soctrace.framesoc.ui.tcl.ITChartsRow;

/**
 * Paje implementation for Tcl Row.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PajeTclRow implements ITChartsRow {
	
	private final String name;
	private final Image img;
	private ITChartsRow parent;
	private ArrayList<ITChartsRow> items;
	private List<ITChartsEvent> events;
	private boolean startTimeSet = false;
	private long startTime;
	private boolean endTimeSet = false;
	private long endTime;
	private int order;
	
	private final static int DEFAULT_ROW_HEIGHT = 15;
	
	public PajeTclRow(String name, Image img, ITChartsRow parent, int order) {
		this.name = "paje_" + name; // XXX just to test we are using the correct model 
		this.img = img;
		this.parent = parent;
		this.items = new ArrayList<ITChartsRow>();
		this.events = new ArrayList<ITChartsEvent>();
		
		if(parent != null)
			getParent().addChildRow(this);
		
		this.order = order;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Image getImg() {
		return img;
	}

	@Override
	public ITChartsRow getParent() {
		return parent;
	}

	@Override
	public long getStartTime() {
		if (!startTimeSet) {
			setStartTime();
			startTimeSet = true;
		}
		return startTime;
	}

	@Override
	public long getEndTime() {
		if (!endTimeSet) {
			setEndTime();
			endTimeSet = true;
		}
		return endTime;
	}

	@Override
	public ArrayList<ITChartsRow> getChildrenRows() {
		return items;
	}

	@Override
	public void setChildrenRows(ArrayList<ITChartsRow> rows) {
		this.items = rows;
		setStartTime();
		setEndTime();
	}
	
	@Override
	public void addChildRow(ITChartsRow row) {
		items.add(row);
	}
	
	@Override
	public List<ITChartsEvent> getEvents() {
		return events;
	}

	@Override
	public void setEvents(List<ITChartsEvent> events) {
		this.events = events;
	}
	
	@Override
	public void addEvent(ITChartsEvent event) {
		events.add(event);
	}
	
	@Override
	public int getPosition() {
		return order;
	}
	
	@Override
	public int getMaxEventHeight() {
		return DEFAULT_ROW_HEIGHT;
	}
	
	/*
	 *  utilities
	 */
	
	private void setStartTime() {
		if(!events.isEmpty()) {
			long eventStartTime;
			startTime = events.get(0).getStartTime();
			
			for(ITChartsEvent event : events) {
				eventStartTime = event.getStartTime();
				if(eventStartTime < startTime)
					startTime = eventStartTime;
			}
		}
		
		if(!items.isEmpty()) {
			long itemStartTime;
			if(events.isEmpty())
				startTime = items.get(0).getStartTime();
			
			for(ITChartsRow item : items) {
				itemStartTime = item.getStartTime();
				if(itemStartTime < startTime)
					startTime = itemStartTime;
			}
		}
	}
	
	private void setEndTime() {
		if(!events.isEmpty()) {
			long eventEndTime;
			endTime = events.get(0).getEndTime();
			
			for(ITChartsEvent event : events) {
				eventEndTime = event.getEndTime();
				if(eventEndTime > endTime)
					endTime = eventEndTime;
			}
		}
		
		if(!items.isEmpty()) {
			long itemEndTime;
			if(events.isEmpty())
				endTime = items.get(0).getEndTime();
			
			for(ITChartsRow item : items) {
				itemEndTime = item.getEndTime();
				if(itemEndTime > endTime)
					endTime = itemEndTime;
			}
		}
	}
	
}