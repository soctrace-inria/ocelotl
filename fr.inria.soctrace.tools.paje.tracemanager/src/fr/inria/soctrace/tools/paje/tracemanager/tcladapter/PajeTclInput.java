package fr.inria.soctrace.tools.paje.tracemanager.tcladapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.framesoc.ui.tcl.ITChartsInput;
import fr.inria.soctrace.framesoc.ui.tcl.ITChartsRow;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.storage.TraceDBObject;
/**
 * Paje implementation of Tcl input.
 * The input contains the Tcl rows.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PajeTclInput implements ITChartsInput {

	private ArrayList<ITChartsRow> mainItems = new ArrayList<ITChartsRow>();
	private boolean startTimeSet = false;
	private long startTime;
	private boolean endTimeSet = false;
	private long endTime;
	
	/**
	 * Debug flag
	 */
	public final static boolean DEBUG = false;
	
	@Override
	public void addTChartsRow(ITChartsRow main) {
		mainItems.add(main);
	}

	@Override
	public ArrayList<ITChartsRow> getTChartsRows() {
		return mainItems;
	}

	@Override
	public long getStartTime() {
		if (startTimeSet)
			return startTime;
		
		startTime = mainItems.get(0).getStartTime();
		long time;
		for(ITChartsRow item : mainItems) {
			time = item.getStartTime();
			if(time < startTime)
				startTime = time;
		}
		startTimeSet = true;
		return startTime;
	}

	@Override
	public long getEndTime() {
		if (endTimeSet)
			return endTime;
		
		endTime = mainItems.get(0).getEndTime();
		long time;
		for(ITChartsRow item : mainItems) {
			time = item.getEndTime();
			if(time > endTime)
				endTime = time;
		}
		endTimeSet = true;
		return endTime;
	}

	@Override
	public void loadPage(Trace trace, List<Event> elist) {
		
		// load all producers
		Map<Integer, EventProducer> eps = new HashMap<Integer, EventProducer>();
		try {
			TraceDBObject traceDB = TraceDBObject.openNewIstance(trace.getDbName());
			EventProducerQuery query = new EventProducerQuery(traceDB);
			List<EventProducer> producers = query.getList();
			for (EventProducer ep: producers) {
				eps.put(ep.getId(), ep);
			}
			traceDB.close();
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		
		//  CPU          EP id
		Map<Integer, Map<Integer, ITChartsRow>> rows = new HashMap<Integer, Map<Integer, ITChartsRow>>();
		//  CPU      row
		Map<Integer, ITChartsRow> main = new HashMap<Integer, ITChartsRow>();
		
		// iterate over all page events 
		for (Event e: elist) {
			// get the map containing all the rows for this CPU
			if (!rows.containsKey(e.getCpu())) {
				rows.put(e.getCpu(), new HashMap<Integer, ITChartsRow>());
				main.put(e.getCpu(), new PajeTclRow("CPU " + e.getCpu(), null, null, e.getCpu()));
				this.addTChartsRow(main.get(e.getCpu()));
			}
			Map<Integer, ITChartsRow> cpuMap = rows.get(e.getCpu());
			
			// get the row for the given producer
			if (!cpuMap.containsKey(e.getEventProducer().getId()))
				cpuMap.put(e.getEventProducer().getId(), getNewEventProducerRow(e.getEventProducer(), eps, cpuMap, main.get(e.getCpu())));		
			ITChartsRow producerRow = cpuMap.get(e.getEventProducer().getId()); 
			
			// finally add the event
			producerRow.addEvent(new PajeTclEvent(e.getTimestamp()));			
		}
	}
	
	private ITChartsRow getNewEventProducerRow(EventProducer ep, Map<Integer, EventProducer> eps, Map<Integer, ITChartsRow> cpuRows, ITChartsRow cpuRow) {
		
		debug("Creating event producer row " + ep.getId() + ", parent " + ep.getParentId());
		
		ITChartsRow parentRow = cpuRow;
		// if there's a parent
		if (ep.getParentId()!=EventProducer.NO_PARENT_ID) {
			// if there is already its row
			if (cpuRows.containsKey(ep.getParentId()))
				parentRow = cpuRows.get(ep.getParentId());
			else {
				parentRow = getNewEventProducerRow(eps.get(ep.getParentId()), eps, cpuRows, cpuRow);
				cpuRows.put(ep.getParentId(), parentRow);
			}
		}
		return new PajeTclRow(ep.getName(), null, parentRow, ep.getId());
	}

	@Override
	public void clear() {
		mainItems.clear();
		startTimeSet = false;
		endTimeSet = false;
	}
	
	/**
	 * Print a debug message
	 * @param s message
	 */
	private void debug(String s) {
		if (DEBUG)
			System.out.println("[Default Input] "+ s);
	}

}