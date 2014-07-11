package fr.inria.soctrace.tools.ocelotl.core.datacache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

/**
 * Class handling the caching of the microscopic models.
 * 
 * It stores the cached data in a given directory in files with a CSV format.
 * The first line of the file is the header, containing several parameters
 * describing the characteristics of the cached microscopic model. The rest of
 * the files are the non-null data values (one per line).
 * 
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
	 * Maximum size of the cache in MB
	 * (-1 no limit size)
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
						+ ") does not exist and will be created now");

				// Create the directory
				if (!dir.mkdirs()) {
					if (this.cacheDirectory.isEmpty()) {
						logger.error("Failed to create cache directory: "
								+ cacheDirectory
								+ ". The current cache directory is still: "
								+ this.cacheDirectory);
					} else {
						logger.error("Failed to create cache directory: "
								+ cacheDirectory + ". The cache will be turned off.");
					}
					return;
				}
			}
			
			// Check that we have at least the reading rights
			if (!dir.canRead()) {
				if (this.cacheDirectory.isEmpty()) {
					logger.error("The application does ot have the right to read in the given: "
							+ cacheDirectory
							+ ". The current cache directory is still: "
							+ this.cacheDirectory);
				} else {
					logger.error("The application does ot have the right to read in the given: "
							+ cacheDirectory + ". The cache will be turned off.");
				}
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
	 *         found, empty string otherwise
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
	 * @return true if parameters are similar or compatible to the ones of the cached
	 *         data
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

		//Compute the time slice factor
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
	public void saveData(OcelotlParameters param, String aFilePath) {
		// TODO check for cache size
		CacheParameters params = new CacheParameters(param);

		cachedData.put(params, aFilePath);
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
	 * Load the existing cache files from the given cache directory
	 */
	private void readCachedData() {
		File workDir = new File(cacheDirectory);

		// Clear the current cache files
		cachedData.clear();
		if (workDir.exists()) {
			try {
				File[] directoryListing = workDir.listFiles();
				if (directoryListing != null) {
					for (File traceCache : directoryListing) {
						if (traceCache.canRead() && traceCache.isFile()) {
							BufferedReader bufFileReader;

							bufFileReader = new BufferedReader(new FileReader(
									traceCache));

							String line;
							// Get header
							line = bufFileReader.readLine();
							if (line != null) {
								String[] header = line
										.split(OcelotlConstants.CSVDelimiter);
								
								if (header.length != OcelotlConstants.CACHE_HEADER_NORMAL_SIZE) {
									continue;
								}
								
								// Check that the file is a trace file // magic
								// number ??

								CacheParameters param = new CacheParameters();

								// Name
								param.traceName = header[0];
								// Time Aggregation Operator
								param.timeAggOperator = header[1];
								// Space Aggregation Operator
								param.spaceAggOperator = header[2];
								// Start timestamp
								param.startTimestamp = Long
										.parseLong(header[3]);
								// End timestamp
								param.endTimestamp = Long.parseLong(header[4]);
								// number of time Slices
								param.nbTimeSlice = Integer.parseInt(header[5]);

								cachedData.put(param, traceCache.toString());

								logger.debug("Found " + param.traceName
										+ " in " + traceCache.toString() + ", "
										+ param.timeAggOperator + ", "
										+ param.spaceAggOperator + ", "
										+ param.startTimestamp + ", "
										+ param.endTimestamp);
							}
							
							bufFileReader.close();
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.err.println("The provided cache directory ("
					+ cacheDirectory + ")does not exist");
		}
	}

	/**
	 * Describe the parameters used to check if a trace can be loaded from cache
	 */
	private class CacheParameters {
		String traceName;
		long startTimestamp;
		long endTimestamp;
		int nbTimeSlice;
		String timeAggOperator;
		String spaceAggOperator;

		CacheParameters() {
			traceName = "";
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
