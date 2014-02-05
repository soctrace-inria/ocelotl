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

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager;

import java.util.List;

import fr.inria.dlpaggreg.quality.DLPQuality;

public interface ITimeManager {

	public void computeDichotomy();

	public void computeParts();

	public void computeQualities();

	public void fillVectors();

	public List<String> getEventProducers();

	public List<Double> getParameters();

	public List<Integer> getParts();

	public List<DLPQuality> getQualities();

	public void printParameters();

	public void printParts();

	public void reset();

}
