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
package fr.inria.soctrace.tools.ocelotl.core.microdesc;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;

public class MicroscopicDescriptionTypeResource {

	protected String type;
	protected String name;
	protected String microModelClass;
	protected String bundle;
	int selectionPriority;
	boolean generic;
	String unit;
	List<String> traceFormats = new ArrayList<String>();
	List<String> eventCategory = new ArrayList<String>();
	List<String> visuCompatibility = new ArrayList<String>();
	List<String> statsCompatibility = new ArrayList<String>();
	String unitDescription;

	public MicroscopicDescriptionTypeResource() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String aType) {
		type = aType;
	}

	public String getMicroModelClass() {
		return microModelClass;
	}

	public void setMicroModelClass(String microModelClass) {
		this.microModelClass = microModelClass;
	}

	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

	public int getSelectionPriority() {
		return selectionPriority;
	}

	public void setSelectionPriority(int selectionPriority) {
		this.selectionPriority = selectionPriority;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public List<String> getTraceFormats() {
		return traceFormats;
	}

	public boolean isGeneric() {
		return generic;
	}

	public String getUnitDescription() {
		return unitDescription;
	}

	public void setGeneric(final boolean generic) {
		this.generic = generic;
	}

	public void setEventCategory(final List<String> eventCategory) {
		this.eventCategory = eventCategory;
	}

	public void setEventCategory(final String eventCategory) {
		decompose(this.eventCategory, eventCategory);
	}

	public void setTraceFormats(final List<String> traceFormats) {
		this.traceFormats = traceFormats;
	}

	public void setTraceFormats(final String traceFormats) {
		decompose(this.traceFormats, traceFormats);
	}

	public void setUnitDescription(String attribute) {
		unitDescription = attribute;
	}

	public List<String> getEventCategory() {
		return eventCategory;
	}

	public void setVisuCompatibility(final List<String> visuCompatibility) {
		this.visuCompatibility = visuCompatibility;
	}

	public void setVisuCompatibility(final String visuCompatibility) {
		decompose(this.visuCompatibility, visuCompatibility);
	}
	

	public List<String> getVisuCompatibility() {
		return visuCompatibility;
	}

	private List<String> decompose(List<String> list, String string) {
		final String[] tmp = string
				.split(OcelotlConstants.MultipleValueExtensionSeparator);
		list.clear();
		for (final String s : tmp)
			list.add(s);
		return list;
	}

	public List<String> getStatsCompatibility() {
		return statsCompatibility;
	}
	
	public void setStatsCompatibility(final String statsCompatibility) {
		decompose(this.statsCompatibility, statsCompatibility);
	}
	
}
