/* ===========================================================
 * Filters UI module
 * ===========================================================
 *
 * (C) Copyright 2013 Damien Dosimont. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 */
package fr.inria.soctrace.tools.filters.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.filters.timefilter.TimeFilter;
import fr.inria.soctrace.tools.filters.timefilter.TimeFilterParameters;
import fr.inria.soctrace.tools.filters.timefilter.TimeRegion;
import fr.inria.soctrace.tools.filters.ui.Activator;
import fr.inria.soctrace.tools.filters.ui.loaders.ConfDataLoader;
import org.eclipse.wb.swt.ResourceManager;

/**
 * View for Filter Tool
 * 
 * @author Damien Dosimont <damien.dosimont@imag.fr"
 */
public class FilterView extends ViewPart {

	public static final String PLUGIN_ID = Activator.PLUGIN_ID;

	public static final String ID = "fr.inria.soctrace.tools.filters.ui.views.FilterView"; //$NON-NLS-1$

	private boolean hasChanged = true;

	/**
	 * Loader to interact with the DB
	 */
	private ConfDataLoader loader = new ConfDataLoader();

	private Text timestampStart;
	private Text timestampEnd;
	private Text label;
	private Button include;
	private Button exclude;

	TimeFilter timeFilter;
	TimeFilterParameters params;

	final Map<Integer, Trace> traceMap = new HashMap<Integer, Trace>();


	/**
	 * @throws SoCTraceException
	 * 
	 */
	public FilterView() throws SoCTraceException {
		setTitleImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.filters.ui", "icons/1366759848_filter_data.png"));
		try {
			loader.loadTraces();
		} catch (SoCTraceException e) {
			MessageDialog.openError(getSite().getShell(), "Exception",
					e.getMessage());
		}
		params = new TimeFilterParameters();
		timeFilter = new TimeFilter(params);
	}

	private void setConfiguration() {

		params.setTrace(loader.getCurrentTrace());
		try {
			params.setTimeRegion(new TimeRegion(Long.valueOf(timestampStart
					.getText()), Long.valueOf(timestampEnd.getText())));
		} catch (NumberFormatException e) {
			MessageDialog.openError(getSite().getShell(), "Exception",
					e.getMessage());
		}
		params.setLabel(label.getText());
		params.setInclude(include.getSelection());
	}

	@Override
	public void createPartControl(Composite parent) {

		// Highest Component
		SashForm sashForm = new SashForm(parent, SWT.NONE);

		TabFolder tabFolder = new TabFolder(sashForm, SWT.NONE);

		TabItem tbtmTimeFilter = new TabItem(tabFolder, SWT.NONE);
		tbtmTimeFilter.setText("Time Filter");

		SashForm sashForm_2 = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmTimeFilter.setControl(sashForm_2);

		Composite composite_2 = new Composite(sashForm_2, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));

