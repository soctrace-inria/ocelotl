package fr.inria.soctrace.tools.ocelotl.ui.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;

public class OcelotlSettings {

	private static final Logger logger = LoggerFactory
			.getLogger(OcelotlSettings.class);

	private boolean cacheActivated;
	private String cacheDirectory;
	private int cacheSize;
	private String snapShotDirectory;

	public OcelotlSettings() {
		cacheActivated = OcelotlConstants.DEFAULT_CACHE_ACTIVATION;
		cacheDirectory = ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.toString()
				+ "/ocelotlCache";
		cacheSize = OcelotlConstants.MAX_CACHESIZE;
		snapShotDirectory = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString()
				+ "/ocelotlSnapshot";

		loadConfigurationFile();
	}

	/**
	 * Load a previously saved configuration file
	 */
	private void loadConfigurationFile() {
		String defaultConfigFile = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString()
				+ "/ocelotl.conf";
		File confFile = new File(defaultConfigFile);

		// If the conf file is there
		if (confFile.exists() && confFile.canRead()) {
			// Parse & load
			BufferedReader bufFileReader;

			try {
				bufFileReader = new BufferedReader(new FileReader(confFile));
				String line;
				// Get header
				line = bufFileReader.readLine();
				
				if (line != null) {
					String[] header = line.split(OcelotlConstants.CSVDelimiter);

					setCacheActivated(Boolean.valueOf(header[0]));
					setCacheDirectory(header[1]);
					setCacheSize(Integer.parseInt(header[2]));
					setSnapShotDirectory(header[3]);
				}

				bufFileReader.close();

				logger.debug("Settings values:\n");
				logger.debug("Cache activated: " + cacheActivated);
				logger.debug("Cache directory: " + cacheDirectory);
				logger.debug("Cache size: " + cacheSize);
				logger.debug("Snapshot directory: " + snapShotDirectory);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			logger.debug("No configuration file was found: Default values will be used");
		}
	}

	public String getSnapShotDirectory() {
		return snapShotDirectory;
	}

	public void setSnapShotDirectory(String snapShotDir) {
		this.snapShotDirectory = snapShotDir;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public String getCacheDirectory() {
		return cacheDirectory;
	}

	public void setCacheDirectory(String cacheDir) {
		this.cacheDirectory = cacheDir;
	}

	public boolean isCacheActivated() {
		return cacheActivated;
	}

	public void setCacheActivated(boolean cacheActivated) {
		this.cacheActivated = cacheActivated;
	}

}
