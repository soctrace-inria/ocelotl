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

public enum OcelotlStatisticsTableColumn implements ITableColumn {

	NAME("Name", 120), PERCENTAGE("Percentage", 120), VALUE("Value", 120);

	private String name;
	private int width;

	private OcelotlStatisticsTableColumn(String name, int width) {
		this.name = name;
		this.width = width;
	}

	@Override
	public String getHeader() {
		return name;
	}

	@Override
	public int getWidth() {
		return width;
	}
}
