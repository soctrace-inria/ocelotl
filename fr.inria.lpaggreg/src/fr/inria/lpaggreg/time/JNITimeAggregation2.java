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

package fr.inria.lpaggreg.time;

import fr.inria.lpaggreg.jni.OLPAggregWrapper;

public class JNITimeAggregation2 extends JNITimeAggregation {

	public JNITimeAggregation2(){
		super();
		jniWrapper = new OLPAggregWrapper(2);
	}

	public void addVector() {
		jniWrapper.addVector();
	}

	public void push_back(double value) {
		jniWrapper.push_back(value);
	}

}
