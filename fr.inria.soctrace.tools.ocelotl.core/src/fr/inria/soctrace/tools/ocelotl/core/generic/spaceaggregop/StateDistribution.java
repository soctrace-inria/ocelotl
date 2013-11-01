package fr.inria.soctrace.tools.ocelotl.core.generic.spaceaggregop;

import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.MLPAggregManager;

public class StateDistribution extends SpaceAggregationOperator {

	final static String		Descriptor	= "State Distribution";
	private List<String>	states;

	public StateDistribution(final OcelotlCore ocelotlCore) {
		super(ocelotlCore);
		// TODO Auto-generated constructor stub
	}

	private void aggregateStates() {
		for (final Part part : parts)
			for (int i = part.getStartPart(); i < part.getEndPart(); i++)
				for (final String ep : ((MLPAggregManager) lpaggregManager).getEventProducers())
					for (final String state : states)
						((PartMap) part.getData()).addElement(state, ((MLPAggregManager) lpaggregManager).getTimeSliceMatrix().getMatrix().get(i).get(ep).get(state).doubleValue());

	}

	@Override
	protected void computeParts() {
		initParts();
		initStates();
		aggregateStates();
		normalize();
	}

	@Override
	public String descriptor() {
		return Descriptor;
	}

	@Override
	protected void initParts() {
		int oldPart = 0;
		parts.add(new Part(0, 1, new PartMap()));
		for (int i = 1; i < lpaggregManager.getParts().size() - 1; i++)
			if (lpaggregManager.getParts().get(i) == oldPart)
				parts.get(parts.size() - 1).setEndPart(i);
			else {
				oldPart = lpaggregManager.getParts().get(i);
				parts.add(new Part(i, i + 1, new PartMap()));
			}
	}

	private void initStates() {
		states = ((MLPAggregManager) lpaggregManager).getTimeSliceMatrix().getKeys();
		for (final Part part : parts)
			for (final String state : states)
				((PartMap) part.getData()).putElement(state, 0.0);
	}

	private void normalize() {
		for (final Part part : parts)
			((PartMap) part.getData()).normalizeElements(timeSliceDuration, part.getPartSize());
	}

}
