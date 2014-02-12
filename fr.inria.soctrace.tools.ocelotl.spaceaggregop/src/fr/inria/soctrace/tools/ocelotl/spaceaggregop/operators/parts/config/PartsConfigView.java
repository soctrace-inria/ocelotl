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


public class PartsConfigView extends ApplicationWindow implements ISetting2ApplicationWindow {


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
	public Control createContents(final Composite parent) {
		// parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		final SashForm sashFormGlobal = new SashForm(parent, SWT.VERTICAL);
		sashFormGlobal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		final Composite OK = new Composite(sashFormGlobal, SWT.NONE);
		OK.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		OK.setLayout(new FormLayout());

		final Button buttonOK = new Button(OK, SWT.NONE);
		final FormData fd_buttonOK = new FormData();
		fd_buttonOK.bottom = new FormAttachment(100, -10);
		fd_buttonOK.right = new FormAttachment(100, -10);
		buttonOK.setLayoutData(fd_buttonOK);
		buttonOK.setText("OK");
		buttonOK.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		buttonOK.setImage(null);
		
		Composite composite = new Composite(OK, SWT.NONE);
		composite.setLayoutData(new FormData());
		
		btnAggregateParts = new Button(composite, SWT.CHECK);
		btnAggregateParts.setSize(101, 22);
		btnAggregateParts.setFont(org.eclipse.wb.swt.SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAggregateParts.setSelection(config.isAggregated());
		btnAggregateParts.setText("Aggregate parts");
		
		
		btnShowPartNumber = new Button(composite, SWT.CHECK);
		btnShowPartNumber.setLocation(0, 0);
		btnShowPartNumber.setSize(165, 126);
		btnShowPartNumber.setFont(org.eclipse.wb.swt.SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnShowPartNumber.setSelection(config.isNumbers());
		btnShowPartNumber.setText("Show part number");
		sashFormGlobal.setWeights(new int[] { 53 });
		buttonOK.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				setParameters();
				close();
			}
		});
		return sashFormGlobal;

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
