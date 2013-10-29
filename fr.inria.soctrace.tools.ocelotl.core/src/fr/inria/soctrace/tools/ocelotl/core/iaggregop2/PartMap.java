package fr.inria.soctrace.tools.ocelotl.core.iaggregop2;

import java.util.Map;

public class PartMap implements IPartData {
	
	Map<String, Float> elements;

	public PartMap() {
		super();
	}

	public PartMap(Map<String, Float> elements) {
		super();
		this.elements = elements;
	}

	public Map<String, Float> getElements() {
		return elements;
	}

	public void setElements(Map<String, Float> elements) {
		this.elements = elements;
	}
	
	public void putElement(String string, float value){
		elements.put(string, value);
	}

}
