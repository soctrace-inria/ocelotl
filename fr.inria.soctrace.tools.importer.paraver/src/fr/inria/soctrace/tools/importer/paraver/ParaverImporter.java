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
package fr.inria.soctrace.tools.importer.paraver;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.tools.management.ArgumentsManager;
import fr.inria.soctrace.framesoc.core.tools.management.PluginImporterJob;
import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IPluginToolJobBody;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.importer.pajedump.core.PJDumpConstants;
import fr.inria.soctrace.tools.importer.pajedump.core.PJDumpParser;
import fr.inria.soctrace.tools.importer.pajedump.core.PJDumpTraceMetadata;
import fr.inria.soctrace.tools.importer.paraver.core.ParaverConstants;
import fr.inria.soctrace.tools.importer.paraver.core.ParaverTraceMetadata;
import fr.inria.soctrace.tools.importer.paraver.reader.ParaverPrintWrapper;

/**
 * Otf2 importer tool.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ParaverImporter extends FramesocTool {

	private final static Logger logger = LoggerFactory.getLogger(ParaverImporter.class);

	/**
	 * Plugin Tool Job body: we use a Job since we have to perform a long
	 * operation and we don't want to freeze the UI.
	 */
	public class ParaverImporterPluginJobBody implements IPluginToolJobBody {

		private String args[];
		
		class ParaverParser extends PJDumpParser{
			String alias;
			
			public ParaverParser(SystemDBObject sysDB, TraceDBObject traceDB,
					String traceFile, String alias) {
				super(sysDB, traceDB, traceFile);
				this.alias=alias;
			}

			protected void saveTraceMetadata(boolean partialImport) throws SoCTraceException {

				ParaverTraceMetadata metadata = new ParaverTraceMetadata(sysDB, traceDB.getDBName(),
						alias, numberOfEvents, minTimestamp, maxTimestamp);
				metadata.createMetadata();
				metadata.saveMetadata();
			}
		}

		public ParaverImporterPluginJobBody(String[] args) {
			this.args = args;
		}
		
		@Override
		public void run(IProgressMonitor monitor) {
			DeltaManager delta = new DeltaManager();
			delta.start();

			logger.debug("Args: ");
			for (String s : args) {
				logger.debug(s);
			}

			ArgumentsManager argsm = new ArgumentsManager();
			argsm.parseArgs(args);
			argsm.printArgs();

			Assert.isTrue(argsm.getTokens().size() == 1);
			String traceFile = argsm.getTokens().get(0);
			if (monitor.isCanceled())
				return;

			String traceDbName = getNewTraceDBName(traceFile);

			SystemDBObject sysDB = null;
			TraceDBObject traceDB = null;

			try {
				// open system DB
				sysDB = SystemDBObject.openNewIstance();
				// create new trace DB
				traceDB = new TraceDBObject(traceDbName, DBMode.DB_CREATE);

				// parsing
				ArrayList<String> arguments = new ArrayList<String>();
				String input=traceFile.replace(ParaverConstants.TRACE_EXT, "");
				String output=ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()+File.separator+"tmp";
				arguments.add("-i");
				arguments.add(input);
				arguments.add("-o");
				arguments.add(output);
				arguments.add("-f");
				arguments.add(PJDumpConstants.TRACE_EXT.replace(".", ""));
				ParaverPrintWrapper printer = new ParaverPrintWrapper(arguments);
				
				IStatus status=printer.executeSync(monitor);
				if (status.equals(IStatus.CANCEL)||monitor.isCanceled()){
					throw new SoCTraceException();
				}
				String trueOutput=output+PJDumpConstants.TRACE_EXT;
				File outputFile = new File(trueOutput);
//				while(!outputFile.exists())
//				{
//					outputFile = new File(output);
//					if (monitor.isCanceled()){
//						throw new SoCTraceException();
//					}
//				}
				ParaverParser parser = new ParaverParser(sysDB, traceDB, trueOutput, FilenameUtils.getBaseName(input));
				parser.parseTrace(monitor, 1, 1);
				outputFile.delete();

			} catch (SoCTraceException ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
				System.err.println("Import failure. Trying to rollback modifications in DB.");
				if (sysDB != null)
					try {
						sysDB.rollback();
					} catch (SoCTraceException e) {
						e.printStackTrace();
					}
				if (traceDB != null)
					try {
						traceDB.dropDatabase();
					} catch (SoCTraceException e) {
						e.printStackTrace();
					}
			} finally {
				// close the trace DB and the system DB (commit)
				DBObject.finalClose(traceDB);
				DBObject.finalClose(sysDB);
				delta.end("Import trace");
			}

		}

	}

	private String getNewTraceDBName(String traceFile) {
		String basename = FilenameUtils.getBaseName(traceFile);
		String extension = FilenameUtils.getExtension(traceFile);
		if (extension.equals(ParaverConstants.TRACE_EXT)) {
			basename = basename.replace(ParaverConstants.TRACE_EXT, "");
		}
		return FramesocManager.getInstance().getTraceDBName(basename);
	}

	@SuppressWarnings("unused")
	private boolean checkArgs(ArgumentsManager argsm) {
		if (argsm.getTokens().size() != 1)
			return false;
		return true;
	}

	@Override
	public void launch(String[] args) {
		PluginImporterJob job = new PluginImporterJob("Paraver Importer",
				new ParaverImporterPluginJobBody(args));
		job.setUser(true);
		job.schedule();
	}

	@Override
	public boolean canLaunch(String[] args) {
		if (args.length != 1)
			return false;

		for (String file : args) {
			File f = new File(file);
			if (!f.isFile())
				return false;
		}

		return true;
	}

}
