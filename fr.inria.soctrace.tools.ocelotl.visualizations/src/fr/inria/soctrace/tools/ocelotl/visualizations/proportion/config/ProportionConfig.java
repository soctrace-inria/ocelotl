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

package fr.inria.soctrace.tools.ocelotl.visualizations.proportion.config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.core.config.ISpaceConfig;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.views.StateColorManager;

public class ProportionConfig implements ISpaceConfig {

	private List<EventType>	types	= new LinkedList<EventType>();
	private StateColorManager colors;

	public ProportionConfig() {
		super();
	}
	
	public List<EventType> getTypes() {
		return types;
	}
	
	public List<String> getTypeNames() {
		List<String> l = new ArrayList<String>();
		for (EventType et: types){
			l.add(et.getName());
		}
		return l;
	}

	public void setTypes(final List<EventType> types) {
		this.types = types;
	}


	public StateColorManager getColors() {
		return colors;
	}


	public void initColors() {
		this.colors = new StateColorManager();
	}

}
