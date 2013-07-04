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

	public LPAggregManager() {
		super();
	}

	@Override
	public abstract void computeDichotomy();

	@Override
	public abstract void computeParts();

	@Override
	public abstract void computeQualities();

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
