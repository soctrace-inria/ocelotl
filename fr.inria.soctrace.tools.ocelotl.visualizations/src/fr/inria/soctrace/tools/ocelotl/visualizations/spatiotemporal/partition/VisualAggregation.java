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
package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.partition;

import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IPartData;

public class VisualAggregation implements IPartData {
	private boolean visualAggregate = false;
	private boolean aggregated = true;
	private int value = -2;
	private boolean noCutInside = false;

	public VisualAggregation(boolean visualAggregate, boolean aggregated,
			int value, boolean noCutInside) {
		super();
		this.visualAggregate = visualAggregate;
		this.aggregated = aggregated;
		this.value = value;
		this.noCutInside = noCutInside;
	}

	public boolean isVisualAggregate() {
		return visualAggregate;
	}

	public void setVisualAggregate(boolean visualAggregate) {
		this.visualAggregate = visualAggregate;
	}

	public boolean isAggregated() {
		return aggregated;
	}

	public void setAggregated(boolean aggregated) {
		this.aggregated = aggregated;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean isNoCutInside() {
		return noCutInside;
	}

	public void setNoCutInside(boolean noCutInside) {
		this.noCutInside = noCutInside;
	}

}
