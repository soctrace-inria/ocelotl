package fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.ILPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.MLPAggregManager;

abstract public class SpaceAggregationOperator implements ISpaceAggregationOperator {
	
	protected List<Part> parts;
	protected OcelotlCore ocelotlCore;
	protected int	timeSliceNumber;
	protected long timeSliceDuration;
	protected ILPAggregManager lpaggregManager;

	abstract public String descriptor();
	
	@Override
	public int getPartNumber() {
		return parts.size();
	}
	@Override
	public Part getPart(int i) {
		return parts.get(i);

	}
	@Override
	public int getSliceNumber() {
		return timeSliceNumber;
	}
	@Override
	public OcelotlCore getOcelotlCore() {
		return ocelotlCore;
	}
	
	public SpaceAggregationOperator(OcelotlCore ocelotlCore) {
		super();
		this.ocelotlCore = ocelotlCore;
		this.lpaggregManager = ocelotlCore.getLpaggregManager();
		this.timeSliceNumber = ocelotlCore.getOcelotlParameters().getTimeSlicesNumber();
		this.timeSliceDuration = ocelotlCore.getOcelotlParameters().getTimeRegion().getTimeDuration()/timeSliceNumber;
		this.parts=new ArrayList<Part>();
		initParts();
		computeParts();
	}
	
	private void initParts() {
		int oldPart = 0;
		parts.add(new Part(0, 1, new PartMap()));
		for (int i = 1; i < lpaggregManager.getParts().size() - 1; i++)
			if (lpaggregManager.getParts().get(i) == oldPart) {
				parts.get(parts.size()-1).setEndPart(i);
			}
			else{
				oldPart = lpaggregManager.getParts().get(i);
				parts.add(new Part(i, i+1, new PartMap()));	
			}
	}
	
	
	abstract protected void computeParts();
	
	
	

}
