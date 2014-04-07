/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
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

package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class TimeLineViewManager {

	OcelotlView	ocelotlView;

	public TimeLineViewManager(final OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	public IAggregatedView create() {
		IAggregatedView timeLineView = null;
		try {
			final Bundle mybundle = Platform.getBundle(ocelotlView.getCore().getSpaceOperators().getSelectedOperatorResource().getBundle());
			timeLineView = (IAggregatedView) mybundle.loadClass(ocelotlView.getCore().getSpaceOperators().getSelectedOperatorResource().getVisualization()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlView);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeLineView;

	}

}
