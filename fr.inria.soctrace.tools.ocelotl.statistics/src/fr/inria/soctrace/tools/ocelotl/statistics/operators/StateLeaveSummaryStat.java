package fr.inria.soctrace.tools.ocelotl.statistics.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.model.SimpleEventProducerHierarchy.SimpleEventProducerNode;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class StateLeaveSummaryStat extends SummaryStat{
	
	public StateLeaveSummaryStat(OcelotlView aView) {
		super(aView);
	}

	@Override
	public void computeData() {
		int i;
		data = new HashMap<String, Double>();
		double total = 0.0;

		setupTimeRegion();

		// Get the corresponding time slices
		// add +1 to avoid falling on the ending timestamp of the previous
		// timeslice (which overlaps with starting timestamp of the next one)
		int startingSlice = (int) microModel.getTimeSliceManager()
				.getTimeSlice(timeRegion.getTimeStampStart() + 2);
		int endingSlice = (int) microModel.getTimeSliceManager().getTimeSlice(
				timeRegion.getTimeStampEnd());
		
		// Get data from the microscopic model
		for (i = startingSlice; i <= endingSlice; i++) {
			for (EventProducer ep : microModel.getMatrix().get(i).keySet()) {
				if (!isInSpatialSelection(ep) || !isLeaf(ep))
					continue;

				for (String et : microModel.getMatrix().get(i).get(ep).keySet()) {
					// If first time we meet the event type
					if (!data.containsKey(et)) {
						// Initialize to 0
						data.put(et, 0.0);
					}
					data.put(et, microModel.getMatrix().get(i).get(ep).get(et)
							+ data.get(et));
				}
			}
		}

		int nbProducers = numberOfSelectedLeaves();
		
		total = timeRegion.getTimeDuration() * nbProducers;
		statData = new ArrayList<ITableRow>();

		// Create the data objects for the table
		for (String ep : data.keySet()) {
			double proportion = 0.0;
			// If there was no value in the selected zone, let proportion at 0
			if (total != 0)
				proportion = (data.get(ep) / total) * 100.0;
			
			statData.add(new SummaryStatModel(ep, data.get(ep),
					proportion, FramesocColorManager
							.getInstance().getEventTypeColor(ep).getSwtColor()));
		}
	}
	
	/**
	 * Check whether or not an event producer is a leaf in the EP hierarchy
	 * 
	 * @param ep
	 * @return
	 */
	protected boolean isLeaf(EventProducer ep) {
		return ocelotlview.getOcelotlParameters().getEventProducerHierarchy()
				.isLeaf(ep)
				|| ocelotlview.getOcelotlParameters()
						.getAggregatedLeavesIndex().keySet().contains(ep);
	}
	
	/**
	 * Select the leaf producers among the current selected producer
	 * 
	 * @return the current number of selected leaves producers
	 */
	public Integer numberOfSelectedLeaves() {
		int numberOfLeaves = 0;
		Set<EventProducer> aggregatedProd = new HashSet<EventProducer>();

		// If there is a spatial selection
		if (ocelotlview.getOcelotlParameters().isSpatialSelection()) {
			// Check for all leave producers (non-aggregated)
			for (SimpleEventProducerNode anSepn : ocelotlview
					.getOcelotlParameters().getEventProducerHierarchy()
					.getLeaves().values()) {
				// That it is part of the selection and active
				if (ocelotlview.getOcelotlParameters()
						.getSpatiallySelectedProducers()
						.contains(anSepn.getMe())
						&& microModel.getActiveProducers().contains(
								anSepn.getMe()))
					numberOfLeaves++;

				// If its parent is selected and active then add it (case where
				// there is aggregation)
				if (ocelotlview.getOcelotlParameters()
						.getSpatiallySelectedProducers()
						.contains(anSepn.getParentNode().getMe())
						&& microModel.getActiveProducers().contains(
								anSepn.getParentNode().getMe()))
					aggregatedProd.add(anSepn.getParentNode().getMe());
			}

			// For all prod that are aggregation of leaves, add the
			// corresponding number of aggregated leaves
			for (EventProducer anEp : aggregatedProd)
				numberOfLeaves = numberOfLeaves
						+ ocelotlview.getOcelotlParameters()
								.getAggregatedLeavesIndex().get(anEp);
		} else {
			// Same thing as above without caring about spatial selection
			for (SimpleEventProducerNode anSepn : ocelotlview
					.getOcelotlParameters().getEventProducerHierarchy()
					.getLeaves().values()) {
				if (microModel.getActiveProducers().contains(anSepn.getMe()))
					numberOfLeaves++;

				if (microModel.getActiveProducers().contains(
						anSepn.getParentNode().getMe()))
					aggregatedProd.add(anSepn.getParentNode().getMe());
			}

			for (EventProducer anEp : aggregatedProd)
				numberOfLeaves = numberOfLeaves
						+ ocelotlview.getOcelotlParameters()
								.getAggregatedLeavesIndex().get(anEp);
		}

		return numberOfLeaves;
	}
}
