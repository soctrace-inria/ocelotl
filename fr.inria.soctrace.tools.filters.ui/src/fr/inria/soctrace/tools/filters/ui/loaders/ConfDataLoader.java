/* ===========================================================
 * Filter Tool UI module
 * ===========================================================
 *
 * (C) Copyright 2013 Generoso Pagano, Damien Dosimont. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 */
package fr.inria.soctrace.tools.filters.ui.loaders;

import java.util.List;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.search.utils.Printer;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;


/**
 * Convenience class to load Trace data related to
 * Filter tool configuration.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ConfDataLoader {
	
	private Trace currentTrace = null;
	private List<Trace> traces;
	private long minTimestamp;
	private long maxTimestamp;
	private TraceDBObject traceDB = null;
	
	/**
	 * The constructor.
	 */
	public ConfDataLoader() {
		clean();
	}
	
	public List<Trace> loadTraces() throws SoCTraceException {
		SystemDBObject sysDB = FramesocManager.getInstance().getSystemDB();
		TraceQuery tQuery = new TraceQuery(sysDB);
		traces = tQuery.getList();
		sysDB.close();
		return traces;		
	}
	
	/**
	 * Load the information related to the new Trace.
	 * 
	 * @param trace trace to consider
	 * @throws SoCTraceException
	 */
	public void load(final Trace trace) throws SoCTraceException {
		clean();
		currentTrace = trace;
		traceDB = new TraceDBObject(trace.getDbName(), DBMode.DB_OPEN);
		minTimestamp = Math.max(0, traceDB.getMinTimestamp());
		maxTimestamp = Math.max(0, traceDB.getMaxTimestamp());
		traceDB.close();
	}
	
	public Trace getCurrentTrace() {
		return currentTrace;
	} 
	
	public List<Trace> getTraces() {
		return traces;
	}

	public long getMinTimestamp() {
		return minTimestamp;
	}

	public long getMaxTimestamp() {
		return maxTimestamp;
	}
	
	private void clean() {
		currentTrace = null;
		minTimestamp = maxTimestamp = 0;
	}
	
	/**
	 * Debug print method
	 */
	public void print() {
		Printer.printTraceList(traces);
		System.out.println("min ts: " + minTimestamp);
		System.out.println("max ts: " + maxTimestamp);
	}
	
}
