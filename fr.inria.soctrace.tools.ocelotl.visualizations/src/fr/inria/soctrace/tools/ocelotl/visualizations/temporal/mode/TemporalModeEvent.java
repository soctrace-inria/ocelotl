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
		double currentMax = 0.0;
		double tempMax = 0.0;
		MainEvent maj;
		int index;

		for (index = 0; index < parts.size(); index++) {
			maj = new MainEvent("void", currentMax);
			tempMax = 0.0;
			currentMax = 0.0;
			for (String state : states) {
				tempMax = ((PartMap) parts.get(index).getData()).getElements()
						.get(state);
				tempMax /= max;

				if (tempMax > currentMax) {
					maj = new MainEvent(state, tempMax);
					currentMax = tempMax;
				}
			}

			mainEvents.put(index, maj);
		}
	}

}
