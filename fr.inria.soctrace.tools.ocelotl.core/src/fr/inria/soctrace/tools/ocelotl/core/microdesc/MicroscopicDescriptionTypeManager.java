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
	private static final String OP_TYPE = "type"; //$NON-NLS-1$

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
			for (String cat : category) {
				if (resource.getType().contains(cat)) {
					op.add(resource.getName());
					break;
				}
			}
		}
		// Sort in alphabetical order
		Collections.sort(op, new Comparator<String>() {

			@Override
			public int compare(final String arg0, final String arg1) {
				return arg0.compareTo(arg1);
			}

		});
		return op;
	}

	private void init() throws SoCTraceException {
		typeList = new HashMap<String, MicroscopicDescriptionTypeResource>();

		final IExtensionRegistry reg = Platform.getExtensionRegistry();
		final IConfigurationElement[] config = reg
				.getConfigurationElementsFor(POINT_ID);
		logger.debug(config.length + " Microscopic model types detected:");

		for (final IConfigurationElement e : config) {
			final MicroscopicDescriptionTypeResource resource = new MicroscopicDescriptionTypeResource();
			resource.setName(e.getAttribute(OP_NAME));
			resource.setMicroModelClass(e.getAttribute(OP_CLASS));
			resource.setBundle(e.getContributor().getName());
			resource.setType(e.getAttribute(OP_TYPE));
			typeList.put(resource.getName(), resource);
			logger.debug("    " + resource.getName());
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
		final Bundle mybundle = Platform.getBundle(typeList.get(name)
				.getBundle());
	
		try {
			selectedMicroModel = (MicroscopicDescription) mybundle.loadClass(
					typeList.get(name).getMicroModelClass()).newInstance();
			selectedType = name;
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public HashMap<String, MicroscopicDescriptionTypeResource> getTypeList() {
		return typeList;
	}

	public void setTypeList(
			HashMap<String, MicroscopicDescriptionTypeResource> typeList) {
		this.typeList = typeList;
	}
}