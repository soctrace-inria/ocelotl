package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time;

import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.jni.OLPAggregWrapper;

public class JNITimeAggregation2 extends JNITimeAggregation{

	public JNITimeAggregation2() {
		super();
		jniWrapper = new OLPAggregWrapper(2);
	}
	
	public void addVector(){
		jniWrapper.addVector();
	}
	
	public void push_back(double value){
		jniWrapper.push_back(value);
	}
	
	

}
