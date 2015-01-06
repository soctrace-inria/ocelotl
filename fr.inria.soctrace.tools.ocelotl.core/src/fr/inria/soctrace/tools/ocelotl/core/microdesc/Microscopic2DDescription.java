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
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public abstract class Microscopic2DDescription extends MicroscopicDescription {

	private static final Logger logger = LoggerFactory
			.getLogger(Microscopic2DDescription.class);
	protected List<HashMap<EventProducer, Double>> matrix;

	public Microscopic2DDescription() {
		super();
		matrix = new ArrayList<HashMap<EventProducer, Double>>();
	}

	@Override
	public void initMatrix() throws SoCTraceException {
		matrix = new ArrayList<HashMap<EventProducer, Double>>();
		final List<EventProducer> producers = parameters.getCurrentProducers();
		for (long i = 0; i < parameters.getTimeSlicesNumber(); i++) {
			matrix.add(new HashMap<EventProducer, Double>());

			for (final EventProducer ep : producers)
				if (!aggregatedProducers.containsKey(ep))
					matrix.get((int) i).put(ep, 0.0);
		}
	}

	@Override
	public void rebuildMatrix(String[] values, EventProducer ep,
			int sliceMultiple) {
		int slice = Integer.parseInt(values[0]);
		double value = Double.parseDouble(values[2]);
		
		// If the event producer is still flag as inactive
		if (!getActiveProducers().contains(ep)) {
			// Remove it
			getActiveProducers().add(ep);
		}

		// If the number of time slice is a multiple of the cached time
		// slice number
		if (sliceMultiple > 1) {
			// Compute the correct slice number
			slice = slice / sliceMultiple;

			// And add the value to the one already in the matrix
			if (matrix.get(slice).get(ep) != null)
				value = matrix.get(slice).get(ep) + value;
		}

		matrix.get(slice).put(ep, value);
	}

	@Override
	public void initToZero(Collection<EventProducer> eventProducers) {
		for (int slice = 0; slice < parameters.getTimeSlicesNumber(); slice++) {
			for (EventProducer ep : eventProducers) {
				matrix.get(slice).put(ep, 0.0);
			}
		}
	}

	@Override
	public void rebuildMatrixFromDirtyCache(String[] values, EventProducer ep,
			int slice, double factor) {
		String evType = values[2];

		// If the event type is filtered out
		if (!typeNames.contains(evType))
			return;

		// If the event producer is still flag as inactive
		if (!getActiveProducers().contains(ep)) {
			// Remove it
			getActiveProducers().add(ep);
		}
		
		// Compute a value proportional to the time ratio spent in the slice
		double value = Double.parseDouble(values[3]) * factor;

		// Add the value to the one potentially already in the matrix
		if (matrix.get(slice).get(ep) != null)
			value = matrix.get(slice).get(ep) + value;

		matrix.get(slice).put(ep, value);
	}

	public List<HashMap<EventProducer, Double>> getMatrix() {
		return matrix;
	}

	public void setMatrix(List<HashMap<EventProducer, Double>> matrix) {
		this.matrix = matrix;
	}

	@Override
	public String matrixToCSV() {
		StringBuffer stringBuf = new StringBuffer();
		int slice = 0;
		// For each slice
		for (final HashMap<EventProducer, Double> it : matrix) {
			// for each event producer
			for (final EventProducer ep : it.keySet()) {
				if (it.get(ep) != 0)
					stringBuf.append(slice + OcelotlConstants.CSVDelimiter
							+ ep.getId() + OcelotlConstants.CSVDelimiter
							+ it.get(ep) + "\n");
			}
			slice++;
		}
		return stringBuf.toString();
	}

	@Override
	public void computeMatrix(IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException, InterruptedException {
		eventsNumber = 0;
		final DeltaManager dmt = new DeltaManagerOcelotl();
		dmt.start();
		final int epsize = getOcelotlParameters().getCurrentProducers().size();
		if (getOcelotlParameters().getMaxEventProducers() == 0
				|| epsize < getOcelotlParameters().getMaxEventProducers())
			computeSubMatrix(getOcelotlParameters().getCurrentProducers(),
					monitor);
		else {
			final List<EventProducer> producers = getOcelotlParameters()
					.getCurrentProducers().size() == 0 ? ocelotlQueries
					.getAllEventProducers() : getOcelotlParameters()
					.getCurrentProducers();
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

		dmt.end("TOTAL (QUERIES + COMPUTATION) : " + epsize
				+ " Event Producers, " + eventsNumber + " Events");
	}

	@Override
	public int getVectorSize() {
		return getMatrix().get(0).size();
	}

	@Override
	public int getVectorNumber() {
		return getMatrix().size();
	}

	public void matrixWrite(final long it, final EventProducer ep,
			final Map<Long, Long> distrib) {
		synchronized (getMatrix()) {
			getMatrix().get((int) it).put(ep,
					getMatrix().get((int) it).get(ep) + distrib.get(it));
		}
	}

	@Override
	public void print() {
		logger.debug("");
		logger.debug("Distribution Vectors");
		int i = 0;
		for (final HashMap<EventProducer, Double> it : getMatrix()) {
			logger.debug("");
			logger.debug("slice " + i++);
			logger.debug("");
			for (final EventProducer ep : it.keySet())
				logger.debug(ep.getName() + " = " + it.get(ep));
		}
	}

}
