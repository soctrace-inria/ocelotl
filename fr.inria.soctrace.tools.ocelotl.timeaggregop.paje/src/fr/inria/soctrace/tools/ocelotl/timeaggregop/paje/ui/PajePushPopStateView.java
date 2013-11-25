/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
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

package fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.ui;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.paje.config.PajeConfig;
import fr.inria.soctrace.tools.ocelotl.ui.com.eclipse.wb.swt.SWTResourceManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.ISettingApplicationWindow;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeExternalConstants;

public class PajePushPopStateView extends PajeView {

	

	public PajePushPopStateView(Shell shell) {
		super(shell);
	}

	@Override
	public void setParameters() {
		if (!init){
		for (int i = 0; i < ocelotlView.getConfDataLoader().getTypes().size(); i++)
			if (ocelotlView.getConfDataLoader().getTypes().get(i).getName().contains(PajeExternalConstants.PajeSetState)) {
				if (!config.getTypes().contains(ocelotlView.getConfDataLoader().getTypes().get(i)))
					config.getTypes().add(ocelotlView.getConfDataLoader().getTypes().get(i));
				break;
			}
		for (int i = 0; i < ocelotlView.getConfDataLoader().getTypes().size(); i++)
			if (ocelotlView.getConfDataLoader().getTypes().get(i).getName().contains(PajeExternalConstants.PajePushState)) {
				if (!config.getTypes().contains(ocelotlView.getConfDataLoader().getTypes().get(i)))
					config.getTypes().add(ocelotlView.getConfDataLoader().getTypes().get(i));
				break;
			}
		for (int i = 0; i < ocelotlView.getConfDataLoader().getTypes().size(); i++)
			if (ocelotlView.getConfDataLoader().getTypes().get(i).getName().contains(PajeExternalConstants.PajePopState)) {
				if (!config.getTypes().contains(ocelotlView.getConfDataLoader().getTypes().get(i)))
					config.getTypes().add(ocelotlView.getConfDataLoader().getTypes().get(i));
				break;
			}
		listViewerEventTypes.setInput(config.getTypes());
		if (!config.getIdles().contains("IDLE"))
			config.getIdles().add("IDLE");
		listViewerIdleStates.setInput(config.getIdles());
		init=true;
		}
		
	}

}
