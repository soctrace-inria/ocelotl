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
package fr.inria.soctrace.tools.ocelotl.core.caches;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.settings.OcelotlSettings;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class DichotomyCache {
	
	private static final Logger logger = LoggerFactory
			.getLogger(DichotomyCache.class);

	private OcelotlSettings settings;

	/**
	 * List of the cache files in the current cache directory
	 */
	protected HashMap<CacheParameters, File> cachedDichotomy;

	/**
	 * Dictionary of cache files associated to to trace
	 */
	protected HashMap<String, List<CacheParameters>> cacheIndex;

	/**
	 * Path to the current cache directory
	 */
	protected String cacheDirectory = "";

	/**
	 * Maximum size of the cache in MB (-1 == no limit size)
	 */
	protected long cacheMaxSize = OcelotlConstants.MAX_CACHESIZE;

	/**
	 * Size of the current cache
	 */
	protected long currentCacheSize;

	protected boolean validDirectory;
	
	public long getCacheMaxSize() {
		return cacheMaxSize;
	}

	public void setCacheMaxSize(long l) throws OcelotlException {
		if (l < -1) {
			throw new OcelotlException(OcelotlException.INVALID_MAX_CACHE_SIZE);
		}
		this.cacheMaxSize = l;
		settings.setCacheSize(this.cacheMaxSize);
	}

	public String getCacheDirectory() {
		return cacheDirectory;
	}

	/**
	 * Perform additional checks on the given path to test its validity
	 * 
	 * @param cacheDirectory
	 */
	public void setCacheDirectory(String cacheDirectory) {

		if (!this.cacheDirectory.equals(cacheDirectory)) {
			validDirectory = checkCacheDirectoryValidity(cacheDirectory);

			// Everything's OK, set the cache directory
			this.cacheDirectory = cacheDirectory;

			// Update settings
			settings.setCacheDirectory(this.cacheDirectory);

			// Search the directory for existing cache files
			readCachedData();
		}
	}
	
	/**
	 * Check that the cache directory is a valid one, i.e. does it exist and can
	 * it be read
	 * 
	 * @param cacheDirectory
	 *            path to the cache directory
	 * @return true if valid, false otherwise
	 */
	public boolean checkCacheDirectoryValidity(String cacheDirectory) {
		
		// Check the existence of the cache directory
		File dir = new File(cacheDirectory);
		if (!dir.exists()) {
			logger.debug("[DICHOTOMY CACHE] Cache directory (" + cacheDirectory
					+ ") does not exist and will be created now.");

			// Create the directory
			if (!dir.mkdirs()) {
				logger.error("[DICHOTOMY CACHE] Failed to create cache directory: "
						+ cacheDirectory + ".");

				if (this.cacheDirectory.isEmpty()) {
					logger.error("[DICHOTOMY CACHE] The current cache directory is still: "
							+ this.cacheDirectory);
				} else {
					validDirectory = false;
					logger.error("[DICHOTOMY CACHE] The cache will be turned off.");
				}
				return false;
			}
		}

		// Check that we have at least the reading rights
		if (!dir.canRead()) {
			logger.error("[DICHOTOMY CACHE] The application does not have the rights to read in the given directory: "
					+ cacheDirectory + ".");

			if (this.cacheDirectory.isEmpty()) {
				validDirectory = false;
				logger.error("[DICHOTOMY CACHE] The cache will be turned off.");
			} else {
				logger.error("[DICHOTOMY CACHE] The current cache directory is still: "
						+ this.cacheDirectory);
			}
			return false;
		}
		
		return true;
	}

	public boolean isValidDirectory() {
		return validDirectory;
	}

	public void setValidDirectory(boolean validDirectory) {
		this.validDirectory = validDirectory;
	}

	public DichotomyCache() {
		super();
		cachedDichotomy = new HashMap<CacheParameters, File>();
		cacheIndex = new HashMap<String, List<CacheParameters>>();
	}

	/**
	 * Set cache parameters from the configuration file
	 * 
	 * @param settings
	 *            Configuration from file
	 * @throws OcelotlException
	 */
	public void setSettings(OcelotlSettings settings) throws OcelotlException {
		this.settings = settings;
		setCacheDirectory(settings.getCacheDirectory());
	}

	/**
	 * Check parameter against the cached data parameters, and return the most
	 * appropriate data cache
	 * 
	 * @param parameters
	 *            parameters to be tested
	 * @return the File of the cached data file if a correspondence was found,
	 *         null otherwise
	 */
	public File checkCache(OcelotlParameters parameters) {
		CacheParameters cache = null;
		CacheParameters cParam = new CacheParameters(parameters);
		
		String uniqueID = buildTraceUniqueID(parameters.getTrace());
		
		// Look for the correct trace
		if (!cacheIndex.containsKey(uniqueID)) {
			logger.debug("[DICHOTOMY CACHE] No dichotomy cache was found.");
			return null;
		}

		for (CacheParameters op : cacheIndex.get(uniqueID)) {
			if (similarParameters(cParam, op)) {
				cache = op;
				break;
			}
		}

		if (cache == null) {
			logger.debug("[DICHOTOMY CACHE] No dichotomy cache was found.");
			return null;
		} else {
			logger.debug("[DICHOTOMY CACHE] Found a cache.");
			return cachedDichotomy.get(cache);
		}
	}

	/**
	 * Check if two traces are similar
	 * 
	 * @param newParam
	 *            new parameters to be tested
	 * @param cacheParam
	 *            parameters of a cached data
	 * @return true if parameters are similar to or compatible with the ones of
	 *         the cached data
	 */
	protected boolean similarParameters(CacheParameters newParam,
			CacheParameters cacheParam) {
		// Check for similar micro model types
		if (!(newParam.getMicroModelType().equals(
				cacheParam.getMicroModelType()) && (!newParam
				.getMicroModelType().equals("null"))))
			return false;
		
		// Check for similar data aggregation types
		if (!(newParam.getDataAggOperator().equals(
				cacheParam.getDataAggOperator()) && (!newParam
				.getDataAggOperator().equals("null"))))
			return false;
		
		// Check for similar time slices number
		if (newParam.getNbTimeSlice() != cacheParam.getNbTimeSlice())
			return false;
		
		// Check for similar threshold values
		if (newParam.getTreshold() != cacheParam.getTreshold())
			return false;
		
		// Check for similar normalize values
		if (newParam.isNormalized() != cacheParam.isNormalized())
			return false;

		// Check that timestamps are equal
		if (!checkCompatibleTimeStamp(newParam, cacheParam))
			return false;
		
		// Check that the event producers are the same
		if(!checkCompatibleEventProducers(newParam, cacheParam))
			return false;

		// Check that the event types are the same
		if(!checkCompatibleEventTypes(newParam, cacheParam))
			return false;
		
		return true;
	}

	/**
	 * Test if the new explored time region is compatible with the cached
	 * dichotomy values
	 * 
	 * @param newParam
	 *            parameters of the new view
	 * @param cachedParam
	 *            parameters of the cached data
	 * @return true if they are compatible, false otherwise
	 */
	protected boolean checkCompatibleTimeStamp(CacheParameters newParam,
			CacheParameters cachedParam) {

		TimeRegion newTimeRegion = new TimeRegion(newParam.getStartTimestamp(),
				newParam.getEndTimestamp());
		TimeRegion cacheTimeRegion = new TimeRegion(
				cachedParam.getStartTimestamp(), cachedParam.getEndTimestamp());

		// If timestamps are equal then OK
		if (newTimeRegion.compareTimeRegion(cacheTimeRegion)) {
			return true;
		}

		return false;
	}
	
	/**
	 * Check that all the current event producers are the same than in the
	 * tested cache
	 * 
	 * @param newParam
	 *            parameters of the new view
	 * @param cachedParam
	 *            parameters of the cached data
	 * @return true if they are compatible, false otherwise
	 */
	protected boolean checkCompatibleEventProducers(CacheParameters newParam,
			CacheParameters cachedParam) {
		if (newParam.getEventProducers().size() != cachedParam
				.getEventProducers().size())
			return false;

		for (Integer anID : newParam.getEventProducers())
			if (!cachedParam.getEventProducers().contains(anID))
				return false;

		return true;
	}
	
	/**
	 * Check that all the current event types are the same than in the tested
	 * cache
	 * 
	 * @param newParam
	 *            parameters of the new view
	 * @param cachedParam
	 *            parameters of the cached data
	 * @return true if they are compatible, false otherwise
	 */
	protected boolean checkCompatibleEventTypes(CacheParameters newParam,
			CacheParameters cachedParam) {
		if (newParam.getEventTypes().size() != cachedParam
				.getEventTypes().size())
			return false;

		for (Integer anID : newParam.getEventTypes())
			if (!cachedParam.getEventTypes().contains(anID))
				return false;

		return true;
	}

	/**
	 * Add a newly saved microscopic model to the list of cache file
	 * 
	 * @param param
	 *            Ocelotl parameters from which trace characteristics are
	 *            extracted
	 * @param aFilePath
	 *            path to the file where the data were saved
	 */
	public void saveDichotomy(OcelotlParameters oParam, String aFilePath) {
		// TODO check for cache size
		CacheParameters params = new CacheParameters(oParam);
		File aFile = new File(aFilePath);

		cachedDichotomy.put(params, aFile);
		
		String uniqueID = buildTraceUniqueID(oParam.getTrace());
		// Update dictionary
		if (!cacheIndex.containsKey(uniqueID)) {
			cacheIndex.put(uniqueID, new ArrayList<CacheParameters>());
		}
		cacheIndex.get(uniqueID).add(params);
	}

	/**
	 * Save the cache of the current trace to the specified path
	 * 
	 * @param oParam
	 *            current parameters
	 * @param destPath
	 *            path to save the file
	 */
	public void saveDichotomyCacheTo(OcelotlParameters oParam, String destPath) {
		// Get the current cache file
		CacheParameters params = new CacheParameters(oParam);
		File source = null;

		// Look for the corresponding file
		for (CacheParameters par : cachedDichotomy.keySet()) {
			if (similarParameters(params, par)) {
				source = cachedDichotomy.get(par);
			}
		}

		if (source != null) {
			File dest = new File(destPath);

			try {
				Files.copy(source.toPath(), dest.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			logger.error("[DICHOTOMY CACHE] No corresponding cache file was found");
		}
	}

	/**
	 * Delete all the files in the cache
	 */
	public void deleteCache() {
		for (File aCacheFile : cachedDichotomy.values()) {
			if (!aCacheFile.delete()) {
				logger.debug("[DICHOTOMY CACHE]: Deletion of cache file " + aCacheFile
						+ " failed.");
			}
		}
		cachedDichotomy.clear();
		currentCacheSize = 0L;
	}

	/**
	 * Load the existing cache files from the current cache directory
	 */
	private void readCachedData() {
		File workDir = new File(cacheDirectory);

		// Clear the current cache files
		cachedDichotomy.clear();
		if (workDir.exists()) {
			Iterator<File> anIT = FileUtils.iterateFiles(workDir, null, true);
			
			while (anIT.hasNext()) {
				File traceCache = anIT.next();

				if (!traceCache.getName().endsWith(
						OcelotlConstants.DichotomyCacheSuffix))
					continue;

				// Try parsing the file and get the cache parameters
				CacheParameters param = parseTraceCache(traceCache);

				// If parsing was successful
				if (param.getTraceID() != -1) {
					// Register the cache file
					cachedDichotomy.put(param, traceCache);

					logger.debug("[DICHOTOMY CACHE] Found "
							+ param.getTraceName() + " in "
							+ traceCache.toString() + ", "
							+ param.getMicroModelType() + ", "
							+ param.getDataAggOperator() + ", "
							+ param.getStartTimestamp() + ", "
							+ param.getEndTimestamp());
				}
			}

			computeCacheSize();
		} else {
			System.err.println("[DICHOTOMY CACHE] The provided cache directory ("
					+ cacheDirectory + ")does not exist");
		}
	}
	
	/**
	 * Build the cache index
	 * 
	 * @param traces
	 *            List of all the traces in database
	 */
	public void buildDictionary(List<Trace> traces) {
		cacheIndex = new HashMap<String, List<CacheParameters>>();

		for (CacheParameters aCache : cachedDichotomy.keySet()) {
			// Check if the corresponding trace still exists
			for (Trace aTrace : traces) {
				if (aCache.getTraceID() == aTrace.getId()) {
					String uniqueID = buildTraceUniqueID(aTrace);
					if (!cacheIndex.containsKey(uniqueID)) {
						cacheIndex
								.put(uniqueID, new ArrayList<CacheParameters>());
					}
					cacheIndex.get(uniqueID).add(aCache);
				}
			}
		}
		removeDeletedTraces(traces);
	}

	/**
	 * Check that every cache file have a corresponding trace in the database,
	 * and if not then delete the cache file
	 * 
	 * @param traces
	 *            list of the traces in the database
	 */
	public void removeDeletedTraces(List<Trace> traces) {
		List<CacheParameters> deletedCache = new ArrayList<CacheParameters>();

		for (CacheParameters aCache : cachedDichotomy.keySet()) {
			boolean deleted = true;

			// Check if the corresponding trace still exists
			for (Trace aTrace : traces) {
				if (aCache.getTraceID() == aTrace.getId()) {
					deleted = false;
					break;
				}
			}

			// If not delete the cache file
			if (deleted) {
				logger.debug("[DICHOTOMY CACHE]: The trace "
						+ aCache.getTraceName()
						+ " (ID = "
						+ aCache.getTraceID()
						+ ") is no longer in the database: the corresponding cache file will be deleted.");
				if (!cachedDichotomy.get(aCache).delete()) {
					logger.debug("[DICHOTOMY CACHE]: Deletion of cache file "
							+ cachedDichotomy.get(aCache).getName() + " failed.");
				}
				deletedCache.add(aCache);
			}
		}

		// Remove the deleted cache
		for (CacheParameters aCache : deletedCache) {
			cachedDichotomy.remove(aCache);
		}

		// Recompute the current cache size
		computeCacheSize();
	}

	/**
	 * Try parsing the parameters from the given file
	 * 
	 * @param aCachefile
	 *            file containing cached data
	 * @return a CacheParameters fully instantiated if successful or with init
	 *         values otherwise
	 */
	private CacheParameters parseTraceCache(File aCachefile) {
		CacheParameters params = new CacheParameters();

		if (aCachefile.canRead() && aCachefile.isFile()) {
			BufferedReader bufFileReader;

			try {
				bufFileReader = new BufferedReader(new FileReader(aCachefile));

				String line;
				// Get header
				line = bufFileReader.readLine();
				if (line != null) {
					String[] header = line.split(OcelotlConstants.CSVDelimiter);

					if (header.length != OcelotlConstants.DICHOTOMYCACHE_HEADER_NORMAL_SIZE) {
						bufFileReader.close();
						return params;
					}

					// Check that the file is a trace file // magic
					// number ??

					// Name
					params.setTraceName(header[0]);
					// Database unique ID
					params.setTraceID(Integer.parseInt(header[1]));
					// Time Aggregation Operator
					params.setMicroModelType(header[2]);
					// Space Aggregation Operator
					params.setDataAggOperator(header[3]);
					// Start timestamp
					params.setStartTimestamp(Long.parseLong(header[4]));
					// End timestamp
					params.setEndTimestamp(Long.parseLong(header[5]));
					// Number of time Slices
					params.setNbTimeSlice(Integer.parseInt(header[6]));
					// Threshold
					params.setTreshold(Double.parseDouble(header[7]));
					// Normalized
					params.setNormalized(Boolean.parseBoolean(header[8]));
				}

				// Parse event prod
				line = bufFileReader.readLine();
				if (line != null && !line.isEmpty()) {
					String[] eventProd = line
							.split(OcelotlConstants.CSVDelimiter);
					for (int i = 0; i < eventProd.length; i++)
						params.getEventProducers().add(
								Integer.parseInt(eventProd[i]));
				}
				
				// Parse event type
				line = bufFileReader.readLine();
				if (line != null && !line.isEmpty()) {
					String[] eventType = line
							.split(OcelotlConstants.CSVDelimiter);
					for (int i = 0; i < eventType.length; i++)
						params.getEventTypes().add(
								Integer.parseInt(eventType[i]));
				}
				
				bufFileReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				logger.error("Could not parse the current cache file: Invalid cache version?");
				return params;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return params;
	}

	/**
	 * Load a trace from a cache file
	 * 
	 * @param cacheFilePath
	 */
	public int loadDichotomyCache(String cacheFilePath, OcelotlParameters oParam)
			throws OcelotlException {
		File cacheFile = new File(cacheFilePath);
		CacheParameters params = parseTraceCache(cacheFile);

		// Invalid data file
		if (params.getTraceID() == -1) {
			throw new OcelotlException(OcelotlException.INVALID_CACHEFILE);
		} else {
			oParam.setTimeSlicesNumber(params.getNbTimeSlice());
			TimeRegion timeRegion = new TimeRegion(params.getStartTimestamp(),
					params.getEndTimestamp());
			oParam.setTimeRegion(timeRegion);
			oParam.setThreshold(params.getTreshold());
			
			if (!params.getMicroModelType().equals("null")) {
				oParam.setMicroModelType(params.getMicroModelType());
			}

			if (!params.getDataAggOperator().equals("null")) {
				oParam.setDataAggOperator(params.getDataAggOperator());
			}

			if (!params.getVisuAggOperator().equals("null")) {
				oParam.setVisuOperator(params.getVisuAggOperator());
			}
		}

		return params.getTraceID();
	}

	/**
	 * Check that the new file fits in the cache size limit
	 */
	public boolean checkCacheSize(long newFileSize) {
		if (cacheMaxSize > -1) {
			if (newFileSize > cacheMaxSize) {
				return false;
			}
			/*while (currentCacheSize + newFileSize > cacheMaxSize
					&& !cachedDichotomy.isEmpty()) {
				//removeCacheFile();
				computeCacheSize();
			}*/
		}
		return true;
	}

	/**
	 * Compute the current size of the cache in bytes
	 */
	public void computeCacheSize() {
		currentCacheSize = 0;
		for (File aCacheFile : cachedDichotomy.values()) {
			currentCacheSize = currentCacheSize + aCacheFile.length();
		}

		logger.debug("[DICHOTOMY CACHE] Size of the current cache is: " + currentCacheSize
				+ " bytes (" + currentCacheSize / 1000000 + " MB).");	 }

	/**
	 * Remove a cache file. The used policy is to suppress the file which has
	 * the oldest accessed time
	 */
	public void removeCacheFile() {
		// Init with current time
		FileTime oldestDate = FileTime.from(System.currentTimeMillis(), null);
		CacheParameters oldestParam = null;

		for (CacheParameters aCacheParam : cachedDichotomy.keySet()) {
			try {
				// Get the last access to the file
				Path path = cachedDichotomy.get(aCacheParam).toPath();
				BasicFileAttributes attrs;
				attrs = Files.readAttributes(path, BasicFileAttributes.class);
				FileTime currentTime = attrs.lastAccessTime();

				// If the access is older than the current oldest
				if (currentTime.compareTo(oldestDate) < 0) {
					oldestDate = currentTime;
					oldestParam = aCacheParam;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Delete oldest accessed cache
		if (!cachedDichotomy.get(oldestParam).delete()) {
			logger.debug("[DICHOTOMY CACHE]: Deletion of cache file "
					+ cachedDichotomy.get(oldestParam).getName() + " failed.");
		}

		cachedDichotomy.remove(oldestParam);
	}
	
	/**
	 * Construct an ID composed of the name of the trace (alias) and the id of
	 * the trace in database
	 * 
	 * @param aTrace
	 * @return
	 * the unique ID
	 */
	String buildTraceUniqueID(Trace aTrace) {
		return aTrace.getDbName() + "_" + aTrace.getId();
	}
}
