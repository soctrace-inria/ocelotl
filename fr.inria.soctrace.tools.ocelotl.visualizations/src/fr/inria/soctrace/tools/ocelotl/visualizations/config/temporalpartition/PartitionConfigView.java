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

package fr.inria.soctrace.tools.ocelotl.visualizations.config.temporalpartition;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.tools.ocelotl.core.config.IVisuConfig;
import fr.inria.soctrace.tools.ocelotl.ui.views.IVisualizationWindow;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;


public class PartitionConfigView extends Dialog implements IVisualizationWindow {


	protected OcelotlView			ocelotlView;

	protected PartitionConfig	config;

	private Button btnAggregateParts;

	private Button btnShowPartNumber;

	public PartitionConfigView(final Shell shell) {
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
	public void init(final OcelotlView ocelotlView, final IVisuConfig config) {
		this.ocelotlView = ocelotlView;
		this.config = (PartitionConfig) config;
	}
}
