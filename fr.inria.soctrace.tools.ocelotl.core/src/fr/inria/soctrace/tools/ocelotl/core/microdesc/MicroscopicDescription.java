package fr.inria.soctrace.tools.ocelotl.core.microdesc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.datacache.DataCache;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.model.SimpleEventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.model.SimpleEventProducerHierarchy.SimpleEventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.queries.IteratorQueries.EventIterator;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSlice;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;
import fr.inria.soctrace.tools.ocelotl.core.utils.FilenameValidator;

public abstract class MicroscopicDescription implements IMicroscopicDescription {
	protected DataCache dataCache;
	protected DeltaManagerOcelotl dm;
	protected ArrayList<String> typeNames = new ArrayList<String>();
	protected ArrayList<EventProducer> inactiveProducers = new ArrayList<EventProducer>();
	protected ArrayList<EventProducer> activeProducers = new ArrayList<EventProducer>();
	protected OcelotlParameters parameters;

	protected EventIterator eventIterator;
	protected int count = 0;
	protected int epit = 0;
	protected int eventsNumber;
	protected OcelotlQueries ocelotlQueries;
	protected TimeSliceManager timeSliceManager;
	protected HashMap<EventProducer, EventProducer> aggregatedProducers = new HashMap<EventProducer, EventProducer>();

	private static final Logger logger = LoggerFactory
			.getLogger(MicroscopicDescription.class);

	public MicroscopicDescription() {
		
	}

	public MicroscopicDescription(OcelotlParameters param) {
		parameters = param;
		dataCache = parameters.getDataCache();
	}

	public abstract void initMatrix() throws SoCTraceException;

	/**
	 * Convert the matrix values in one String formatted in CSV
	 * 
	 * @return the String containing the matrix values
	 */
	public abstract String matrixToCSV();

	/**
	 * Initialize the matrix with zero values. Since a lot of values in the
	 * matrix are zeroes, this trick reduces the size of the cached data and
	 * improves the performances of loading data from cache.
	 * 
	 * @param eventProducers
	 *            List of event of the currently selected event producers
	 */
	public abstract void initToZero(Collection<EventProducer> eventProducers);

	/**
	 * Fill the matrix with values from the cache file
	 * 
	 * @param values
	 *            Array of Strings containing the values and the indexes of the
	 *            matrix
	 * @param sliceMultiple
	 *            used to compute the current slice number if the number of time
	 *            slices is a divisor of the number of slices of the cached data
	 */
	public abstract void rebuildMatrix(String[] values, EventProducer ep,
			int sliceMultiple);

	public DataCache getDataCache() {
		return dataCache;
	}

	public void setDataCache(DataCache dataCache) {
		this.dataCache = dataCache;
	}

	public HashMap<EventProducer, EventProducer> getAggregatedProducers() {
		return aggregatedProducers;
	}

	public void setAggregatedProducers(
			HashMap<EventProducer, EventProducer> aggregatedProducers) {
		this.aggregatedProducers = aggregatedProducers;
	}

	public TimeSliceManager getTimeSliceManager() {
		return timeSliceManager;
	}

	public void setTimeSliceManager(TimeSliceManager timeSliceManager) {
		this.timeSliceManager = timeSliceManager;
	}

	/**
	 * Fill the matrix with values from the cache multiplied by the factor
	 * corresponding to the proportional amount of the cached timeslice in the
	 * built slice
	 * 
	 * @param values
	 *            Array of Strings containing the values and the indexes of the
	 *            matrix
	 * @param ep
	 * @param currentSliceNumber
	 *            the number of the currently built time slice
	 * @param factor
	 *            the proportional factor
	 */
	public abstract void rebuildMatrixFromDirtyCache(String[] values,
			EventProducer ep, int currentSliceNumber, double factor);

