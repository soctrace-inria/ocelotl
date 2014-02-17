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

package fr.inria.soctrace.tools.ocelotl.spaceaggregop.operators.parts.config;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.tools.ocelotl.core.config.ISpaceConfig;
import fr.inria.soctrace.tools.ocelotl.ui.com.eclipse.wb.swt.SWTResourceManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.ISetting2ApplicationWindow;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

import org.eclipse.swt.widgets.Label;


public class PartsConfigView extends Dialog implements ISetting2ApplicationWindow {


	protected OcelotlView			ocelotlView;

	protected PartsConfig	config;

	private Button btnAggregateParts;

	private Button btnShowPartNumber;

	public PartsConfigView(final Shell shell) {
		super(shell);
		ocelotlView = null;
		config = null;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Aggregation Operator Settings");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		Composite all = (Composite) super.createDialogArea(parent);
		
		btnAggregateParts = new Button(all, SWT.CHECK);
		btnAggregateParts.setFont(org.eclipse.wb.swt.SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		btnAggregateParts.setSelection(config.isAggregated());
		btnAggregateParts.setText("Aggregate parts");
		
		
		btnShowPartNumber = new Button(all, SWT.CHECK);
		btnShowPartNumber.setFont(org.eclipse.wb.swt.SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		btnShowPartNumber.setSelection(config.isNumbers());
		btnShowPartNumber.setText("Show part number");
		return all;

	}
	
    @Override
    protected void okPressed() {
    	setParameters();
    	super.okPressed();
    }
    
    @Override
    protected void cancelPressed() {
    	super.cancelPressed();
    }

	private void setParameters() {
		config.setAggregated(btnAggregateParts.getSelection());
		config.setNumbers(btnShowPartNumber.getSelection());	
	}

	@Override
	public void init(final OcelotlView ocelotlView, final ISpaceConfig config) {
		this.ocelotlView = ocelotlView;
		this.config = (PartsConfig) config;
	}
}
