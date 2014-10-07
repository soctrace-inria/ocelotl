package fr.inria.soctrace.tools.ocelotl.core.itimeaggregop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.micromodel.Microscopic3DModel;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;

public abstract class _3DMatrixMicroDescription extends
		MultiThreadTimeAggregationOperator {

//	protected List<HashMap<EventProducer, HashMap<String, Double>>> matrix;
	protected Microscopic3DModel microModel;

	private static final Logger logger = LoggerFactory
			.getLogger(_3DMatrixMicroDescription.class);

	public _3DMatrixMicroDescription() {
		super();
	}

	@Override
	public void computeMatrix(IProgressMonitor monitor) throws SoCTraceException, OcelotlException,
			InterruptedException {
		eventsNumber = 0;
		final DeltaManager dm = new DeltaManagerOcelotl();
		dm.start();
		final int epsize = getOcelotlParameters().getEventProducers().size();
		if (getOcelotlParameters().getMaxEventProducers() == 0
				|| epsize < getOcelotlParameters().getMaxEventProducers())
			computeSubMatrix(getOcelotlParameters().getEventProducers(), monitor);
		else {
			final List<EventProducer> producers = getOcelotlParameters()
					.getEventProducers().size() == 0 ? ocelotlQueries
					.getAllEventProducers() : getOcelotlParameters()
					.getEventProducers();
			for (int i = 0; i < epsize; i = i
					+ getOcelotlParameters().getMaxEventProducers())
				computeSubMatrix(producers.subList(i, Math.min(epsize - 1, i
						+ getOcelotlParameters().getMaxEventProducers())), monitor);
		}

		dm.end("TOTAL (QUERIES + COMPUTATION): " + epsize
				+ " Event Producers, " + eventsNumber + " Events");
	}

	public List<HashMap<EventProducer, HashMap<String, Double>>> getMatrix() {
		return microModel.getMatrix();
	}

	public int getVectorSize() {
		return microModel.getMatrix().get(0).size();
	}

	public int getVectorNumber() {
		return microModel.getMatrix().size();
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
	public void buildMicroscopicModel(IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		microModel = new Microscopic3DModel(this);
		microModel.buildMicroscopicModel(parameters, monitor);
	}

	public void matrixWrite(final long it, final EventProducer ep,
			final String key, final Map<Long, Double> distrib) {
		microModel.getMatrix().get((int) it)
				.get(ep)
				.put(key,
						microModel.getMatrix().get((int) it).get(ep).get(key) + distrib.get(it));
	}

	public void print() {
		logger.debug("");
		logger.debug("Distribution Vectors");
		int i = 0;
		for (final HashMap<EventProducer, HashMap<String, Double>> it : microModel.getMatrix()) {
			logger.debug("");
			logger.debug("slice " + i++);
			logger.debug("");
			for (final EventProducer ep : it.keySet())
				logger.debug(ep.getName() + " = " + it.get(ep));
		}
	}

	
	public void rebuildClean(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException {
		microModel.rebuildClean(aCacheFile, eventProducers, monitor);
	}

	public void rebuildApproximate(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException {
		microModel.rebuildApproximate(aCacheFile, eventProducers, monitor);
	}

	public void rebuildDirty(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException, SoCTraceException,
			InterruptedException, OcelotlException {
		microModel.rebuildClean(aCacheFile, eventProducers, monitor);
	}

}
