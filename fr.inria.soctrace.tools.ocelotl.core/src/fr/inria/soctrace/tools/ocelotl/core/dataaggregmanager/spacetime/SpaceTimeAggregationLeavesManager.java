package fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.lpaggreg.spacetime.JNISpaceTimeAggregation2;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;

public class SpaceTimeAggregationLeavesManager extends SpaceTimeAggregation2Manager{

	public SpaceTimeAggregationLeavesManager(MicroscopicDescription matrix,
			IProgressMonitor monitor) throws OcelotlException {
		super(matrix, monitor);
	}

	@Override
	public void reset(IProgressMonitor monitor) throws OcelotlException {
		setHierarchy();
		timeAggregation = new JNISpaceTimeAggregation2();
		fillNodes();
	}
	
	@Override
	protected void addLeaves() {
		for (int id : hierarchy.getLeaves().keySet()){
			timeAggregation.addLeaf(hierarchy.getLeaves().get(id).getID(), hierarchy.getLeaves().get(id).getParentNode().getID(),
					hierarchy.getLeaves().get(id).getValues(), hierarchy.getLeaves().get(id).getWeight());
		}
	}
	
	@Override
	protected void fillNodesJNI() {
		for (EventProducer ep : getEventProducers()) {
			List<HashMap<String, Double>> values = new ArrayList<HashMap<String, Double>>();
			for (int i = 0; i < matrix.getVectorNumber(); i++)
				values.add(matrix.getMatrix().get(i).get(ep));
			hierarchy.setValues(ep, values);
		}
		hierarchy.buildLeavesFromActiveProducers(matrix.getActiveProducers(), ocelotlParameters.isSpatialSelection(), 
				ocelotlParameters.getSelectedEventProducerNodes());
		addHierarchyToJNI();
	}
	
	/**
	 * Build the hierarchy
	 * @throws OcelotlException
	 */
	private void setHierarchy() throws OcelotlException {
		hierarchy = new EventProducerHierarchy(getEventProducers(), matrix);
	}
}
