package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager;

import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.jni.LPAggregWrapper;

public class JNITimeAggregation3 extends JNITimeAggregation{

	public JNITimeAggregation3() {
		super();
		jniWrapper = new LPAggregWrapper(3);
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
