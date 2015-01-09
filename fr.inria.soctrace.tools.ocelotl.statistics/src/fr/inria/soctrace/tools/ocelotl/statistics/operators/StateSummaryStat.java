package fr.inria.soctrace.tools.ocelotl.statistics.operators;

import java.util.ArrayList;
import java.util.HashMap;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class StateSummaryStat extends SummaryStat {

	public StateSummaryStat(OcelotlView aView) {
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
				if (!isInSpatialSelection(ep))
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

		int nbProducers = getNumberOfProducers();
		
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
	 * Get the number of producers
	 * @return
	 */
	public int getNumberOfProducers() {
		int nbProducers = 0;
		if (ocelotlview.getOcelotlParameters().isSpatialSelection()) {
			ArrayList<EventProducer> currentSelection = new ArrayList<EventProducer>();

			// Make the intersection of the selected producers and the filtered
			// ones && remove the one that were aggregated
			for (EventProducer anEP : ocelotlview.getOcelotlParameters()
					.getUnfilteredEventProducers()) {
				if (ocelotlview.getOcelotlParameters()
						.getSpatiallySelectedProducers().contains(anEP)
						&& (!ocelotlview.getOcelotlParameters()
								.getOcelotlSettings().isAggregateLeaves() || (ocelotlview.getOcelotlParameters()
								.getOcelotlSettings().isAggregateLeaves() && !ocelotlview
								.getOcelotlParameters()
								.getAggregatedEventProducers().contains(anEP)))) {
					currentSelection.add(anEP);
				}
			}
			
			nbProducers = currentSelection.size();

			if (nbProducers == 2)
				// Since we add the parent producers when only one producer is
				// selected, we remove it to have correct data
				nbProducers = 1;
			
			// Add the corresponding number of aggregated producers
			for (EventProducer anEP : ocelotlview.getOcelotlParameters()
					.getAggregatedLeavesIndex().keySet())
				if (ocelotlview.getOcelotlParameters()
						.getSpatiallySelectedProducers().contains(anEP))
					nbProducers = nbProducers
							+ ocelotlview.getOcelotlParameters()
									.getAggregatedLeavesIndex().get(anEP);
			
			System.err.println("state + selecy " + nbProducers);
		} else {
			nbProducers = microModel.getActiveProducers().size()
					+ microModel.getInactiveProducers().size();
			
			System.err.println("state + all " + nbProducers);
		}
		return nbProducers;
	}
	
}
