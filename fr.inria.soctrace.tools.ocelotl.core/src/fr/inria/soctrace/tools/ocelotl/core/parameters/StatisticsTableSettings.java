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
package fr.inria.soctrace.tools.ocelotl.core.parameters;

public class StatisticsTableSettings {
	
	private int columnNumber;
	private int direction;
	private double[] columnWidthWeight;

	public StatisticsTableSettings(){
		columnNumber = OcelotlDefaultParameterConstants.SORT_TABLE_DEFAULT_COLUMN;
		direction = OcelotlDefaultParameterConstants.SORT_TABLE_DEFAULT_ORDER;
		columnWidthWeight = new double[] { 1.0/3.0, 1.0/3.0, 1.0/3.0 };
	}

	public StatisticsTableSettings(int aColumn, int aDirection){
		columnNumber = aColumn;
		direction = aDirection;
	}
	
	public int getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(int columnNumber) {
		this.columnNumber = columnNumber;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public double[] getColumnWidthWeight() {
		return columnWidthWeight;
	}

	public void setColumnWidthWeight(double[] columnWidthWeight) {
		this.columnWidthWeight = columnWidthWeight;
	}
	
}
