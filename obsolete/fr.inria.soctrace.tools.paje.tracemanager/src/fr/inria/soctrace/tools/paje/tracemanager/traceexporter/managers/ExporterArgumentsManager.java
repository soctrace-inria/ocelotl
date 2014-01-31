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

package fr.inria.soctrace.tools.paje.tracemanager.traceexporter.managers;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;
import fr.inria.soctrace.tools.paje.utils.argumentmanager.ArgumentManager;

public class ExporterArgumentsManager extends ArgumentManager {

	private String	sysDbName	= null;
	private String	traceDbName	= null;
	private String	traceFile	= null;

	public ExporterArgumentsManager(final String[] args) throws SoCTraceException {
		super(args);
	}

	public String getSysDbName() {
		return sysDbName;
	}

	public String getTraceDbName() {
		return traceDbName;
	}

	public String getTraceFile() {
		return traceFile;
	}

	@Override
	public void printArgs() {
		System.out.println();
		System.out.println("Pajé Exporter arguments");
		System.out.println("System DB: " + (sysDbName != null ? sysDbName : "-"));
		System.out.println("Trace DB: " + (traceDbName != null ? traceDbName : "-"));
		System.out.println("Trace File: " + (traceFile != null ? traceFile : "-"));
		System.out.println();
	}

	@Override
	public void processArgs() throws SoCTraceException {
		sysDbName = Configuration.getInstance().get(SoCTraceProperty.soctrace_db_name);
		traceDbName = options.containsKey("db") ? options.get("db") : null;
		if (traceDbName.isEmpty())
			throw new SoCTraceException("Missing data base file");
		traceFile = options.containsKey("output") ? options.get("output") : PajeConstants.PajeExportTraceFile;
	}

}
