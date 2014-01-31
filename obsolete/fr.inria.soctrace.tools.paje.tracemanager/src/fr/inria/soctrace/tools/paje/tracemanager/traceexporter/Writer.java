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

package fr.inria.soctrace.tools.paje.tracemanager.traceexporter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;
import fr.inria.soctrace.tools.paje.tracemanager.traceexporter.managers.ExporterArgumentsManager;

class Writer {

	private final ExporterArgumentsManager	arguments;
	private final TraceDBObject				traceDB;
	public final static int					pageIncrement	= 6;

	public Writer(final ExporterArgumentsManager arguments, final TraceDBObject traceDB) {
		this.arguments = arguments;
		this.traceDB = traceDB;
	}

	public void exportTrace() throws IOException, SoCTraceException {
		final PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(arguments.getTraceFile())));
		writeHeader(bw);
		writeEvents(bw);
		bw.close();
	}

	private void writeEvents(final PrintWriter bw) throws IOException, SoCTraceException {
		int page = 0;
		List<Event> events = new ArrayList<Event>();
		do {
			events.clear();
			final EventQuery eventQuery = new EventQuery(traceDB);
			//eventQuery.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.EQ, Integer.toString(page++)));
			final LogicalCondition logic = new LogicalCondition(LogicalOperation.AND);
			logic.addCondition(new SimpleCondition("PAGE", ComparisonOperation.GE, Integer.toString(page)));
			//get 6 pages
			page = page + pageIncrement; // XXX do it better, please
			logic.addCondition(new SimpleCondition("PAGE", ComparisonOperation.LT, Integer.toString(page)));
			eventQuery.setElementWhere(logic);
			events = eventQuery.getList();
			for (final Event e : events) {
				final String tokens[] = e.getType().getName().split(PajeConstants.PajeIdNameSeparator);
				bw.write(tokens[0]);
				for (final EventParam ep : e.getEventParams()) {
					String temp = ep.getValue();
					if (temp.contains(" "))
						temp = "\"" + temp + "\"";
					bw.write(" " + temp);
				}
				bw.write("\n");
			}
		} while (events.size() > 0);
	}

	private void writeHeader(final PrintWriter bw) throws IOException, SoCTraceException {
		final EventTypeQuery eventTypeQuery = new EventTypeQuery(traceDB);
		final List<EventType> eventTypes = eventTypeQuery.getList();
		for (final EventType et : eventTypes) {
			final String tokens[] = et.getName().split(PajeConstants.PajeIdNameSeparator);
			bw.write(PajeConstants.PajeFormatStartEventDef + " " + tokens[1] + " " + tokens[0] + "\n");
			for (final EventParamType etp : et.getEventParamTypes())
				bw.write(PajeConstants.PajeFormatParamEventDef + "\t" + etp.getName() + " " + etp.getType() + "\n");
			bw.write(PajeConstants.PajeFormatEndEventDef + "\n");
		}
	}

}
