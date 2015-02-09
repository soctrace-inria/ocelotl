/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
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

	private boolean dataCacheActivated;
	private boolean dichoCacheActivated;
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
	private int yAxisXResolution;
	private int xAxisYResolution;
	private int qualCurveXResolution;
	private int qualCurveYResolution;

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
	
	private Color mainSelectionFgColor;
	private Color mainSelectionBgColor;
	private int mainSelectionAlphaValue;
	private Color mainDisplayFgColor;
	private Color mainDisplayBgColor;
	private int mainDisplayAlphaValue;
	
	private ParameterPPolicy parameterPPolicy;
	private boolean aggregateLeaves;
	private int maxNumberOfLeaves;
	
	// Default directory where the config file is
	private String defaultConfigFile;

	public OcelotlSettings() {
		// Init with default configuration
		setDefaultValues();

		// Check if a configuration file exists and if so, load the saved
		// configuration
		loadConfigurationFile();
	}

	/**
	 * Set all settings to their default values
	 */
	public void setDefaultValues() {
		dataCacheActivated = OcelotlDefaultParameterConstants.DEFAULT_DATA_CACHE_ACTIVATION;
		dichoCacheActivated = OcelotlDefaultParameterConstants.DEFAULT_DICHO_CACHE_ACTIVATION;

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
		xAxisYResolution = OcelotlDefaultParameterConstants.XAXIS_DEFAULT_Y_RESOLUTION;
		yAxisXResolution = OcelotlDefaultParameterConstants.YAXIS_DEFAULT_X_RESOLUTION;
		qualCurveXResolution = OcelotlDefaultParameterConstants.QUALCURVE_DEFAULT_X_RESOLUTION;
		qualCurveYResolution = OcelotlDefaultParameterConstants.QUALCURVE_DEFAULT_Y_RESOLUTION;
		
		overviewSelectionBgColor = OcelotlDefaultParameterConstants.OVERVIEW_SELECT_BG_COLOR;
		overviewSelectionFgColor = OcelotlDefaultParameterConstants.OVERVIEW_SELECT_FG_COLOR;
		overviewSelectionAlphaValue = OcelotlDefaultParameterConstants.OVERVIEW_SELECT_ALPHA;
		overviewDisplayBgColor = OcelotlDefaultParameterConstants.OVERVIEW_DISPLAY_BG_COLOR;
		overviewDisplayFgColor = OcelotlDefaultParameterConstants.OVERVIEW_DISPLAY_FG_COLOR;
		overviewDisplayAlphaValue = OcelotlDefaultParameterConstants.OVERVIEW_DISPLAY_ALPHA;

		mainSelectionBgColor = OcelotlDefaultParameterConstants.MAIN_SELECT_BG_COLOR;
		mainSelectionFgColor = OcelotlDefaultParameterConstants.MAIN_SELECT_FG_COLOR;
		mainSelectionAlphaValue = OcelotlDefaultParameterConstants.MAIN_SELECT_ALPHA;
		mainDisplayBgColor = OcelotlDefaultParameterConstants.MAIN_DISPLAY_BG_COLOR;
		mainDisplayFgColor = OcelotlDefaultParameterConstants.MAIN_DISPLAY_FG_COLOR;
		mainDisplayAlphaValue = OcelotlDefaultParameterConstants.MAIN_DISPLAY_ALPHA;

		parameterPPolicy = OcelotlDefaultParameterConstants.DEFAULT_PARAMETERP_POLICY;
		enableOverview = OcelotlDefaultParameterConstants.OVERVIEW_ENABLE;
		aggregateLeaves = OcelotlDefaultParameterConstants.AGGREGATE_LEAVES;
		maxNumberOfLeaves = OcelotlDefaultParameterConstants.MAX_NUMBER_OF_LEAVES;
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

				setDataCacheActivated(theConf.get(
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
						OcelotlConstants.JSONOverviewSelectionAlpha).getAsInt());
				setOverviewDisplayAlphaValue(theConf.get(
						OcelotlConstants.JSONOverviewDisplayAlpha).getAsInt());
				setParameterPPolicy(ParameterPPolicy.valueOf(theConf.get(
						OcelotlConstants.JSONParameterPPolicy).getAsString()));
				setEnableOverview(theConf.get(
						OcelotlConstants.JSONEnableOverview).getAsBoolean());
				setAggregateLeaves(theConf.get(
						OcelotlConstants.JSONAggregateLeaves).getAsBoolean());
				setMaxNumberOfLeaves(theConf.get(
						OcelotlConstants.JSONMaxNumberOfLeaves).getAsInt());
				setMainDisplayBgColor(loadColor(theConf.get(
						OcelotlConstants.JSONMainDisplayBgColor).getAsString()));
				setMainDisplayFgColor(loadColor(theConf.get(
						OcelotlConstants.JSONMainDisplayFgColor).getAsString()));
				setMainSelectionBgColor(loadColor(theConf.get(
						OcelotlConstants.JSONMainSelectionBgColor)
						.getAsString()));
				setMainSelectionFgColor(loadColor(theConf.get(
						OcelotlConstants.JSONMainSelectionFgColor)
						.getAsString()));
				setMainSelectionAlphaValue(theConf.get(
						OcelotlConstants.JSONMainSelectionAlpha).getAsInt());
				setMainDisplayAlphaValue(theConf.get(
						OcelotlConstants.JSONMainDisplayAlpha).getAsInt());
				setxAxisYResolution(theConf.get(
						OcelotlConstants.JSONXAxisYResolution).getAsInt());
				setyAxisXResolution(theConf.get(
						OcelotlConstants.JSONYAxisXResolution).getAsInt());
				setQualCurveXResolution(theConf.get(
						OcelotlConstants.JSONQualCurveXResolution).getAsInt());
				setQualCurveYResolution(theConf.get(
						OcelotlConstants.JSONQualCurveYResolution).getAsInt());
				setDichoCacheActivated(theConf.get(
						OcelotlConstants.JSONDichoCacheActivated).getAsBoolean());
			
				logger.debug("Settings values:\n");
				logger.debug("Cache activated: " + dataCacheActivated);
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
				dataCacheActivated);
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
		theConfig.addProperty(OcelotlConstants.JSONMainSelectionBgColor,
				saveColor(mainSelectionBgColor));
		theConfig.addProperty(OcelotlConstants.JSONMainSelectionFgColor,
				saveColor(mainSelectionFgColor));
		theConfig.addProperty(OcelotlConstants.JSONMainSelectionAlpha,
				mainSelectionAlphaValue);
		theConfig.addProperty(OcelotlConstants.JSONMainDisplayBgColor,
				saveColor(mainDisplayBgColor));
		theConfig.addProperty(OcelotlConstants.JSONMainDisplayFgColor,
				saveColor(mainDisplayFgColor));
		theConfig.addProperty(OcelotlConstants.JSONMainDisplayAlpha,
				mainDisplayAlphaValue);
		theConfig.addProperty(OcelotlConstants.JSONXAxisYResolution,
				xAxisYResolution);
		theConfig.addProperty(OcelotlConstants.JSONYAxisXResolution,
				yAxisXResolution);
		theConfig.addProperty(OcelotlConstants.JSONQualCurveXResolution,
				qualCurveXResolution);
		theConfig.addProperty(OcelotlConstants.JSONQualCurveYResolution,
				qualCurveYResolution);
		theConfig.addProperty(OcelotlConstants.JSONDichoCacheActivated,
				dichoCacheActivated);
		
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
		this.snapShotDirectory = snapShotDir;
	}

	public long getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(long cacheMaxSize) {
		this.cacheSize = cacheMaxSize;
	}

	public String getCacheDirectory() {
		return cacheDirectory;
	}

	public void setCacheDirectory(String cacheDir) {
		this.cacheDirectory = cacheDir;
	}

	public boolean isDataCacheActivated() {
		return dataCacheActivated;
	}

	public void setDataCacheActivated(boolean cacheActivated) {
		this.dataCacheActivated = cacheActivated;
	}

	public DatacachePolicy getCachePolicy() {
		return cachePolicy;
	}

	public void setCachePolicy(DatacachePolicy cachePolicy) {
		this.cachePolicy = cachePolicy;
	}

	public int getCacheTimeSliceNumber() {
		return cacheTimeSliceNumber;
	}

	public void setCacheTimeSliceNumber(int cacheTimeSliceNumber) {
		this.cacheTimeSliceNumber = cacheTimeSliceNumber;
	}

	public boolean isDichoCacheActivated() {
		return dichoCacheActivated;
	}

	public void setDichoCacheActivated(boolean dichoCacheActivated) {
		this.dichoCacheActivated = dichoCacheActivated;
	}

	public int getEventsPerThread() {
		return eventsPerThread;
	}

	public void setEventsPerThread(int eventsPerThread) {
		this.eventsPerThread = eventsPerThread;
	}

	public int getMaxEventProducersPerQuery() {
		return maxEventProducersPerQuery;
	}

	public void setMaxEventProducersPerQuery(int maxEventProducers) {
		this.maxEventProducersPerQuery = maxEventProducers;
	}

	public int getNumberOfThread() {
		return numberOfThread;
	}

	public void setNumberOfThread(int numberOfThread) {
		this.numberOfThread = numberOfThread;
	}

	public boolean isNormalizedCurve() {
		return normalizedCurve;
	}

	public void setNormalizedCurve(boolean normalizedCurve) {
		this.normalizedCurve = normalizedCurve;
	}

	public double getThresholdPrecision() {
		return thresholdPrecision;
	}

	public void setThresholdPrecision(double thresholdPrecision) {
		this.thresholdPrecision = thresholdPrecision;
	}

	public boolean getIncreasingQualities() {
		return increasingQualities;
	}

	public void setIncreasingQualities(boolean increasingQualities) {
		this.increasingQualities = increasingQualities;
	}

	public double getOverviewParameter() {
		return overviewParameter;
	}

	public void setOverviewParameter(double overviewParameter) {
		this.overviewParameter = overviewParameter;
	}

	public int getOverviewTimesliceNumber() {
		return overviewTimesliceNumber;
	}

	public void setOverviewTimesliceNumber(int overviewTimesliceNumber) {
		this.overviewTimesliceNumber = overviewTimesliceNumber;
	}

	public String getOverviewAggregOperator() {
		return overviewAggregOperator;
	}

	public void setOverviewAggregOperator(String overviewAggregOperator) {
		this.overviewAggregOperator = overviewAggregOperator;
	}

	public int getSnapshotYResolution() {
		return snapshotYResolution;
	}

	public void setSnapshotYResolution(int snapshotYResolution) {
		this.snapshotYResolution = snapshotYResolution;
	}

	public int getSnapshotXResolution() {
		return snapshotXResolution;
	}

	public void setSnapshotXResolution(int snapshotXResolution) {
		this.snapshotXResolution = snapshotXResolution;
	}

	public int getyAxisXResolution() {
		return yAxisXResolution;
	}

	public void setyAxisXResolution(int yAxisXResolution) {
		this.yAxisXResolution = yAxisXResolution;
	}

	public int getxAxisYResolution() {
		return xAxisYResolution;
	}

	public void setxAxisYResolution(int xAxisYResolution) {
		this.xAxisYResolution = xAxisYResolution;
	}

	public int getQualCurveXResolution() {
		return qualCurveXResolution;
	}

	public void setQualCurveXResolution(int qualCurveXResolution) {
		this.qualCurveXResolution = qualCurveXResolution;
	}

	public int getQualCurveYResolution() {
		return qualCurveYResolution;
	}

	public void setQualCurveYResolution(int qualCurveYResolution) {
		this.qualCurveYResolution = qualCurveYResolution;
	}

	public Color getOverviewSelectionFgColor() {
		return overviewSelectionFgColor;
	}

	public void setOverviewSelectionFgColor(Color overviewSelectionFgColor) {
		this.overviewSelectionFgColor = overviewSelectionFgColor;
	}

	public Color getOverviewSelectionBgColor() {
		return overviewSelectionBgColor;
	}

	public void setOverviewSelectionBgColor(Color overviewSelectionBgColor) {
		this.overviewSelectionBgColor = overviewSelectionBgColor;
	}

	public Color getOverviewDisplayFgColor() {
		return overviewDisplayFgColor;
	}

	public void setOverviewDisplayFgColor(Color overviewDisplayFgColor) {
		this.overviewDisplayFgColor = overviewDisplayFgColor;
	}

	public Color getOverviewDisplayBgColor() {
		return overviewDisplayBgColor;
	}

	public void setOverviewDisplayBgColor(Color overviewDisplayBgColor) {
		this.overviewDisplayBgColor = overviewDisplayBgColor;
	}

	public int getOverviewSelectionAlphaValue() {
		return overviewSelectionAlphaValue;
	}

	public void setOverviewSelectionAlphaValue(int overviewSelectionAlphaValue) {
		this.overviewSelectionAlphaValue = overviewSelectionAlphaValue;
	}

	public int getOverviewDisplayAlphaValue() {
		return overviewDisplayAlphaValue;
	}

	public void setOverviewDisplayAlphaValue(int overviewDisplayAlphaValue) {
		this.overviewDisplayAlphaValue = overviewDisplayAlphaValue;
	}

	public Color getMainSelectionFgColor() {
		return mainSelectionFgColor;
	}

	public void setMainSelectionFgColor(Color mainSelectionFgColor) {
		this.mainSelectionFgColor = mainSelectionFgColor;
	}

	public Color getMainSelectionBgColor() {
		return mainSelectionBgColor;
	}

	public void setMainSelectionBgColor(Color mainSelectionBgColor) {
		this.mainSelectionBgColor = mainSelectionBgColor;
	}

	public int getMainSelectionAlphaValue() {
		return mainSelectionAlphaValue;
	}

	public void setMainSelectionAlphaValue(int mainSelectionAlphaValue) {
		this.mainSelectionAlphaValue = mainSelectionAlphaValue;
	}

	public Color getMainDisplayFgColor() {
		return mainDisplayFgColor;
	}

	public void setMainDisplayFgColor(Color mainDisplayFgColor) {
		this.mainDisplayFgColor = mainDisplayFgColor;
	}

	public Color getMainDisplayBgColor() {
		return mainDisplayBgColor;
	}

	public void setMainDisplayBgColor(Color mainDisplayBgColor) {
		this.mainDisplayBgColor = mainDisplayBgColor;
	}

	public int getMainDisplayAlphaValue() {
		return mainDisplayAlphaValue;
	}

	public void setMainDisplayAlphaValue(int mainDisplayAlphaValue) {
		this.mainDisplayAlphaValue = mainDisplayAlphaValue;
	}

	public boolean isEnableOverview() {
		return enableOverview;
	}

	public void setEnableOverview(boolean enableOverview) {
		this.enableOverview = enableOverview;
	}

	public ParameterPPolicy getParameterPPolicy() {
		return parameterPPolicy;
	}

	public void setParameterPPolicy(ParameterPPolicy parameterPPolicy) {
		this.parameterPPolicy = parameterPPolicy;
	}

	public boolean isAggregateLeaves() {
		return aggregateLeaves;
	}

	public boolean setAggregateLeaves(boolean aggregateLeaves) {
		if (this.aggregateLeaves != aggregateLeaves) {
			this.aggregateLeaves = aggregateLeaves;
			return true;
		}
		return false;
	}

	public int getMaxNumberOfLeaves() {
		return maxNumberOfLeaves;
	}

	public boolean setMaxNumberOfLeaves(int maxNumberOfLeaves) {
		if (this.maxNumberOfLeaves != maxNumberOfLeaves) {
			this.maxNumberOfLeaves = maxNumberOfLeaves;
			return true;
		}
		return false;
	}
}
