package fr.inria.soctrace.tools.ocelotl.core.microdesc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public abstract class Microscopic3DDescription extends MicroscopicDescription {

	private static final Logger logger = LoggerFactory
			.getLogger(Microscopic3DDescription.class);

	protected List<HashMap<EventProducer, HashMap<String, Double>>> matrix;

	public Microscopic3DDescription() {
		super();
		matrix = new ArrayList<HashMap<EventProducer, HashMap<String, Double>>>();
	}

	public void initToZero(Collection<EventProducer> eventProducers) {
		for (int slice = 0; slice < parameters.getTimeSlicesNumber(); slice++) {
			for (EventProducer ep : eventProducers) {
				for (String evType : typeNames) {
					matrix.get(slice).get(ep).put(evType, 0.0);
				}
			}
		}
	}

	@Override
	public void rebuildMatrix(String[] values, EventProducer ep,
			int sliceMultiple) {

		String evType = values[2];

		// If the event type is filtered out
		if (!typeNames.contains(evType))
			return;

		int slice = Integer.parseInt(values[0]);
		double value = Double.parseDouble(values[3]);

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

	@Override
	public void rebuildMatrixFromDirtyCache(String[] values, EventProducer ep,
			int slice, double factor) {

		String evType = values[2];

		// If the event type is filtered out
		if (!typeNames.contains(evType))
			return;

		// Compute a value proportional to the time ratio spent in the slice
		double value = Double.parseDouble(values[3]) * factor;

		// Add the value to the one potentially already in the matrix
		if (matrix.get(slice).get(ep).get(evType) != null)
			value = matrix.get(slice).get(ep).get(evType) + value;

		matrix.get(slice).get(ep).put(evType, value);
	}

	@Override
	public String matrixToCSV() {
		StringBuffer stringBuf = new StringBuffer();
		int slice = 0;
		// For each slice
		for (final HashMap<EventProducer, HashMap<String, Double>> it : matrix) {
			// For each event producer
			for (final EventProducer ep : it.keySet()) {
				// For each event type
				for (String evtType : it.get(ep).keySet()) {
					if (it.get(ep).get(evtType) != 0.0)
						stringBuf.append(slice + OcelotlConstants.CSVDelimiter
								+ ep.getId() + OcelotlConstants.CSVDelimiter
								+ evtType + OcelotlConstants.CSVDelimiter
								+ it.get(ep).get(evtType) + "\n");
				}
			}
			slice++;
		}
		return stringBuf.toString();
	}

	public List<HashMap<EventProducer, HashMap<String, Double>>> getMatrix() {
		return matrix;
	}

	public void setMatrix(
			List<HashMap<EventProducer, HashMap<String, Double>>> matrix) {
		this.matrix = matrix;
	}

	@Override
	public void initVectors() throws SoCTraceException {
		matrix = new ArrayList<HashMap<EventProducer, HashMap<String, Double>>>();
		final List<EventProducer> producers = parameters.getEventProducers();
		for (long i = 0; i < parameters.getTimeSlicesNumber(); i++) {
			matrix.add(new HashMap<EventProducer, HashMap<String, Double>>());

			for (final EventProducer ep : producers)
				matrix.get((int) i).put(ep, new HashMap<String, Double>());
		}
	}

	@Override
	public void initQueries() {
		try {
			ocelotlQueries = new OcelotlQueries(parameters);
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void computeMatrix(IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException, InterruptedException {
		eventsNumber = 0;
		final DeltaManager dm = new DeltaManagerOcelotl();
		dm.start();
		final int epsize = getOcelotlParameters().getEventProducers().size();
		if (getOcelotlParameters().getMaxEventProducers() == 0
				|| epsize < getOcelotlParameters().getMaxEventProducers())
			computeSubMatrix(getOcelotlParameters().getEventProducers(),
					monitor);
		else {
			final List<EventProducer> producers = getOcelotlParameters()
					.getEventProducers().size() == 0 ? ocelotlQueries
					.getAllEventProducers() : getOcelotlParameters()
					.getEventProducers();
			for (int i = 0; i < epsize; i = i
					+ getOcelotlParameters().getMaxEventProducers())
				computeSubMatrix(
						producers.subList(
								i,
								Math.min(epsize - 1, i
										+ getOcelotlParameters()
												.getMaxEventProducers())),
						monitor);
		}

		dm.end("TOTAL (QUERIES + COMPUTATION): " + epsize
				+ " Event Producers, " + eventsNumber + " Events");
	}

	public void matrixWrite(final long it, final EventProducer ep,
			final String key, final Map<Long, Double> distrib) {
		matrix.get((int) it)
				.get(ep)
				.put(key,
						matrix.get((int) it).get(ep).get(key) + distrib.get(it));
	}

	public void matrixPushType(final int incr, final EventProducer ep,
			final String key) {
		getMatrix().get(incr).get(ep).put(key, 0.0);
	}

	public int getVectorNumber() {
		return getMatrix().size();
	}

	public int getVectorSize() {
		return getMatrix().get(0).size();
	}

	public void print() {
		logger.debug("");
		logger.debug("Distribution Vectors");
		int i = 0;
		for (final HashMap<EventProducer, HashMap<String, Double>> it : getMatrix()) {
			logger.debug("");
			logger.debug("slice " + i++);
			logger.debug("");
			for (final EventProducer ep : it.keySet())
				logger.debug(ep.getName() + " = " + it.get(ep));
		}
	}
}
