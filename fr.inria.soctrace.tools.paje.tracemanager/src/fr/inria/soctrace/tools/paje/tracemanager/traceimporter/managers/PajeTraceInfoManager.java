/* ===========================================================
 * Paje Trace Manager module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to import, export and
 * process Pajé trace files
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.paje.tracemanager.traceimporter.managers;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;

public class PajeTraceInfoManager {

	private final ImporterArgumentsManager	arguments;
	private final SystemDBObject			sysDB;
	private final TraceDBObject				traceDB;

	private Trace							trace;
	private TraceType						traceType;
	private final boolean					isTraceTypeExisting;

	/**
	 * @throws SoCTraceException
	 * 
	 */
	public PajeTraceInfoManager(final ImporterArgumentsManager arguments, final SystemDBObject sysDB, final TraceDBObject traceDB) throws SoCTraceException {
		this.arguments = arguments;
		this.sysDB = sysDB;
		this.traceDB = traceDB;
		isTraceTypeExisting = sysDB.isTraceTypePresent(PajeConstants.PajeFormatName);
	}

	/**
	 * Builds the Trace object.
	 * 
	 * @throws SoCTraceException
	 */
	private void buildTrace() throws SoCTraceException {

		trace = new Trace(sysDB.getNewId(FramesocTable.TRACE.toString(), "ID"));
		trace.setDbName(traceDB.getDBName());
		trace.setType(traceType);
		trace.setDescription(arguments.getTraceDescription() != null ? arguments.getTraceDescription() : "Paje trace imported on " + getCurrentDate());

		// unknown info: TODO (Damien) put correct values
		trace.setBoard("unknown");
		trace.setTracingDate(new Timestamp(1000));
		trace.setTracedApplication("unknown");
		trace.setOperatingSystem("unknown");
		trace.setNumberOfCpus(1);
		trace.setOutputDevice("unknown");

	}

	/**
	 * Builds the TraceType object.
	 * 
	 * @throws SoCTraceException
	 */
	private void buildTraceType() throws SoCTraceException {

		if (isTraceTypeExisting)
			traceType = sysDB.getTraceType(PajeConstants.PajeFormatName);
		else {
			traceType = new TraceType(sysDB.getNewId(FramesocTable.TRACE_TYPE.toString(), "ID"));
			traceType.setName(PajeConstants.PajeFormatName);

			// Sample code to add trace parameter types
			// TraceParamType tpt;
			// IdManager tptIdManager = new IdManager();
			// tptIdManager.setNextId(sysDB.getMaxId(STITables.TRACE_PARAM_TYPE.toString(),
			// "ID") + 1);
			// tpt = new TraceParamType(tptIdManager.getNextId());
			// tpt.setTraceType(traceType);
			// tpt.setName( /* TODO: put a value here */ );
			// tpt.setType( /* TODO: put a type here */ );

		}
	}

	/**
	 * Create the Trace and TraceType objects, with corresponding parameters.
	 * Note that if the trace type is already in the SysDB, the TraceType object
	 * is loaded from the DB, otherwise it is created.
	 */
	public void createTraceInfo() throws SoCTraceException {

		// Trace Type
		buildTraceType();

		// Trace
		buildTrace();

	}

	/**
	 * Get the current date.
	 * 
	 * @return a string with the current date
	 */
	private String getCurrentDate() {
		final SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
		sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
		return sdf.format(new Date()).toString();
	}

	/**
	 * @return the Trace
	 */
	public Trace getTrace() {
		return trace;
	}

	/**
	 * @return the TraceType
	 */
	public TraceType getTraceType() {
		return traceType;
	}

	/**
	 * Save the trace general information into the System DB. If the trace type
	 * was already present, is not saved again (avoid redundancy). Note: the
	 * method does not commit.
	 * 
	 * @throws SoCTraceException
	 */
	public void saveTraceInfo() throws SoCTraceException {

		if (!isTraceTypeExisting) {
			sysDB.save(traceType);
			for (final TraceParamType tpt : traceType.getTraceParamTypes())
				sysDB.save(tpt);
		}

		sysDB.save(trace);
		for (final TraceParam tp : trace.getParams())
			sysDB.save(tp);
	}

}
