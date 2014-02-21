/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
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

package fr.inria.soctrace.tools.ocelotl.visualizations.operators.parts.config;

import fr.inria.soctrace.tools.ocelotl.core.config.ISpaceConfig;

public class PartsConfig implements ISpaceConfig {

	private boolean numbers=false;
	private boolean aggregated=true;
	
	public boolean isAggregated() {
		return aggregated;
	}
	public void setAggregated(boolean aggregated) {
		this.aggregated = aggregated;
	}
	public boolean isNumbers() {
		return numbers;
	}
	public void setNumbers(boolean numbers) {
		this.numbers = numbers;
	}
	public PartsConfig() {
		super();
		this.numbers=false;
		this.aggregated=true;
	}
	
	

}