		final Combo comboTraces = new Combo(composite_2, SWT.READ_ONLY);
		comboTraces.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));
		comboTraces.setFont(SWTResourceManager.getFont("Cantarell", 9,
				SWT.NORMAL));

		Group grpResultLabel = new Group(sashForm_2, SWT.NONE);
		grpResultLabel.setFont(SWTResourceManager.getFont("Cantarell", 9,
				SWT.NORMAL));
		grpResultLabel.setText("Result Label");
		grpResultLabel.setLayout(new GridLayout(1, false));

		label = new Text(grpResultLabel, SWT.BORDER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		label.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		Group grpEventProducers = new Group(sashForm_2, SWT.NONE);
		grpEventProducers.setFont(SWTResourceManager
				.getFont("Cantarell", 9, SWT.NORMAL));
		grpEventProducers.setText("Producers");
		grpEventProducers.setLayout(new GridLayout(2, false));

		include = new Button(grpEventProducers, SWT.RADIO);
		include.setSelection(true);
		include.setFont(SWTResourceManager.getFont("Cantarell", 9,
				SWT.NORMAL));
		include.setText("contain events");

		exclude = new Button(grpEventProducers, SWT.RADIO);
		exclude.setFont(SWTResourceManager.getFont("Cantarell", 9,
				SWT.NORMAL));
		exclude.setText("do not contain events");

		Group groupTime = new Group(sashForm_2, SWT.NONE);
		groupTime.setFont(SWTResourceManager
				.getFont("Cantarell", 9, SWT.NORMAL));
		groupTime.setText("Time Interval");
		groupTime.setLayout(new GridLayout(2, false));

		Label lblStartTimestamp = new Label(groupTime, SWT.NONE);
		lblStartTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 9,
				SWT.NORMAL));
		lblStartTimestamp.setText("Start Timestamp");

		timestampStart = new Text(groupTime, SWT.BORDER);
		timestampStart.setFont(SWTResourceManager.getFont("Cantarell", 9,
				SWT.NORMAL));
		timestampStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		timestampStart.addModifyListener(new ConfModificationListener());

		Label lblEndTimestamp = new Label(groupTime, SWT.NONE);
		lblEndTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 9,
				SWT.NORMAL));
		lblEndTimestamp.setText("End Timestamp");

		timestampEnd = new Text(groupTime, SWT.BORDER);
		timestampEnd.setFont(SWTResourceManager.getFont("Cantarell", 9,
				SWT.NORMAL));
		timestampEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		timestampEnd.addModifyListener(new ConfModificationListener());

		Composite composite = new Composite(sashForm_2, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		Button btnProcess = new Button(composite, SWT.NONE);
		btnProcess.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnProcess.setFont(SWTResourceManager.getFont("Cantarell", 9,
				SWT.NORMAL));
		btnProcess.setText("Process");
		sashForm_2.setWeights(new int[] {43, 56, 47, 87, 39});

		btnProcess.addSelectionListener(new RunSelectionListener());
		comboTraces.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cleanAll();
				try {
					loader.load(traceMap.get(comboTraces.getSelectionIndex()));
					timestampStart.setText(String.valueOf(loader
							.getMinTimestamp()));
					timestampEnd.setText(String.valueOf(loader
							.getMaxTimestamp()));
				} catch (SoCTraceException e1) {
					MessageDialog.openError(getSite().getShell(), "Exception",
							e1.getMessage());
				}
			}
		});
		int index = 0;
		for (Trace t : loader.getTraces()) {
			comboTraces.add(t.getAlias(), index);
			traceMap.put(index, t);
			index++;
		}

		GridData gd_listIdle = new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1);
		gd_listIdle.widthHint = 203;
		sashForm.setWeights(new int[] { 268 });
		include.addSelectionListener(new IncludeSelectionListener());
		include.addSelectionListener(new ExcludeSelectionListener());
		label.addModifyListener(new ConfModificationListener());
		cleanAll();

	}

	private void cleanAll() {
		timestampStart.setText("0");
		timestampEnd.setText("0");
		include.setSelection(true);
		exclude.setSelection(false);
		label.setText("Type Result Label");
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	private class ConfModificationListener implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			if (loader.getCurrentTrace() == null)
				return;
			hasChanged=true;
			try {
				if ( (Long.parseLong(timestampEnd.getText()) > loader.getMaxTimestamp())
						|| (Long.parseLong(timestampEnd.getText()) < loader.getMinTimestamp())) 
				{
					timestampEnd.setText(String.valueOf(loader.getMaxTimestamp()));
				}
			} catch (NumberFormatException err) {
				timestampEnd.setText("0");
			}
			try {
				if ((Long.parseLong(timestampStart.getText()) < loader.getMinTimestamp())
						|| (Long.parseLong(timestampStart.getText()) > loader.getMaxTimestamp())) 
				{
					timestampStart.setText(String.valueOf(loader.getMinTimestamp()));
				}
			} catch (NumberFormatException err) {
				timestampStart.setText("0");
			}
		}
	}

	private class IncludeSelectionListener extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			hasChanged=true;
			if (include.getSelection())
				exclude.setSelection(false);
		}

	}

	private class ExcludeSelectionListener extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			hasChanged=true;
			if (exclude.getSelection())
				include.setSelection(false);
		}

	}


	private class RunSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			
			if (loader.getCurrentTrace() == null)
				return;
			
			if (!hasChanged)
				return;
			
			setConfiguration();
			
			final String title = "Filtering...";
			Job job = new Job(title) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {		
					monitor.beginTask(title, IProgressMonitor.UNKNOWN);
					try {	
						timeFilter.compute();
						hasChanged = false;
						monitor.done();
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								MessageDialog.openInformation(getSite().getShell(), "Filter", "Filtering finished");
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule(); 
		}
	}
}