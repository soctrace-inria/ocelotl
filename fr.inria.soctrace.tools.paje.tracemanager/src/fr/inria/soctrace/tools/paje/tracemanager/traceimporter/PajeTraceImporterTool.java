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

import fr.inria.soctrace.framesoc.core.tools.management.PluginImporterJob;
import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IPluginToolJobBody;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.paje.tracemanager.traceimporter.managers.ImporterArgumentsManager;

/**
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PajeTraceImporterTool extends FramesocTool {

	/**
	 * Plugin Tool Job body: we use a Job since we have to
	 * perform a long operation and we don't want to freeze the UI.
	 */
	private class PajeImporterPluginJobBody implements IPluginToolJobBody {

		private String	args[];

		public PajeImporterPluginJobBody(String[] args) {
			this.args = args;
		}

		@Override
		public void run() throws SoCTraceException {
			DeltaManager dm = new DeltaManager();
			dm.start();

			ImporterArgumentsManager arguments = new ImporterArgumentsManager(args);
			arguments.parseArgs();
			arguments.processArgs();
			if (DEBUG)
				arguments.debugArgs();

			SystemDBObject sysDB =  null;
			TraceDBObject traceDB = null;
			for (int i=0; i<arguments.getTraceDbName().size(); i++ ){
				sysDB = new SystemDBObject(arguments.getSysDbName(), DBMode.DB_OPEN);
				dm.end("Paje Trace " + i + " Importing");
				traceDB = new TraceDBObject(arguments.getTraceDbName().get(i), DBMode.DB_CREATE);

			/** Import trace */

			Parser importer = new Parser(arguments, sysDB, traceDB);
			importer.importTrace(i);

			/** DB close (commit) */

			traceDB.close();
			sysDB.close();
			

			dm.end("Paje Trace " + i + " Imported");
			}
			
			dm.end("Paje Trace Importer end");

		}

	}

	private static final boolean	DEBUG	= true;

	@Override
	public void launch(String[] args) {
		PluginImporterJob job = new PluginImporterJob("Paje Importer", new PajeImporterPluginJobBody(args));
		job.setUser(true);
		job.schedule();
	}

}
