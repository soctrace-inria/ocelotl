package fr.inria.soctrace.tools.ocelotl.core.generic.aggregop2;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop2.AggregationOperator2;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop2.Part;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop2.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.ILPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.MLPAggregManager;

public class StateDistribution extends AggregationOperator2 {
	
	public StateDistribution(OcelotlCore ocelotlCore) {
		super(ocelotlCore);
		// TODO Auto-generated constructor stub
	}

	final static String Descriptor = "State Distribution";
	private List<String> states;



	public String descriptor() {
		return Descriptor;
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
	
	
	private void initStates(){
		states = ((MLPAggregManager) lpaggregManager).getTimeSliceMatrix().getKeys();
		for (Part part: parts)
			for (String state : states)
				((PartMap) part.getData()).putElement(state, 0.0);
	}
	
	private void aggregateStates(){
		for (Part part: parts)
			for (int i=part.getStartPart(); i<part.getEndPart(); i++){
				for (String ep: ((MLPAggregManager) lpaggregManager).getEventProducers())
					for (String state : states)
						((PartMap) part.getData()).addElement(state, ((MLPAggregManager) lpaggregManager).getTimeSliceMatrix().getMatrix().get(i).get(ep).get(state).doubleValue());	
			}
			
	}
	
	private void normalize(){
		for (Part part: parts)
			((PartMap) part.getData()).normalizeElements(timeSliceDuration, part.getPartSize());	
	}
	
	protected void computeParts(){
		initStates();
		aggregateStates();
		normalize();
	}
	
	
	

}
