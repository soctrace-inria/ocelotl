package fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.ILPAggregManager;

abstract public class SpaceAggregationOperator implements ISpaceAggregationOperator {

	protected List<Part>		parts;
	protected OcelotlCore		ocelotlCore;
	protected int				timeSliceNumber;
	protected long				timeSliceDuration;
	protected ILPAggregManager	lpaggregManager;

	public SpaceAggregationOperator(final OcelotlCore ocelotlCore) {
		super();
		this.ocelotlCore = ocelotlCore;
		lpaggregManager = ocelotlCore.getLpaggregManager();
		timeSliceNumber = ocelotlCore.getOcelotlParameters().getTimeSlicesNumber();
		timeSliceDuration = ocelotlCore.getOcelotlParameters().getTimeRegion().getTimeDuration() / timeSliceNumber;
		parts = new ArrayList<Part>();
		initParts();
		computeParts();
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
		parts.add(new Part(0, 1, new PartMap()));
		for (int i = 1; i < lpaggregManager.getParts().size() - 1; i++)
			if (lpaggregManager.getParts().get(i) == oldPart)
				parts.get(parts.size() - 1).setEndPart(i);
			else {
				oldPart = lpaggregManager.getParts().get(i);
				parts.add(new Part(i, i + 1, null));
			}
	}

}
