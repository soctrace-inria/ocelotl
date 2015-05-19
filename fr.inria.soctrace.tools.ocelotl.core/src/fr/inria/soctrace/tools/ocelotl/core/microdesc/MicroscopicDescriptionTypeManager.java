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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.idataaggregop.DataAggregationOperatorManager;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public class MicroscopicDescriptionTypeManager {
	
	protected HashMap<String, MicroscopicDescriptionTypeResource> typeList;
	protected String selectedType;
	protected MicroscopicDescription selectedMicroModel;

	private static final String POINT_ID = "fr.inria.soctrace.tools.ocelotl.core.microscopicmodel"; //$NON-NLS-1$
	private static final String OP_NAME = "name"; //$NON-NLS-1$
	private static final String OP_CLASS = "class"; //$NON-NLS-1$
	private static final String OP_EVENT_CATEGORY = "event_category"; //$NON-NLS-1$
	private static final String OP_SELECTION_PRIORITY = "selection_priority"; //$NON-NLS-1$
	private static final String OP_VISUALIZATION_COMPATIBILITY = "visual_compatibility"; //$NON-NLS-1$
	private static final String OP_STATS_COMPATIBILITY = "stats_compatibility"; //$NON-NLS-1$
	private static final String OP_TRACE_FORMATS = "trace_formats"; //$NON-NLS-1$
	private static final String OP_GENERIC = "generic"; //$NON-NLS-1$
	private static final String OP_UNIT = "unit"; //$NON-NLS-1$
	private static final String OP_UNIT_DESCRIPTION = "unit_description"; //$NON-NLS-1$
	
	private static final Logger logger = LoggerFactory
			.getLogger(DataAggregationOperatorManager.class);

	public MicroscopicDescriptionTypeManager() {
		try {
			init();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void activateSelectedMicroModel(OcelotlParameters params)
			throws OcelotlException {
		selectedMicroModel.setOcelotlParameters(params);
	}

	public List<String> getTypes(final String traceType,
			final List<String> category) {
		logger.debug("Comparing microscopic model format with " + traceType);
		final List<String> op = new ArrayList<String>();
		for (final MicroscopicDescriptionTypeResource resource : typeList.values()) {
			StringBuffer buff = new StringBuffer();
			buff.append(resource.getTraceFormats());
			logger.debug(buff.toString());
			if (resource.isGeneric() || resource.getTraceFormats().contains(traceType)) {
				for (String cat : category) {
					if (resource.getEventCategory().contains(cat)) {
						op.add(resource.getName());
						break;
					}
				}
			}
		}
		// Sort in alphabetical order
		Collections.sort(op, new Comparator<String>() {

			@Override
			public int compare(final String arg0, final String arg1) {
				int diff = typeList.get(arg0).getSelectionPriority()
						- typeList.get(arg1).getSelectionPriority();

				// If the two operators have the same priority
				if (diff == 0) {
					// Sort them alphabetically
					return arg0.compareTo(arg1);
				}
				return diff;
			}

		});
		return op;
	}

	private void init() throws SoCTraceException {
		typeList = new HashMap<String, MicroscopicDescriptionTypeResource>();

		final IExtensionRegistry reg = Platform.getExtensionRegistry();
		final IConfigurationElement[] config = reg
				.getConfigurationElementsFor(POINT_ID);
		logger.debug(config.length + " Metrics detected:");

		for (final IConfigurationElement e : config) {
			final MicroscopicDescriptionTypeResource resource = new MicroscopicDescriptionTypeResource();
			resource.setName(e.getAttribute(OP_NAME));
			resource.setMicroModelClass(e.getAttribute(OP_CLASS));
			resource.setBundle(e.getContributor().getName());
			resource.setType(e.getAttribute(OP_EVENT_CATEGORY));
			resource.setSelectionPriority(Integer.parseInt(e.getAttribute(OP_SELECTION_PRIORITY)));
			resource.setGeneric(e.getAttribute(OP_GENERIC).contains("true"));
			resource.setTraceFormats(e.getAttribute(OP_TRACE_FORMATS));
			resource.setEventCategory(e.getAttribute(OP_EVENT_CATEGORY));
			resource.setUnit(e.getAttribute(OP_UNIT));
			resource.setUnitDescription(e.getAttribute(OP_UNIT_DESCRIPTION));
			resource.setVisuCompatibility(e
					.getAttribute(OP_VISUALIZATION_COMPATIBILITY));
			resource.setStatsCompatibility(e
					.getAttribute(OP_STATS_COMPATIBILITY));
			typeList.put(resource.getName(), resource);
			logger.debug("    " + resource.getName() + " "
					+ resource.getTraceFormats() + " " + resource.getVisuCompatibility());
		}
	}

	public MicroscopicDescriptionTypeResource getSelectedOperatorResource() {
		return typeList.get(selectedType);
	}

	public String getSelectedType() {
		return selectedType;
	}

	public void setSelectedType(final String name) {
		selectedType = name;
	}

	public MicroscopicDescription getSelectedMicroModel() {
		return selectedMicroModel;
	}

	public void setSelectedMicroModel(final String name) {
		selectedMicroModel = instantiateMicroModel(name);
		selectedType = name;
	}
	
	public MicroscopicDescription instantiateMicroModel(final String name) {
		MicroscopicDescription aNewMicromodel = null;

		final Bundle mybundle = Platform.getBundle(typeList.get(name)
				.getBundle());
		try {
			aNewMicromodel = (MicroscopicDescription) mybundle.loadClass(
					typeList.get(name).getMicroModelClass()).newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return aNewMicromodel;
	}
	
	public HashMap<String, MicroscopicDescriptionTypeResource> getTypeList() {
		return typeList;
	}

	public void setTypeList(
			HashMap<String, MicroscopicDescriptionTypeResource> typeList) {
		this.typeList = typeList;
	}
}
