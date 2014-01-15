package fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop;

import java.util.ArrayList;
import java.util.List;

public class SpaceAggregationOperatorResource {

	String			operatorClass;
	String			name;
	List<String>	timeCompatibility = new ArrayList<String>();
//	String			paramWinClass;
//	String			paramConfig;
	String			visualization;
	String			bundle;

	public SpaceAggregationOperatorResource() {
		// TODO Auto-generated constructor stub
	}

//	public SpaceAggregationOperatorResource(final String operatorClass, final String name, final List<String> timeCompatibility, String visualization, final String paramWinClass, final String paramConfig, final String bundle) {
//		super();
//		this.operatorClass = operatorClass;
//		this.name = name;
//		this.timeCompatibility = timeCompatibility;
//		this.visualization = visualization;
//		this.paramWinClass = paramWinClass;
//		this.paramConfig = paramConfig;
//		this.bundle = bundle;
//	}
	
	public SpaceAggregationOperatorResource(final String operatorClass, final String name, final List<String> timeCompatibility, String visualization, final String bundle) {
		super();
		this.operatorClass = operatorClass;
		this.name = name;
		this.timeCompatibility = timeCompatibility;
		this.visualization = visualization;
//		this.paramWinClass = paramWinClass;
//		this.paramConfig = paramConfig;
		this.bundle = bundle;
	}

	public String getBundle() {
		return bundle;
	}

	public String getName() {
		return name;
	}

	public String getOperatorClass() {
		return operatorClass;
	}

	public void setTimeCompatibility(String string) {
		final String[] tmp = string.split(", ");
		for (final String s : tmp)
			this.timeCompatibility.add(s);
	}
	
	

	public List<String> getTimeCompatibility() {
		return timeCompatibility;
	}

	public String getVisualization() {
		return visualization;
	}

	public void setVisualization(String visualization) {
		this.visualization = visualization;
	}

//	public String getParamConfig() {
//		return paramConfig;
//	}
//
//	public String getParamWinClass() {
//		return paramWinClass;
//	}


	public void setBundle(final String bundle) {
		this.bundle = bundle;
	}


	public void setName(final String name) {
		this.name = name;
	}

	public void setOperatorClass(final String operatorClass) {
		this.operatorClass = operatorClass;
	}

//	public void setParamConfig(final String paramConfig) {
//		this.paramConfig = paramConfig;
//	}
//
//	public void setParamWinClass(final String paramWinClass) {
//		this.paramWinClass = paramWinClass;
//	}

}
