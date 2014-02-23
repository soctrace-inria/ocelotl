package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime;

import java.util.HashMap;
import java.util.List;

import fr.inria.dlpaggreg.spacetime.ISpaceTimeAggregation;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.jni.DLPAggregWrapper;

public class JNISpaceTimeAggregation2 extends JNISpaceTimeAggregation implements
		ISpaceTimeAggregation {

	public JNISpaceTimeAggregation2() {
		super();
		jniWrapper = new DLPAggregWrapper(2);
	}

	@Override
	public void addNode(int id, int parentID) {
		jniWrapper.newNode(id, parentID);
	}

	@Override
	public void addRoot(int id) {
		jniWrapper.newRoot(id);
	}

	@Override
	public void addLeaf(int id, int parentID, Object values) {
		@SuppressWarnings("unchecked")
		List<HashMap<String, Long>> val= (List<HashMap<String, Long>>) values;
		jniWrapper.newLeaf(id, parentID);
		for (int i = 0; i<val.size(); i++){
			jniWrapper.addVector(id);
			for (String s: val.get(i).keySet()){
				jniWrapper.push_back(id, val.get(i).get(s));
			}
		}
		
		
	}
	
	



}
