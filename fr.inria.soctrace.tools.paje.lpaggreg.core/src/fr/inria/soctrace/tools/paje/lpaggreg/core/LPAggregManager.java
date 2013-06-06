/* ===========================================================
 * LPAggreg core module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to visualize a Paje
 * trace across an aggregated representation.
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
 */

package fr.inria.soctrace.tools.paje.lpaggreg.core;

import java.util.ArrayList;
import java.util.List;

public abstract class LPAggregManager implements ILPAggregManager{

	static {
		try {
			System.loadLibrary("lpaggregjni");
		} catch (UnsatisfiedLinkError e) {
			System.err.println("Native code library failed to load. \n" + e);
			System.exit(1);
		}
		System.err.println("Native code library loaded. \n");
	}

	protected List<Integer>		parts		= new ArrayList<Integer>();
	protected List<Float>			parameters	= new ArrayList<Float>();
	protected List<List<Boolean>>	eqMatrix;

	public LPAggregManager() {
		super();
	}

	public abstract void computeDichotomy();
	
	public abstract void computeParts();

	public abstract void computeQualities();

	public abstract void fillVectors();

	public List<Float> getParameters() {
		return parameters;
	}

	public List<Integer> getParts() {
		return parts;
	}

	public void printParameters() {
		System.out.println();
		System.out.println("Parameters :");
		for (float i : parameters)
			System.out.print(i + " ");
		System.out.println();
	}

	public void printParts() {
		System.out.println();
		System.out.println("Parts :");
		for (int i : parts)
			System.out.print(i + " ");
		System.out.println();
	}

	public abstract void reset();

}
