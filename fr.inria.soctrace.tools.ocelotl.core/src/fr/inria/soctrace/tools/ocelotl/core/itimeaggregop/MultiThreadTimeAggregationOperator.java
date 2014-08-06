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
import java.util.HashMap;
import java.util.List;

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
	
	//protected TimeSliceStateManager timeSliceManager;
	protected EventIterator eventIterator;
	protected int count = 0;
	protected int epit = 0;
	protected DeltaManagerOcelotl dm;
	protected int eventsNumber;
	protected OcelotlParameters parameters;
	protected OcelotlQueries ocelotlQueries;
	protected ArrayList<String> typeNames = new ArrayList<String>();
	
	abstract protected void computeMatrix() throws SoCTraceException,
			InterruptedException, OcelotlException;

	abstract protected void computeSubMatrix(
			final List<EventProducer> eventProducers) throws SoCTraceException,
			InterruptedException, OcelotlException;
	
	abstract protected void computeSubMatrix(
			final List<EventProducer> eventProducers, List<IntervalDesc> time) throws SoCTraceException,
			InterruptedException, OcelotlException;
	
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
	 *            Array of Strings containing the values and the indexes of the matrix
	 * @param sliceMultiple
	 *            used to compute the current slice number if the number of time
	 *            slices is a divisor of the number of slices of the cached data
	 */
	public abstract void rebuildMatrix(String[] values, EventProducer ep, int sliceMultiple);

	/**
	 * Fill the matrix with values from the cache multiplied by the factor
	 * correponding to the proportional amount of the cached timeslice in the
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

//	public TimeSliceStateManager getTimeSlicesManager() {
//		return timeSliceManager;
//	}

	abstract public void initQueries();

	abstract protected void initVectors() throws SoCTraceException;

	public void setOcelotlParameters(final OcelotlParameters parameters)
			throws SoCTraceException, InterruptedException, OcelotlException {
		this.parameters = parameters;
		count = 0;
		epit = 0;
		// timeSliceManager = new TimeSliceStateManager(getOcelotlParameters()
		// .getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		initQueries();
		initVectors();

		if (parameters.getDataCache().isCacheActive()) {
			File cacheFile = parameters.getDataCache().checkCache(parameters);

			// if there is a file and it is valid
			if (cacheFile != null) {
				loadFromCache(cacheFile);
			} else {
				computeMatrix();

				if (eventsNumber == 0)
					throw new OcelotlException(OcelotlException.NO_EVENTS);

				// Save the newly computed matrix + parameters
				dm.start();
				saveMatrix();
				dm.end("DATACACHE - Save the matrix to cache");
			}
		} else {
			computeMatrix();

			if (eventsNumber == 0)
				throw new OcelotlException(OcelotlException.NO_EVENTS);
		}
	}

	public void total(final int rows) {
		dm.end("VECTOR COMPUTATION " + rows + " rows computed");
	}

	public List<Event> getEvents(final int size) {
		final List<Event> events = new ArrayList<Event>();
		synchronized (eventIterator) {
			for (int i = 0; i < size; i++) {
				if (eventIterator.getNext() == null)
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

		String filePath = parameters.getDataCache().getCacheDirectory() + "/"
				+ parameters.getTrace().getAlias() + "_"
				+ parameters.getTrace().getId() + "_"
				+ System.currentTimeMillis();

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
	public void loadFromCache(File aCacheFile) throws OcelotlException {
		try {
			dm = new DeltaManagerOcelotl();
			dm.start();

			HashMap<String, EventProducer> eventProducers = new HashMap<String, EventProducer>();
			for (EventProducer ep : parameters.getEventProducers()) {
				eventProducers.put(String.valueOf(ep.getId()), ep);
			}
			
			// If no event producer is selected
			if(eventProducers.isEmpty())
				throw new OcelotlException(OcelotlException.NO_EVENT_PRODUCER);
		
			typeNames.clear();
			for (EventType evt : parameters.getTraceTypeConfig().getTypes()) {
				typeNames.add(evt.getName());
			}
			// If no event type is selected
			if(typeNames.isEmpty())
				throw new OcelotlException(OcelotlException.NO_EVENT_TYPE);

			// Fill the matrix with zeroes
			initMatrixToZero(eventProducers.values());
			
			if(parameters.getDataCache().isRebuildDirty())
			{
				rebuildDirtyMatrix(aCacheFile, eventProducers);
				return;
			}
				
			BufferedReader bufFileReader = new BufferedReader(new FileReader(
					aCacheFile.getPath()));
			
			String line;
			// Get header
			line = bufFileReader.readLine();

			// Read data
			while ((line = bufFileReader.readLine()) != null) {
				String[] values = line.split(OcelotlConstants.CSVDelimiter);

				//TODO check that the values are correct (3/4 values per line)
				
				// If the event producer is not filtered out
				if (eventProducers.containsKey(values[1])) {
					// Fill the matrix
					rebuildMatrix(values, eventProducers.get(values[1]),
							parameters.getDataCache().getTimeSliceFactor());
				}
			}
			bufFileReader.close();
			dm.end("Load matrix from cache");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void rebuildDirtyMatrix(File aCacheFile,
			HashMap<String, EventProducer> eventProducers) throws IOException {

		BufferedReader bufFileReader;
		bufFileReader = new BufferedReader(new FileReader(aCacheFile.getPath()));
		ArrayList<Integer> rebuiltTimeSlice = new ArrayList<Integer>();
		
		String line;
		// Get header
		line = bufFileReader.readLine();

		// Read data
		while ((line = bufFileReader.readLine()) != null) {
			String[] values = line.split(OcelotlConstants.CSVDelimiter);

			// If the event producer is not filtered out
			if (eventProducers.containsKey(values[1])) {
				int slice = Integer.parseInt(values[0]);

				for (TimeSlice cachedTimeSlice : parameters.getDataCache()
						.getTimeSliceMapping().keySet()) {
					// Look for the current time slice
					if (cachedTimeSlice.getNumber() == slice) {

						// Is it dirty (does it cover to more than one new time
						// slice?)
						// Note it should not be more than 2 since it would mean
						// that the cached timeslice is larger than a new time
						// slice
						if (parameters.getDataCache().getTimeSliceMapping()
								.get(cachedTimeSlice).size() > 1) {

							switch (parameters.getDataCache()
									.getBuildingStrategy()) {
								// Strategy one
								// Compute (or get precomputed) factor
								case DATACACHE_PROPORTIONAL:
									proportionalRebuild(values, cachedTimeSlice,
									eventProducers);
									break;
									
								// Strategy two
								// Get the values from the db
								case DATACACHE_DATABASE:
								if (!rebuiltTimeSlice.contains(slice)) {
									rebuiltTimeSlice.add(slice);
									databaseRebuild(values, cachedTimeSlice,
											eventProducers);
								}
									break;
									
								default:
									logger.error("Undefined rebuilding datacache strategy");
							}

						} else {
							// Not dirty
							rebuildMatrixFromDirtyCache(
									values,
									eventProducers.get(values[1]),
									(int) parameters.getDataCache()
											.getTimeSliceMapping()
											.get(cachedTimeSlice).get(0)
											.getNumber(), 1.0);
						}
					}
				}
			}
		}
		bufFileReader.close();
		dm.end("Load matrix from cache (dirty)");
	}

	/**
	 * Rebuild a timeslice from a dirty cached time slice
	 * 
	 * @param values
	 * @param cachedTimeSlice
	 * @param eventProducers
	 */
	public void proportionalRebuild(String[] values, TimeSlice cachedTimeSlice,
			HashMap<String, EventProducer> eventProducers) {
		double factor;

		for (TimeSlice aNewTimeSlice : parameters.getDataCache()
				.getTimeSliceMapping().get(cachedTimeSlice)) {

			// Compute the proportion factor of the dirty time
			// slice in each slice
			if (cachedTimeSlice.getTimeRegion().getTimeStampStart() > aNewTimeSlice
					.getTimeRegion().getTimeStampStart()) {
				factor = (double) (aNewTimeSlice.getTimeRegion()
						.getTimeStampEnd() - cachedTimeSlice.getTimeRegion()
						.getTimeStampStart())
						/ (double) cachedTimeSlice.getTimeRegion()
								.getTimeDuration();
			} else {
				factor = (double) (cachedTimeSlice.getTimeRegion()
						.getTimeStampEnd() - aNewTimeSlice.getTimeRegion()
						.getTimeStampStart())
						/ (double) cachedTimeSlice.getTimeRegion()
								.getTimeDuration();
			}

			rebuildMatrixFromDirtyCache(values, eventProducers.get(values[1]),
					(int) aNewTimeSlice.getNumber(), factor);
		}
	}
	
	public void databaseRebuild(String[] values, TimeSlice cachedTimeSlice,
			HashMap<String, EventProducer> eventProducers) {
		long startTimeStamp;
		long endTimeStamp;

		for (TimeSlice aNewTimeSlice : parameters.getDataCache()
				.getTimeSliceMapping().get(cachedTimeSlice)) {
			
			// Compute start and end timestamps
			if (cachedTimeSlice.getTimeRegion().getTimeStampStart() > aNewTimeSlice
					.getTimeRegion().getTimeStampStart()) {
				startTimeStamp = cachedTimeSlice.getTimeRegion()
						.getTimeStampStart();
				endTimeStamp = aNewTimeSlice.getTimeRegion().getTimeStampEnd();
			} else {
				startTimeStamp = aNewTimeSlice.getTimeRegion()
						.getTimeStampStart();
				endTimeStamp = cachedTimeSlice.getTimeRegion().getTimeStampEnd();
			}

			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(startTimeStamp, endTimeStamp));
			
			try {
				computeSubMatrix(new ArrayList<EventProducer>(eventProducers.values()), time);
			} catch (SoCTraceException | InterruptedException
					| OcelotlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
				.getAllEventTypes().size()) {
			logger.debug("At least one event type is filtered: no cache will be saved.");
			return false;
		}

		return true;
	}
}
