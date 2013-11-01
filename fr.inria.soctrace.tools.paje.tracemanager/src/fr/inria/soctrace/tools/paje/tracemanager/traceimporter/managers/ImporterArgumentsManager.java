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

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;
import fr.inria.soctrace.tools.paje.utils.argumentmanager.ArgumentManager;

public class ImporterArgumentsManager extends ArgumentManager {

	private String			sysDbName			= null;
	private List<String>	traceDbName			= new ArrayList<String>();
	private List<String>	traceFiles			= null;
	private String			logFile				= null;
	private String			traceDescription	= null;

	public ImporterArgumentsManager(String[] args) throws SoCTraceException {
		super(args);
	}

	public String getLogFile() {
		return logFile;
	}

	public String getSysDbName() {
		return sysDbName;
	}

	public List<String> getTraceDbName() {
		return traceDbName;
	}

	public String getTraceDescription() {
		return traceDescription;
	}

	public List<String> getTraceFiles() {
		return traceFiles;
	}

	@Override
	public void printArgs() {
		System.out.println();
		System.out.println("Pajé Importer arguments");
		System.out.println("System DB: " + (sysDbName != null ? sysDbName : "-"));
		System.out.println("Trace DB: " + (traceDbName != null ? traceDbName : "-"));
		System.out.println("Log file: " + (logFile != null ? logFile : "-"));
		System.out.println("Trace Description: " + (traceDescription != null ? traceDescription : "-"));
		System.out.println("Trace Files:");
		if (!traceFiles.isEmpty())
			for (String it : traceFiles)
				System.out.println(it);
		else
			System.out.println("Trace files: -");
		System.out.println();
	}

	@Override
	public void processArgs() throws SoCTraceException {
		sysDbName = Configuration.getInstance().get(SoCTraceProperty.soctrace_db_name);
		//traceDbName = options.containsKey("db") ? options.get("db") : FramesocManager.getInstance().getTraceDBName(PajeConstants.ImportToolName);
		logFile = options.containsKey("log") ? options.get("log") : null;
		traceDescription = options.containsKey("desc") ? options.get("desc") : null;
		if (defaults.isEmpty())
			throw new SoCTraceException("Missing trace files");
		traceFiles = defaults;
		for (String td : traceFiles){
			String[] tokens = td.split("/");
			String string = tokens[tokens.length-1].replace(".paje", "");
			traceDbName.add(string);
		}
	}

}
