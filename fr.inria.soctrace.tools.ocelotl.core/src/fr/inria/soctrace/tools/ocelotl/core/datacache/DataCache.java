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
 * It stores the cached data in a given directory in files with a CSV format.
 * The first line of the file is the header, containing several parameters
 * describing the characteristics of the cached microscopic model. The rest of
 * the files are the non-null data values (one per line).
 */
public class DataCache {

	private static final Logger logger = LoggerFactory
			.getLogger(DataCache.class);

	/**
	 * List of the cache files in the current cache directory
	 */
	protected HashMap<CacheParameters, String> cachedData;

	/**
	 * Factor between the number of time slices in the current aggregation and
	 * the number of time slices of th cache model
	 */
	protected int timeSliceFactor = 1;

	/**
	 * Path to the cache directory
	 */
	protected String cacheDirectory = "";

	/**
	 * Maximum size of the cache in MB (-1 no limit size)
	 */
	protected int cacheMaxSize = -1;

	public int getCacheMaxSize() {
		return cacheMaxSize;
	}

	public void setCacheMaxSize(int cacheMaxSize) {
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
		cachedData = new HashMap<CacheParameters, String>();

		// Default cache directory is the directory "ocelotlCache" in the
		// running directory
		setCacheDirectory(ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString()
				+ "/ocelotlCache");
	}

	/**
	 * Check parameter against the cached data parameters
	 * 
	 * @param parameters
	 *            parameters to be tested
	 * @return the filepath of the cached data file if a correspondence was
	 *         found, an empty String otherwise
	 */
	public String checkCache(OcelotlParameters parameters) {

		CacheParameters cParam = new CacheParameters(parameters);
		for (CacheParameters op : cachedData.keySet()) {
			if (similarParameters(cParam, op))
				return cachedData.get(op);
		}

		return "";
	}

	/**
	 * Check if two traces are similar
	 * 
	 * @param op1
	 *            new parameters to be tested
	 * @param op2
	 *            parameters of cached data
	 * @return true if parameters are similar or compatible to the ones of the
	 *         cached data
	 */
	protected boolean similarParameters(CacheParameters op1, CacheParameters op2) {

		// Is the trace the same?
		if (!op1.traceName.equals(op2.traceName))
			return false;

		// Are timestamps equals?
		if (op1.startTimestamp != op2.startTimestamp
				|| op1.endTimestamp != op2.endTimestamp)
			return false;

		// Is the aggregation operator the same?
		if ((!op1.timeAggOperator.equals(op2.timeAggOperator))
				|| (!op1.spaceAggOperator.equals(op2.spaceAggOperator)))
			return false;

		// Is the number of slices of cached data divisible by the tested number
		// of slices?
		if (!(op2.nbTimeSlice % op1.nbTimeSlice == 0))
			return false;

		// Compute the time slice factor
		timeSliceFactor = op2.nbTimeSlice / op1.nbTimeSlice;

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
	public void saveData(OcelotlParameters oParam, String aFilePath) {
		// TODO check for cache size
		CacheParameters params = new CacheParameters(oParam);

		cachedData.put(params, aFilePath);
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
		String sourcePath = "";

		// Look for the corresponding file
		for (CacheParameters par : cachedData.keySet()) {
			if (similarParameters(params, par)) {
				sourcePath = cachedData.get(par);
			}
		}

		if (!sourcePath.isEmpty()) {
			File source = new File(sourcePath);
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
		for (String aFilePath : cachedData.values()) {
			File cacheFile = new File(aFilePath);
			if (!cacheFile.delete()) {
				logger.debug("DataCache: Deletion of cache file " + aFilePath
						+ " failed.");
			}
		}
		cachedData.clear();
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
						cachedData.put(param, traceCache.toString());

						logger.debug("Found " + param.traceName + " in "
								+ traceCache.toString() + ", "
								+ param.timeAggOperator + ", "
								+ param.spaceAggOperator + ", "
								+ param.startTimestamp + ", "
								+ param.endTimestamp);
					}
				}
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
