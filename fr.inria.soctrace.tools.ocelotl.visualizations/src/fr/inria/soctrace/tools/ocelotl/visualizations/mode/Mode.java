package fr.inria.soctrace.tools.ocelotl.visualizations.mode;

import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.TimeAggregation3Manager;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.Proportion;

public class Mode extends Proportion {

	private List<String>		states;
	
	@Override
	protected void computeParts() {
		initParts();
		initStates();
		aggregateStates();
	}
	
	@Override
	protected void initParts() {
		int oldPart = 0;
		parts.add(new Part(0, 1, new PartMap()));
		for (int i = 0; i < lpaggregManager.getParts().size(); i++)
			if (lpaggregManager.getParts().get(i) == oldPart)
				parts.get(parts.size() - 1).setEndPart(i + 1);
			else {
				oldPart = lpaggregManager.getParts().get(i);
				parts.add(new Part(i, i + 1, new PartMap()));
			}
	}

	private void initStates() {
		states = ((TimeAggregation3Manager) lpaggregManager).getKeys();
		for (final Part part : parts)
			for (final String state : states)
				((PartMap) part.getData()).putElement(state, 0.0);
	}

	private void aggregateStates() {
		for (final Part part : parts)
			for (int i = part.getStartPart(); i < part.getEndPart(); i++)
				for (final EventProducer ep : ((TimeAggregation3Manager) lpaggregManager)
						.getEventProducers())
					for (final String state : states)
						((PartMap) part.getData()).addElement(state,
								((TimeAggregation3Manager) lpaggregManager)
										.getTimeSliceMatrix().getMatrix()
										.get(i).get(ep).get(state));
	}

	public List<String> getStates() {
		return ((TimeAggregation3Manager) lpaggregManager).getKeys();
	}
}
