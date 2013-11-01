package fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop;

import java.util.HashMap;
import java.util.Map;

public class PartMap implements IPartData {

	Map<String, Double>	elements;

	public PartMap() {
		super();
		elements = new HashMap<String, Double>();
	}

	public PartMap(final Map<String, Double> elements) {
		super();
		this.elements = elements;
	}

	public void addElement(final String string, final double value) {
		elements.put(string, elements.get(string) + value);
	}

	public void divideElement(final String string, final double value) {
		if (value != 0)
			elements.put(string, elements.get(string) / value);
	}

	public Map<String, Double> getElements() {
		return elements;
	}

	public double getTotal() {
		double total = 0;
		for (final String key : elements.keySet())
			total += elements.get(key);
		return total;
	}

	public void normalizeElements(final long timeSliceDuration, final int partNumber) {
		for (final String key : elements.keySet())
			divideElement(key, (double) timeSliceDuration * (double) partNumber);
	}

	public void putElement(final String string, final double value) {
		elements.put(string, value);
	}

	public void setElements(final Map<String, Double> elements) {
		this.elements = elements;
	}

}
