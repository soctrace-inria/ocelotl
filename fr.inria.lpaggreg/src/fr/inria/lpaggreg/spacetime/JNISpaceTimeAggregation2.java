/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
 * overview by using aggregation techniques
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

package fr.inria.lpaggreg.spacetime;

import java.util.HashMap;
import java.util.List;

import fr.inria.lpaggreg.jni.DLPAggregWrapper;


public class JNISpaceTimeAggregation2 extends JNISpaceTimeAggregation implements
		ISpaceTimeAggregation {

	public JNISpaceTimeAggregation2(){
		super();
		jniWrapper = new DLPAggregWrapper(2);
	}

	@Override
	public void addNode(int id, int parentID) {
		jniWrapper.newNode(id, parentID);
	}

	@Override
	public void addRoot(int id) {
		jniWrapper.newRoot(id);
	}

	@Override
	public void addLeaf(int id, int parentID, Object values, int weight) {
		@SuppressWarnings("unchecked")
		List<HashMap<String, Double>> val = (List<HashMap<String, Double>>) values;
		jniWrapper.newLeaf(id, parentID);
		for (int i = 0; i < val.size(); i++) {
			jniWrapper.addVector(id);
			for (String s : val.get(i).keySet()) {
				jniWrapper.push_back(id, val.get(i).get(s) / (double) weight);
			}
		}
	}

}
