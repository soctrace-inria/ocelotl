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
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacacheStrategy;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.settings.OcelotlSettings;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSlice;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceStateManager;

/**
 * Class handling the caching of the microscopic models.
 * 
 * It stores the cached data in a given directory in files using CSV. The first
 * line of the file is the header, containing several parameters describing the
 * characteristics of the cached microscopic model. The rest of the file is
 * composed of the non-null data values (one per line).
 */
public class DataCache {

	private static final Logger logger = LoggerFactory
			.getLogger(DataCache.class);

	private OcelotlSettings settings;

	/**
	 * List of the cache files in the current cache directory
	 */
	protected HashMap<CacheParameters, File> cachedData;

	/**
	 * Dictionary of cache files associated to to trace
	 */
	protected HashMap<Trace, List<CacheParameters>> cacheIndex;

	/**
	 * Factor between the number of time slices in the current aggregation and
	 * the number of time slices of the cache model
	 */
	protected int timeSliceFactor = 1;

	/**
	 * Path to the current cache directory
	 */
	protected String cacheDirectory = "";

	/**
	 * Maximum size of the cache in MB (-1 == no limit size)
	 */
	protected long cacheMaxSize = OcelotlConstants.MAX_CACHESIZE;

	/**
	 * Size of the current data cache
	 */
	protected long currentCacheSize;

	/**
	 * Minimal ratio value that can happen
	 */
	protected double minimalRatio = OcelotlConstants.MINIMAL_TIMESLICE_RATIO;

	/**
	 * Maximal ratio value of dirty time slices in a cache
	 */
	protected double maxDirtyRatio = OcelotlConstants.MAXIMAL_DIRTY_RATIO;

	/**
	 * Do we have to do some extra computation to rebuild the matrix from the
	 * cache ?
	 */
	protected boolean rebuildDirty;

	protected HashMap<TimeSlice, List<TimeSlice>> timeSliceMapping;

	protected DatacacheStrategy buildingStrategy;

	protected boolean validDirectory;
	
	protected double currentDirtyRatio;

	public DatacacheStrategy getBuildingStrategy() {
		return buildingStrategy;
	}

	public void setBuildingStrategy(DatacacheStrategy buildingStrategy) {
		this.buildingStrategy = buildingStrategy;
	}

	public HashMap<TimeSlice, List<TimeSlice>> getTimeSliceMapping() {
		return timeSliceMapping;
	}

	public void setTimeSliceMapping(
			HashMap<TimeSlice, List<TimeSlice>> timeSliceMapping) {
		this.timeSliceMapping = timeSliceMapping;
	}

	public boolean isRebuildDirty() {
		return rebuildDirty;
	}

	public void setRebuildDirty(boolean rebuildDirty) {
		this.rebuildDirty = rebuildDirty;
	}

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
			logger.debug("Cache directory (" + cacheDirectory
					+ ") does not exist and will be created now.");

