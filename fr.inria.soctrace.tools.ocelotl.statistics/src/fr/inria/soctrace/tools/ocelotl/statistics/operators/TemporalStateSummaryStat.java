package fr.inria.soctrace.tools.ocelotl.statistics.operators;

import java.util.ArrayList;
import java.util.HashMap;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class TemporalStateSummaryStat extends TemporalSummaryStat {

	public TemporalStateSummaryStat(OcelotlView aView) {
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
				.getTimeSlice(timeRegion.getTimeStampStart() + 1);
		int endingSlice = (int) microModel.getTimeSliceManager().getTimeSlice(
				timeRegion.getTimeStampEnd());
		
		// Get data from the microscopic model
		for (i = startingSlice; i <= endingSlice; i++) {
			for (EventProducer ep : microModel.getMatrix().get(i).keySet()) {
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

		total = timeRegion.getTimeDuration() * microModel.getMatrix().get(0).keySet().size();
		statData = new ArrayList<SummaryStatModel>();

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
	
}
