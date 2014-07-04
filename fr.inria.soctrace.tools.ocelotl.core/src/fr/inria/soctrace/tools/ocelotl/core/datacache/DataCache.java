package fr.inria.soctrace.tools.ocelotl.core.datacache;

import java.io.File;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class DataCache {

	private static final Logger logger = LoggerFactory.getLogger(DataCache.class);

	protected HashMap<OcelotlParameters, String> cachedData;
	protected int timeSliceMultiple = 1;

	public int getTimeSliceMultiple() {
		return timeSliceMultiple;
	}

	public DataCache() {
		super();
		cachedData = new HashMap<OcelotlParameters, String>();
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
		for (OcelotlParameters op : cachedData.keySet()) {
			if (similarParameters(parameters, op))
				return cachedData.get(op);
		}

		return "";
	}

	/**
	 * 
	 * @param op1
	 *            new parameters to be tested
	 * @param op2
	 *            parameters of a cached data
	 * @return true if parameters are similar or compatible to the one of cached
	 *         data
	 */
	protected boolean similarParameters(OcelotlParameters op1,
			OcelotlParameters op2) {
		// Are timestamps equals ?
		if (!op1.getTimeRegion().compareTimeRegion(op2.getTimeRegion()))
			return false;

		// Is the number of slices of cached data divisible by the tested number
		// of slices ?
		if (!(op2.getTimeSlicesNumber() % op1.getTimeSlicesNumber() == 0))
			return false;

		timeSliceMultiple = op2.getTimeSlicesNumber() / op1.getTimeSlicesNumber();
				
		return true;
	}

	public void saveData(OcelotlParameters param, String aFilePath) {

		// TODO check for cache size
		cachedData.put(new OcelotlParameters(param), aFilePath);
	}

	public void deleteCache()
	{
		for(String aFilePath: cachedData.values())
		{
			File cacheFile = new File(aFilePath);
			if(!cacheFile.delete())
			{
				logger.debug("DataCache: Deletion of cache file " + aFilePath + " failed.");
			}
		}
		cachedData.clear();
	}

}
