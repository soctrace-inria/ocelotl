/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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

package fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators;

import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.ts.TimeSliceManager;

public interface ITimeSliceCubicMatrix {
	public void computeVectors();

	public List<HashMap<String, HashMap<String, Long>>> getMatrix();

	public Query getQueries();

	public TimeSliceManager getTimeSlicesManager();

	public int getVectorSize();

	public int getVectorsNumber();

	public void initVectors() throws SoCTraceException;

	public void print();

	public void setQueries(Query query);
}