package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time;

import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.jni.OLPAggregWrapper;

public class JNITimeAggregation3 extends JNITimeAggregation{

	public JNITimeAggregation3() {
		super();
		jniWrapper = new OLPAggregWrapper(3);
	}
	
	public void addMatrix(){
		jniWrapper.addMatrix();
	}
	
	public void addVector(){
		jniWrapper.addVector();
	}
	
	public void push_back(double value){
		jniWrapper.push_back(value);
	}
	
	

}
