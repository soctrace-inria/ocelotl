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
package fr.inria.soctrace.tools.ocelotl.core.statistics;

import java.util.List;

import fr.inria.soctrace.framesoc.ui.model.ITableRow;

public interface IStatisticsProvider {
	
	/**
	 * Compute the statistics data
	 */
	public void computeData();

	/*
	 * Provide a list of data to put in the table
	 */
	public List<ITableRow> getTableData();
	
	/**
	 * Update the color of the event types
	 */
	public void updateColor();
}
