/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AggregationOperators {

	static public String		ActivityTime						= "Activity Time";
	static public String		ActivityTimeProbabilityDistribution	= "Activity Time Probability Distribution";
	static public String		ActivityTimeByStateType				= "Activity Time by State Type";
	static public List<String>	List								= new ArrayList<String>(Arrays.asList(ActivityTime, ActivityTimeProbabilityDistribution, ActivityTimeByStateType));

}
