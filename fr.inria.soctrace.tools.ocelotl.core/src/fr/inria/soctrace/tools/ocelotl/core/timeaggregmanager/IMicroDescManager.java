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

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.lpaggreg.quality.DLPQuality;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;

public interface IMicroDescManager {

	public void computeDichotomy(IProgressMonitor monitor) throws OcelotlException;

	public void computeParts(IProgressMonitor monitor);

	public void computeQualities(IProgressMonitor monitor);

	public List<EventProducer> getEventProducers();

	public List<Double> getParameters();

	public List<DLPQuality> getQualities();

	public void printParameters();

	public void printParts();

	public void reset(IProgressMonitor monitor) throws OcelotlException;

	public void print(OcelotlCore core);

}
