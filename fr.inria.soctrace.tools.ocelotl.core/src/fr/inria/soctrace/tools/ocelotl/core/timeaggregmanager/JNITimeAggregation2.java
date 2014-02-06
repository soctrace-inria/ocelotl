package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager;

import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.jni.LPAggregWrapper;

public class JNITimeAggregation2 extends JNITimeAggregation{

	public JNITimeAggregation2() {
		super();
		jniWrapper = new LPAggregWrapper(2);
	}
	
	public void addVector(){
		jniWrapper.addVector();
	}
	
	public void push_back(double value){
		jniWrapper.push_back(value);
	}
	
	

}
