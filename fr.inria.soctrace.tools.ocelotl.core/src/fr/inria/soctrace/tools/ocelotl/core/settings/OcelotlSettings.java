package fr.inria.soctrace.tools.ocelotl.core.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacachePolicy;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlDefaultParameterConstants;

public class OcelotlSettings {

	private static final Logger logger = LoggerFactory
			.getLogger(OcelotlSettings.class);

	private boolean cacheActivated;
	private String cacheDirectory;
	private int cacheSize;
	private String snapShotDirectory;
	private DatacachePolicy cachePolicy;
	
	// Default directory where the config file is
	private String defaultConfigFile;

	public OcelotlSettings() {
		
		// Init with default configuration
		cacheActivated = OcelotlDefaultParameterConstants.Normalize;
			//	OcelotlConstants.DEFAULT_CACHE_ACTIVATION;

		// Default cache directory is the directory "ocelotlCache" in the
		// running directory
		cacheDirectory = ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.toString()
				+ "/ocelotlCache";

		cacheSize = OcelotlConstants.MAX_CACHESIZE;
		snapShotDirectory = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString()
				+ "/ocelotlSnapshot";
		
		
		cachePolicy = OcelotlDefaultParameterConstants.DEFAULT_CACHE_POLICY;

		// Check if a configuration file exists and if so, load the saved
		// configuration
		loadConfigurationFile();
	}

	/**
	 * Load a previously saved configuration file
	 */
	private void loadConfigurationFile() {
		defaultConfigFile = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString()
				+ "/ocelotl.conf";
		File confFile = new File(defaultConfigFile);

		// If the conf file exists and can be read
		if (confFile.exists() && confFile.canRead()) {
			// Then parse & load
			BufferedReader bufFileReader;

			try {
				bufFileReader = new BufferedReader(new FileReader(confFile));
				String line;
				// Get the first line
				line = bufFileReader.readLine();

				if (line != null) {
					String[] config = line.split(OcelotlConstants.CSVDelimiter);

					// If the configuration has the right number of objects 
					if (config.length == OcelotlConstants.CONFIGURATION_NORMAL_SIZE) {
						setCacheActivated(Boolean.valueOf(config[0]));
						setCacheDirectory(config[1]);			
						if (Integer.parseInt(config[2]) >= 0) {
							// Convert from megabytes to bytes
							setCacheSize(Integer.parseInt(config[2]) * 1000000);
						} else {
							setCacheSize(-1);
						}
						setSnapShotDirectory(config[3]);
						//setCachePolicy(Integer.valueOf(config[4]));
						
					} else {
						logger.debug("Invalid configuration file: Default values will be used");
					}
				} else {
					logger.debug("Invalid configuration file: Default values will be used");
				}

				bufFileReader.close();

				logger.debug("Settings values:\n");
				logger.debug("Cache activated: " + cacheActivated);
				logger.debug("Cache directory: " + cacheDirectory);
				logger.debug("Cache size: " + cacheSize);
				logger.debug("Snapshot directory: " + snapShotDirectory);
				logger.debug("Cache Policy: " + cachePolicy);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.debug("No configuration file was found: Default values will be used");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			logger.debug("The configuration file was not found or could not be read: Default values will be used");
		}
	}

	/**
	 * Save the current settings in the configuration file
	 */
	public void saveSettings() {
		StringBuffer output = new StringBuffer();

		output.append(cacheActivated);
		output.append(";");
		output.append(cacheDirectory);
		output.append(";");
		if (cacheSize >= 0) {
			output.append(cacheSize / 1000000);
		} else {
			output.append(-1);
		}		
		output.append(";");
		output.append(snapShotDirectory);
		output.append(";");
		output.append(cachePolicy);

		String newSettings = output.toString();

		PrintWriter writer;
		try {
			writer = new PrintWriter(defaultConfigFile, "UTF-8");
			writer.print(newSettings);

			// Close the fd
			writer.flush();
			writer.close();

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getSnapShotDirectory() {
		return snapShotDirectory;
	}

	public void setSnapShotDirectory(String snapShotDir) {
		if (!snapShotDirectory.equals(snapShotDir)) {
			this.snapShotDirectory = snapShotDir;
			saveSettings();
		}
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		if (this.cacheSize != cacheSize) {
			this.cacheSize = cacheSize;
			saveSettings();
		}
	}

	public String getCacheDirectory() {
		return cacheDirectory;
	}

	public void setCacheDirectory(String cacheDir) {
		if (!this.cacheDirectory.equals(cacheDir)) {
			this.cacheDirectory = cacheDir;
			saveSettings();
		}
	}

	public boolean isCacheActivated() {
		return cacheActivated;
	}

	public void setCacheActivated(boolean cacheActivated) {
		if (this.cacheActivated != cacheActivated) {
			this.cacheActivated = cacheActivated;
			saveSettings();
		}
	}

	public DatacachePolicy getCachePolicy() {
		return cachePolicy;
	}

	public void setCachePolicy(DatacachePolicy cachePolicy) {
		this.cachePolicy = cachePolicy;
	}

}
