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

	private ExporterArgumentsManager	arguments;
	private TraceDBObject				traceDB;
	public final static int				pageIncrement	= 6;

	public Writer(ExporterArgumentsManager arguments, TraceDBObject traceDB) {
		this.arguments = arguments;
		this.traceDB = traceDB;
	}

	public void exportTrace() throws IOException, SoCTraceException {
		PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(arguments.getTraceFile())));
		writeHeader(bw);
		writeEvents(bw);
		bw.close();
	}

	private void writeEvents(PrintWriter bw) throws IOException, SoCTraceException {
		int page = 0;
		List<Event> events = new ArrayList<Event>();
		do {
			events.clear();
			EventQuery eventQuery = new EventQuery(traceDB);
			//eventQuery.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.EQ, Integer.toString(page++)));
			LogicalCondition logic = new LogicalCondition(LogicalOperation.AND);
			logic.addCondition(new SimpleCondition("PAGE", ComparisonOperation.GE, Integer.toString(page)));
			//get 6 pages
			page = page + pageIncrement; // XXX do it better, please
			logic.addCondition(new SimpleCondition("PAGE", ComparisonOperation.LT, Integer.toString(page)));
			eventQuery.setElementWhere(logic);
			events = eventQuery.getList();
			for (Event e : events) {
				String tokens[] = e.getType().getName().split(PajeConstants.PajeIdNameSeparator);
				bw.write(tokens[0]);
				for (EventParam ep : e.getEventParams()) {
					String temp = ep.getValue();
					if (temp.contains(" "))
						temp = "\"" + temp + "\"";
					bw.write(" " + temp);
				}
				bw.write("\n");
			}
		} while (events.size() > 0);
	}

	private void writeHeader(PrintWriter bw) throws IOException, SoCTraceException {
		EventTypeQuery eventTypeQuery = new EventTypeQuery(traceDB);
		List<EventType> eventTypes = eventTypeQuery.getList();
		for (EventType et : eventTypes) {
			String tokens[] = et.getName().split(PajeConstants.PajeIdNameSeparator);
			bw.write(PajeConstants.PajeFormatStartEventDef + " " + tokens[1] + " " + tokens[0] + "\n");
			for (EventParamType etp : et.getEventParamTypes())
				bw.write(PajeConstants.PajeFormatParamEventDef + "\t" + etp.getName() + " " + etp.getType() + "\n");
			bw.write(PajeConstants.PajeFormatEndEventDef + "\n");
		}
	}

}
