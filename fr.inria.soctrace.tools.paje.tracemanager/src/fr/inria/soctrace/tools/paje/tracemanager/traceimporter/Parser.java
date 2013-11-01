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

package fr.inria.soctrace.tools.paje.tracemanager.traceimporter;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;
import fr.inria.soctrace.tools.paje.tracemanager.traceimporter.managers.ImporterArgumentsManager;
import fr.inria.soctrace.tools.paje.tracemanager.traceimporter.managers.PajeEventManager;
import fr.inria.soctrace.tools.paje.tracemanager.traceimporter.managers.PajeEventTypeManager;
import fr.inria.soctrace.tools.paje.tracemanager.traceimporter.managers.PajeTraceInfoManager;

/**
 * PajeTraceImporter parser
 */
public class Parser {

	/**
	 * Importer arguments
	 */
	private final ImporterArgumentsManager	arguments;

	/**
	 * System DB Object (the importer has no ownership on it)
	 */
	private final SystemDBObject			sysDB;

	/**
	 * Trace DB Object (the importer has no ownership on it)
	 */
	private final TraceDBObject				traceDB;

	/**
	 * Event number to reach before saving into the traceDB
	 */
	private static final int				eventNumberToSave		= 20000;
	/**
	 * pajeEventTypesManager
	 */
	private final PajeEventTypeManager		pajeEventTypeManager	= new PajeEventTypeManager();
	/**
	 * pajeEventsManager
	 */
	private final PajeEventManager			pajeEventManager		= new PajeEventManager(pajeEventTypeManager);

	/**
	 * The constructor.
	 */
	public Parser(final ImporterArgumentsManager arguments, final SystemDBObject sysDB, final TraceDBObject traceDB) {
		this.arguments = arguments;
		this.sysDB = sysDB;
		this.traceDB = traceDB;
	}

	/**
	 * create a BufferedReader table containing each file passed in argument
	 * TODO catch the exception
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	private BufferedReader getFileReaders(final int i) throws FileNotFoundException {

		BufferedReader br;
		final FileInputStream fstream = new FileInputStream(arguments.getTraceFiles().get(i));
		final DataInputStream in = new DataInputStream(fstream);
		br = new BufferedReader(new InputStreamReader(in));
		return br;
	}

	/**
	 * call parsing/saving methods
	 * 
	 * @throws SoCTraceException
	 */
	public void importTrace(final int i) throws SoCTraceException {

		try {

			// create trace general info
			final PajeTraceInfoManager pajeTraceInfoManager = new PajeTraceInfoManager(arguments, sysDB, traceDB);
			pajeTraceInfoManager.createTraceInfo();
			pajeTraceInfoManager.saveTraceInfo();

			parseEventDef(getFileReaders(i));
			pajeEventTypeManager.save(traceDB);
			parseEvent(getFileReaders(i));
			pajeEventManager.saveEventProducer(traceDB);

		} catch (final Exception e) {
			throw new SoCTraceException(e);
		}

	}

	/**
	 * parse the second part of the trace (events) TODO catch IOException
	 * 
	 * @param br
	 * @throws IOException
	 * @throws SoCTraceException
	 */
	private void parseEvent(final BufferedReader br) throws IOException, SoCTraceException {
		String strLine = "";
		String currentPajeId = "";
		while ((strLine = br.readLine()) != null) {
			strLine = strLine.trim();
			// Comments
			if (strLine.startsWith(PajeConstants.PajeFormatComment))
				continue;
			// Trace beginning
			if (strLine.startsWith(PajeConstants.PajeFormatParamEventDef))
				continue;
			// Empty line
			if (strLine.equals(""))
				continue;
			// Event
			else {// TODO manage error case : non valid line (if first arg is
					// not a number for instance)
				final String[] tokens = strLine.split(" (?=[^\"]*(\"[^\"]*\"[^\"]*)*$)");
				// TODO put this horror somewhere (and check if it works good)
				currentPajeId = tokens[0];
				final List<String> parameters = new ArrayList<String>();
				for (int i = 1; i < tokens.length; i++) {
					tokens[i] = tokens[i].replace("\"", "");
					parameters.add(tokens[i]);
				}
				pajeEventManager.createEvent(currentPajeId, parameters);
				// save when eventNumberToSave is reached and reallocate to
				// force garbage collector and avoid MEMORY explosion...
				if (pajeEventManager.getEvents().size() >= eventNumberToSave) {
					pajeEventManager.save(traceDB);
					pajeEventManager.reallocateEvents();
				}
			}

		}
		// do it a last time
		pajeEventManager.save(traceDB);
		pajeEventManager.reallocateEvents();
	}

	/**
	 * parse the first part of the trace (header = EventDef) to get Event Types
	 * and their associate Param Types
	 * 
	 * @param br
	 * @throws IOException
	 */
	private void parseEventDef(final BufferedReader br) throws IOException {
		String strLine = "";
		String currentPajeId = "";
		while ((strLine = br.readLine()) != null) {
			strLine = strLine.trim();
			// Comments
			if (strLine.startsWith(PajeConstants.PajeFormatComment))
				continue;
			// Empty line
			if (strLine.equals(""))
				continue;
			// Event definition
			if (strLine.startsWith(PajeConstants.PajeFormatStartEventDef)) {
				// TODO constant
				final String[] tokens = strLine.split(" ");
				currentPajeId = tokens[2];
				pajeEventTypeManager.addPajeEventType(tokens[1], currentPajeId);
			}
			// End definition
			else if (strLine.startsWith(PajeConstants.PajeFormatEndEventDef))
				currentPajeId = "";
			else if (strLine.startsWith(PajeConstants.PajeFormatParamEventDef) && currentPajeId != "") {// TODO
																										// better
																										// management
																										// of
																										// error
																										// case
				// TODO does it work good for tabs space and cie mixing?
				final String[] tokens = strLine.split("\\s+");
				pajeEventTypeManager.addPajeEventParamType(currentPajeId, tokens[1], tokens[2]);
			} else
				return;

		}
	}

}
