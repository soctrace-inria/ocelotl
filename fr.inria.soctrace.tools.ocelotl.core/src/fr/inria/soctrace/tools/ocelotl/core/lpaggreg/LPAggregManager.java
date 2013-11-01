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

package fr.inria.soctrace.tools.ocelotl.core.lpaggreg;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.jni.LPAggregWrapper;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public abstract class LPAggregManager implements ILPAggregManager {

	static {
		try {
			System.loadLibrary("lpaggregjni");
		} catch (final UnsatisfiedLinkError e) {
			System.err.println("Native code library failed to load. \n" + e);
			System.exit(1);
		}
		System.err.println("Native code library loaded. \n");
	}

	protected List<Integer>			parts		= new ArrayList<Integer>();
	protected List<Quality>			qualities	= new ArrayList<Quality>();
	protected List<Float>			parameters	= new ArrayList<Float>();
	protected List<List<Boolean>>	eqMatrix;
	protected LPAggregWrapper		lpaggregWrapper;
	protected OcelotlParameters		ocelotlParameters;

	public LPAggregManager(final OcelotlParameters ocelotlParameters) {
		super();
		this.ocelotlParameters = ocelotlParameters;
	}

	@Override
	public void computeDichotomy() {
		final DeltaManager dm = new DeltaManager();
		dm.start();
		parameters.clear();
		qualities.clear();
		lpaggregWrapper.computeDichotomy(ocelotlParameters.getThreshold());
		for (int i = 0; i < lpaggregWrapper.getParameterNumber(); i++) {
			parameters.add(lpaggregWrapper.getParameter(i));
			qualities.add(new Quality(lpaggregWrapper.getGainByIndex(i), lpaggregWrapper.getLossByIndex(i), lpaggregWrapper.getParameter(i)));
		}
		dm.end("LPAGGREG - PARAMETERS LIST");

	}

	@Override
	public void computeParts() {
		parts.clear();
		final DeltaManager dm = new DeltaManager();
		dm.start();
		lpaggregWrapper.computeParts(ocelotlParameters.getParameter());
		for (int i = 0; i < lpaggregWrapper.getPartNumber(); i++)
			parts.add(lpaggregWrapper.getPart(i));
		dm.end("LPAGGREG - COMPUTE PARTS");
	}

	@Override
	public void computeQualities() {
		final DeltaManager dm = new DeltaManager();
		dm.start();
		lpaggregWrapper.computeQualities(ocelotlParameters.isNormalize());
		dm.end("LPAGGREG - COMPUTE QUALITIES");
	}

	@Override
	public abstract void fillVectors();

	@Override
	public List<Float> getParameters() {
		return parameters;
	}

	@Override
	public List<Integer> getParts() {
		return parts;
	}

	@Override
	public List<Quality> getQualities() {
		return qualities;
	}

	@Override
	public void printParameters() {
		System.out.println();
		System.out.println("Parameters :");
		for (final float i : parameters)
			System.out.print(i + " ");
		System.out.println();
	}

	@Override
	public void printParts() {
		System.out.println();
		System.out.println("Parts :");
		for (final int i : parts)
			System.out.print(i + " ");
		System.out.println();
	}

	@Override
	public abstract void reset();
	


}
