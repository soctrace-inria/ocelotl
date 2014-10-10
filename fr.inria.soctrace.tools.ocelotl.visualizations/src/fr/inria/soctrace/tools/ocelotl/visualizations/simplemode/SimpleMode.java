package fr.inria.soctrace.tools.ocelotl.visualizations.simplemode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.Part;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.time.ITimeManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.time.TimeAggregation3Manager;
import fr.inria.soctrace.tools.ocelotl.visualizations.mode.MajState;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.VisuTOperator;

public class SimpleMode extends VisuTOperator {

	protected HashMap<Integer, MajState> majStates;
	private List<String> states;
	protected ITimeManager timeManager;

	private static final Logger logger = LoggerFactory.getLogger(SimpleMode.class);

	@Override
	public OcelotlCore getOcelotlCore() {
		return ocelotlCore;
	}

	@Override
	public void setOcelotlCore(OcelotlCore ocelotlCore) {
		this.ocelotlCore = ocelotlCore;
		lpaggregManager = (ITimeManager) ocelotlCore.getLpaggregManager();
		timeManager = (ITimeManager) ocelotlCore.getLpaggregManager();
		computeParts();
	}

	public SimpleMode(OcelotlCore ocelotlCore) {
		super();
		setOcelotlCore(ocelotlCore);
	}

	public SimpleMode() {
		super();
	}

	public HashMap<Integer, MajState> getMajStates() {
		return majStates;
	}

	public void setMajStates(HashMap<Integer, MajState> majStates) {
		this.majStates = majStates;
	}
	
	@Override
	public void computeParts() {
		initParts();
		initStates();
		aggregateStates();
		computeMajStates();
	}

	protected void initParts() {
		parts = new ArrayList<Part>();
		int oldPart = 0;
		parts.add(new Part(0, 1, new PartMap()));
		for (int i = 0; i < timeManager.getParts().size(); i++)
			if (timeManager.getParts().get(i) == oldPart)
				parts.get(parts.size() - 1).setEndPart(i + 1);
			else {
				oldPart = timeManager.getParts().get(i);
				parts.add(new Part(i, i + 1, new PartMap()));
			}
	}

	private void initStates() {
		states = ((TimeAggregation3Manager) timeManager).getKeys();
		for (final Part part : parts)
			for (final String state : states)
				((PartMap) part.getData()).putElement(state, 0.0);
	}

	private void aggregateStates() {
		for (final Part part : parts)
			for (int i = part.getStartPart(); i < part.getEndPart(); i++)
				for (final EventProducer ep : ((TimeAggregation3Manager) timeManager)
						.getEventProducers())
					for (final String state : states)
						((PartMap) part.getData()).addElement(state,
								((TimeAggregation3Manager) timeManager)
										.getTimeSliceMatrix().getMatrix()
										.get(i).get(ep).get(state));
	}

	public List<String> getStates() {
		return ((TimeAggregation3Manager) timeManager).getKeys();
	}

	public void computeMajStates() {
		majStates = new HashMap<Integer, MajState>();
		double max = 0.0;
		double tempMax = 0.0;
		MajState maj;
		int index;

		for (index = 0; index < parts.size(); index++) {
			maj = new MajState("void", max);
			tempMax = 0.0;
			max = 0.0;
			for (String state : states) {
				tempMax = ((PartMap) parts.get(index).getData()).getElements()
						.get(state);
				if (tempMax > max) {
					maj = new MajState(state, tempMax);
					max = tempMax;
				}
			}
			majStates.put(index, maj);
		}
	}

}