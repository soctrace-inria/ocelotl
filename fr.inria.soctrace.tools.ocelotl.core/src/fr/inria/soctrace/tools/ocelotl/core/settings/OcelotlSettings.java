package fr.inria.soctrace.tools.ocelotl.core.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacachePolicy;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.ParameterPPolicy;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlDefaultParameterConstants;

public class OcelotlSettings {

	private static final Logger logger = LoggerFactory
			.getLogger(OcelotlSettings.class);

	private boolean cacheActivated;
	private String cacheDirectory;
	private long cacheSize;
	private String snapShotDirectory;
	private DatacachePolicy cachePolicy;
	private int cacheTimeSliceNumber;
	private int eventsPerThread;
	private int maxEventProducersPerQuery;
	private int numberOfThread;
	private boolean normalizedCurve;
	private double thresholdPrecision;
	private boolean increasingQualities;
	private int snapshotXResolution;
	private int snapshotYResolution;

	private boolean enableOverview;
	private double overviewParameter;
	private String overviewAggregOperator;
	private int overviewTimesliceNumber;
	private Color overviewSelectionFgColor;
	private Color overviewSelectionBgColor;
	private int overviewSelectionAlphaValue;
	private Color overviewDisplayFgColor;
	private Color overviewDisplayBgColor;
	private int overviewDisplayAlphaValue;
	private ParameterPPolicy parameterPPolicy;
	private boolean aggregateLeaves;
	private int maxNumberOfLeaves;
	
	// Default directory where the config file is
	private String defaultConfigFile;

	public OcelotlSettings() {
		// Init with default configuration
		cacheActivated = OcelotlDefaultParameterConstants.DEFAULT_CACHE_ACTIVATION;

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
		cacheTimeSliceNumber = OcelotlDefaultParameterConstants.DEFAULT_CACHE_TS_NUMBER;
		eventsPerThread = OcelotlDefaultParameterConstants.EVENTS_PER_THREAD;
		maxEventProducersPerQuery = OcelotlDefaultParameterConstants.EventProducersPerQuery;
		numberOfThread = OcelotlDefaultParameterConstants.NUMBER_OF_THREADS;

		normalizedCurve = OcelotlDefaultParameterConstants.Normalize;
		thresholdPrecision = OcelotlDefaultParameterConstants.Threshold;
		increasingQualities = OcelotlDefaultParameterConstants.IncreasingQualities;
		overviewAggregOperator = OcelotlDefaultParameterConstants.OVERVIEW_AGGREG_OPERATOR;

		snapshotXResolution = OcelotlDefaultParameterConstants.SNAPSHOT_DEFAULT_X_RESOLUTION;
		snapshotYResolution = OcelotlDefaultParameterConstants.SNAPSHOT_DEFAULT_Y_RESOLUTION;

		overviewSelectionBgColor = OcelotlDefaultParameterConstants.OVERVIEW_SELECT_BG_COLOR;
		overviewSelectionFgColor = OcelotlDefaultParameterConstants.OVERVIEW_SELECT_FG_COLOR;
		overviewSelectionAlphaValue = OcelotlDefaultParameterConstants.OVERVIEW_SELECT_ALPHA;
		overviewDisplayBgColor = OcelotlDefaultParameterConstants.OVERVIEW_DISPLAY_BG_COLOR;
		overviewDisplayFgColor = OcelotlDefaultParameterConstants.OVERVIEW_DISPLAY_FG_COLOR;
		overviewDisplayAlphaValue = OcelotlDefaultParameterConstants.OVERVIEW_DISPLAY_ALPHA;
		parameterPPolicy = OcelotlDefaultParameterConstants.DEFAULT_PARAMETERP_POLICY;
		enableOverview = OcelotlDefaultParameterConstants.OVERVIEW_ENABLE;
		aggregateLeaves = OcelotlDefaultParameterConstants.AGGREGATE_LEAVES;
		maxNumberOfLeaves = OcelotlDefaultParameterConstants.MAX_NUMBER_OF_LEAVES;
		
		// Check if a configuration file exists and if so, load the saved
		// configuration
		loadConfigurationFile();
	}

