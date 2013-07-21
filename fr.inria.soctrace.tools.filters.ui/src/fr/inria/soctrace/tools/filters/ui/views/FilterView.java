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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.filters.timefilter.TimeFilter;
import fr.inria.soctrace.tools.filters.timefilter.TimeFilterParameters;
import fr.inria.soctrace.tools.filters.timefilter.TimeRegion;
import fr.inria.soctrace.tools.filters.ui.Activator;
import fr.inria.soctrace.tools.filters.ui.loaders.ConfDataLoader;

/**
 * View for Filter Tool
 * 
 * @author Damien Dosimont <damien.dosimont@imag.fr"
 */
public class FilterView extends ViewPart {

	private class ConfModificationListener implements ModifyListener {
		@Override
		public void modifyText(final ModifyEvent e) {
			if (loader.getCurrentTrace() == null)
				return;
			hasChanged = true;
			try {
				if (Long.parseLong(timestampEnd.getText()) > loader.getMaxTimestamp() || Long.parseLong(timestampEnd.getText()) < loader.getMinTimestamp())
					timestampEnd.setText(String.valueOf(loader.getMaxTimestamp()));
			} catch (final NumberFormatException err) {
				timestampEnd.setText("0");
			}
			try {
				if (Long.parseLong(timestampStart.getText()) < loader.getMinTimestamp() || Long.parseLong(timestampStart.getText()) > loader.getMaxTimestamp())
					timestampStart.setText(String.valueOf(loader.getMinTimestamp()));
			} catch (final NumberFormatException err) {
				timestampStart.setText("0");
			}
		}
	}

