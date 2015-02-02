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
package fr.inria.soctrace.tools.ocelotl.statistics.operators;

import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;
import fr.inria.soctrace.tools.ocelotl.core.statistics.IStatisticsProvider;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public abstract class StatisticsProvider implements IStatisticsProvider {

	protected OcelotlView ocelotlview;
	protected TimeRegion timeRegion;

	public StatisticsProvider(OcelotlView aView) {
		this.ocelotlview = aView;
	}

	public void setTimeRegion(TimeRegion aRegion) {
		timeRegion = aRegion;
	}

	public void setTimeRegion(Long startTimeStamp, Long endTimeStamp) {
		timeRegion = new TimeRegion(startTimeStamp, endTimeStamp);
	}

	/**
	 * Compute the statistics data
	 */
	public abstract void computeData();

	public abstract void setMicroMode(MicroscopicDescription microModel);

	public abstract MicroscopicDescription getMicroMode();

	/**
	 * Update the color of the event types
	 */
	public abstract void updateColor();
}
