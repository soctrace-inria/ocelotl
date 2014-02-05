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
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.ITimeManager;

abstract public class SpaceAggregationOperator implements ISpaceAggregationOperator {

	protected List<Part>		parts;
	protected OcelotlCore		ocelotlCore;
	protected int				timeSliceNumber;
	protected long				timeSliceDuration;
	protected ITimeManager	lpaggregManager;

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
	public Part getPart(final int i) {
		return parts.get(i);
	}

	@Override
	public int getPartNumber() {
		return parts.size();
	}

	@Override
	public int getSliceNumber() {
		return timeSliceNumber;
	}

	protected void initParts() {
		int oldPart = 0;
		parts.add(new Part(0, 1, null));
		for (int i = 0; i < lpaggregManager.getParts().size(); i++)
			if (lpaggregManager.getParts().get(i) == oldPart)
				parts.get(parts.size() - 1).setEndPart(i + 1);
			else {
				oldPart = lpaggregManager.getParts().get(i);
				parts.add(new Part(i, i + 1, null));
			}
	}

	@Override
	public void setOcelotlCore(final OcelotlCore ocelotlCore) {
		this.ocelotlCore = ocelotlCore;
		lpaggregManager = ocelotlCore.getLpaggregManager();
		timeSliceNumber = ocelotlCore.getOcelotlParameters().getTimeSlicesNumber();
		timeSliceDuration = ocelotlCore.getOcelotlParameters().getTimeRegion().getTimeDuration() / timeSliceNumber;
		parts = new ArrayList<Part>();
		initParts();
		computeParts();
	}

}