	/**
	 * Load matrix values from a cache file
	 * 
	 * @param aCacheFile
	 *            the cache file
	 * @throws OcelotlException
	 * @throws InterruptedException
	 * @throws SoCTraceException
	 */
	public void loadFromCache(File aCacheFile, IProgressMonitor monitor)
			throws OcelotlException, SoCTraceException, InterruptedException {
		try {
			dm = new DeltaManagerOcelotl();
			dm.start();

			HashMap<String, EventProducer> eventProducers = new HashMap<String, EventProducer>();
			for (EventProducer ep : parameters.getCurrentProducers()) {
				eventProducers.put(String.valueOf(ep.getId()), ep);
			}

			// If no event producer is selected
			if (eventProducers.isEmpty())
				throw new OcelotlException(OcelotlException.NO_EVENT_PRODUCER);

			typeNames.clear();
			for (EventType evt : parameters.getTraceTypeConfig().getTypes()) {
				typeNames.add(evt.getName());
			}

			// If no event type is selected
			if (typeNames.isEmpty())
				throw new OcelotlException(OcelotlException.NO_EVENT_TYPE);

			// Fill the matrix with zeroes
			initToZero(eventProducers.values());

			if (monitor.isCanceled())
				return;

			// Check how to rebuild the matrix
			if (parameters.getDataCache().isRebuildDirty()) {
				switch (parameters.getDataCache().getBuildingStrategy()) {
				case DATACACHE_DATABASE:
					rebuildDirty(aCacheFile, eventProducers, monitor);
					break;

				case DATACACHE_PROPORTIONAL:
					rebuildApproximate(aCacheFile, eventProducers, monitor);
					break;

				default:
					logger.error("DATACACHE - Undefined rebuilding datacache strategy");
				}

				dm.end("Load matrix from cache (dirty)");
			} else {
				monitor.setTaskName("Rebuilding with cache data");
				rebuildClean(aCacheFile, eventProducers, monitor);
				dm.end("Load matrix from cache");
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Rebuild the matrix from a dirty cache using one of the available strategy
	 * 
	 * @param aCacheFile
	 *            the cachefile
	 * @param eventProducers
	 *            List of the event producers not filtered out
	 * @throws IOException
	 * @throws OcelotlException
	 * @throws InterruptedException
	 * @throws SoCTraceException
	 */
	public void rebuildDirty(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException, SoCTraceException,
			InterruptedException, OcelotlException {
		monitor.setTaskName("Rebuilding the matrix with the precise strategy");
		monitor.subTask("Initializing");

		// Contains the time interval of the events to query
		ArrayList<IntervalDesc> times = new ArrayList<IntervalDesc>();

		// Build an index in order to get quick access to a cached time slice
		HashMap<Integer, TimeSlice> cacheTimeSliceIndex = new HashMap<Integer, TimeSlice>();

		// Build a reverse index from time slice to cached time slice
		HashMap<Long, List<TimeSlice>> timesliceIndex = new HashMap<Long, List<TimeSlice>>();

		// Value of the biggest cache timeslice number that is used
		int maxSliceNumber = 0;

		for (TimeSlice aCachedTimeSlice : parameters.getDataCache()
				.getTimeSliceMapping().keySet()) {
			cacheTimeSliceIndex.put((int) aCachedTimeSlice.getNumber(),
					aCachedTimeSlice);

			if (maxSliceNumber < (int) aCachedTimeSlice.getNumber())
				maxSliceNumber = (int) aCachedTimeSlice.getNumber();

			// If the time slice is dirty
			if (parameters.getDataCache().getTimeSliceMapping()
					.get(aCachedTimeSlice).size() > 1
					|| aCachedTimeSlice.getTimeRegion().getTimeStampStart() < parameters
							.getTimeRegion().getTimeStampStart()
					|| aCachedTimeSlice.getTimeRegion().getTimeStampEnd() > parameters
							.getTimeRegion().getTimeStampEnd()) {

				// Create an interval corresponding to the dirty time slice
				times.add(databaseRebuild(aCachedTimeSlice));

				for (TimeSlice ts : parameters.getDataCache()
						.getTimeSliceMapping().get(aCachedTimeSlice)) {

					if (!timesliceIndex.containsKey(ts.getNumber())) {
						timesliceIndex.put(ts.getNumber(),
								new ArrayList<TimeSlice>());
					}
					timesliceIndex.get(ts.getNumber()).add(aCachedTimeSlice);
				}

			}
		}

		if (monitor.isCanceled())
			return;

		// Run a single database query with all the times of the dirty time
		// slices to rebuild the matrix

		monitor.subTask("Fetching incomplete data from database");

		computeDirtyCacheMatrix(
				new ArrayList<EventProducer>(eventProducers.values()), times,
				timesliceIndex, monitor);
		if (monitor.isCanceled())
			return;

		BufferedReader bufFileReader;
		bufFileReader = new BufferedReader(new FileReader(aCacheFile.getPath()));

		String line;
		// Get header
		line = bufFileReader.readLine();

		monitor.subTask("Filling the matrix with cache data");
		// Read data
		while ((line = bufFileReader.readLine()) != null) {
			String[] values = line.split(OcelotlConstants.CSVDelimiter);

			// If the event producer is not filtered out
			if (eventProducers.containsKey(values[1])) {
				int slice = Integer.parseInt(values[0]);

				// Since slices are sorted in increasing order in the cache file
				// once we get over the biggest slice number used to rebuild the
				// matrix, it is no longer necessary to parse the file
				if (slice > maxSliceNumber)
					break;

				// Is the current time slice part of the used datacache slice?
				if (!cacheTimeSliceIndex.keySet().contains(slice))
					continue;

				TimeSlice cachedTimeSlice = cacheTimeSliceIndex.get(slice);

				// Is it dirty (i.e. does it cover more than one new time
				// slice)?
				// Note: it should not be more than 2 since it would mean
				// that the cached timeslice is larger than a new time
				// slice
				if (!(parameters.getDataCache().getTimeSliceMapping()
						.get(cachedTimeSlice).size() > 1
						|| cachedTimeSlice.getTimeRegion().getTimeStampStart() < parameters
								.getTimeRegion().getTimeStampStart() || cachedTimeSlice
						.getTimeRegion().getTimeStampEnd() > parameters
						.getTimeRegion().getTimeStampEnd())) {
					// Not dirty
					rebuildMatrixFromDirtyCache(values,
							eventProducers.get(values[1]),
							(int) parameters.getDataCache()
									.getTimeSliceMapping().get(cachedTimeSlice)
									.get(0).getNumber(), 1.0);
				}
			}

			if (monitor.isCanceled()) {
				bufFileReader.close();
				return;
			}
		}

		bufFileReader.close();
	}

	/**
	 * Rebuild the matrix from a dirty cache using one of the available strategy
	 * 
	 * @param aCacheFile
	 *            the cachefile
	 * @param eventProducers
	 *            List of the event producers not filtered out
	 * @throws IOException
	 */
	public void rebuildApproximate(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException {

		monitor.setTaskName("Rebuilding the matrix with the fast strategy");
		monitor.subTask("Initializing");
		
		parameters.setApproximateRebuild(true);

		// Contains the proportion factor for the dirty cached time slices
		HashMap<TimeSlice, List<Double>> cachedSliceProportions = new HashMap<TimeSlice, List<Double>>();

		// Build an index in order to get quick access to a cached time slice
		HashMap<Integer, TimeSlice> cacheTimeSliceIndex = new HashMap<Integer, TimeSlice>();

		// Value of the biggest cache timeslice number that is used
		int maxSliceNumber = 0;

		for (TimeSlice aCachedTimeSlice : parameters.getDataCache()
				.getTimeSliceMapping().keySet()) {
			cacheTimeSliceIndex.put((int) aCachedTimeSlice.getNumber(),
					aCachedTimeSlice);

			if (maxSliceNumber < (int) aCachedTimeSlice.getNumber())
				maxSliceNumber = (int) aCachedTimeSlice.getNumber();

			// If the time slice is dirty
			if (parameters.getDataCache().getTimeSliceMapping()
					.get(aCachedTimeSlice).size() > 1
					|| aCachedTimeSlice.getTimeRegion().getTimeStampStart() < parameters
							.getTimeRegion().getTimeStampStart()
					|| aCachedTimeSlice.getTimeRegion().getTimeStampEnd() > parameters
							.getTimeRegion().getTimeStampEnd()) {

				// Compute the proportion factors
				cachedSliceProportions.put(aCachedTimeSlice,
						computeProportions(aCachedTimeSlice));
			}
		}

		if (monitor.isCanceled())
			return;

		monitor.subTask("Filling the matrix with cache data");

		BufferedReader bufFileReader;
		bufFileReader = new BufferedReader(new FileReader(aCacheFile.getPath()));

		String line;
		// Get header
		line = bufFileReader.readLine();

		// Read data
		while ((line = bufFileReader.readLine()) != null) {
			String[] values = line.split(OcelotlConstants.CSVDelimiter);

			// If the event producer is not filtered out
			if (eventProducers.containsKey(values[1])) {
				int slice = Integer.parseInt(values[0]);

				// Since slices are sorted in increasing order in the cache file
				// once we get over the biggest slice number used to rebuild the
				// matrix, it is no longer necessary to parse the file
				if (slice > maxSliceNumber)
					break;

				// Is the current time slice part of the used datacache slice?
				if (!cacheTimeSliceIndex.keySet().contains(slice))
					continue;

				TimeSlice cachedTimeSlice = cacheTimeSliceIndex.get(slice);

				// Is it dirty (i.e. does it cover more than one new time
				// slice)?
				// Note: it should not be more than 2 since it would mean
				// that the cached timeslice is larger than a new time
				// slice
				if (parameters.getDataCache().getTimeSliceMapping()
						.get(cachedTimeSlice).size() > 1
						|| cachedTimeSlice.getTimeRegion().getTimeStampStart() < parameters
								.getTimeRegion().getTimeStampStart()
						|| cachedTimeSlice.getTimeRegion().getTimeStampEnd() > parameters
								.getTimeRegion().getTimeStampEnd()) {

					// Multiply the cached values by the proportion of the
					// cached TS they are in
					proportionalRebuild(values, cachedTimeSlice,
							eventProducers,
							cachedSliceProportions.get(cachedTimeSlice));

				} else {
					// Not dirty
					rebuildMatrixFromDirtyCache(values,
							eventProducers.get(values[1]),
							(int) parameters.getDataCache()
									.getTimeSliceMapping().get(cachedTimeSlice)
									.get(0).getNumber(), 1.0);
				}
			}

			if (monitor.isCanceled()) {
				bufFileReader.close();
				return;
			}
		}

		bufFileReader.close();
	}

	/**
	 * Rebuild the matrix from the cache with no problem detected 
	 */
	public void rebuildClean(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException {
		BufferedReader bufFileReader = new BufferedReader(new FileReader(
				aCacheFile.getPath()));
		monitor.setTaskName("Rebuilding the matrix with a clean cache");
		monitor.subTask("Filling matrix with cache data");

		String line;
		// Get header
		line = bufFileReader.readLine();

		// Read data
		while ((line = bufFileReader.readLine()) != null) {
			String[] values = line.split(OcelotlConstants.CSVDelimiter);

			// TODO check that the values are correct (3/4 values per line)

			// If the event producer is not filtered out
			if (eventProducers.containsKey(values[1])) {
				// Fill the matrix
				rebuildMatrix(values, eventProducers.get(values[1]),
						parameters.getTimeSliceFactor());
			}

			if (monitor.isCanceled()) {
				bufFileReader.close();
				return;
			}

		}
		bufFileReader.close();
	}

	/**
	 * Compute the proportions for each new time slice the dirty cached time
	 * slice is in
	 * 
	 * @param cachedTimeSlice
	 *            the cached times slice
	 * @return a List of proportions (size should not be higher than 2)
	 */
	public ArrayList<Double> computeProportions(TimeSlice cachedTimeSlice) {
		ArrayList<Double> factors = new ArrayList<Double>();

		// For each time slice the cached time slice is in
		for (TimeSlice aNewTimeSlice : parameters.getDataCache()
				.getTimeSliceMapping().get(cachedTimeSlice)) {

			// Compute the proportion factor of the dirty time
			// slice in each slice
			// if the cached time slice starts in the current time slice
			if (cachedTimeSlice.getTimeRegion().getTimeStampStart() > aNewTimeSlice
					.getTimeRegion().getTimeStampStart()) {
				factors.add(((double) (aNewTimeSlice.getTimeRegion()
						.getTimeStampEnd() - cachedTimeSlice.getTimeRegion()
						.getTimeStampStart()))
						/ ((double) cachedTimeSlice.getTimeRegion()
								.getTimeDuration()));
			} else {
				factors.add(((double) (cachedTimeSlice.getTimeRegion()
						.getTimeStampEnd() - aNewTimeSlice.getTimeRegion()
						.getTimeStampStart()))
						/ ((double) cachedTimeSlice.getTimeRegion()
								.getTimeDuration()));
			}
		}

		return factors;
	}

	/**
	 * Create a time interval in which we get the event
	 * 
	 * @param cachedTimeSlice
	 *            Time slice from which we get the interval boundaries
	 * @return the created time interval
	 */
	public IntervalDesc databaseRebuild(TimeSlice cachedTimeSlice) {
		long startInterval;
		long endInterval;

		// If time slice begins within the time region
		if (cachedTimeSlice.getTimeRegion().getTimeStampStart() > parameters
				.getTimeRegion().getTimeStampStart())
			startInterval = cachedTimeSlice.getTimeRegion().getTimeStampStart();
		else
			startInterval = parameters.getTimeRegion().getTimeStampStart();

		// If time slice ends within the time region
		if (cachedTimeSlice.getTimeRegion().getTimeStampEnd() < parameters
				.getTimeRegion().getTimeStampEnd())
			endInterval = cachedTimeSlice.getTimeRegion().getTimeStampEnd();
		else
			endInterval = parameters.getTimeRegion().getTimeStampEnd();

		return new IntervalDesc(startInterval, endInterval);
	}

	/**
	 * Rebuild a timeslice from a dirty cached time slice
	 * 
	 * @param values
	 * @param cachedTimeSlice
	 * @param eventProducers
	 */
	public void proportionalRebuild(String[] values, TimeSlice cachedTimeSlice,
			HashMap<String, EventProducer> eventProducers,
			List<Double> proportionfactors) {

		int index = 0;
		for (TimeSlice aNewTimeSlice : parameters.getDataCache()
				.getTimeSliceMapping().get(cachedTimeSlice)) {

			rebuildMatrixFromDirtyCache(values, eventProducers.get(values[1]),
					(int) aNewTimeSlice.getNumber(),
					proportionfactors.get(index));

			index++;
		}
	}

	/**
	 * Save the matrix data to a cache file. Save only the values that are
	 * different from 0
	 */
	public void saveMatrix() {
		// Check that no event type or event producer was filtered out which
		// would result in an incomplete datacache
		if (!parameters.getOcelotlSettings().isCacheActivated() || !noFiltering()
				|| !parameters.getDataCache().isValidDirectory())
			return;

		Date theDate = new Date(System.currentTimeMillis());

		// Reformat the date to remove unsupported characters in file name (e.g.
		// ":" on windows)
		String convertedDate = new SimpleDateFormat("dd-MM-yyyy HHmmss z")
				.format(theDate);
		
		String fileName = parameters.getTrace().getAlias() + "_"
				+ parameters.getTrace().getId() + "_"
				+ parameters.getMicroModelType() + "_" + convertedDate;
		
		fileName = FilenameValidator.checkNameValidity(fileName);
			
		
		String filePath = parameters.getDataCache().getCacheDirectory() + "/"
				+ fileName;
		
		// Write to file,
		try {
			PrintWriter writer = new PrintWriter(filePath, "UTF-8");

			// write header (parameters)
			// traceName; timeAggOp; spaceAggOp starTimestamp; endTimestamp;
			// timeSliceNumber; parameter; threshold
			String header = parameters.getTrace().getAlias()
					+ OcelotlConstants.CSVDelimiter
					+ parameters.getTrace().getId()
					+ OcelotlConstants.CSVDelimiter
					+ parameters.getMicroModelType()
					+ OcelotlConstants.CSVDelimiter
					+ parameters.getVisuOperator()
					+ OcelotlConstants.CSVDelimiter
					+ parameters.getTimeRegion().getTimeStampStart()
					+ OcelotlConstants.CSVDelimiter
					+ parameters.getTimeRegion().getTimeStampEnd()
					+ OcelotlConstants.CSVDelimiter
					+ parameters.getTimeSlicesNumber()
					+ OcelotlConstants.CSVDelimiter + parameters.getParameter()
					+ OcelotlConstants.CSVDelimiter + parameters.getThreshold()
					+ "\n";
			writer.print(header);

			// Iterate over matrix and write data
			writer.print(matrixToCSV());

			// Close the fd
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Could not write cache file in " + filePath);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		parameters.getDataCache().saveData(parameters, filePath);
	}

	/**
	 * Check if there are filters on event types or producers
	 * 
	 * @return true if nothing is filtered out, false otherwise
	 */
	public boolean noFiltering() {
		if (parameters.getCurrentProducers().size() != parameters
				.getEventProducerHierarchy().getEventProducers().size()) {
			logger.debug("At least one event producer is filtered: cache will not be generated.");
			return false;
		}

		if (parameters.getTraceTypeConfig().getTypes().size() != parameters
				.getOperatorEventTypes().size()) {
			logger.debug("At least one event type is filtered: cache will not be generated.");
			return false;
		}

		return true;
	}

	/**
	 * Generate a cache matrix with the number of TimeSlice provided in settings
	 * 
	 * @return true if a cache was generated, false otherwise
	 */
	public boolean generateCache(IProgressMonitor monitor) {

		// Check that the timestamps are covering the whole trace time region
		if (parameters.getTrace().getMinTimestamp() != parameters
				.getTimeRegion().getTimeStampStart())
			return false;

		// If there is some leaves aggregated, do not generate a cache
		if (parameters.isHasLeaveAggregated())
			return false;
		
		if (parameters.getTrace().getMaxTimestamp() != parameters
				.getTimeRegion().getTimeStampEnd())
			return false;

		if (parameters.getOcelotlSettings().isCacheActivated()) {
			try {
				// Set the number of time slices for the generated cache
				int savedTimeSliceNumber = parameters.getTimeSlicesNumber();

				// If the number of generated time slices is divisible by the
				// current number of time slices
				if (parameters.getOcelotlSettings().getCacheTimeSliceNumber()
						% savedTimeSliceNumber == 0) {
					// Use the setting number
					parameters.setTimeSlicesNumber(parameters
							.getOcelotlSettings().getCacheTimeSliceNumber());
				} else if (parameters.getOcelotlSettings()
						.getCacheTimeSliceNumber() > savedTimeSliceNumber) {
					// If it is not divisible but still greater than the current
					// number of time slices
					// Then set the number of generated time by subtracting the
					// remnant from the number given in the settings
					parameters
							.setTimeSlicesNumber(parameters
									.getOcelotlSettings()
									.getCacheTimeSliceNumber()
									- (parameters.getOcelotlSettings()
											.getCacheTimeSliceNumber() % savedTimeSliceNumber));
				} else {
					// Else simply put the number of current time slices
					parameters.setTimeSlicesNumber(savedTimeSliceNumber);
				}

				logger.debug("Generating cache with "
						+ parameters.getTimeSlicesNumber() + " time slices");

				monitor.subTask("Generating cache with "
						+ parameters.getTimeSlicesNumber() + " time slices");

				// Make sure we got all event types
				List<EventType> oldEventTypes = new ArrayList<EventType>();
				for (EventType aType : parameters.getTraceTypeConfig()
						.getTypes())
					oldEventTypes.add(aType);

				parameters.getTraceTypeConfig().setTypes(
						parameters.getOperatorEventTypes());

				buildNormalMatrix(monitor);
				if (monitor.isCanceled())
					return false;

				saveMatrix();

				// Restoring parameters
				parameters.setTimeSlicesNumber(savedTimeSliceNumber);
				parameters.getTraceTypeConfig().setTypes(oldEventTypes);

				return true;
			} catch (SoCTraceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OcelotlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	public void buildNormalMatrix(IProgressMonitor monitor)
			throws SoCTraceException, InterruptedException, OcelotlException {

		monitor.setTaskName("Fetching data from database");
		initMatrix();
		initQueries();
		computeMatrix(monitor);

		if (getEventsNumber() == 0)
			throw new OcelotlException(OcelotlException.NO_EVENTS);
	}

	public void buildMicroscopicModel(final OcelotlParameters parameters,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		activeProducers = new ArrayList<EventProducer>();
		aggregatedProducers = new HashMap<EventProducer, EventProducer>(); 
		parameters.setApproximateRebuild(false);
		
		// Reset the aggregation index
		parameters.setAggregatedLeavesIndex(new HashMap<EventProducer, Integer>());
		parameters.setAggregatedEventProducers(new ArrayList<EventProducer>());
		parameters.checkLeaveAggregation();
		
		// Check whether we should aggregate the leaves of the event producers
		// hierarchy
		if (parameters.isHasLeaveAggregated()) {
			aggregateLeaveHierarchy();
		}

		initMatrix();

		// If the cache is enabled
		if (parameters.getOcelotlSettings().isCacheActivated()) {
			File cacheFile = parameters.getDataCache().checkCache(parameters);

			// If a cache was found
			if (cacheFile != null) {
				loadFromCache(cacheFile, monitor);
			} else {
				if (!generateCache(monitor)) {
					// If the cache generation was not successful
					if (monitor.isCanceled())
						return;

					// Do a normal build
					buildNormalMatrix(monitor);

					if (monitor.isCanceled())
						return;

					// Save the newly computed matrix + parameters
					dm.start();
					monitor.subTask("Saving matrix in the cache.");
					saveMatrix();
					dm.end("DATACACHE - Save the matrix to cache");
				} else {
					// If a cache was generated successfully, build a the micro
					// model from it
					if (monitor.isCanceled())
						return;

					File aCacheFile = parameters.getDataCache().checkCache(
							parameters);

					initMatrix();

					monitor.subTask("Loading from the newly generated cache");
					loadFromCache(aCacheFile, monitor);
				}
			}
		} else {
			buildNormalMatrix(monitor);
		}
		// Get the list of inactive producers
		computeInactiveProducers();
	}
	
	/**
	 * Set a list of inactive (i.e. not producing events) producers, by making the
	 * difference between the current producers and the active ones
	 */
	public void computeInactiveProducers() {
		inactiveProducers = new ArrayList<EventProducer>();
		for (EventProducer ep : parameters.getCurrentProducers())
			if (!activeProducers.contains(ep))
				inactiveProducers.add(ep);
	}

	public void total(final int rows) {
		dm.end("VECTOR COMPUTATION " + rows + " rows computed");
	}

	public List<Event> getEvents(final int size, IProgressMonitor monitor) {
		final List<Event> events = new ArrayList<Event>();
		if (monitor.isCanceled())
			return events;
		synchronized (eventIterator) {
			for (int i = 0; i < size; i++) {
				if (eventIterator.getNext(monitor) == null)
					return events;
				events.add(eventIterator.getEvent());
				eventsNumber++;
			}
		}
		return events;
	}

	public abstract void computeMatrix(IProgressMonitor monitor)
			throws SoCTraceException, InterruptedException, OcelotlException;

	protected void computeSubMatrix(final List<EventProducer> eventProducers,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		// Default time interval
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(parameters.getTimeRegion()
				.getTimeStampStart(), parameters.getTimeRegion()
				.getTimeStampEnd()));

		computeSubMatrix(eventProducers, time, monitor);
	}

	abstract public void computeSubMatrix(
			final List<EventProducer> eventProducers, List<IntervalDesc> time,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException;

	protected void computeDirtyCacheMatrix(
			final List<EventProducer> eventProducers, List<IntervalDesc> time,
			HashMap<Long, List<TimeSlice>> timesliceIndex,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		computeSubMatrix(eventProducers, time, monitor);
	}

	public synchronized int getCount() {
		count++;
		return count;
	}

	public synchronized int getEP() {
		epit++;
		return epit - 1;
	}

	public int getEventsNumber() {
		return eventsNumber;
	}

	public void setEventsNumber(int eventsNumber) {
		this.eventsNumber = eventsNumber;
	}

	public ArrayList<EventProducer> getInactiveProducers() {
		return inactiveProducers;
	}

	public void setInactiveProducers(ArrayList<EventProducer> inactiveProducers) {
		this.inactiveProducers = inactiveProducers;
	}

	public ArrayList<EventProducer> getActiveProducers() {
		return activeProducers;
	}

	public void setActiveProducers(ArrayList<EventProducer> activeProducers) {
		this.activeProducers = activeProducers;
	}

	public OcelotlParameters getOcelotlParameters() {
		return parameters;
	}

	public void setOcelotlParameters(OcelotlParameters params) {
		parameters = params;
		dataCache = params.getDataCache();
	}

	abstract public void initQueries();

	public void setOcelotlParameters(final OcelotlParameters parameters,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		this.parameters = parameters;
		count = 0;
		epit = 0;
		eventsNumber = 0;

		initQueries();
		if (monitor.isCanceled())
			return;

		buildMicroscopicModel(parameters, monitor);
	}
	
	/**
	 * Aggregate the leave of an event producer in order to reduce the memory
	 * footprint of the matrix
	 */
	public void aggregateLeaveHierarchy() {
		SimpleEventProducerHierarchy fullHierarchy = parameters
				.getEventProducerHierarchy();

		int maxHierarchyLevel = fullHierarchy.getMaxHierarchyLevel();
		int acceptedHierarchyLevel = -1;
	
		for (int i = maxHierarchyLevel; i >= 0; i--) {
		
			if (removeFilteredEP(fullHierarchy.getEventProducerNodesFromHierarchyLevel(i)).size() > parameters
					.getOcelotlSettings().getMaxNumberOfLeaves()) {
				continue;
			} else {
				acceptedHierarchyLevel = i;
				break;
			}
		}

		for (SimpleEventProducerNode newLeafProducer : fullHierarchy
				.getEventProducerNodesFromHierarchyLevel(acceptedHierarchyLevel)) {
			int numberOfAggregatedLeaves = 0;
			ArrayList<SimpleEventProducerNode> childrenNodes = fullHierarchy
					.getAllChildrenNodes(newLeafProducer);
			childrenNodes.remove(newLeafProducer);

			for (SimpleEventProducerNode aChildNode : childrenNodes) {
				if (!parameters.getCurrentProducers().contains(
						aChildNode.getMe()))
					continue;

				aggregatedProducers.put(aChildNode.getMe(),
						newLeafProducer.getMe());
				parameters.getAggregatedEventProducers()
						.add(aChildNode.getMe());
				numberOfAggregatedLeaves++;
			}

			logger.debug(numberOfAggregatedLeaves
					+ " children nodes of the following operator were aggregated: "
					+ newLeafProducer.getName() + " ("
					+ newLeafProducer.getID() + ")");

			parameters.getAggregatedLeavesIndex().put(newLeafProducer.getMe(),
					numberOfAggregatedLeaves);
		}
	}
	
	/**
	 * Remove the filtered EP from a given list of event producer nodes
	 * 
	 * @param listOfNodes
	 *            the list of EPN to be checked
	 * @return the filtered list of EPN
	 */
	public ArrayList<SimpleEventProducerNode> removeFilteredEP(
			ArrayList<SimpleEventProducerNode> listOfNodes) {
		ArrayList<SimpleEventProducerNode> unfilteredNodes = new ArrayList<SimpleEventProducerNode>();

		for (SimpleEventProducerNode aSEPN : listOfNodes) {
			if (!parameters.getCurrentProducers().contains(aSEPN.getMe()))
				continue;

			unfilteredNodes.add(aSEPN);
		}

		return unfilteredNodes;
	}
	
	/**
	 * Check whether an event producer is an aggregated leave
	 * 
	 * @param anEP
	 *            the tested event producer
	 * @return true if it is aggregated leaf, false otherwise
	 */
	public boolean isAggretedLeave(EventProducer anEP) {
		for (EventProducer aggEP : aggregatedProducers.values())
			if (aggEP == anEP)
				return true;

		return false;
	}
	
}
