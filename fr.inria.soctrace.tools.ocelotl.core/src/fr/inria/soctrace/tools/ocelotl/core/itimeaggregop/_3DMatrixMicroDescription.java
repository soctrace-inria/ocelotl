package fr.inria.soctrace.tools.ocelotl.core.itimeaggregop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public abstract class _3DMatrixMicroDescription extends
		MultiThreadTimeAggregationOperator {

	protected List<HashMap<EventProducer, HashMap<String, Long>>> matrix;

	private static final Logger logger = LoggerFactory
			.getLogger(_3DMicroDescription.class);

	public _3DMatrixMicroDescription() {
		super();
	}

	@Override
	public void computeMatrix() throws SoCTraceException, OcelotlException,
			InterruptedException {
		eventsNumber = 0;
		final DeltaManager dm = new DeltaManagerOcelotl();
		dm.start();
		final int epsize = getOcelotlParameters().getEventProducers().size();
		if (getOcelotlParameters().getMaxEventProducers() == 0
				|| epsize < getOcelotlParameters().getMaxEventProducers())
			computeSubMatrix(getOcelotlParameters().getEventProducers());
		else {
			final List<EventProducer> producers = getOcelotlParameters()
					.getEventProducers().size() == 0 ? ocelotlQueries
					.getAllEventProducers() : getOcelotlParameters()
					.getEventProducers();
			for (int i = 0; i < epsize; i = i
					+ getOcelotlParameters().getMaxEventProducers())
				computeSubMatrix(producers.subList(i, Math.min(epsize - 1, i
						+ getOcelotlParameters().getMaxEventProducers())));

		}

		dm.end("TOTAL (QUERIES + COMPUTATION) : " + epsize
				+ " Event Producers, " + eventsNumber + " Events");
	}

	public List<HashMap<EventProducer, HashMap<String, Long>>> getMatrix() {
		return matrix;
	}

	public int getVectorSize() {
		return matrix.get(0).size();
	}

	public int getVectorNumber() {
		return matrix.size();
	}

	@Override
	public void initVectors() throws SoCTraceException {
		matrix = new ArrayList<HashMap<EventProducer, HashMap<String, Long>>>();
		final List<EventProducer> producers = getOcelotlParameters()
				.getEventProducers();
		for (long i = 0; i < timeSliceManager.getSlicesNumber(); i++) {
			matrix.add(new HashMap<EventProducer, HashMap<String, Long>>());

			for (final EventProducer ep : producers)
				matrix.get((int) i).put(ep, new HashMap<String, Long>());
		}
	}

	public void matrixWrite(final long it, final EventProducer ep,
			final String key, final Map<Long, Long> distrib) {
		matrix.get((int) it)
				.get(ep)
				.put(key,
						matrix.get((int) it).get(ep).get(key) + distrib.get(it));
	}

	public void print() {
		logger.debug("");
		logger.debug("Distribution Vectors");
		int i = 0;
		for (final HashMap<EventProducer, HashMap<String, Long>> it : matrix) {
			logger.debug("");
			logger.debug("slice " + i++);
			logger.debug("");
			for (final EventProducer ep : it.keySet())
				logger.debug(ep.getName() + " = " + it.get(ep));
		}
	}

	@Override
	public String matrixToCSV() {
		StringBuffer stringBuf = new StringBuffer();
		int slice = 0;
		for (final HashMap<EventProducer, HashMap<String, Long>> it : matrix) {
			for (final EventProducer ep : it.keySet()) {
				for (String value : it.get(ep).keySet()) {
					stringBuf.append(slice + CSVDelimiter + ep.getId()
							+ CSVDelimiter + value + CSVDelimiter
							+ it.get(ep).get(value) + "\n");
				}
			}
			slice++;
		}
		return stringBuf.toString();
	}

	@Override
	public void rebuildMatrix(String[] values, int sliceMultiple) {
		int slice = Integer.parseInt(values[0]);
		int epID = Integer.parseInt(values[1]);
		String evType = values[2];
		long value = Long.parseLong(values[3]);
		final List<EventProducer> producers = getOcelotlParameters()
				.getEventProducers();

		// Look for the right producer
		for (EventProducer ep : producers) {

			if (ep.getId() == epID) {
				// If the number of time slice is a multiple of the cached time
				// slice number
				if (sliceMultiple > 1) {
					// Compute the correct slice number
					slice = slice / sliceMultiple;

					// And add the value to the one already in the matrix
					if (matrix.get(slice).get(ep).get(evType) != null)
						value = matrix.get(slice).get(ep).get(evType) + value;
				}

				matrix.get(slice).get(ep).put(evType, value);
			}
		}
	}
}
