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

package fr.inria.soctrace.tools.ocelotl.microdesc.ui;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.lib.model.EventType;

public abstract class TypeDistributionView extends DistributionBaseView {

	public TypeDistributionView(final Shell shell) {
		super(shell);
	}
	
	abstract int getType();

	@Override
	public void setParameters() {
		if (config.getTypes().isEmpty())
			for (int i = 0; i < ocelotlView.getConfDataLoader().getTypes()
					.size(); i++)
				if (ocelotlView.getConfDataLoader().getTypes().get(i)
						.getCategory() == getType())
					config.getTypes().add(
							ocelotlView.getConfDataLoader().getTypes().get(i));
		
	}

	@Override
	protected java.util.List<EventType> getEventTypes() {
		java.util.List<EventType> types = new ArrayList<EventType>();
		for (int i = 0; i < ocelotlView.getConfDataLoader().getTypes().size(); i++)
			if (ocelotlView.getConfDataLoader().getTypes().get(i).getCategory() == getType())
				types.add(ocelotlView.getConfDataLoader().getTypes().get(i));
		return types;
	}
}