	/**
	 * Load a previously saved configuration file
	 */
	public void loadConfigurationFile() {

		defaultConfigFile = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString()
				+ "/ocelotl.conf";
		File confFile = new File(defaultConfigFile);

		// If the conf file exists and can be read
		if (confFile.exists() && confFile.canRead()) {
			// Then parse & load
			Reader fileReader;

			try {
				fileReader = new FileReader(confFile);
				JsonParser jsonParser = new JsonParser();
				JsonElement theConfig = jsonParser.parse(fileReader);
				JsonObject theConf = theConfig.getAsJsonObject();
				fileReader.close();

				setCacheActivated(theConf.get(
						OcelotlConstants.JSONCacheActivated).getAsBoolean());
				setCacheDirectory(theConf.get(
						OcelotlConstants.JSONCacheDirectory).getAsString());
				int cacheSize = theConf.get(OcelotlConstants.JSONCacheSize)
						.getAsInt();
				if (cacheSize >= 0) {
					// Convert from megabytes to bytes
					setCacheSize(cacheSize * 1000000);
				} else {
					setCacheSize(-1);
				}
				setSnapShotDirectory(theConf.get(
						OcelotlConstants.JSONSnapShotDirectory).getAsString());
				setCachePolicy(DatacachePolicy.valueOf(theConf.get(
						OcelotlConstants.JSONCachePolicy).getAsString()));
				setCacheTimeSliceNumber(theConf.get(
						OcelotlConstants.JSONCacheTimeSliceNumber).getAsInt());
				setEventsPerThread(theConf.get(
						OcelotlConstants.JSONEventsPerThread).getAsInt());
				setMaxEventProducersPerQuery(theConf.get(
						OcelotlConstants.JSONMaxEventProducersPerQuery)
						.getAsInt());
				setNumberOfThread(theConf.get(
						OcelotlConstants.JSONNumberOfThread).getAsInt());
				setNormalizedCurve(theConf.get(
						OcelotlConstants.JSONNormalizedCurve).getAsBoolean());
				setThresholdPrecision(theConf.get(
						OcelotlConstants.JSONThresholdPrecision).getAsDouble());
				setIncreasingQualities(theConf.get(
						OcelotlConstants.JSONIncreasingQualities)
						.getAsBoolean());
				setSnapshotXResolution(theConf.get(
						OcelotlConstants.JSONSnapshotXResolution).getAsInt());
				setSnapshotYResolution(theConf.get(
						OcelotlConstants.JSONSnapshotYResolution).getAsInt());
				setOverviewDisplayBgColor(loadColor(theConf.get(
						OcelotlConstants.JSONOverviewDisplayBgColor)
						.getAsString()));
				setOverviewDisplayFgColor(loadColor(theConf.get(
						OcelotlConstants.JSONOverviewDisplayFgColor)
						.getAsString()));
				setOverviewSelectionBgColor(loadColor(theConf.get(
						OcelotlConstants.JSONOverviewSelectionBgColor)
						.getAsString()));
				setOverviewSelectionFgColor(loadColor(theConf.get(
						OcelotlConstants.JSONOverviewSelectionFgColor)
						.getAsString()));
				setOverviewSelectionAlphaValue(theConf.get(
						OcelotlConstants.JSONOverviewSelectionAlpha)
						.getAsInt());
				setOverviewDisplayAlphaValue(theConf.get(
						OcelotlConstants.JSONOverviewDisplayAlpha)
						.getAsInt());
				setParameterPPolicy(ParameterPPolicy.valueOf(theConf.get(
						OcelotlConstants.JSONParameterPPolicy).getAsString()));
				setEnableOverview(theConf.get(
						OcelotlConstants.JSONEnableOverview).getAsBoolean());
				setAggregateLeaves(theConf.get(
						OcelotlConstants.JSONAggregateLeaves).getAsBoolean());
				setMaxNumberOfLeaves(theConf.get(
						OcelotlConstants.JSONMaxNumberOfLeaves).getAsInt());
			
				logger.debug("Settings values:\n");
				logger.debug("Cache activated: " + cacheActivated);
				logger.debug("Cache directory: " + cacheDirectory);
				logger.debug("Cache size: " + cacheSize);
				logger.debug("Snapshot directory: " + snapShotDirectory);
				logger.debug("Cache Policy: " + cachePolicy);
				logger.debug("Cache Time slices: " + cacheTimeSliceNumber);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.debug("No configuration file was found: default values will be used");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonParseException | NullPointerException e) {
				logger.error("Incomplete or invalid JSON configuration file: the file will be regenerated with default values will be used");
				// Regenerate the configuration file with the default values
				saveSettings();
			}
		}
	}

