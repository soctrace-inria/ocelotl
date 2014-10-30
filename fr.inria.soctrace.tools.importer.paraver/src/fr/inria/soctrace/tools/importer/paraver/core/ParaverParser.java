/*******************************************************************************
 * Copyright (c) 2012-2014 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.tools.importer.paraver.core;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.Variable;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.tools.importer.paraver.reader.ParaverPrintWrapper;

/**
 * Otf2 Parser core class.
 * 
 * TODO
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ParaverParser {

	private static final Logger logger = LoggerFactory
			.getLogger(ParaverParser.class);

	private SystemDBObject sysDB;
	private TraceDBObject traceDB;
	private String traceFile;

	private Map<String, EventProducer> producersMap = new HashMap<String, EventProducer>();
	/**
	 * Event producers indexed by their in-trace id for easier access during
	 * parsing
	 */
	private Map<Integer, EventProducer> idProducersMap = new HashMap<Integer, EventProducer>();
	private Map<String, EventType> types = new HashMap<String, EventType>();
	private List<Event> eventList = new LinkedList<Event>();
	private int numberOfEvents = 0;
	private long minTimestamp = -1;
	private long maxTimestamp = -1;
	private int page = 0;

	// Starting time of the time stamp to avoid having huge timestamps
	private long timeOffset = 0;
	private long timeGranularity = -1;

	private Map<String, String> eventCategory = new HashMap<String, String>();

	/**
	 * Keep the current states for each event producer It must be able to hold
	 * several states since it is possible to have embedded states
	 */
	private HashMap<EventProducer, List<State>> stateMaps = new HashMap<EventProducer, List<State>>();
	/**
	 * Keep the communication links for each sending event producer
	 */
	private HashMap<EventProducer, List<Link>> linkMaps = new HashMap<EventProducer, List<Link>>();

	public ParaverParser(SystemDBObject sysDB, TraceDBObject traceDB,
			String traceFile) {
		this.traceFile = traceFile;
		this.sysDB = sysDB;
		this.traceDB = traceDB;
	}

	public String getTraceFile() {
		return traceFile;
	}

	/**
	 * Parser entry point.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws SoCTraceException
	 */
	public void parseTrace(IProgressMonitor monitor) throws SoCTraceException {

		logger.debug("Trace file: {}", traceFile);

		try {
			monitor.beginTask("Import trace " + traceFile, ParaverConstants.WORK);

			// parse
			boolean complete = parse(monitor);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Parse and build the actual events
	 * 
	 * @param monitor
	 * @return true if the process was not cancelled, false otherwise
	 * @throws SoCTraceException
	 */
	private boolean parse(IProgressMonitor monitor) throws SoCTraceException {
		try{
			List<String> args = new ArrayList<String>();
			args.add(getTraceFile());
			ParaverPrintWrapper wrapper = new ParaverPrintWrapper(args);
			BufferedReader br = wrapper.execute(monitor);
			
	} catch (Exception e) {
		throw new SoCTraceException(e);
	}
	return !monitor.isCanceled();

		}
	
	private void saveTraceMetadata(boolean partialImport)
			throws SoCTraceException {
		String alias = FilenameUtils.getBaseName(traceFile);
		String realAlias = (partialImport) ? (alias + " [part]") : alias;
		ParaverTraceMetadata metadata = new ParaverTraceMetadata(sysDB,
				traceDB.getDBName(), realAlias);
		metadata.setNumberOfEvents(numberOfEvents);
		metadata.setMinTimestamp(minTimestamp);
		metadata.setMaxTimestamp(maxTimestamp);
		metadata.setTimeUnit(metadata.getTimeUnit(timeGranularity));
		metadata.createMetadata();
		metadata.saveMetadata();
	}

}
