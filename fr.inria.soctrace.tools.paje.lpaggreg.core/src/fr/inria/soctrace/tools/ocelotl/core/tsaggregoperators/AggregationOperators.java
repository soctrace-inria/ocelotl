package fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class AggregationOperators {
	
	static public String ActivityTime = "Activity Time";
	static public String ActivityTimeProbabilityDistribution = "Activity Time Probability Distribution";
	static public String ActivityTimeByStateType = "Activity Time by State Type";
	static public List<String> List= new ArrayList<String>(Arrays.asList(ActivityTime, ActivityTimeProbabilityDistribution, ActivityTimeByStateType));
	
}
