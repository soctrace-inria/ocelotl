/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
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

package fr.inria.soctrace.tools.ocelotl.microdesc.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.microdesc.config.DistributionConfig;
import fr.inria.soctrace.tools.ocelotl.ui.views.IAggregationWindow;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public abstract class DistributionBaseView extends Dialog implements
		IAggregationWindow {

	private class AddAllEventProducersAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			producers.clear();
			producers.addAll(ocelotlView.getConfDataLoader().getProducers());
			listViewerEventProducers.setInput(producers);
			// hasChanged = HasChanged.ALL;
		}
	}

	private class AddEventProducersAdapter extends SelectionAdapter {

		// all - input
		java.util.List<Object> diff(final java.util.List<EventProducer> all,
				final java.util.List<EventProducer> input) {
			final java.util.List<Object> tmp = new LinkedList<>();
			for (final Object oba : all)
				tmp.add(oba);
			tmp.removeAll(input);
			return tmp;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {

			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(
					getShell(), new EventProducerLabelProvider());
			dialog.setTitle("Select Event Producers");
			dialog.setMessage("Select a String (* = any string, ? = any char):");
			dialog.setElements(diff(
					ocelotlView.getConfDataLoader().getProducers(), producers)
					.toArray());
			dialog.setMultipleSelection(true);
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				producers.add((EventProducer) o);
			listViewerEventProducers.setInput(producers);
			// hasChanged = HasChanged.ALL;
		}
	}
	
	private class RemoveEventProducerAdapter extends SelectionAdapter {

		private final ListViewer viewer;

		public RemoveEventProducerAdapter(final ListViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final IStructuredSelection selection = (IStructuredSelection) viewer
					.getSelection();
			final Object obj = selection.getFirstElement();
			final Collection<?> c = (Collection<?>) viewer.getInput();
			c.remove(obj);
			viewer.refresh(false);
		}
	}
	
	private class AddResultsEventProducersAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {

			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(
					getShell(), new AnalysisResultLabelProvider());
			dialog.setTitle("Select a Result");
			dialog.setMessage("Select a String (* = any string, ? = any char):");
			dialog.setElements(ocelotlView.getConfDataLoader().getResults()
					.toArray());
			dialog.setMultipleSelection(false);
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				try {
					for (final EventProducer ep : ocelotlView
							.getConfDataLoader().getProducersFromResult(
									(AnalysisResult) o))
						if (!producers.contains(ep))
							producers.add(ep);
				} catch (final SoCTraceException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			listViewerEventProducers.setInput(producers);
			// hasChanged = HasChanged.ALL;
		}
	}

	private class ResetSelectionAdapter extends SelectionAdapter {

		private final ListViewer viewer;

		public ResetSelectionAdapter(final ListViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Collection<?> c = (Collection<?>) viewer.getInput();
			c.clear();
			viewer.refresh(false);
			// hasChanged = HasChanged.ALL;
		}
	}

	private class AnalysisResultLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((AnalysisResult) element).getDescription();
		}
	}

	private class EventTypeLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((EventType) element).getName();
		}
	}

	private class EventProducerLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((EventProducer) element).getName();
		}
	}

	private class RemoveSelectionAdapter extends SelectionAdapter {

		private final ListViewer viewer;

		public RemoveSelectionAdapter(final ListViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final IStructuredSelection selection = (IStructuredSelection) viewer
					.getSelection();
			final Object obj = selection.getFirstElement();
			final Collection<?> c = (Collection<?>) viewer.getInput();
			c.remove(obj);
			viewer.refresh(false);
		}
	}

	private class TypesSelectionAdapter extends SelectionAdapter {

		// all - input
		java.util.List<EventType> diff(final java.util.List<EventType> all,
				final java.util.List<EventType> input) {
			final java.util.List<EventType> tmp = new ArrayList<>();
			for (final EventType oba : all)
				tmp.add(oba);
			tmp.removeAll(input);
			Collections.sort(tmp, new Comparator<EventType>() {

				@Override
				public int compare(EventType arg0, EventType arg1) {
					return arg0.getName().compareToIgnoreCase(arg1.getName());
				}

			});
			return tmp;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (ocelotlView.getConfDataLoader().getCurrentTrace() == null)
				return;
			final ListSelectionDialog dialog = new ListSelectionDialog(
					getShell(), diff(getEventTypes(), config.getTypes()),
					new ArrayContentProvider(), new EventTypeLabelProvider(),
					"Select Event Types");
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				config.getTypes().add((EventType) o);
			listViewerEventTypes.setInput(config.getTypes());
		}
	}

	protected OcelotlView ocelotlView;

	protected ListViewer listViewerIdleStates;

	protected ListViewer listViewerEventTypes;

	protected DistributionConfig config;

	protected OcelotlParameters params;

	private java.util.List<EventType> oldEventTypes;

	private Spinner spinnerEventSize;

	private Spinner spinnerDivideDbQuery;

	private Spinner spinnerThread;

	private ListViewer listViewerEventProducers;

	private final java.util.List<EventProducer> producers = new LinkedList<EventProducer>();

	public DistributionBaseView(final Shell parent) {
		super(parent);
		ocelotlView = null;
		config = null;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		oldEventTypes = new ArrayList<EventType>(config.getTypes());
		// parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		Composite all = (Composite) super.createDialogArea(parent);

		final SashForm sashFormGlobal = new SashForm(all, SWT.VERTICAL);
		sashFormGlobal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		sashFormGlobal.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		TabFolder tabFolder = new TabFolder(sashFormGlobal, SWT.NONE);

		TabItem tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem.setText("Event Types");

		final Group groupEventTypes = new Group(tabFolder, SWT.NONE);
		tbtmNewItem.setControl(groupEventTypes);
		groupEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		groupEventTypes.setText("Set Event Types");
		final GridLayout gl_groupEventTypes = new GridLayout(2, false);
		gl_groupEventTypes.horizontalSpacing = 0;
		groupEventTypes.setLayout(gl_groupEventTypes);

		listViewerEventTypes = new ListViewer(groupEventTypes, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerEventTypes.setContentProvider(new ArrayContentProvider());
		listViewerEventTypes.setLabelProvider(new EventTypeLabelProvider());
		listViewerEventTypes.setComparator(new ViewerComparator());
		final List listEventTypes = listViewerEventTypes.getList();
		listEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		listEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));

		final ScrolledComposite scrCompositeEventTypeButtons = new ScrolledComposite(
				groupEventTypes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrCompositeEventTypeButtons.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, false, false, 1, 1));
		scrCompositeEventTypeButtons.setExpandHorizontal(true);
		scrCompositeEventTypeButtons.setExpandVertical(true);

		final Composite compositeEventTypeButtons = new Composite(
				scrCompositeEventTypeButtons, SWT.NONE);
		compositeEventTypeButtons.setLayout(new GridLayout(1, false));

		final Button btnAddEventTypes = new Button(compositeEventTypeButtons,
				SWT.NONE);
		btnAddEventTypes.setText("Add");
		btnAddEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		btnAddEventTypes.setImage(null);
		btnAddEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		btnAddEventTypes.addSelectionListener(new TypesSelectionAdapter());

		final Button btnRemoveEventTypes = new Button(
				compositeEventTypeButtons, SWT.NONE);
		btnRemoveEventTypes.setText("Remove");
		btnRemoveEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		btnRemoveEventTypes.setImage(null);
		btnRemoveEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		scrCompositeEventTypeButtons.setContent(compositeEventTypeButtons);
		scrCompositeEventTypeButtons.setMinSize(compositeEventTypeButtons
				.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnRemoveEventTypes.addSelectionListener(new RemoveSelectionAdapter(
				listViewerEventTypes));
		Button btnResetEventTypes = new Button(compositeEventTypeButtons,
				SWT.NONE);
		btnResetEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		btnResetEventTypes.setText("Reset");
		btnResetEventTypes.addSelectionListener(new ResetSelectionAdapter(
				listViewerEventTypes));
		btnResetEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		btnResetEventTypes.setImage(null);

		TabItem tbtmNewItem_2 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_2.setText("Event Producers");

		final Group groupEventProducers = new Group(tabFolder, SWT.NONE);
		tbtmNewItem_2.setControl(groupEventProducers);
		groupEventProducers.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		groupEventProducers.setText("Event Producers");
		final GridLayout gl_groupEventProducers = new GridLayout(2, false);//
		gl_groupEventProducers.horizontalSpacing = 0;
		groupEventProducers.setLayout(gl_groupEventProducers);

		listViewerEventProducers = new ListViewer(groupEventProducers,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerEventProducers.setContentProvider(new ArrayContentProvider());
		listViewerEventProducers
				.setLabelProvider(new EventProducerLabelProvider());
		listViewerEventProducers.setComparator(new ViewerComparator());
		final List listEventProducers = listViewerEventProducers.getList();
		listEventProducers.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		final GridData gd_listEventProducers = new GridData(SWT.FILL, SWT.FILL,
				true, true, 1, 1);
		gd_listEventProducers.heightHint = 79;
		gd_listEventProducers.widthHint = 120;
		listEventProducers.setLayoutData(gd_listEventProducers);

		final ScrolledComposite scrCompositeEventProducerButtons = new ScrolledComposite(
				groupEventProducers, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrCompositeEventProducerButtons.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, false, true, 1, 1));
		scrCompositeEventProducerButtons.setExpandHorizontal(true);
		scrCompositeEventProducerButtons.setExpandVertical(true);

		final Composite compositeEventProducerButtons = new Composite(
				scrCompositeEventProducerButtons, SWT.NONE);
		compositeEventProducerButtons.setLayout(new GridLayout(1, false));
		final Button btnAddEventProducer = new Button(
				compositeEventProducerButtons, SWT.NONE);
		btnAddEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		btnAddEventProducer.setText("Add");
		btnAddEventProducer.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		btnAddEventProducer.setImage(null);

		Button btnAddAllEventProducer = new Button(
				compositeEventProducerButtons, SWT.NONE);
		btnAddAllEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));
		btnAddAllEventProducer.setText("Add All");
		btnAddAllEventProducer.setImage(null);
		btnAddAllEventProducer.setFont(SWTResourceManager.getFont("Cantarell",
				11, SWT.NORMAL));
		btnAddAllEventProducer
				.addSelectionListener(new AddAllEventProducersAdapter());
			
		final Button btnAddResult = new Button(compositeEventProducerButtons,
				SWT.NONE);
		btnAddResult.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		btnAddResult.setText("Add Result");
		btnAddResult.setImage(null);
		btnAddResult.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		btnAddResult
				.addSelectionListener(new AddResultsEventProducersAdapter());
		
		final Button btnRemoveEventProducer = new Button(
				compositeEventProducerButtons, SWT.NONE);
		btnRemoveEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		btnRemoveEventProducer.setText("Remove");
		btnRemoveEventProducer.setFont(SWTResourceManager.getFont("Cantarell",
				11, SWT.NORMAL));
		btnRemoveEventProducer.setImage(null);
		btnRemoveEventProducer.setImage(null);
		btnRemoveEventProducer
				.addSelectionListener(new RemoveEventProducerAdapter(
						listViewerEventProducers));

		
		Button btnResetEventProducer = new Button(
				compositeEventProducerButtons, SWT.NONE);
		btnResetEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		btnResetEventProducer.setText("Remove All");
		btnResetEventProducer.addSelectionListener(new ResetSelectionAdapter(
				listViewerEventProducers));
		btnResetEventProducer.setFont(SWTResourceManager.getFont("Cantarell",
				11, SWT.NORMAL));
		btnResetEventProducer.setImage(null);
		scrCompositeEventProducerButtons
				.setContent(compositeEventProducerButtons);
		scrCompositeEventProducerButtons
				.setMinSize(compositeEventProducerButtons.computeSize(
						SWT.DEFAULT, SWT.DEFAULT));
		btnAddEventProducer
				.addSelectionListener(new AddEventProducersAdapter());

		TabItem tbtmNewItem_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_1.setText("Advanced Settings");

		SashForm sashForm = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmNewItem_1.setControl(sashForm);
		final Group grpCacheManagement = new Group(sashForm, SWT.NONE);
		grpCacheManagement.setFont(org.eclipse.wb.swt.SWTResourceManager
				.getFont("Cantarell", 11, SWT.NORMAL));
		grpCacheManagement.setText("Iterator Management");
		grpCacheManagement.setLayout(new GridLayout(2, false));

		final Label lblPageSize = new Label(grpCacheManagement, SWT.NONE);
		lblPageSize.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		lblPageSize.setText("Event Number Retrieved by Threads");

		spinnerEventSize = new Spinner(grpCacheManagement, SWT.BORDER);
		spinnerEventSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		spinnerEventSize.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		spinnerEventSize.setMinimum(100);
		spinnerEventSize.setMaximum(10000000);
		spinnerEventSize.setSelection(config.getEventsPerThread());

		final Group grpDivideDbQuery = new Group(sashForm, SWT.NONE);
		grpDivideDbQuery.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		grpDivideDbQuery.setText("Query Management");
		grpDivideDbQuery.setLayout(new GridLayout(2, false));

		final Label lblDivideDbQueries = new Label(grpDivideDbQuery, SWT.NONE);
		lblDivideDbQueries.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		lblDivideDbQueries.setText("Event Producers per Query (0=All)");

		spinnerDivideDbQuery = new Spinner(grpDivideDbQuery, SWT.BORDER);
		spinnerDivideDbQuery.setFont(SWTResourceManager.getFont("Cantarell",
				11, SWT.NORMAL));
		spinnerDivideDbQuery.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		spinnerDivideDbQuery.setMinimum(0);
		spinnerDivideDbQuery.setMaximum(1000000);
		spinnerDivideDbQuery.setSelection(params.getMaxEventProducers());

		final Group grpMultiThread = new Group(sashForm, SWT.NONE);
		grpMultiThread.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		grpMultiThread.setText("Multi Threading");
		grpMultiThread.setLayout(new GridLayout(2, false));

		final Label lblThread = new Label(grpMultiThread, SWT.NONE);
		lblThread.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		lblThread.setText("Working Threads");

		spinnerThread = new Spinner(grpMultiThread, SWT.BORDER);
		spinnerThread.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		spinnerThread.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		spinnerThread.setMinimum(1);
		spinnerThread.setMaximum(1000000);
		spinnerThread.setSelection(config.getThreadNumber());
		sashForm.setWeights(new int[] { 1, 1, 1 });
		sashFormGlobal.setWeights(new int[] { 1 });
		producers.clear();
		producers.addAll(params.getEventProducers());
		listViewerEventProducers.setInput(producers);
		if (producers.isEmpty())
			btnAddAllEventProducer.notifyListeners(SWT.Selection, new Event());
		setParameters();
		return sashFormGlobal;

	}

	@Override
	public void init(final OcelotlView ocelotlView,
			final ITraceTypeConfig config) {
		this.ocelotlView = ocelotlView;
		this.config = (DistributionConfig) config;
		this.params = ocelotlView.getParams();
	}

	@Override
	protected void okPressed() {
		config.setEventsPerThread(spinnerEventSize.getSelection());
		config.setThreadNumber(spinnerThread.getSelection());
		params.setEventProducers(producers);
		params.setMaxEventProducers(spinnerDivideDbQuery.getSelection());
		super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		config.setTypes(oldEventTypes);
		super.cancelPressed();
	}

	public abstract void setParameters();

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Microscopic Description Settings");
	}

	protected java.util.List<EventType> getEventTypes() {
		return ocelotlView.getConfDataLoader().getTypes();
	}

}
