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
package fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.lpaggreg.quality.DLPQuality;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.utils.FilenameValidator;

public abstract class DataAggregationManager {

	private static final Logger logger = LoggerFactory
			.getLogger(DataAggregationManager.class);

	protected List<DLPQuality> qualities = new ArrayList<DLPQuality>();
	protected List<Double> parameters = new ArrayList<Double>();
	protected OcelotlParameters ocelotlParameters;

	public List<Double> getParameters() {
		return parameters;
	}

	public List<DLPQuality> getQualities() {
		return qualities;
	}
	
	public abstract void computeDichotomy();

	/**
	 * Get dichotomy values from cache if possible, or compute and save them
	 * otherwise
	 * 
	 * @throws OcelotlException
	 */
	public void getDichotomyValue() throws OcelotlException {
		File dichoCache;

		// If the cache is activated
		if (ocelotlParameters.getOcelotlSettings().isDichoCacheActivated()
				&& noFiltering()
				&& (dichoCache = checkForValidCache()) != null) {

			// and if If a valid cache file was found
			loadDichoCache(dichoCache);
		} else {
			computeDichotomy();
			saveDichotomyValues();
		}
	}
	
	public File checkForValidCache() {
		return ocelotlParameters.getDichotomyCache().checkCache(ocelotlParameters);
	}
	
	/**
	 * Load the parameter and quality values from a cache file
	 * @param aDichoCacheFile
	 */
	public void loadDichoCache(File aDichoCacheFile) {
		BufferedReader bufFileReader;
		try {
			bufFileReader = new BufferedReader(new FileReader(
					aDichoCacheFile.getPath()));

			String line;
			// Get header
			line = bufFileReader.readLine();

			// Read data
			while ((line = bufFileReader.readLine()) != null) {
				String[] values = line.split(OcelotlConstants.CSVDelimiter);

				parameters.add(Double.valueOf(values[0]));
				qualities.add(new DLPQuality(Double.valueOf(values[1]), Double
						.valueOf(values[2])));
			}
			bufFileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Save the current dichotomy values in a cache file
	 */
	public void saveDichotomyValues() {
		// Check that no event type or event producer was filtered out which
		// would result in an incorrect cache
		if (!ocelotlParameters.getOcelotlSettings().isDichoCacheActivated()
				|| !noFiltering()
				|| !ocelotlParameters.getDichotomyCache().isValidDirectory())
			return;

		Date theDate = new Date(System.currentTimeMillis());

		// Reformat the date to remove unsupported characters in file name (e.g.
		// ":" on windows)
		String convertedDate = new SimpleDateFormat("dd-MM-yyyy HHmmss z")
				.format(theDate);

		String fileName = ocelotlParameters.getTrace().getAlias() + "_"
				+ ocelotlParameters.getTrace().getId() + "_"
				+ ocelotlParameters.getMicroModelType() + "_" + convertedDate
				+ OcelotlConstants.DichotomyCacheSuffix;

		fileName = FilenameValidator.checkNameValidity(fileName);

		String filePath = ocelotlParameters.getDichotomyCache()
				.getCacheDirectory()
				+ "/"
				+ ocelotlParameters.getTrace().getAlias()
				+ "_"
				+ ocelotlParameters.getTrace().getId();
		
		File aFile = new File(filePath);
		
		// If the directory does not exists
		if (!aFile.exists()) {
			// Create it
			aFile.mkdir();
		}
		
		filePath = filePath + "/" + fileName;
		// Write to file,
		try {
			PrintWriter writer = new PrintWriter(filePath, "UTF-8");

			// write header (parameters)
			// traceName; microModelAggOp; dataAggOp; starTimestamp; endTimestamp;
			// timeSliceNumber; parameter; threshold
			String header = ocelotlParameters.getTrace().getAlias()
					+ OcelotlConstants.CSVDelimiter
					+ ocelotlParameters.getTrace().getId()
					+ OcelotlConstants.CSVDelimiter
					+ ocelotlParameters.getMicroModelType()
					+ OcelotlConstants.CSVDelimiter
					+ ocelotlParameters.getDataAggOperator()
					+ OcelotlConstants.CSVDelimiter
					+ ocelotlParameters.getTimeRegion().getTimeStampStart()
					+ OcelotlConstants.CSVDelimiter
					+ ocelotlParameters.getTimeRegion().getTimeStampEnd()
					+ OcelotlConstants.CSVDelimiter
					+ ocelotlParameters.getTimeSlicesNumber()
					+ OcelotlConstants.CSVDelimiter
					+ ocelotlParameters.getThreshold() 
					+ OcelotlConstants.CSVDelimiter
					+ ocelotlParameters.isNormalize() + "\n";
			
			writer.print(header);

			// Iterate over matrix and write data
			writer.print(dichoValueToCSV());

			// Close the fd
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Could not write dichotomy cache file in " + filePath);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ocelotlParameters.getDichotomyCache().saveDichotomy(ocelotlParameters, filePath);
	}

	/**
	 * Convert the parameter and quality values into CSV in the form:
	 * parameter;gain;loss
	 * 
	 * @return a string formatted in csv
	 */
	public String dichoValueToCSV() {
		StringBuffer stringBuf = new StringBuffer();
		int i;
		for (i = 0; i < qualities.size(); i++) {

			stringBuf.append(parameters.get(i) + OcelotlConstants.CSVDelimiter
					+ qualities.get(i).getGain()
					+ OcelotlConstants.CSVDelimiter
					+ qualities.get(i).getLoss() + "\n");
		}

		return stringBuf.toString();
	}

	/**
	 * Check if there are filters on event types or producers
	 * 
	 * @return true if nothing is filtered out, false otherwise
	 */
	public boolean noFiltering() {
		if (ocelotlParameters.getCurrentProducers().size() != ocelotlParameters
				.getEventProducerHierarchy().getEventProducers().size()) {
			logger.debug("At least one event producer is filtered: dichotomy cache will not be used/generated.");
			return false;
		}
		
		if (ocelotlParameters.isHasLeaveAggregated()) {
			logger.debug("Some event producers are aggregated: dichotomy cache will not be used/generated.");
			return false;
		}

		if (ocelotlParameters.getTraceTypeConfig().getTypes().size() != ocelotlParameters
				.getAllEventTypes().size()) {
			logger.debug("At least one event type is filtered: dichotomy cache will not be used/generated.");
			return false;
		}

		return true;
	}
	
}
