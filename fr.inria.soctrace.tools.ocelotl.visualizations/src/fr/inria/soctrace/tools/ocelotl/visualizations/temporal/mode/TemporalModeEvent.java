/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.mode;

import java.util.HashMap;

import fr.inria.soctrace.tools.ocelotl.core.ivisuop.PartMap;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.MainEvent;

public class TemporalModeEvent extends TemporalMode {

	@Override
	public void computeMainStates() {
		mainEvents = new HashMap<Integer, MainEvent>();
		double max = 0.0;
		double tempMax = 0.0;
		double amplitudeMax = 0.0;
		MainEvent maj;
		int index;

		for (index = 0; index < parts.size(); index++) {
			maj = new MainEvent("void", max);
			tempMax = 0.0;
			max = 0.0;
			double total = 0.0;
			for (String state : states) {
				tempMax = ((PartMap) parts.get(index).getData()).getElements()
						.get(state);

				double duration = (parts.get(index).getEndPart() - parts.get(
						index).getStartPart())
						* Long.valueOf(timeSliceDuration).doubleValue();
				tempMax /= duration;
				total += tempMax;
				if (tempMax > max) {
					maj = new MainEvent(state, tempMax);
					max = tempMax;
				}
			}
			if (total > amplitudeMax) {
				amplitudeMax = total;
			}
			mainEvents.put(index, maj);
		}
		for (Integer i : mainEvents.keySet())
			mainEvents.get(i).setAmplitude(
					mainEvents.get(i).getAmplitude() / amplitudeMax);
	}

}
