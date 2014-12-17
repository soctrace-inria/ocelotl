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

package fr.inria.soctrace.tools.ocelotl.ui.views.statview;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.tools.ocelotl.core.statistics.IStatisticsProvider;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class StatViewManager {

	OcelotlView	ocelotlView;

	public StatViewManager(final OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	public IStatView create() {
		IStatView statView = null;
		IStatisticsProvider statProvider = null;
		try {
			final Bundle mybundle = Platform.getBundle(ocelotlView.getCore().getStatOperators().getSelectedOperatorResource().getBundle());
			statView = (IStatView) mybundle.loadClass(ocelotlView.getCore().getStatOperators().getSelectedOperatorResource().getVisualization()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlView);

			// Set statistics operator
			statProvider = (IStatisticsProvider) mybundle.loadClass(ocelotlView.getCore().getStatOperators().getSelectedOperatorResource().getOperatorClass()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlView);
			statView.setStatProvider(statProvider);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statView;
	}

	public IStatView create(String aVisualization) {
		IStatView statView = null;
		IStatisticsProvider statProvider = null;
		try {
			final Bundle mybundle = Platform.getBundle(ocelotlView.getCore().getStatOperators().getSelectedOperatorResource(aVisualization).getBundle());
			statView = (IStatView) mybundle.loadClass(ocelotlView.getCore().getStatOperators().getSelectedOperatorResource(aVisualization).getVisualization()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlView);

			statProvider = (IStatisticsProvider) mybundle.loadClass(ocelotlView.getCore().getStatOperators().getSelectedOperatorResource().getOperatorClass()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlView);
			statView.setStatProvider(statProvider);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statView;
	}

}
