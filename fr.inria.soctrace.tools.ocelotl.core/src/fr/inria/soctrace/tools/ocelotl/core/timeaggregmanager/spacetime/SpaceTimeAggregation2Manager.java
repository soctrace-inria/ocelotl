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

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.I3DMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop._2DSpaceTimeMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;

public class SpaceTimeAggregation2Manager extends SpaceTimeAggregationManager {

	_2DSpaceTimeMicroDescription matrix;
	
	public SpaceTimeAggregation2Manager(_2DSpaceTimeMicroDescription _2dSpaceTimeMicroDescription) {
		super(_2dSpaceTimeMicroDescription.getOcelotlParameters());
		matrix= _2dSpaceTimeMicroDescription;
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
		}
		addHierarchyToJNI();
	}



	@Override
	protected void addLeaves() {
		for (int id: hierarchy.getLeaves().keySet())
			timeAggregation.addLeaf(id, hierarchy.getParentID(id), hierarchy.getValues(id));
		
	}

	@Override
	protected void addNodes() {
		addChildren(hierarchy.getRoot().getID());
		
	}
	
	protected void addChildren(int id){
		for (EventProducerNode epn:hierarchy.getEventProducerNodes().get(id).getChildrenNodes()){
			if (!epn.getChildrenNodes().isEmpty()){
				timeAggregation.addNode(epn.getID(), id);
				addChildren(epn.getID());	
			}
		}
	}

	@Override
	protected void addRoot() {
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