	/**
	 * Save the current settings in the configuration file
	 */
	public void saveSettings() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		JsonObject theConfig = new JsonObject();

		theConfig.addProperty(OcelotlConstants.JSONCacheActivated,
				cacheActivated);
		theConfig.addProperty(OcelotlConstants.JSONCacheDirectory,
				cacheDirectory);
		// Convert from MB to bytes
		if (cacheSize >= 0) {
			theConfig.addProperty(OcelotlConstants.JSONCacheSize,
					cacheSize / 1000000);
		} else {
			theConfig.addProperty(OcelotlConstants.JSONCacheSize, -1);
		}
		theConfig.addProperty(OcelotlConstants.JSONSnapShotDirectory,
				snapShotDirectory);
		theConfig.addProperty(OcelotlConstants.JSONCachePolicy,
				cachePolicy.toString());
		theConfig.addProperty(OcelotlConstants.JSONCacheTimeSliceNumber,
				cacheTimeSliceNumber);
		theConfig.addProperty(OcelotlConstants.JSONEventsPerThread,
				eventsPerThread);
		theConfig.addProperty(OcelotlConstants.JSONMaxEventProducersPerQuery,
				maxEventProducersPerQuery);
		theConfig.addProperty(OcelotlConstants.JSONNumberOfThread,
				numberOfThread);
		theConfig.addProperty(OcelotlConstants.JSONNormalizedCurve,
				normalizedCurve);
		theConfig.addProperty(OcelotlConstants.JSONThresholdPrecision,
				thresholdPrecision);
		theConfig.addProperty(OcelotlConstants.JSONIncreasingQualities,
				increasingQualities);
		theConfig.addProperty(OcelotlConstants.JSONSnapshotXResolution,
				snapshotXResolution);
		theConfig.addProperty(OcelotlConstants.JSONSnapshotYResolution,
				snapshotYResolution);
		theConfig.addProperty(OcelotlConstants.JSONOverviewSelectionBgColor,
				saveColor(overviewSelectionBgColor));
		theConfig.addProperty(OcelotlConstants.JSONOverviewSelectionFgColor,
				saveColor(overviewSelectionFgColor));
		theConfig.addProperty(OcelotlConstants.JSONOverviewSelectionAlpha,
				overviewSelectionAlphaValue);
		theConfig.addProperty(OcelotlConstants.JSONOverviewDisplayBgColor,
				saveColor(overviewDisplayBgColor));
		theConfig.addProperty(OcelotlConstants.JSONOverviewDisplayFgColor,
				saveColor(overviewDisplayFgColor));
		theConfig.addProperty(OcelotlConstants.JSONOverviewDisplayAlpha,
				overviewDisplayAlphaValue);
		theConfig.addProperty(OcelotlConstants.JSONParameterPPolicy,
				parameterPPolicy.toString());
		theConfig.addProperty(OcelotlConstants.JSONEnableOverview,
				enableOverview);
		theConfig.addProperty(OcelotlConstants.JSONAggregateLeaves,
				aggregateLeaves);
		theConfig.addProperty(OcelotlConstants.JSONMaxNumberOfLeaves,
				maxNumberOfLeaves);
		
		String newSettings = gson.toJson(theConfig);

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

