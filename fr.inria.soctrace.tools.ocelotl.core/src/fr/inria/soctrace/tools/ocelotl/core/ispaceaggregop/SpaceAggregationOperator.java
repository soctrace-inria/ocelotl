/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
 * overview by using aggregation techniques
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.IMicroDescManager;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.ISpaceTimeManager;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.ITimeManager;

abstract public class SpaceAggregationOperator implements ISpaceAggregationOperator {

	protected OcelotlCore	ocelotlCore;
	protected int			timeSliceNumber;
	protected long			timeSliceDuration;
	protected ISpaceTimeManager	lpaggregManager;

	public SpaceAggregationOperator() {
		super();
	}

	public SpaceAggregationOperator(final OcelotlCore ocelotlCore) {
		super();
		setOcelotlCore(ocelotlCore);
	}

	abstract protected void computeParts();
	
	@Override
	abstract public String descriptor();

	@Override
	public OcelotlCore getOcelotlCore() {
		return ocelotlCore;
	}

	@Override
	public void setOcelotlCore(final OcelotlCore ocelotlCore) {
		this.ocelotlCore = ocelotlCore;
		lpaggregManager = (ISpaceTimeManager) ocelotlCore.getLpaggregManager();
		timeSliceNumber = ocelotlCore.getOcelotlParameters().getTimeSlicesNumber();
		timeSliceDuration = ocelotlCore.getOcelotlParameters().getTimeRegion().getTimeDuration() / timeSliceNumber;
	}

}
