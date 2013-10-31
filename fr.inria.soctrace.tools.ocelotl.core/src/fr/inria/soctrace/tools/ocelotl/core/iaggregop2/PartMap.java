package fr.inria.soctrace.tools.ocelotl.core.iaggregop2;

import java.util.HashMap;
import java.util.Map;


public class PartMap implements IPartData {
	
	Map<String, Double> elements;

	public PartMap() {
		super();
		elements=new HashMap<String, Double>();
	}

	public PartMap(Map<String, Double> elements) {
		super();
		this.elements = elements;
	}

	public Map<String, Double> getElements() {
		return elements;
	}

	public void setElements(Map<String, Double> elements) {
		this.elements = elements;
	}
	
	public void putElement(String string, double value){
		elements.put(string, value);
	}
	
	public void addElement(String string, double value){
		elements.put(string, elements.get(string)+ value);
	}
	
	public void divideElement(String string, double value){
		if (value!=0)
		elements.put(string, elements.get(string)/ value);
	}
	
	public void normalizeElements(long timeSliceDuration, int partNumber){
		for (String key : elements.keySet())
			divideElement(key, (double) timeSliceDuration * (double) partNumber);	
	}
	
	public double getTotal(){
		double total=0;
		for (String key : elements.keySet())
			total+=elements.get(key);
		return total;
	}

}