	private String saveColor(Color aColor) {
		StringBuffer output = new StringBuffer();
		output.append(aColor.getRed());
		output.append(",");
		output.append(aColor.getGreen());
		output.append(",");
		output.append(aColor.getBlue());
		output.append(",");

		return output.toString();
	}

	/**
	 * Load a color from a string
	 * 
	 * @param aColorValue
	 *            string containing the rgb values separated by comma
	 * @return the corresponding color
	 */
	private Color loadColor(String aColorValue) {
		String[] colorValues = aColorValue.split(",");
		int red = Integer.parseInt(colorValues[0]);
		int green = Integer.parseInt(colorValues[1]);
		int blue = Integer.parseInt(colorValues[2]);

		return new Color(Display.getDefault(), red, green, blue);
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

	public long getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(long cacheMaxSize) {
		if (this.cacheSize != cacheMaxSize) {
			this.cacheSize = cacheMaxSize;
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
		if (this.cachePolicy != cachePolicy) {
			this.cachePolicy = cachePolicy;
			saveSettings();
		}
	}

	public int getCacheTimeSliceNumber() {
		return cacheTimeSliceNumber;
	}

	public void setCacheTimeSliceNumber(int cacheTimeSliceNumber) {
		if (this.cacheTimeSliceNumber != cacheTimeSliceNumber) {
			this.cacheTimeSliceNumber = cacheTimeSliceNumber;
			saveSettings();
		}
	}

	public int getEventsPerThread() {
		return eventsPerThread;
	}

	public void setEventsPerThread(int eventsPerThread) {
		if (this.eventsPerThread != eventsPerThread) {
			this.eventsPerThread = eventsPerThread;
			saveSettings();
		}
	}

	public int getMaxEventProducersPerQuery() {
		return maxEventProducersPerQuery;
	}

	public void setMaxEventProducersPerQuery(int maxEventProducers) {
		if (this.maxEventProducersPerQuery != maxEventProducers) {
			this.maxEventProducersPerQuery = maxEventProducers;
			saveSettings();
		}
	}

	public int getNumberOfThread() {
		return numberOfThread;
	}

	public void setNumberOfThread(int numberOfThread) {
		if (this.numberOfThread != numberOfThread) {
			this.numberOfThread = numberOfThread;
			saveSettings();
		}
	}

	public boolean isNormalizedCurve() {
		return normalizedCurve;
	}

	public void setNormalizedCurve(boolean normalizedCurve) {
		if (this.normalizedCurve != normalizedCurve) {
			this.normalizedCurve = normalizedCurve;
			saveSettings();
		}
	}

	public double getThresholdPrecision() {
		return thresholdPrecision;
	}

	public void setThresholdPrecision(double thresholdPrecision) {
		if (this.thresholdPrecision != thresholdPrecision) {
			this.thresholdPrecision = thresholdPrecision;
			saveSettings();
		}
	}

	public boolean getIncreasingQualities() {
		return increasingQualities;
	}

	public void setIncreasingQualities(boolean increasingQualities) {
		if (this.increasingQualities != increasingQualities) {
			this.increasingQualities = increasingQualities;
			saveSettings();
		}
	}

	public double getOverviewParameter() {
		return overviewParameter;
	}

	public void setOverviewParameter(double overviewParameter) {
		if (this.overviewParameter != overviewParameter) {
			this.overviewParameter = overviewParameter;
			saveSettings();
		}
	}

	public int getOverviewTimesliceNumber() {
		return overviewTimesliceNumber;
	}

	public void setOverviewTimesliceNumber(int overviewTimesliceNumber) {
		if (this.overviewTimesliceNumber != overviewTimesliceNumber) {
			this.overviewTimesliceNumber = overviewTimesliceNumber;
			saveSettings();
		}
	}

	public String getOverviewAggregOperator() {
		return overviewAggregOperator;
	}

	public void setOverviewAggregOperator(String overviewAggregOperator) {
		this.overviewAggregOperator = overviewAggregOperator;
		if (!this.overviewAggregOperator.equals(overviewAggregOperator)) {
			this.overviewAggregOperator = overviewAggregOperator;
			saveSettings();
		}
	}

	public int getSnapshotYResolution() {
		return snapshotYResolution;
	}

	public void setSnapshotYResolution(int snapshotYResolution) {
		if (this.snapshotYResolution != snapshotYResolution) {
			this.snapshotYResolution = snapshotYResolution;
			saveSettings();
		}
	}

	public int getSnapshotXResolution() {
		return snapshotXResolution;
	}

	public void setSnapshotXResolution(int snapshotXResolution) {
		if (this.snapshotXResolution != snapshotXResolution) {
			this.snapshotXResolution = snapshotXResolution;
			saveSettings();
		}
	}

	public Color getOverviewSelectionFgColor() {
		return overviewSelectionFgColor;
	}

	public void setOverviewSelectionFgColor(Color overviewSelectionFgColor) {
		if (this.overviewSelectionFgColor != overviewSelectionFgColor) {
			this.overviewSelectionFgColor = overviewSelectionFgColor;
			saveSettings();
		}
	}

	public Color getOverviewSelectionBgColor() {
		return overviewSelectionBgColor;
	}

	public void setOverviewSelectionBgColor(Color overviewSelectionBgColor) {
		if (this.overviewSelectionBgColor != overviewSelectionBgColor) {
			this.overviewSelectionBgColor = overviewSelectionBgColor;
			saveSettings();
		}

	}

	public Color getOverviewDisplayFgColor() {
		return overviewDisplayFgColor;
	}

	public void setOverviewDisplayFgColor(Color overviewDisplayFgColor) {
		if (this.overviewDisplayFgColor != overviewDisplayFgColor) {
			this.overviewDisplayFgColor = overviewDisplayFgColor;
			saveSettings();
		}
	}

	public Color getOverviewDisplayBgColor() {
		return overviewDisplayBgColor;
	}

	public void setOverviewDisplayBgColor(Color overviewDisplayBgColor) {
		if (this.overviewDisplayBgColor != overviewDisplayBgColor) {
			this.overviewDisplayBgColor = overviewDisplayBgColor;
			saveSettings();
		}
	}

	public int getOverviewSelectionAlphaValue() {
		return overviewSelectionAlphaValue;
	}

	public void setOverviewSelectionAlphaValue(int overviewSelectionAlphaValue) {
		if (this.overviewSelectionAlphaValue != overviewSelectionAlphaValue) {
			this.overviewSelectionAlphaValue = overviewSelectionAlphaValue;
			saveSettings();
		}
	}

	public int getOverviewDisplayAlphaValue() {
		return overviewDisplayAlphaValue;
	}

	public void setOverviewDisplayAlphaValue(int overviewDisplayAlphaValue) {
		if (this.overviewDisplayAlphaValue != overviewDisplayAlphaValue) {
			this.overviewDisplayAlphaValue = overviewDisplayAlphaValue;
			saveSettings();
		}
	}

	public boolean isEnableOverview() {
		return enableOverview;
	}

	public void setEnableOverview(boolean enableOverview) {
		if (this.enableOverview != enableOverview) {
			this.enableOverview = enableOverview;
			saveSettings();
		}
	}

	public ParameterPPolicy getParameterPPolicy() {
		return parameterPPolicy;
	}

	public void setParameterPPolicy(ParameterPPolicy parameterPPolicy) {
		if (this.parameterPPolicy != parameterPPolicy) {
			this.parameterPPolicy = parameterPPolicy;
			saveSettings();
		}
	}

	public boolean isAggregateLeaves() {
		return aggregateLeaves;
	}

	public void setAggregateLeaves(boolean aggregateLeaves) {
		if (this.aggregateLeaves != aggregateLeaves) {
			this.aggregateLeaves = aggregateLeaves;
			saveSettings();
		}
	}

	public int getMaxNumberOfLeaves() {
		return maxNumberOfLeaves;
	}

	public void setMaxNumberOfLeaves(int maxNumberOfLeaves) {
		if (this.maxNumberOfLeaves != maxNumberOfLeaves) {
			this.maxNumberOfLeaves = maxNumberOfLeaves;
			saveSettings();
		}
	}
}
