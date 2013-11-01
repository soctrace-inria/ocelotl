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

package fr.inria.soctrace.tools.paje.tracemanager.tracecleaner;

import fr.inria.soctrace.framesoc.core.tools.management.PluginToolJob;
import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IPluginToolJobBody;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.paje.tracemanager.tracecleaner.managers.CleanerArgumentsManager;

public class PajeTraceCleanerTool extends FramesocTool {

	/**
	 * Plugin Tool Job body: we use a Job since we have to
	 * perform a long operation and we don't want to freeze the UI.
	 */
	private class PajeCleanerPluginJobBody implements IPluginToolJobBody {

		private final String	args[];

		public PajeCleanerPluginJobBody(final String[] args) {
			this.args = args;
		}

		@Override
		public void run() throws SoCTraceException {
			final DeltaManager dm = new DeltaManager();
			dm.start();

			try {
				final CleanerArgumentsManager arguments = new CleanerArgumentsManager(args);
				arguments.parseArgs();
				arguments.processArgs();
				if (DEBUG)
					arguments.debugArgs();

				final SystemDBObject sysDB = new SystemDBObject(arguments.getSysDbName(), DBMode.DB_OPEN);
				final TraceDBObject traceDB = new TraceDBObject(arguments.getTraceDbName(), DBMode.DB_OPEN);

				/** Clean trace*/

				final Cleaner cleaner = new Cleaner(traceDB);
				cleaner.cleanup();

				/** DB close (commit) */

				traceDB.close();
				sysDB.close();
			} finally {
				dm.end("Paje Trace Cleaner end");
			}
		}

	}

	private static final boolean	DEBUG	= true;

	@Override
	public void launch(final String[] args) {
		final PluginToolJob job = new PluginToolJob("Paje Cleaner", new PajeCleanerPluginJobBody(args));
		job.setUser(true);
		job.schedule();
	}

}
