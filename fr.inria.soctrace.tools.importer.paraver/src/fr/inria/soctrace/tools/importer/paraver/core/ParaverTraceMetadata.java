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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

import fr.inria.soctrace.framesoc.core.tools.importers.AbstractTraceMetadataManager;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.tools.importer.pajedump.core.PJDumpConstants;
import fr.inria.soctrace.tools.importer.pajedump.core.PJDumpTraceMetadata;

/**
 * Class to manage Otf2 Trace metadata.
 * 
 * TODO: expose a setter for each trace metadata you want to explicitly set in
 * the parser.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ParaverTraceMetadata extends PJDumpTraceMetadata {

	private String dbName;
	private String alias;
	private int events;
	private long min;
	private long max;
	
	@Override
	public String getTraceTypeName() {
		return ParaverConstants.TRACE_TYPE;
	}
		
	public ParaverTraceMetadata(SystemDBObject sysDB, String dbName, String alias, int events, long min, long max) throws SoCTraceException {
	super(sysDB, dbName, alias, events, min, max);
	}
	
	@Override
	public void setTraceFields(Trace trace) {
		trace.setAlias(alias);
		trace.setDbName(dbName);
		trace.setDescription("paraver trace imported " + getCurrentDate());
		trace.setNumberOfCpus(1);
		trace.setNumberOfEvents(events);
		trace.setOutputDevice("paraver");
		trace.setProcessed(false);
		trace.setMinTimestamp(min);
		trace.setMaxTimestamp(max);
		trace.setTimeUnit(TimeUnit.NANOSECONDS.getInt());
		
		trace.setTracedApplication("unknown");
		trace.setBoard("unknown");
		trace.setOperatingSystem("unknown");
	}


}