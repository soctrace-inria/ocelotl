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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Event;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.framesoc.ui.providers.SquareIconLabelProvider;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.statistics.operators.SummaryStat.SummaryStatModel;

public class OcelotlStatisticsTableRowLabelImageProvider extends
		SquareIconLabelProvider {

	/**
	 * Managed column
	 */
	protected ITableColumn col;

	/**
	 * Constructor
	 * 
	 * @param col
	 *            ITableColumn the provider is related to.
	 */
	public OcelotlStatisticsTableRowLabelImageProvider(ITableColumn col) {
		super();
		this.col = col;
	}

	@Override
	protected void paint(Event event, Object element) {
		super.paint(event, element);
	}

	@Override
	public String getText(Object element) {
		String text = "";
		try {
			text = ((ITableRow) element).get(col);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		return text;
	}

	@Override
	public Color getColor(Object element) {
		SummaryStatModel row = (SummaryStatModel) element;
		return row.getColor();
	}
}
