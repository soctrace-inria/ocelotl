/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
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
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.ISpaceTimeManager;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.ITimeManager;

abstract public class SpaceSTAggregationOperator implements ISpaceSTAggregationOperator {

	protected EventProducerHierarchy hierarchy;
	protected OcelotlCore	ocelotlCore;
	protected int			timeSliceNumber;
	protected long			timeSliceDuration;
	protected ISpaceTimeManager	lpaggregManager;

	public SpaceSTAggregationOperator() {
		super();
	}

	public SpaceSTAggregationOperator(final OcelotlCore ocelotlCore) {
		super();
		setOcelotlCore(ocelotlCore);
	}

	abstract protected void computeParts();

	@Override
	public OcelotlCore getOcelotlCore() {
		return ocelotlCore;
	}

	@Override
	public int getSliceNumber() {
		return timeSliceNumber;
	}

	abstract protected void initParts();
	


	@Override
	public EventProducerHierarchy getHierarchy() {
		return hierarchy;
	}

	@Override
	public void setOcelotlCore(final OcelotlCore ocelotlCore) {
		this.ocelotlCore = ocelotlCore;
		lpaggregManager = (ISpaceTimeManager) ocelotlCore.getLpaggregManager();
		timeSliceNumber = ocelotlCore.getOcelotlParameters().getTimeSlicesNumber();
		timeSliceDuration = ocelotlCore.getOcelotlParameters().getTimeRegion().getTimeDuration() / timeSliceNumber;
		hierarchy = lpaggregManager.getHierarchy();
		initParts();
		computeParts();
	}

}