	private class EventProducerSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			hasChanged = true;
			if (btnEventProducer.getSelection())
				btnEvent.setSelection(false);
		}

	}

	private class EventSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			hasChanged = true;
			if (btnEvent.getSelection())
				btnEventProducer.setSelection(false);
		}

	}

	private class EventTypeLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((EventType) element).getName();
		}
	}

	private class ExcludeSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			hasChanged = true;
			if (exclude.getSelection())
				include.setSelection(false);
		}

	}

	private class IdlesSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final InputDialog dialog = new InputDialog(getSite().getShell(), "Type Idle State", "Select Idle state", "", null);
			if (dialog.open() == Window.CANCEL)
				return;
			idles.add(dialog.getValue());
			listViewerIdleStates.setInput(idles);
			hasChanged = true;
		}
	}

	private class IncludeSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			hasChanged = true;
			if (include.getSelection())
				exclude.setSelection(false);
		}

	}

	private class RemoveSelectionAdapter extends SelectionAdapter {

		private final ListViewer	viewer;

		public RemoveSelectionAdapter(final ListViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			final Object obj = selection.getFirstElement();
			final Collection<?> c = (Collection<?>) viewer.getInput();
			c.remove(obj);
			viewer.refresh(false);
			hasChanged = true;
		}
	}

	private class RunSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {

			if (loader.getCurrentTrace() == null)
				return;

			if (!hasChanged)
				return;

			setConfiguration();

			final String title = "Filtering...";
			final Job job = new Job(title) {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
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
					} catch (final Exception e) {
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

	private class TypesSelectionAdapter extends SelectionAdapter {

		// all - input
		java.util.List<Object> diff(final java.util.List<EventType> all, final java.util.List<EventType> input) {
			final java.util.List<Object> tmp = new LinkedList<>();
			for (final Object oba : all)
				tmp.add(oba);
			tmp.removeAll(input);
			return tmp;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (loader.getCurrentTrace() == null)
				return;
			final ListSelectionDialog dialog = new ListSelectionDialog(getSite().getShell(), diff(loader.getTypes(), types), new ArrayContentProvider(), new EventTypeLabelProvider(), "Select Event Types");
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				types.add((EventType) o);
			listViewerEventTypes.setInput(types);
			hasChanged = true;
		}
	}

	public static final String				PLUGIN_ID	= Activator.PLUGIN_ID;
	public static final String				ID			= "fr.inria.soctrace.tools.filters.ui.views.FilterView";	//$NON-NLS-1$
	private boolean							hasChanged	= true;

	/**
	 * Loader to interact with the DB
	 */
	private final ConfDataLoader			loader		= new ConfDataLoader();
	private Text							timestampStart;

	private Text							timestampEnd;
	private Text							label;
	private Button							include;
	private Button							exclude;
	private Button							btnEvent;
	private Button							btnEventProducer;
	private ListViewer						listViewerIdleStates;
	private ListViewer						listViewerEventTypes;
	private TimeFilter						timeFilter;
	private TimeFilterParameters			params;
	private final java.util.List<String>	idles		= new LinkedList<String>();
	final Map<Integer, Trace>				traceMap	= new HashMap<Integer, Trace>();
	private final java.util.List<EventType>	types		= new LinkedList<EventType>();

	/**
	 * @throws SoCTraceException
	 * 
	 */
	public FilterView() throws SoCTraceException {
		setTitleImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.filters.ui", "icons/1366759848_filter_data.png"));
		try {
			loader.loadTraces();
		} catch (final SoCTraceException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
		params = new TimeFilterParameters();
		timeFilter = new TimeFilter(params);
	}

	private void cleanAll() {
		timestampStart.setText("0");
		timestampEnd.setText("0");
		include.setSelection(true);
		exclude.setSelection(false);
		btnEvent.setSelection(false);
		btnEventProducer.setSelection(true);
		label.setText("Type Result Label");
	}

	@Override
	public void createPartControl(final Composite parent) {

		// Highest Component
		final SashForm sashForm = new SashForm(parent, SWT.NONE);

		final TabFolder tabFolder = new TabFolder(sashForm, SWT.NONE);

		final TabItem tbtmTimeFilter = new TabItem(tabFolder, SWT.NONE);
		tbtmTimeFilter.setText("Time Filter");

		final SashForm sashForm_2 = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmTimeFilter.setControl(sashForm_2);

		final Composite composite_2 = new Composite(sashForm_2, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));

		final Combo comboTraces = new Combo(composite_2, SWT.READ_ONLY);
		comboTraces.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		comboTraces.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		final Group grpResultLabel = new Group(sashForm_2, SWT.NONE);
		grpResultLabel.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		grpResultLabel.setText("Result Label");
		grpResultLabel.setLayout(new GridLayout(1, false));

		label = new Text(grpResultLabel, SWT.BORDER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		label.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		final Group groupEventTypes = new Group(sashForm_2, SWT.NONE);
		groupEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		groupEventTypes.setText("Event Types");
		final GridLayout gl_groupEventTypes = new GridLayout(2, false);
		gl_groupEventTypes.horizontalSpacing = 0;
		groupEventTypes.setLayout(gl_groupEventTypes);

		listViewerEventTypes = new ListViewer(groupEventTypes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerEventTypes.setContentProvider(new ArrayContentProvider());
		listViewerEventTypes.setLabelProvider(new EventTypeLabelProvider());
		listViewerEventTypes.setComparator(new ViewerComparator());
		final List listEventTypes = listViewerEventTypes.getList();
		listEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		listEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final ScrolledComposite scrCompositeEventTypeButtons = new ScrolledComposite(groupEventTypes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrCompositeEventTypeButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		scrCompositeEventTypeButtons.setExpandHorizontal(true);
		scrCompositeEventTypeButtons.setExpandVertical(true);

		final Composite compositeEventTypeButtons = new Composite(scrCompositeEventTypeButtons, SWT.NONE);
		compositeEventTypeButtons.setLayout(new GridLayout(1, false));

		final Button btnAddEventTypes = new Button(compositeEventTypeButtons, SWT.NONE);
		btnAddEventTypes.setText("Add");
		btnAddEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		btnAddEventTypes.setImage(null);
		btnAddEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddEventTypes.addSelectionListener(new TypesSelectionAdapter());

		final Button btnRemoveEventTypes = new Button(compositeEventTypeButtons, SWT.NONE);
		btnRemoveEventTypes.setText("Remove");
		btnRemoveEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		btnRemoveEventTypes.setImage(null);
		btnRemoveEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scrCompositeEventTypeButtons.setContent(compositeEventTypeButtons);
		scrCompositeEventTypeButtons.setMinSize(compositeEventTypeButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnRemoveEventTypes.addSelectionListener(new RemoveSelectionAdapter(listViewerEventTypes));
		final Group groupIdleStates = new Group(sashForm_2, SWT.NONE);
		groupIdleStates.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		groupIdleStates.setText("Idle States");
		final GridLayout gl_groupIdleStates = new GridLayout(2, false);
		gl_groupIdleStates.horizontalSpacing = 0;
		gl_groupIdleStates.verticalSpacing = 0;
		groupIdleStates.setLayout(gl_groupIdleStates);

		listViewerIdleStates = new ListViewer(groupIdleStates, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerIdleStates.setContentProvider(new ArrayContentProvider());
		listViewerIdleStates.setComparator(new ViewerComparator());
		final List listIdleStates = listViewerIdleStates.getList();
		final GridData gd_listIdleStates = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_listIdleStates.widthHint = 203;
		listIdleStates.setLayoutData(gd_listIdleStates);
		listIdleStates.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		final ScrolledComposite scrCompositeIdleStateButton = new ScrolledComposite(groupIdleStates, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrCompositeIdleStateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		scrCompositeIdleStateButton.setExpandHorizontal(true);
		scrCompositeIdleStateButton.setExpandVertical(true);

		final Composite compositeIdleStateButtons = new Composite(scrCompositeIdleStateButton, SWT.NONE);
		compositeIdleStateButtons.setLayout(new GridLayout(1, false));

		final Button btnAddIdleStates = new Button(compositeIdleStateButtons, SWT.NONE);
		btnAddIdleStates.setText("Add");
		btnAddIdleStates.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		btnAddIdleStates.setImage(null);
		btnAddIdleStates.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddIdleStates.addSelectionListener(new IdlesSelectionAdapter());

		final Button btnRemoveIdle = new Button(compositeIdleStateButtons, SWT.NONE);
		btnRemoveIdle.setText("Remove");
		btnRemoveIdle.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		btnRemoveIdle.setImage(null);
		btnRemoveIdle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scrCompositeIdleStateButton.setContent(compositeIdleStateButtons);
		scrCompositeIdleStateButton.setMinSize(compositeIdleStateButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnRemoveIdle.addSelectionListener(new RemoveSelectionAdapter(listViewerIdleStates));

		final Group grpResultType = new Group(sashForm_2, SWT.NONE);
		grpResultType.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		grpResultType.setText("Result Type");
		grpResultType.setLayout(new GridLayout(2, false));

		btnEvent = new Button(grpResultType, SWT.RADIO);
		btnEvent.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		btnEvent.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		btnEvent.setText("Events");

		btnEventProducer = new Button(grpResultType, SWT.RADIO);
		btnEventProducer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		btnEventProducer.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		btnEventProducer.setText("Event Producers");

		final Group grpEventProducers = new Group(sashForm_2, SWT.NONE);
		grpEventProducers.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		grpEventProducers.setText("Settings");
		grpEventProducers.setLayout(new GridLayout(2, false));

		include = new Button(grpEventProducers, SWT.RADIO);
		include.setSelection(true);
		include.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		include.setText("Included in Time Interval");

		exclude = new Button(grpEventProducers, SWT.RADIO);
		exclude.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		exclude.setText("Excluded from Time Interval");

		final Group groupTime = new Group(sashForm_2, SWT.NONE);
		groupTime.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		groupTime.setText("Time Interval");
		groupTime.setLayout(new GridLayout(2, false));

		final Label lblStartTimestamp = new Label(groupTime, SWT.NONE);
		lblStartTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		lblStartTimestamp.setText("Start Timestamp");

		timestampStart = new Text(groupTime, SWT.BORDER);
		timestampStart.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		timestampStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		timestampStart.addModifyListener(new ConfModificationListener());

		final Label lblEndTimestamp = new Label(groupTime, SWT.NONE);
		lblEndTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		lblEndTimestamp.setText("End Timestamp");

		timestampEnd = new Text(groupTime, SWT.BORDER);
		timestampEnd.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		timestampEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		timestampEnd.addModifyListener(new ConfModificationListener());

		final Composite composite = new Composite(sashForm_2, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		final Button btnProcess = new Button(composite, SWT.NONE);
		btnProcess.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnProcess.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		btnProcess.setText("Process");
		sashForm_2.setWeights(new int[] { 49, 56, 125, 123, 64, 49, 97, 46 });

		btnProcess.addSelectionListener(new RunSelectionListener());
		comboTraces.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				cleanAll();
				try {
					loader.load(traceMap.get(comboTraces.getSelectionIndex()));
					timestampStart.setText(String.valueOf(loader.getMinTimestamp()));
					timestampEnd.setText(String.valueOf(loader.getMaxTimestamp()));
				} catch (final SoCTraceException e1) {
					MessageDialog.openError(getSite().getShell(), "Exception", e1.getMessage());
				}
			}
		});
		int index = 0;
		for (final Trace t : loader.getTraces()) {
			comboTraces.add(t.getAlias(), index);
			traceMap.put(index, t);
			index++;
		}

		final GridData gd_listIdle = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_listIdle.widthHint = 203;
		sashForm.setWeights(new int[] { 268 });
		include.addSelectionListener(new IncludeSelectionListener());
		exclude.addSelectionListener(new ExcludeSelectionListener());
		btnEvent.addSelectionListener(new EventSelectionListener());
		btnEventProducer.addSelectionListener(new EventProducerSelectionListener());
		label.addModifyListener(new ConfModificationListener());
		cleanAll();

	}

	private void setConfiguration() {

		params.setTrace(loader.getCurrentTrace());
		try {
			params.setTimeRegion(new TimeRegion(Long.valueOf(timestampStart.getText()), Long.valueOf(timestampEnd.getText())));
		} catch (final NumberFormatException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
		params.setLabel(label.getText());
		params.setInclude(include.getSelection());
		params.setEvent(btnEvent.getSelection());
		params.setEventTypes(types);
		params.setValues(idles);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}
}