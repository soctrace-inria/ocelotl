/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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
package fr.inria.soctrace.tools.ocelotl.visualizations.config.eventcolor;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;
import fr.inria.soctrace.tools.ocelotl.core.config.IVisuConfig;
import fr.inria.soctrace.tools.ocelotl.ui.views.IVisualizationWindow;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Dialog to manage Framesoc colors
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class OcelotlManageColorsDialog extends ConfigurationDialog implements
		IVisualizationWindow {

	private OcelotlView ocelotlView;
	private ColorsConfig config;

	/**
	 * Constructor
	 * 
	 * @param parentShell
	 *            shell
	 */
	public OcelotlManageColorsDialog(Shell parentShell) {
		super(parentShell);
		this.images = new HashMap<String, Image>();
		this.entities = new TreeMap<Integer, Entity>();
		this.entities.put(0, new Entity(ET_NAME, ModelEntity.EVENT_TYPE));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		config.setTypes(ocelotlView.getOcelotlParameters().getTraceTypeConfig()
				.getTypes());
		Composite composite = (Composite) super.createDialogArea(parent);

		return composite;
	}

	@Override
	protected Collection<String> getNames() {
		return config.getTypeNames();
	}

	@Override
	public void init(OcelotlView ocelotlView, IVisuConfig aConfig) {
		this.ocelotlView = ocelotlView;
		this.config = (ColorsConfig) aConfig;
	}
	
	
}
