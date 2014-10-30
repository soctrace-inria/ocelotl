package fr.inria.soctrace.tools.importer.paraver.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.tools.management.ExternalProgramWrapper;
import fr.inria.soctrace.tools.importer.paraver.Activator;

/**
 * Wrapper for otf2-print program.
 * 
 * It looks for the otf2-print executable path in the configuration file
 * ./<eclipse.dir>/configuration/<plugin.name>/otf2-print.path.
 * 
 * If this file is not found, one is created with a default value, pointing to
 * the precompiled executable (./<plugin.name>/exe/otf2-print).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ParaverPrintWrapper extends ExternalProgramWrapper {

	private final static Logger logger = LoggerFactory.getLogger(ParaverPrintWrapper.class);

	/**
	 * Default oft2-print executable location
	 */
	private static final String DEFAULT_PATH = "exe" + File.separator + "otf2-print";

	/**
	 * Constructor
	 * 
	 * @param arguments
	 *            program arguments
	 */
	public ParaverPrintWrapper(List<String> arguments) {
		super(readPath(), arguments);
	}

	/**
	 * Read the executable path from the configuration file
	 * 
	 * @return the executable path
	 */
	private static String readPath() {
			// executable path
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			Path path = new Path(DEFAULT_PATH);
			URL fileURL = FileLocator.find(bundle, path, null);
			String executablePath = null;
			try {
				executablePath = FileLocator.resolve(fileURL).getPath().toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return executablePath;

	}

}
