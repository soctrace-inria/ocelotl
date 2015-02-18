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

package fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class UnitAxisViewManager {

	OcelotlView	ocelotlView;

	public UnitAxisViewManager(final OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	public UnitAxisView create() {
		UnitAxisView unitAxisView = null;
		try {
			final Bundle mybundle = Platform.getBundle(ocelotlView.getCore().getVisuOperators().getSelectedOperatorResource().getBundle());
			unitAxisView = (UnitAxisView) mybundle.loadClass(ocelotlView.getCore().getVisuOperators().getSelectedOperatorResource().getYAxisView()).getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return unitAxisView;
	}

}
