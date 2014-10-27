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

package fr.inria.soctrace.tools.ocelotl.core.idataaggregop;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;

public class DataAggregationOperatorResource {

	String operatorClass;
	String name;
	List<String> visuCompatibility = new ArrayList<String>();
	List<String> dimension = new ArrayList<String>();
	String paramWinClass;
	String paramConfig;
	int ts;
	String bundle;
	int selectionPriority;

	public DataAggregationOperatorResource() {
		// TODO Auto-generated constructor stub
	}

	public DataAggregationOperatorResource(final String operatorClass,
			final String name, final boolean generic,
			final List<String> traceFormats,
			final List<String> visuCompatibility, final String paramWinClass,
			final String paramConfig, final List<String> eventCategory,
			final String ts, final String unit, final String bundle) {
		super();
		this.operatorClass = operatorClass;
		this.name = name;
		this.visuCompatibility = visuCompatibility;
		this.paramWinClass = paramWinClass;
		this.paramConfig = paramConfig;
		setTs(ts);
		this.bundle = bundle;
	}

	public DataAggregationOperatorResource(final String operatorClass,
			final String name, final boolean generic,
			final String traceFormats, final String spaceCompatibility,
			final String paramWinClass, final String paramConfig,
			final String ts, final String unit, final String bundle) {
		super();
		this.operatorClass = operatorClass;
		this.name = name;
		setVisuCompatibility(spaceCompatibility);
		this.paramWinClass = paramWinClass;
		this.paramConfig = paramConfig;
		this.bundle = bundle;
	}

	public String getBundle() {
		return bundle;
	}

	public String getName() {
		return name;
	}

	public int getTs() {
		return ts;
	}

	public void setTs(String ts) {
		try {
			this.ts = Integer.parseInt(ts);
		} catch (NumberFormatException e) {
			this.ts = 0;
		}
	}

	public String getOperatorClass() {
		return operatorClass;
	}

	public String getParamConfig() {
		return paramConfig;
	}

	public String getParamWinClass() {
		return paramWinClass;
	}

	public List<String> getVisuCompatibility() {
		return visuCompatibility;
	}

	public List<String> getDimension() {
		return dimension;
	}

	public void setBundle(final String bundle) {
		this.bundle = bundle;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setOperatorClass(final String operatorClass) {
		this.operatorClass = operatorClass;
	}

	public void setParamConfig(final String paramConfig) {
		this.paramConfig = paramConfig;
	}

	public void setParamWinClass(final String paramWinClass) {
		this.paramWinClass = paramWinClass;
	}

	public void setVisuCompatibility(final List<String> visuCompatibility) {
		this.visuCompatibility = visuCompatibility;
	}

	public void setVisuCompatibility(final String visuCompatibility) {
		decompose(this.visuCompatibility, visuCompatibility);
	}

	public int getSelectionPriority() {
		return selectionPriority;
	}

	public void setSelectionPriority(String selectionPriority) {
		try {
			this.selectionPriority = Integer.parseInt(selectionPriority);
		} catch (NumberFormatException e) {
			this.selectionPriority = 0;
		}
	}

	private List<String> decompose(List<String> list, String string) {
		final String[] tmp = string
				.split(OcelotlConstants.MultipleValueExtensionSeparator);
		list.clear();
		for (final String s : tmp)
			list.add(s);
		return list;
	}

	public void setDimension(final List<String> dimension) {
		this.dimension = dimension;
	}

	public void setDimension(final String dimension) {
		decompose(this.dimension, dimension);
	}

}
