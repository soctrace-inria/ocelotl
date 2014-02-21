package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.I3DMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public class SpaceTimeAggregation2Manager extends SpaceTimeAggregationManager {

	I3DMicroDescription	matrix;
	
	public SpaceTimeAggregation2Manager(I3DMicroDescription matrix) {
		super(matrix.getOcelotlParameters());
		reset();
	}

	@Override
	public List<EventProducer> getEventProducers() {
		return new ArrayList<EventProducer>(matrix.getMatrix().get(0).keySet());
	}

	@Override
	protected void fillNodesJava() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fillNodesJNI() {
		for (EventProducer ep: getEventProducers()){
			List<HashMap<String, Long>> values=new ArrayList<HashMap<String, Long>>();
			for (int i=0; i<matrix.getVectorNumber(); i++)
				values.add(matrix.getMatrix().get(i).get(ep));	
			hierarchy.setValues(ep, values);
			addHierarchyToJNI();
		}
	}



	@Override
	protected void addLeaves() {
		for (int id: hierarchy.getLeaves().keySet())
			timeAggregation.addLeaf(id, hierarchy.getParentID(id), hierarchy.getValues(id));
		
	}

	@Override
	protected void addNodes() {
		for (int id: hierarchy.getNodes().keySet())
			timeAggregation.addNode(id, hierarchy.getParentID(id));
		
	}

	@Override
	protected void AddRoot() {
		timeAggregation.addRoot(hierarchy.getRoot().getID());
		
	}

	@Override
	public void reset() {
		setHierarchy();
		if (OcelotlParameters.isJniFlag())
			timeAggregation = new JNISpaceTimeAggregation2();
	//	else
	//		timeAggregation = new SpaceTimeAggregation2();
		//TODO implements
		fillNodes();

	}
	
	private void setHierarchy(){
		hierarchy=new EventProducerHierarchy(getEventProducers());
	}

}
