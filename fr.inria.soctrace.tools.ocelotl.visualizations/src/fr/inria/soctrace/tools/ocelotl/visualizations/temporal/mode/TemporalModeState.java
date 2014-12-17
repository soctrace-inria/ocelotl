package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.mode;

import java.util.HashMap;

import fr.inria.soctrace.tools.ocelotl.core.ivisuop.PartMap;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.MainEvent;

public class TemporalModeState extends TemporalMode {

	@Override
	public void computeMainStates() {
		mainEvents = new HashMap<Integer, MainEvent>();
		double max = 0.0;
		double tempMax = 0.0;
		MainEvent maj;
		int index;

		for (index = 0; index < parts.size(); index++) {
			maj = new MainEvent("void", max);
			tempMax = 0.0;
			max = 0.0;
			for (String state : states) {
				tempMax = ((PartMap) parts.get(index).getData()).getElements()
						.get(state);
				
				double duration = (parts.get(index).getEndPart() - parts.get(
						index).getStartPart())
						* Long.valueOf(timeSliceDuration).doubleValue();
				tempMax = tempMax
						/ (duration * ocelotlCore.getOcelotlParameters()
								.getCurrentProducers().size());

				if (tempMax > max) {
					maj = new MainEvent(state, tempMax);
					max = tempMax;
				}
			}
			mainEvents.put(index, maj);
		}
	}
}
