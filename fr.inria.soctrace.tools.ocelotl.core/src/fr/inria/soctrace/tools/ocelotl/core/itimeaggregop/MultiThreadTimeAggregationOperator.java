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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.datacache.DataCache;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.queries.IteratorQueries.EventIterator;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public abstract class MultiThreadTimeAggregationOperator {

	String CSVDelimiter = ";";
	
	protected TimeSliceManager timeSliceManager;
	protected EventIterator it;
	protected int count = 0;
	protected int epit = 0;
	protected DeltaManagerOcelotl dm;
	public final static int EPCOUNT = 200;
	protected int eventsNumber;
	protected OcelotlParameters parameters;
	protected OcelotlQueries ocelotlQueries;
	protected DataCache dataCache = new DataCache();
	
	abstract protected void computeMatrix() throws SoCTraceException,
			InterruptedException, OcelotlException;

	abstract protected void computeSubMatrix(
			final List<EventProducer> eventProducers) throws SoCTraceException,
			InterruptedException, OcelotlException;
	
	/**
	 * Convert the matrix values in one String in CSV format
	 * 
	 * @return the String containing the matrix values
	 */
	public abstract String matrixToCSV();

	/**
	 * Fill the matrix with value from the cache file
	 * 
	 * @param values
	 *            Array of String containing a value of the matrix
	 * @param sliceMultiple
	 *            used to compute the current slice number if the number of time
	 *            slices is a divisor of the of the cached data
	 */
	public abstract void rebuildMatrix(String[] values, int sliceMultiple);

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

	public TimeSliceManager getTimeSlicesManager() {
		return timeSliceManager;
	}

	abstract public void initQueries();

	abstract protected void initVectors() throws SoCTraceException;

	public void setOcelotlParameters(final OcelotlParameters parameters)
			throws SoCTraceException, InterruptedException, OcelotlException {
		this.parameters = parameters;
		count = 0;
		epit = 0;
		timeSliceManager = new TimeSliceManager(getOcelotlParameters()
				.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		initQueries();
		initVectors();
		computeMatrix();

		if (eventsNumber == 0)
			throw new OcelotlException(OcelotlException.NOEVENTS);
	}

	public void total(final int rows) {
		dm.end("VECTOR COMPUTATION " + rows + " rows computed");
	}

	public List<Event> getEvents(final int size) {
		final List<Event> events = new ArrayList<Event>();
		synchronized (it) {
			for (int i = 0; i < size; i++) {
				if (it.getNext() == null)
					return events;
				events.add(it.getEvent());
				eventsNumber++;
			}
		}
		return events;
	}
	
	/**
	 * Save the matrix data to a cache file
	 */
	public void saveMatrix()
	{
		// Get workspace path
		String workspaceDirectory = System.getProperty("java.io.tmpdir");
		String filePath = workspaceDirectory +"/"+ System.currentTimeMillis();

		// Write to file,  
		try {
			PrintWriter writer = new PrintWriter(filePath, "UTF-8");

			// write header (parameters)
			// starTimestamp; endTimestamp; timeSliceNumber; parameter; threshold; matrixData
			String header = parameters.getTimeRegion().getTimeStampStart()
					+ CSVDelimiter
					+ parameters.getTimeRegion().getTimeStampEnd()
					+ CSVDelimiter + parameters.getTimeSlicesNumber()
					+ CSVDelimiter + parameters.getParameter() + CSVDelimiter
					+ parameters.getThreshold() + "\n";
			writer.print(header);
					
			// Iterate over matrix and write data
			writer.print(matrixToCSV());
		
			// Close the fd
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dataCache.saveData(parameters, filePath);
	}
	
	/**
	 * Load matrix value from a cache file
	 * 
	 * @param aFilepath
	 *            path to the cache file
	 */
	public void loadFromCache(String aFilepath) {
		try {
			dm = new DeltaManagerOcelotl();
			dm.start();
			BufferedReader bufFileReader = new BufferedReader(new FileReader(
					aFilepath));

			String header, line;
			header = bufFileReader.readLine();
			// read data
			while ((line = bufFileReader.readLine()) != null) {
				String[] values = line.split(CSVDelimiter);
				// fill matrix
				rebuildMatrix(values, dataCache.getTimeSliceMultiple());
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
}
