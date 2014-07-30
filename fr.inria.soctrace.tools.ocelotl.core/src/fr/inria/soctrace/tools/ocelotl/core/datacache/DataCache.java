package fr.inria.soctrace.tools.ocelotl.core.datacache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

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

	/**
	 * List of the cache files in the current cache directory
	 */
	protected HashMap<CacheParameters, File> cachedData;

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
	protected int cacheMaxSize = OcelotlConstants.MAX_CACHESIZE;

	/**
	 * Size of the current data cache
	 */
	protected long currentCacheSize;

	/**
	 * Minimal ratio value that can happen
	 */
	protected double minimalRatio = OcelotlConstants.MINIMAL_TIMESLICE_RATIO;

	public long getCacheMaxSize() {
		return cacheMaxSize;
	}

	public void setCacheMaxSize(int cacheMaxSize) throws OcelotlException {
		if (cacheMaxSize < -1) {
			throw new OcelotlException(OcelotlException.INVALID_MAX_CACHE_SIZE);
		}
		this.cacheMaxSize = cacheMaxSize;
	}

	public String getCacheDirectory() {
		return cacheDirectory;
	}

	public void setCacheDirectory(String cacheDirectory) {
		if (!this.cacheDirectory.equals(cacheDirectory)) {

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
						logger.error("The cache will be turned off.");
					}
					return;
				}
			}

			// Check that we have at least the reading rights
			if (!dir.canRead()) {
				logger.error("The application does not have the rights to read in the given directory: "
						+ cacheDirectory + ".");

				if (this.cacheDirectory.isEmpty()) {
					logger.error("The cache will be turned off.");
				} else {
					logger.error("The current cache directory is still: "
							+ this.cacheDirectory);
				}
				return;
			}

			// Everything's OK, set the cache directory
			this.cacheDirectory = cacheDirectory;

			// Search the directory for existing cache files
			readCachedData();
		}
	}

	public int getTimeSliceFactor() {
		return timeSliceFactor;
	}

	public DataCache() {
		super();
		cachedData = new HashMap<CacheParameters, File>();

		// Default cache directory is the directory "ocelotlCache" in the
		// running directory
		setCacheDirectory(ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString()
				+ "/ocelotlCache");

		currentCacheSize = 0L;
	}

	/**
	 * Check parameter against the cached data parameters
	 * 
	 * @param parameters
	 *            parameters to be tested
	 * @return the File of the cached data file if a correspondence was found,
	 *         an empty String otherwise
	 */
	public File checkCache(OcelotlParameters parameters) {

		CacheParameters cParam = new CacheParameters(parameters);
		for (CacheParameters op : cachedData.keySet()) {
			if (similarParameters(cParam, op))
				return cachedData.get(op);
		}

		return null;
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

		// Is the trace the same?
		if (!newParam.traceName.equals(cacheParam.traceName))
			return false;

		// Are timestamps equals or are they included inside the cache
		// timeregion
		if (!checkCompatibleTimeStamp(newParam, cacheParam))
			return false;

		// Is the aggregation operator the same?
		if ((!newParam.timeAggOperator.equals(cacheParam.timeAggOperator))
				|| (!newParam.spaceAggOperator
						.equals(cacheParam.spaceAggOperator)))
			return false;

		// Is the number of slices of cached data divisible by the tested number
		// of slices?
		if (!(cacheParam.nbTimeSlice % newParam.nbTimeSlice == 0))
			return false;

		// Compute the time slice factor
		timeSliceFactor = cacheParam.nbTimeSlice / newParam.nbTimeSlice;

		return true;
	}

	protected boolean checkCompatibleTimeStamp(CacheParameters newParam,
			CacheParameters cachedParam) {
		TimeRegion newTimeRegion = new TimeRegion(newParam.startTimestamp,
				newParam.endTimestamp);
		TimeRegion cacheTimeRegion = new TimeRegion(newParam.startTimestamp,
				newParam.endTimestamp);

		// If timestamps are equal then OK
		if (newTimeRegion.compareTimeRegion(cacheTimeRegion))
			return true;

		// If timestamps are included in the cache time stamps
		if (newTimeRegion.containsTimeRegion(cacheTimeRegion)) {
			// compute the number of included time slices
			// time slice duration
			long timeSliceDuration = (cachedParam.endTimestamp - cachedParam.startTimestamp)
					/ cachedParam.nbTimeSlice;
			int includedTimeslice = (int) ((newParam.endTimestamp - newParam.startTimestamp) / timeSliceDuration);

			// Compute the ratio between the demanded time slice and the current
			// time slice
			double ratio = includedTimeslice / newParam.nbTimeSlice;

			if (ratio < minimalRatio) {
				return false;
			} else {
				return false;
			}
		}

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
			File[] directoryListing = workDir.listFiles();
			if (directoryListing != null) {
				for (File traceCache : directoryListing) {

					// Try parsing the file and get the cache parameters
					CacheParameters param = parseTraceCache(traceCache);

					// If parsing was successful
					if (param.traceID != -1) {
						// Register the cache file
						cachedData.put(param, traceCache);

						logger.debug("Found " + param.traceName + " in "
								+ traceCache.toString() + ", "
								+ param.timeAggOperator + ", "
								+ param.spaceAggOperator + ", "
								+ param.startTimestamp + ", "
								+ param.endTimestamp);
					}
				}
				computeCacheSize();
			}
		} else {
			System.err.println("The provided cache directory ("
					+ cacheDirectory + ")does not exist");
		}
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

					if (header.length != OcelotlConstants.CACHE_HEADER_NORMAL_SIZE) {
						bufFileReader.close();
						return params;
					}

					// Check that the file is a trace file // magic
					// number ??

					// Name
					params.traceName = header[0];
					// Database unique ID
					params.traceID = Integer.parseInt(header[1]);
					// Time Aggregation Operator
					params.timeAggOperator = header[2];
					// Space Aggregation Operator
					params.spaceAggOperator = header[3];
					// Start timestamp
					params.startTimestamp = Long.parseLong(header[4]);
					// End timestamp
					params.endTimestamp = Long.parseLong(header[5]);
					// Number of time Slices
					params.nbTimeSlice = Integer.parseInt(header[6]);
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
		if (params.traceID == -1) {
			throw new OcelotlException(OcelotlException.INVALID_CACHEFILE);
		} else {
			oParam.setTimeSlicesNumber(params.nbTimeSlice);
			TimeRegion timeRegion = new TimeRegion(params.startTimestamp,
					params.endTimestamp);
			oParam.setTimeRegion(timeRegion);

			if (!params.timeAggOperator.equals("null")) {
				oParam.setTimeAggOperator(params.timeAggOperator);
			}

			if (!params.spaceAggOperator.equals("null")) {
				oParam.setSpaceAggOperator(params.spaceAggOperator);
			}
		}

		return params.traceID;
	}

	/**
	 * Chec
	 */
	public boolean checkCacheSize(long newFileSize) {
		if (cacheMaxSize > -1) {
			if (newFileSize > cacheMaxSize) {
				return false;
			}

			while (currentCacheSize + newFileSize > cacheMaxSize) {
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

		logger.debug("Size of the current cache: " + currentCacheSize);
	}

	/**
	 * Remove a cache file
	 * 
	 * TODO decide suppression policy (biggest cache, oldest cache file)
	 */
	public void removeCacheFile() {

	}

	/**
	 * Describe the parameters used to check if a trace can be loaded from cache
	 */
	private class CacheParameters {
		String traceName;
		int traceID;
		long startTimestamp;
		long endTimestamp;
		int nbTimeSlice;
		String timeAggOperator;
		String spaceAggOperator;

		CacheParameters() {
			traceName = "";
			traceID = -1;
			startTimestamp = 0L;
			endTimestamp = 0L;
			nbTimeSlice = 0;
			timeAggOperator = "null";
			spaceAggOperator = "null";
		}

		/**
		 * Create a CacheParameter from an OcelotlParameter
		 * 
		 * @param oParam
		 *            the Ocelotl parameters
		 */
		CacheParameters(OcelotlParameters oParam) {
			traceName = oParam.getTrace().getAlias();
			traceID = oParam.getTrace().getId();

			startTimestamp = oParam.getTimeRegion().getTimeStampStart();
			endTimestamp = oParam.getTimeRegion().getTimeStampEnd();
			nbTimeSlice = oParam.getTimeSlicesNumber();
			if (oParam.getTimeAggOperator() == null) {
				timeAggOperator = "null";
			} else {
				timeAggOperator = oParam.getTimeAggOperator();
			}

			if (oParam.getSpaceAggOperator() == null) {
				spaceAggOperator = "null";
			} else {
				spaceAggOperator = oParam.getSpaceAggOperator();
			}
		}
	}

}
