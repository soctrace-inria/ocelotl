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
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;

public class VariableDistributionView extends TypeDistributionView {

	public VariableDistributionView(final Shell shell) {
		super(shell);
	}


	int getType() {
		return EventCategory.VARIABLE;
	}

}
