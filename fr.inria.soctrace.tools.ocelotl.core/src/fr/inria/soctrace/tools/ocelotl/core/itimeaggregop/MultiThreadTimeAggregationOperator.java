/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
 * overview by using aggregation techniques
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.core.itimeaggregop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacacheStrategy;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.queries.IteratorQueries.EventIterator;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSlice;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public abstract class MultiThreadTimeAggregationOperator {

	private static final Logger logger = LoggerFactory
			.getLogger(MultiThreadTimeAggregationOperator.class);

	// protected TimeSliceStateManager timeSliceManager;
	protected EventIterator eventIterator;
	protected int count = 0;
	protected int epit = 0;
	protected DeltaManagerOcelotl dm;
	protected int eventsNumber;
	protected OcelotlParameters parameters;
	protected OcelotlQueries ocelotlQueries;
	protected ArrayList<String> typeNames = new ArrayList<String>();

	abstract protected void computeMatrix(IProgressMonitor monitor)
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

	abstract protected void computeSubMatrix(
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
	public abstract void initMatrixToZero(
			Collection<EventProducer> eventProducers);

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

	public synchronized int getCount() {
		count++;
		return count;
	}

	public synchronized int getEP() {
		epit++;
		return epit - 1;
	}

	public OcelotlParameters getOcelotlParameters() {
		return parameters;
	}

	// public TimeSliceStateManager getTimeSlicesManager() {
	// return timeSliceManager;
	// }

	abstract public void initQueries();

	abstract protected void initVectors() throws SoCTraceException;

	public void setOcelotlParameters(final OcelotlParameters parameters,
			IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		this.parameters = parameters;
		count = 0;
		epit = 0;
		// timeSliceManager = new TimeSliceStateManager(getOcelotlParameters()
		// .getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		initQueries();
		initVectors();
		if (monitor.isCanceled())
			return;

		// If the cache is enabled
		if (parameters.getDataCache().isCacheActive()) {
			File cacheFile = parameters.getDataCache().checkCache(parameters);

			// If a valid cache file was found
			if (cacheFile != null) {
				monitor.setTaskName("Loading data from cache");
				loadFromCache(cacheFile, monitor);
			} else {
				monitor.setTaskName("Loading data from database");
				computeMatrix(monitor);

				if (monitor.isCanceled())
					return;

				if (eventsNumber == 0)
					throw new OcelotlException(OcelotlException.NO_EVENTS);

				// Save the newly computed matrix + parameters
				dm.start();
				monitor.subTask("Saving matrix in the cache.");
				//saveMatrix();
				dm.end("DATACACHE - Save the matrix to cache");
			}
		} else {
			monitor.setTaskName("Loading data from database");
			computeMatrix(monitor);

			if (eventsNumber == 0)
				throw new OcelotlException(OcelotlException.NO_EVENTS);
		}
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

	/**
	 * Save the matrix data to a cache file. Save only the values that are
	 * different from 0
	 */
	public void saveMatrix() {
		// Check that no event type or event producer was filtered out which
		// would result in an incomplete datacache
		if (!parameters.getDataCache().isCacheActive() || !noFiltering())
			return;

		Date convertedDate = new Date(System.currentTimeMillis() * 1000);

		String filePath = parameters.getDataCache().getCacheDirectory() + "/"
				+ parameters.getTrace().getAlias() + "_"
				+ parameters.getTrace().getId() + "_"
				+ parameters.getTimeAggOperator() + "_" + convertedDate;

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
					+ parameters.getTimeAggOperator()
					+ OcelotlConstants.CSVDelimiter
					+ parameters.getSpaceAggOperator()
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
	 * Load matrix values from a cache file
	 * 
	 * @param aCacheFile
	 *            the cache file
	 * @throws OcelotlException
	 */
	public void loadFromCache(File aCacheFile, IProgressMonitor monitor)
			throws OcelotlException {
		try {
			dm = new DeltaManagerOcelotl();
			dm.start();

			HashMap<String, EventProducer> eventProducers = new HashMap<String, EventProducer>();
			for (EventProducer ep : parameters.getEventProducers()) {
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
			initMatrixToZero(eventProducers.values());

			if (monitor.isCanceled())
				return;

			// Check how to rebuild the matrix
			if (parameters.getDataCache().isRebuildDirty()) {
				monitor.setTaskName("Rebuilding with strategy "
						+ parameters.getDataCache().getBuildingStrategy());
				rebuildDirtyMatrix(aCacheFile, eventProducers, monitor);
				dm.end("Load matrix from cache (dirty)");
			} else {
				monitor.setTaskName("Rebuilding with cache data");
				rebuildNormalMatrix(aCacheFile, eventProducers, monitor);
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

	public void rebuildNormalMatrix(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException {
		BufferedReader bufFileReader = new BufferedReader(new FileReader(
				aCacheFile.getPath()));

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
				rebuildMatrix(values, eventProducers.get(values[1]), parameters
						.getDataCache().getTimeSliceFactor());
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
	public void rebuildDirtyMatrix(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException {

		// Contains the time interval of the events to query
		ArrayList<IntervalDesc> times = new ArrayList<IntervalDesc>();

		// Contains the proportion factor for the dirty cached time slices
		HashMap<TimeSlice, List<Double>> cachedSliceProportions = new HashMap<TimeSlice, List<Double>>();

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
				switch (parameters.getDataCache().getBuildingStrategy()) {

				case DATACACHE_PROPORTIONAL:
					// Compute the proportion factors
					cachedSliceProportions.put(aCachedTimeSlice,
							computeProportions(aCachedTimeSlice));
					break;

				case DATACACHE_DATABASE:
					// Create an interval corresponding to the dirty time slice
					times.add(databaseRebuild(aCachedTimeSlice));

					for (TimeSlice ts : parameters.getDataCache()
							.getTimeSliceMapping().get(aCachedTimeSlice)) {

						if (!timesliceIndex.containsKey(ts.getNumber())) {
							timesliceIndex.put(ts.getNumber(),
									new ArrayList<TimeSlice>());
						}

						timesliceIndex.get(ts.getNumber())
								.add(aCachedTimeSlice);
					}
					break;
				}
			}
		}

		if (monitor.isCanceled())
			return;

		// If strategy is DATACACHE_DATABASE
		// Run a single database query with all the times of the dirty time
		// slices to rebuild the matrix
		if (parameters.getDataCache().getBuildingStrategy() == DatacacheStrategy.DATACACHE_DATABASE) {
			try {
				monitor.subTask("Fetching dirty data from database");
				computeDirtyCacheMatrix(new ArrayList<EventProducer>(
						eventProducers.values()), times, timesliceIndex,
						monitor);
				if (monitor.isCanceled())
					return;
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
				if (parameters.getDataCache().getTimeSliceMapping()
						.get(cachedTimeSlice).size() > 1
						|| cachedTimeSlice.getTimeRegion().getTimeStampStart() < parameters
								.getTimeRegion().getTimeStampStart()
						|| cachedTimeSlice.getTimeRegion().getTimeStampEnd() > parameters
								.getTimeRegion().getTimeStampEnd()) {

					switch (parameters.getDataCache().getBuildingStrategy()) {
					// Strategy one
					// Multiply the cached values by the proportion of the
					// cached TS they are in
					case DATACACHE_PROPORTIONAL:
						proportionalRebuild(values, cachedTimeSlice,
								eventProducers,
								cachedSliceProportions.get(cachedTimeSlice));
						break;

					// Strategy two
					// Get the values from the db
					case DATACACHE_DATABASE:
						// Do nothing
						break;

					default:
						logger.error("DATACACHE - Undefined rebuilding datacache strategy");
					}
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
	 * Check if there are filters on event types or producers
	 * 
	 * @return true if nothing is filtered out, false otherwise
	 */
	public boolean noFiltering() {
		if (parameters.getEventProducers().size() != parameters
				.getEventProducerHierarchy().getEventProducers().size()) {
			logger.debug("At least one event producer is filtered: no cache will be saved.");
			return false;
		}

		if (parameters.getTraceTypeConfig().getTypes().size() != parameters
				.getOperatorEventTypes().size()) {
			logger.debug("At least one event type is filtered: no cache will be saved.");
			return false;
		}

		return true;
	}
}