			// Create the directory
			if (!dir.mkdirs()) {
				logger.error("Failed to create cache directory: "
						+ cacheDirectory + ".");

				if (this.cacheDirectory.isEmpty()) {
					logger.error("The current cache directory is still: "
							+ this.cacheDirectory);
				} else {
					validDirectory = false;
					logger.error("The cache will be turned off.");
				}
				return false;
			}
		}

		// Check that we have at least the reading rights
		if (!dir.canRead()) {
			logger.error("The application does not have the rights to read in the given directory: "
					+ cacheDirectory + ".");

			if (this.cacheDirectory.isEmpty()) {
				validDirectory = false;
				logger.error("The cache will be turned off.");
			} else {
				logger.error("The current cache directory is still: "
						+ this.cacheDirectory);
			}
			return false;
		}
		
		return true;
	}

	public int getTimeSliceFactor() {
		return timeSliceFactor;
	}

	public boolean isValidDirectory() {
		return validDirectory;
	}

	public void setValidDirectory(boolean validDirectory) {
		this.validDirectory = validDirectory;
	}

	public DataCache() {
		super();
		cachedData = new HashMap<CacheParameters, File>();
		cacheIndex = new HashMap<Trace, List<CacheParameters>>();

		buildingStrategy = DatacacheStrategy.DATACACHE_DATABASE;
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
		setCacheMaxSize(settings.getCacheSize());
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
		rebuildDirty = false;
		CacheParameters cache = null;
		currentDirtyRatio = Double.MAX_VALUE;
		double bestRatio = Double.MAX_VALUE;

		CacheParameters cParam = new CacheParameters(parameters);
		// Look for the correct trace
		if (!cacheIndex.containsKey(parameters.getTrace())) {
			logger.debug("No datacache was found");
			return null;
		}
		
		for (CacheParameters op : cacheIndex.get(parameters.getTrace())) {
			if (similarParameters(cParam, op)) {
				// If first iteration
				if (cache == null) {
					// Init
					cache = op;
					bestRatio = currentDirtyRatio;
				} else {
					// If the dirty ratio of the cache is better than the
					// current best
					if (bestRatio < currentDirtyRatio) {
						// Set it as the best candidate
						cache = op;
						bestRatio = currentDirtyRatio;
					}
				}
				// If perfect cache
				if (currentDirtyRatio == 0) {
					cache = op;
					// There is no better solution so stop looking
					break;
				}
			}
		}
		
		if (cache == null) {
			logger.debug("No datacache was found");
			return null;
		} else {
			similarParameters(cParam, cache);
			parameters.setTimeSliceFactor(timeSliceFactor);
			return cachedData.get(cache);
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
		
		if (!(newParam.getMicroModelType().equals(
				cacheParam.getMicroModelType()) && (!newParam
				.getMicroModelType().equals("null"))))
			return false;

		// Are timestamps equal or are they included inside the cache
		// timeregion
		if (!checkCompatibleTimeStamp(newParam, cacheParam))
			return false;

		// Compute the time slice factor
		timeSliceFactor = cacheParam.getNbTimeSlice()
				/ newParam.getNbTimeSlice();

		return true;
	}

	/**
	 * Test if the new explored time region is compatible with the cached data
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
		
		currentDirtyRatio = Double.MAX_VALUE;
		rebuildDirty = false;

		// If timestamps are equal then OK
		if (newTimeRegion.compareTimeRegion(cacheTimeRegion)) {
			// Is the number of slices of cached data divisible by the tested
			// number of slices?
			if ((cachedParam.getNbTimeSlice() % newParam.getNbTimeSlice() == 0)){
				timeSliceMapping = null;
				logger.debug("[DATACACHE] Found full compatibility");
				currentDirtyRatio = 0;
				return true;
			}
		}

		// If timestamps are included in the cache time stamps
		if (cacheTimeRegion.containsTimeRegion(newTimeRegion)) {
			// Compute the duration of a time slice in the cache
			long timeSliceDuration = (cachedParam.getEndTimestamp() - cachedParam
					.getStartTimestamp()) / cachedParam.getNbTimeSlice();

			// Compute the number of cached time slices included in the new time
			// region
			int includedTimeslice = (int) ((newParam.getEndTimestamp() - newParam
					.getStartTimestamp()) / timeSliceDuration);

			// Compute the ratio between the demanded time slice and the current
			// time slice
			double ratio = includedTimeslice / newParam.getNbTimeSlice();

			// If we have enough timeslices to build the zoomed view from the
			// cache
			if (ratio < minimalRatio)
				return false;

			TimeSliceStateManager cachedTsManager = new TimeSliceStateManager(
					cacheTimeRegion, cachedParam.getNbTimeSlice());
			TimeSliceStateManager newTsManager = new TimeSliceStateManager(
					newTimeRegion, newParam.getNbTimeSlice());

			return computeDirtyTimeSlice(newParam, cachedParam, newTsManager,
					cachedTsManager);
		}

		return false;
	}

	/**
	 * "Dirty" time slices are time slices of the cache that do not fit inside a
	 * time slice of the new view (i.e. they are used to build at least two new
	 * time slices)
	 * 
	 * @param newParam
	 * @param cachedParam
	 * @param newTsManager
	 * @param cachedTsManager
	 * @return the ratio of dirty cache time slices over the total of used time
	 *         slices in cache
	 */
	public boolean computeDirtyTimeSlice(CacheParameters newParam,
			CacheParameters cachedParam, TimeSliceStateManager newTsManager,
			TimeSliceStateManager cachedTsManager) {
		double dirtyTimeslicesNumber = 0.0;
		double usedCachedTimeSlices = 0.0;

		List<TimeSlice> cachedTimeSlice = cachedTsManager.getTimeSlices();
		List<TimeSlice> newTimeSlice = newTsManager.getTimeSlices();

		HashMap<TimeSlice, List<TimeSlice>> tmpTimeSliceMapping = new HashMap<TimeSlice, List<TimeSlice>>();

		for (TimeSlice aCachedTimeSlice : cachedTimeSlice) {
			// If the time slice is inside the new time region
			if (!(aCachedTimeSlice.getTimeRegion().getTimeStampEnd() < newParam
					.getStartTimestamp())
					&& !(aCachedTimeSlice.getTimeRegion().getTimeStampStart() > newParam
							.getEndTimestamp())) {

				usedCachedTimeSlices++;

				for (TimeSlice aNewTimeSlice : newTimeSlice) {
					// Is the cached time slice is at least partly inside a new
					// time slice ?
					if (aNewTimeSlice.startIsInsideMe(aCachedTimeSlice
							.getTimeRegion().getTimeStampStart())
							|| aNewTimeSlice.startIsInsideMe(aCachedTimeSlice
									.getTimeRegion().getTimeStampEnd())) {

						if (!tmpTimeSliceMapping.containsKey(aCachedTimeSlice)) {
							tmpTimeSliceMapping.put(aCachedTimeSlice,
									new ArrayList<TimeSlice>());
						}
						tmpTimeSliceMapping.get(aCachedTimeSlice).add(
								aNewTimeSlice);
					}
				}

				// If a cached time slice is used in more than one new slice
				// then it is dirty
				if (tmpTimeSliceMapping.get(aCachedTimeSlice).size() > 1
						|| aCachedTimeSlice.getTimeRegion().getTimeStampStart() < newParam
								.getStartTimestamp()
						|| aCachedTimeSlice.getTimeRegion().getTimeStampEnd() > newParam
								.getEndTimestamp()) {
					dirtyTimeslicesNumber++;
				}
			}
		}

		// Proportion of dirty time slices in the part of the cache used to
		// rebuild the matrix
		currentDirtyRatio = (dirtyTimeslicesNumber / usedCachedTimeSlices);

		// No dirty time slice
		if (currentDirtyRatio == 0) {
			timeSliceMapping = null;
			logger.debug("[DATACACHE] Found " + dirtyTimeslicesNumber
					+ " dirty Timeslices among " + usedCachedTimeSlices
					+ " used cache time slices" + " (i.e. a ratio of "
					+ currentDirtyRatio + ").");
			rebuildDirty = false;
			return true;
		}

		// Set the flag for rebuild from dirty
		if (currentDirtyRatio > 0)
			rebuildDirty = true;

		// If the ratio is not over the max
		if (currentDirtyRatio <= maxDirtyRatio) {
			// Precompute stuff
			if (timeSliceMapping != null)
				timeSliceMapping.clear();

			timeSliceMapping = tmpTimeSliceMapping;

			logger.debug("[DATACACHE] Found " + dirtyTimeslicesNumber
					+ " dirty Timeslices among " + usedCachedTimeSlices
					+ " used cache time slices" + " (i.e. a ratio of "
					+ currentDirtyRatio + ").");
			return true;
		}

		rebuildDirty = false;
		return false;
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
	public void saveData(OcelotlParameters oParam, String aFilePath) {
		// TODO check for cache size
		CacheParameters params = new CacheParameters(oParam);
		File aFile = new File(aFilePath);

		cachedData.put(params, aFile);
		
		// Update dictionary
		if (!cacheIndex.containsKey(oParam.getTrace())) {
			cacheIndex
					.put(oParam.getTrace(), new ArrayList<CacheParameters>());
		}
		cacheIndex.get(oParam.getTrace()).add(params);
	}

	/**
	 * Save the cache of the current trace to the specified path
	 * 
	 * @param oParam
	 *            current parameters
	 * @param destPath
	 *            path to save the file
	 */
	public void saveDataCacheTo(OcelotlParameters oParam, String destPath) {
		// Get the current cache file
		CacheParameters params = new CacheParameters(oParam);
		File source = null;

		// Look for the corresponding file
		for (CacheParameters par : cachedData.keySet()) {
			if (similarParameters(params, par)) {
				source = cachedData.get(par);
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
			logger.error("No corresponding cache file was found");
		}
	}

	/**
	 * Delete all the files in the cache
	 */
	public void deleteCache() {
		for (File aCacheFile : cachedData.values()) {
			if (!aCacheFile.delete()) {
				logger.debug("DataCache: Deletion of cache file " + aCacheFile
						+ " failed.");
			}
		}
		cachedData.clear();
		currentCacheSize = 0L;
	}

	/**
	 * Load the existing cache files from the current cache directory
	 */
	private void readCachedData() {
		File workDir = new File(cacheDirectory);

		// Clear the current cache files
		cachedData.clear();
		if (workDir.exists()) {
			Iterator<File> anIT = FileUtils.iterateFiles(workDir, null, true);
			
			while (anIT.hasNext()) {
				File traceCache = anIT.next();

				// Try parsing the file and get the cache parameters
				CacheParameters param = parseTraceCache(traceCache);

				// If parsing was successful
				if (param.getTraceID() != -1) {
					// Register the cache file
					cachedData.put(param, traceCache);

					logger.debug("Found " + param.getTraceName() + " in "
							+ traceCache.toString() + ", "
							+ param.getMicroModelType() + ", "
							+ param.getVisuAggOperator() + ", "
							+ param.getStartTimestamp() + ", "
							+ param.getEndTimestamp());
				}
			}
			
			computeCacheSize();
		} else {
			System.err.println("The provided cache directory ("
					+ cacheDirectory + ") does not exist");
		}
	}
	
	/**
	 * Build the cache index
	 * 
	 * @param traces
	 *            List of all the traces in database
	 */
	public void buildDictionary(List<Trace> traces) {
		cacheIndex = new HashMap<Trace, List<CacheParameters>>();

		for (CacheParameters aCache : cachedData.keySet()) {
			// Check if the corresponding trace still exists
			for (Trace aTrace : traces) {
				if (aCache.getTraceID() == aTrace.getId()) {
					if (!cacheIndex.containsKey(aTrace)) {
						cacheIndex
								.put(aTrace, new ArrayList<CacheParameters>());
					}
					cacheIndex.get(aTrace).add(aCache);
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

		for (CacheParameters aCache : cachedData.keySet()) {
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
				logger.debug("DataCache: The trace "
						+ aCache.getTraceName()
						+ " (ID = "
						+ aCache.getTraceID()
						+ ") is no longer in the database: the corresponding cache file will be deleted.");
				if (!cachedData.get(aCache).delete()) {
					logger.debug("DataCache: Deletion of cache file "
							+ cachedData.get(aCache).getName() + " failed.");
				}
				deletedCache.add(aCache);
			}
		}

		// Remove the deleted cache
		for (CacheParameters aCache : deletedCache) {
			cachedData.remove(aCache);
		}

		// Check for empty directories
		File workDir = new File(cacheDirectory);
		if (workDir.exists()) {
			File[] directoryListing = workDir.listFiles();
			if (directoryListing != null) {
				for (File traceCacheDir : directoryListing) {
					if (traceCacheDir.isDirectory()) {
						// If it is empty
						if (traceCacheDir.list().length == 0)
							// Delete it
							traceCacheDir.delete();
					}
				}
			}
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

					if (header.length != OcelotlConstants.DATACACHE_HEADER_NORMAL_SIZE) {
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
					params.setVisuAggOperator(header[3]);
					// Start timestamp
					params.setStartTimestamp(Long.parseLong(header[4]));
					// End timestamp
					params.setEndTimestamp(Long.parseLong(header[5]));
					// Number of time Slices
					params.setNbTimeSlice(Integer.parseInt(header[6]));
				}

				bufFileReader.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
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
	public int loadDataCache(String cacheFilePath, OcelotlParameters oParam)
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
			while (currentCacheSize + newFileSize > cacheMaxSize
					&& !cachedData.isEmpty()) {
				removeCacheFile();
				computeCacheSize();
			}
		}
		return true;
	}

	/**
	 * Compute the current size of the cache in bytes
	 */
	public void computeCacheSize() {
		currentCacheSize = 0;
		for (File aCacheFile : cachedData.values()) {
			currentCacheSize = currentCacheSize + aCacheFile.length();
		}

		logger.debug("Size of the current cache is: " + currentCacheSize
				+ " bytes (" + currentCacheSize / 1000000 + " MB).");	 }

	/**
	 * Remove a cache file. The used policy is to suppress the file which has
	 * the oldest accessed time
	 */
	public void removeCacheFile() {
		// Init with current time
		FileTime oldestDate = FileTime.from(System.currentTimeMillis(), null);
		CacheParameters oldestParam = null;

		for (CacheParameters aCacheParam : cachedData.keySet()) {
			try {
				// Get the last access to the file
				Path path = cachedData.get(aCacheParam).toPath();
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
		if (!cachedData.get(oldestParam).delete()) {
			logger.debug("DataCache: Deletion of cache file "
					+ cachedData.get(oldestParam).getName() + " failed.");
		}

		cachedData.remove(oldestParam);
	}
}
