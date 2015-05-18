/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.statistics.view;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.providers.TableRowLabelProvider;

public class OcelotlStatisticsTableRowLabelProvider extends
		TableRowLabelProvider {

	protected String toolTip;

	public OcelotlStatisticsTableRowLabelProvider(ITableColumn col) {
		super(col);
		toolTip = "";
	}

	/**
	 * Provide the displayed tooltip
	 */
	@Override
	public String getToolTipText(Object element) {
		// If empty or only white space(s)
		if(toolTip.trim().isEmpty())
			// Do not show anything
			return null;
		
		return toolTip;
	}

	public String getToolTip() {
		return toolTip;
	}

	public void setToolTip(String toolTip) {
		this.toolTip = toolTip.toString();
	}
}
