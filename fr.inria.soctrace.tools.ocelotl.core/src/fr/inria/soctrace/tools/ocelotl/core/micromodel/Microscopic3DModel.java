package fr.inria.soctrace.tools.ocelotl.core.micromodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.MultiThreadTimeAggregationOperator;

public class Microscopic3DModel extends MicroscopicModel {

	protected List<HashMap<EventProducer, HashMap<String, Double>>> matrix;
	
	public Microscopic3DModel(MultiThreadTimeAggregationOperator anOperator) {
		super(anOperator);
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
	public void rebuildMatrixFromDirtyCache(String[] values, EventProducer ep, int slice,
			double factor) {

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
		final List<EventProducer> producers = parameters
				.getEventProducers();
		for (long i = 0; i < parameters.getTimeSlicesNumber(); i++) {
			matrix.add(new HashMap<EventProducer, HashMap<String, Double>>());

			for (final EventProducer ep : producers)
				matrix.get((int) i).put(ep, new HashMap<String, Double>());
		}
	}

}
