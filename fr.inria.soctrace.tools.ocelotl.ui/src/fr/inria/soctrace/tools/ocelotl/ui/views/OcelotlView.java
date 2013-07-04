/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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

package fr.inria.soctrace.tools.ocelotl.ui.views;

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
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlConstants.HasChanged;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators.AggregationOperators;
import fr.inria.soctrace.tools.ocelotl.ui.Activator;
import fr.inria.soctrace.tools.ocelotl.ui.loaders.ConfDataLoader;

/**
 * Main view for LPAggreg Paje Tool
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class OcelotlView extends ViewPart {

	private class AddAllEventProducersAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			producers.clear();
			producers.addAll(confDataLoader.getProducers());
			listViewerEventProducers.setInput(producers);
			hasChanged = HasChanged.ALL;
		}
	}

	private class AddEventProducersAdapter extends SelectionAdapter {

		// all - input
		java.util.List<Object> diff(final java.util.List<EventProducer> all, final java.util.List<EventProducer> input) {
			final java.util.List<Object> tmp = new LinkedList<>();
			for (final Object oba : all)
				tmp.add(oba);
			tmp.removeAll(input);
			return tmp;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;

			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getSite().getShell(), new EventProducerLabelProvider());
			dialog.setTitle("Select Event Producers");
			dialog.setMessage("Select a String (* = any string, ? = any char):");
			dialog.setElements(diff(confDataLoader.getProducers(), producers).toArray());
			dialog.setMultipleSelection(true);
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				producers.add((EventProducer) o);
			listViewerEventProducers.setInput(producers);
			hasChanged = HasChanged.ALL;
		}
	}

	private class AddResultsEventProducersAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;

			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getSite().getShell(), new AnalysisResultLabelProvider());
			dialog.setTitle("Select a Result");
			dialog.setMessage("Select a String (* = any string, ? = any char):");
			dialog.setElements(confDataLoader.getResults().toArray());
			dialog.setMultipleSelection(false);
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				try {
					for (final EventProducer ep : confDataLoader.getProducersFromResult((AnalysisResult) o))
						if (!producers.contains(ep))
							producers.add(ep);
				} catch (final SoCTraceException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			listViewerEventProducers.setInput(producers);
			hasChanged = HasChanged.ALL;
		}
	}

	private class AnalysisResultLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((AnalysisResult) element).getDescription();
		}
	}

	private class ConfModificationListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			hasChanged = HasChanged.ALL;
			if (confDataLoader.getCurrentTrace() == null)
				return;
			try {
				if (Long.parseLong(textTimestampEnd.getText()) > confDataLoader.getMaxTimestamp() || Long.parseLong(textTimestampEnd.getText()) < confDataLoader.getMinTimestamp())
					textTimestampEnd.setText(String.valueOf(confDataLoader.getMaxTimestamp()));
			} catch (final NumberFormatException err) {
				textTimestampEnd.setText("0");
			}
			try {
				if (Long.parseLong(textTimestampStart.getText()) < confDataLoader.getMinTimestamp() || Long.parseLong(textTimestampStart.getText()) > confDataLoader.getMaxTimestamp())
					textTimestampStart.setText(String.valueOf(confDataLoader.getMinTimestamp()));
			} catch (final NumberFormatException err) {
				textTimestampStart.setText("0");
			}
		}

	}

	private class EventProducerLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((EventProducer) element).getName();
		}
	}

	private class EventTypeLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((EventType) element).getName();
		}
	}

	private class GetAggregationAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ || hasChanged == HasChanged.PARAMETER)
				hasChanged = HasChanged.PARAMETER;
			else
				listParameters.removeAll();
			setConfiguration();
			final String title = "Computing parts...";
			final Job job = new Job(title) {

				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					monitor.beginTask(title, IProgressMonitor.UNKNOWN);
					try {
						ocelotlCore.computeParts(hasChanged);
						monitor.done();
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								// MessageDialog.openInformation(getSite().getShell(),
								// "Parts", "Parts processing finished");
								hasChanged = HasChanged.NOTHING;
								matrixView.deleteDiagram();
								matrixView.createDiagram(ocelotlCore.getLpaggregManager().getParts(), ocelotlParameters.getTimeRegion(), btnMergeAggregatedParts.getSelection(), btnShowNumbers.getSelection());
								timeAxisView.createDiagram(ocelotlParameters.getTimeRegion());
								qualityView.createDiagram();
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

	private class GetParametersAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.PARAMETER || hasChanged == HasChanged.EQ)
				hasChanged = HasChanged.THRESHOLD;
			setConfiguration();
			final String title = "Getting parameters...";
			final Job job = new Job(title) {

				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					monitor.beginTask(title, IProgressMonitor.UNKNOWN);
					try {
						ocelotlCore.computeDichotomy(hasChanged);

						monitor.done();
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								// MessageDialog.openInformation(getSite().getShell(),
								// "Parameters", "Parameters retrieved");
								hasChanged = HasChanged.NOTHING;
								listParameters.removeAll();
								for (int i =ocelotlCore.getLpaggregManager().getParameters().size()-1; i>=0; i--)
									listParameters.add(Float.toString(ocelotlCore.getLpaggregManager().getParameters().get(i)));
								listParameters.select(0);
								listParameters.notifyListeners(SWT.Selection, new Event());
								qualityView.createDiagram();
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

	private class IdlesSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final InputDialog dialog = new InputDialog(getSite().getShell(), "Type Idle State", "Select Idle state", "", null);
			if (dialog.open() == Window.CANCEL)
				return;
			idles.add(dialog.getValue());
			listViewerIdleStates.setInput(idles);
			hasChanged = HasChanged.ALL;
		}
	}

	private class NormalizeSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (hasChanged != HasChanged.ALL)
				hasChanged = HasChanged.NORMALIZE;
		}
	}

	private class ParameterModifyListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			try {
				if (Float.parseFloat(textRun.getText()) < 0 || Float.parseFloat(textRun.getText()) > 1)
					textRun.setText("0");
			} catch (final NumberFormatException err) {
				textRun.setText("0");
			}
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ)
				hasChanged = HasChanged.PARAMETER;
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
			hasChanged = HasChanged.ALL;
		}
	}

	private class ResetSelectionAdapter extends SelectionAdapter {

		private final ListViewer	viewer;

		public ResetSelectionAdapter(final ListViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Collection<?> c = (Collection<?>) viewer.getInput();
			c.clear();
			viewer.refresh(false);
			hasChanged = HasChanged.ALL;
		}
	}

	private class SelectSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			try {
				if (confDataLoader.getCurrentTrace() == null)
					return;
				if (listParameters.getSelectionCount() > 0)
					textRun.setText(listParameters.getSelection()[0]);
				if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ || hasChanged == HasChanged.PARAMETER)
					hasChanged = HasChanged.PARAMETER;
				else
					listParameters.removeAll();
				setConfiguration();
				final String title = "Computing parts...";
				final Job job = new Job(title) {

					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						monitor.beginTask(title, IProgressMonitor.UNKNOWN);
						try {
							ocelotlCore.computeParts(hasChanged);
							monitor.done();
							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {
									// MessageDialog.openInformation(getSite().getShell(),
									// "Parts", "Parts processing finished");
									hasChanged = HasChanged.NOTHING;
									matrixView.deleteDiagram();
									timeAxisView.createDiagram(ocelotlParameters.getTimeRegion());
									matrixView.createDiagram(ocelotlCore.getLpaggregManager().getParts(), ocelotlParameters.getTimeRegion(), btnMergeAggregatedParts.getSelection(), btnShowNumbers.getSelection());
									qualityView.createDiagram();
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
			} catch (final NumberFormatException e1) {

			}

		}
	}

	private class ThresholdModifyListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			try {
				if (Float.parseFloat(textThreshold.getText()) < Float.MIN_VALUE || Float.parseFloat(textThreshold.getText()) > 1)
					textThreshold.setText("0.001");
			} catch (final NumberFormatException err) {
				textThreshold.setText("0.001");
			}
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
			if (confDataLoader.getCurrentTrace() == null)
				return;
			final ListSelectionDialog dialog = new ListSelectionDialog(getSite().getShell(), diff(confDataLoader.getTypes(), types), new ArrayContentProvider(), new EventTypeLabelProvider(), "Select Event Types");
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				types.add((EventType) o);
			listViewerEventTypes.setInput(types);
			hasChanged = HasChanged.ALL;
		}
	}

	public static final String					ID			= "fr.inria.soctrace.tools.ocelotl.ui.Ocelotl"; //$NON-NLS-1$
	public static final String					PLUGIN_ID	= Activator.PLUGIN_ID;
	private Button								btnMergeAggregatedParts;

	private Button								btnNormalize;
	private Button								btnRun;
	private Button								btnShowNumbers;
	private Combo								comboAggregationOperator;
	private Combo								comboTraces;
	private final ConfDataLoader				confDataLoader		= new ConfDataLoader();
	private HasChanged							hasChanged	= HasChanged.ALL;
	private final java.util.List<String>		idles		= new LinkedList<String>();
	private List								listParameters;
	private ListViewer							listViewerIdleStates;
	private ListViewer							listViewerEventProducers;
	private ListViewer							listViewerEventTypes;
	private MatrixView							matrixView;
	private final OcelotlCore					ocelotlCore;
	private final OcelotlParameters				ocelotlParameters;
	private Text								textRun;
	private final java.util.List<EventProducer>	producers	= new LinkedList<EventProducer>();
	private QualityView							qualityView;
	private Spinner								spinnerDivideDbQuery;
	private Spinner								spinnerTSNumber;
	private Text								textThreshold;
	private TimeAxisView						timeAxisView;
	private Text								textTimestampEnd;
	private Text								textTimestampStart;
	final Map<Integer, Trace>					traceMap	= new HashMap<Integer, Trace>();
	private final java.util.List<EventType>		types		= new LinkedList<EventType>();

	/** @throws SoCTraceException */
	public OcelotlView() throws SoCTraceException {
		try {
			confDataLoader.loadTraces();
		} catch (final SoCTraceException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
		ocelotlParameters = new OcelotlParameters();
		ocelotlCore = new OcelotlCore(ocelotlParameters);
	}

	private void cleanAll() {
		hasChanged = HasChanged.ALL;
		textThreshold.setText("0.001");
		textTimestampStart.setText("0");
		textTimestampEnd.setText("0");
		btnNormalize.setSelection(false);
		spinnerTSNumber.setSelection(200);
		spinnerDivideDbQuery.setSelection(0);
		textRun.setText("0");
		btnMergeAggregatedParts.setSelection(true);
		producers.clear();
		types.clear();
		idles.clear();
		listViewerEventProducers.setInput(producers);
		listViewerEventTypes.setInput(types);
		listViewerIdleStates.setInput(idles);
	}

	@Override
	public void createPartControl(final Composite parent) {

		final SashForm sashFormGlobal = new SashForm(parent, SWT.VERTICAL);
		matrixView = new MatrixView();
		timeAxisView = new TimeAxisView();
		qualityView = new QualityView(this);
		final SashForm sashFormView = new SashForm(sashFormGlobal, SWT.BORDER | SWT.VERTICAL);
		sashFormView.setSashWidth(1);
		final Composite compositeMatrixView = new Composite(sashFormView, SWT.NONE);
		compositeMatrixView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeMatrixView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		final GridLayout gl_compositeMatrixView = new GridLayout();
		compositeMatrixView.setLayout(gl_compositeMatrixView);
		compositeMatrixView.setSize(500, 500);
		final Canvas canvasMatrixView = matrixView.initDiagram(compositeMatrixView);
		canvasMatrixView.setLayoutData(new GridData(GridData.FILL_BOTH));
		final Composite compositeTimeAxisView = new Composite(sashFormView, SWT.NONE);
		compositeTimeAxisView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		final GridLayout gl_compositeTimeAxisView = new GridLayout();
		compositeTimeAxisView.setLayout(gl_compositeTimeAxisView);
		final Canvas canvasTimeAxisView = timeAxisView.initDiagram(compositeTimeAxisView);
		sashFormView.setWeights(new int[] { 125, 36 });

		final TabFolder tabFolder = new TabFolder(sashFormGlobal, SWT.NONE);
		tabFolder.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		final TabItem tbtmTraceParameters = new TabItem(tabFolder, SWT.NONE);
		tbtmTraceParameters.setText("Trace Parameters");

		final SashForm sashFormTraceParameter = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmTraceParameters.setControl(sashFormTraceParameter);

		final Composite sashFormComboTrace = new Composite(sashFormTraceParameter, SWT.NONE);
		sashFormComboTrace.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		sashFormComboTrace.setLayout(new GridLayout(1, false));
		comboTraces = new Combo(sashFormComboTrace, SWT.READ_ONLY);
		final GridData gd_comboTraces = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_comboTraces.widthHint = 327;
		comboTraces.setLayoutData(gd_comboTraces);
		comboTraces.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		int index = 0;
		for (final Trace t : confDataLoader.getTraces()) {
			comboTraces.add(t.getAlias(), index);
			traceMap.put(index, t);
			index++;
		}
		;

		final SashForm sashFormList = new SashForm(sashFormTraceParameter, SWT.NONE);
		final Group groupEventProducers = new Group(sashFormList, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		groupEventProducers.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupEventProducers.setText("Event Producers");
		final GridLayout gl_groupEventProducers = new GridLayout(2, false);//
		gl_groupEventProducers.horizontalSpacing = 0;
		groupEventProducers.setLayout(gl_groupEventProducers);

		listViewerEventProducers = new ListViewer(groupEventProducers, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerEventProducers.setContentProvider(new ArrayContentProvider());
		listViewerEventProducers.setLabelProvider(new EventProducerLabelProvider());
		listViewerEventProducers.setComparator(new ViewerComparator());
		final List listEventProducers = listViewerEventProducers.getList();
		listEventProducers.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		final GridData gd_listEventProducers = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_listEventProducers.heightHint = 79;
		gd_listEventProducers.widthHint = 120;
		listEventProducers.setLayoutData(gd_listEventProducers);

		final ScrolledComposite scrCompositeEventProducerButtons = new ScrolledComposite(groupEventProducers, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrCompositeEventProducerButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		scrCompositeEventProducerButtons.setExpandHorizontal(true);
		scrCompositeEventProducerButtons.setExpandVertical(true);

		final Composite compositeEventProducerButtons = new Composite(scrCompositeEventProducerButtons, SWT.NONE);
		compositeEventProducerButtons.setLayout(new GridLayout(1, false));
		final Button btnAddEventProducer = new Button(compositeEventProducerButtons, SWT.NONE);
		btnAddEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddEventProducer.setText("Add");
		btnAddEventProducer.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddEventProducer.setImage(null);

		final Button btnAddAllEventProducer = new Button(compositeEventProducerButtons, SWT.NONE);
		btnAddAllEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAddAllEventProducer.setText("Add All");
		btnAddAllEventProducer.setImage(null);
		btnAddAllEventProducer.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddAllEventProducer.addSelectionListener(new AddAllEventProducersAdapter());

		final Button btnAddResult = new Button(compositeEventProducerButtons, SWT.NONE);
		btnAddResult.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddResult.setText("Add Result");
		btnAddResult.setImage(null);
		btnAddResult.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddResult.addSelectionListener(new AddResultsEventProducersAdapter());
		final Button btnRemoveEventProducer = new Button(compositeEventProducerButtons, SWT.NONE);
		btnRemoveEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnRemoveEventProducer.setText("Reset");
		btnRemoveEventProducer.addSelectionListener(new ResetSelectionAdapter(listViewerEventProducers));
		btnRemoveEventProducer.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRemoveEventProducer.setImage(null);
		scrCompositeEventProducerButtons.setContent(compositeEventProducerButtons);
		scrCompositeEventProducerButtons.setMinSize(compositeEventProducerButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnAddEventProducer.addSelectionListener(new AddEventProducersAdapter());
		final Group groupEventTypes = new Group(sashFormList, SWT.NONE);
		groupEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupEventTypes.setText("Event Types");
		final GridLayout gl_groupEventTypes = new GridLayout(2, false);
		gl_groupEventTypes.horizontalSpacing = 0;
		groupEventTypes.setLayout(gl_groupEventTypes);

		listViewerEventTypes = new ListViewer(groupEventTypes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerEventTypes.setContentProvider(new ArrayContentProvider());
		listViewerEventTypes.setLabelProvider(new EventTypeLabelProvider());
		listViewerEventTypes.setComparator(new ViewerComparator());
		final List listEventTypes = listViewerEventTypes.getList();
		listEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		listEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final ScrolledComposite scrCompositeEventTypeButtons = new ScrolledComposite(groupEventTypes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrCompositeEventTypeButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		scrCompositeEventTypeButtons.setExpandHorizontal(true);
		scrCompositeEventTypeButtons.setExpandVertical(true);

		final Composite compositeEventTypeButtons = new Composite(scrCompositeEventTypeButtons, SWT.NONE);
		compositeEventTypeButtons.setLayout(new GridLayout(1, false));

		final Button btnAddEventTypes = new Button(compositeEventTypeButtons, SWT.NONE);
		btnAddEventTypes.setText("Add");
		btnAddEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddEventTypes.setImage(null);
		btnAddEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddEventTypes.addSelectionListener(new TypesSelectionAdapter());

		final Button btnRemoveEventTypes = new Button(compositeEventTypeButtons, SWT.NONE);
		btnRemoveEventTypes.setText("Remove");
		btnRemoveEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRemoveEventTypes.setImage(null);
		btnRemoveEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scrCompositeEventTypeButtons.setContent(compositeEventTypeButtons);
		scrCompositeEventTypeButtons.setMinSize(compositeEventTypeButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnRemoveEventTypes.addSelectionListener(new RemoveSelectionAdapter(listViewerEventTypes));
		final Group groupIdleStates = new Group(sashFormList, SWT.NONE);
		groupIdleStates.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
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
		listIdleStates.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final ScrolledComposite scrCompositeIdleStateButton = new ScrolledComposite(groupIdleStates, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrCompositeIdleStateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		scrCompositeIdleStateButton.setExpandHorizontal(true);
		scrCompositeIdleStateButton.setExpandVertical(true);

		final Composite compositeIdleStateButtons = new Composite(scrCompositeIdleStateButton, SWT.NONE);
		compositeIdleStateButtons.setLayout(new GridLayout(1, false));

		final Button btnAddIdleStates = new Button(compositeIdleStateButtons, SWT.NONE);
		btnAddIdleStates.setText("Add");
		btnAddIdleStates.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddIdleStates.setImage(null);
		btnAddIdleStates.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddIdleStates.addSelectionListener(new IdlesSelectionAdapter());

		final Button btnRemoveIdle = new Button(compositeIdleStateButtons, SWT.NONE);
		btnRemoveIdle.setText("Remove");
		btnRemoveIdle.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRemoveIdle.setImage(null);
		btnRemoveIdle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scrCompositeIdleStateButton.setContent(compositeIdleStateButtons);
		scrCompositeIdleStateButton.setMinSize(compositeIdleStateButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnRemoveIdle.addSelectionListener(new RemoveSelectionAdapter(listViewerIdleStates));
		sashFormList.setWeights(new int[] { 1, 1, 1 });
		sashFormTraceParameter.setWeights(new int[] { 23, 226 });
		comboTraces.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				cleanAll();
				try {
					confDataLoader.load(traceMap.get(comboTraces.getSelectionIndex()));
					textTimestampStart.setText(String.valueOf(confDataLoader.getMinTimestamp()));
					textTimestampEnd.setText(String.valueOf(confDataLoader.getMaxTimestamp()));
					for (int i = 0; i < confDataLoader.getTypes().size(); i++)
						if (confDataLoader.getTypes().get(i).getName().contains("PajeSetState")) {
							types.add(confDataLoader.getTypes().get(i));
							break;
						}
					listViewerEventTypes.setInput(types);
					idles.add("IDLE");
					listViewerIdleStates.setInput(idles);
				} catch (final SoCTraceException e1) {
					MessageDialog.openError(getSite().getShell(), "Exception", e1.getMessage());
				}
			}
		});

		final TabItem tbtmTimeAggregationParameters = new TabItem(tabFolder, SWT.NONE);
		tbtmTimeAggregationParameters.setText("Time Aggregation Parameters");

		final SashForm sashFormTimeAggregationParameters = new SashForm(tabFolder, SWT.NONE);
		tbtmTimeAggregationParameters.setControl(sashFormTimeAggregationParameters);

		final Group groupTSParameters = new Group(sashFormTimeAggregationParameters, SWT.NONE);
		groupTSParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupTSParameters.setText("Time Slicing Parameters ");
		groupTSParameters.setLayout(new GridLayout(1, false));

		final Group groupAggregationOperator = new Group(groupTSParameters, SWT.NONE);
		groupAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupAggregationOperator.setText("Aggregation Operator");
		groupAggregationOperator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		groupAggregationOperator.setLayout(new GridLayout(1, false));

		final Composite compositeAggregationOperator = new Composite(groupAggregationOperator, SWT.NONE);
		compositeAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		compositeAggregationOperator.setLayout(new GridLayout(1, false));
		final GridData gd_compositeAggregationOperator = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_compositeAggregationOperator.widthHint = 85;
		compositeAggregationOperator.setLayoutData(gd_compositeAggregationOperator);

		comboAggregationOperator = new Combo(compositeAggregationOperator, SWT.READ_ONLY);
		comboAggregationOperator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		comboAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		comboAggregationOperator.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (confDataLoader.getCurrentTrace() == null)
					return;
				hasChanged = HasChanged.ALL;
			}
		});
		for (final String op : AggregationOperators.List)
			comboAggregationOperator.add(op);
		comboAggregationOperator.setText(AggregationOperators.List.get(0));

		final Group groupTimeInterval = new Group(groupTSParameters, SWT.NONE);
		final GridData gd_groupTimeInterval = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_groupTimeInterval.widthHint = 200;
		groupTimeInterval.setLayoutData(gd_groupTimeInterval);
		groupTimeInterval.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupTimeInterval.setText("Time Interval");
		groupTimeInterval.setLayout(new GridLayout(2, false));

		final Label lblStartTimestamp = new Label(groupTimeInterval, SWT.NONE);
		lblStartTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblStartTimestamp.setText("Start Timestamp");

		textTimestampStart = new Text(groupTimeInterval, SWT.BORDER);
		textTimestampStart.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		textTimestampStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textTimestampStart.addModifyListener(new ConfModificationListener());

		final Label lblEndTimestamp = new Label(groupTimeInterval, SWT.NONE);
		lblEndTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblEndTimestamp.setText("End Timestamp");

		textTimestampEnd = new Text(groupTimeInterval, SWT.BORDER);
		textTimestampEnd.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		textTimestampEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textTimestampEnd.addModifyListener(new ConfModificationListener());
		final Composite compositeTSNumber = new Composite(groupTSParameters, SWT.NONE);
		compositeTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		compositeTSNumber.setLayout(new GridLayout(2, false));
		compositeTSNumber.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		final Label lblTSNumber = new Label(compositeTSNumber, SWT.NONE);
		lblTSNumber.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		lblTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblTSNumber.setText("Number of time slices");

		spinnerTSNumber = new Spinner(compositeTSNumber, SWT.BORDER);
		final GridData gd_spinnerTSNumber = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_spinnerTSNumber.widthHint = 36;
		spinnerTSNumber.setLayoutData(gd_spinnerTSNumber);
		spinnerTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerTSNumber.setMaximum(10000);
		spinnerTSNumber.setMinimum(1);
		spinnerTSNumber.setSelection(200);
		spinnerTSNumber.addModifyListener(new ConfModificationListener());
		final Group groupLPAParameters = new Group(sashFormTimeAggregationParameters, SWT.NONE);
		groupLPAParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupLPAParameters.setLayout(new GridLayout(1, false));
		groupLPAParameters.setText("Get Best-Cut Partition Gain/Loss Parameter List");

		btnNormalize = new Button(groupLPAParameters, SWT.CHECK);
		btnNormalize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnNormalize.setSelection(false);
		btnNormalize.setText("Normalize Qualities");
		btnNormalize.addSelectionListener(new NormalizeSelectionAdapter());

		final Composite compositeGetParameters = new Composite(groupLPAParameters, SWT.NONE);
		compositeGetParameters.setLayout(new GridLayout(3, false));
		compositeGetParameters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		final Label lblThreshold = new Label(compositeGetParameters, SWT.NONE);
		lblThreshold.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblThreshold.setText("Threshold");

		textThreshold = new Text(compositeGetParameters, SWT.BORDER);
		final GridData gd_textThreshold = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textThreshold.widthHint = 342;
		textThreshold.setLayoutData(gd_textThreshold);
		textThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		final Button btnGetParameters = new Button(compositeGetParameters, SWT.NONE);
		btnGetParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnGetParameters.setText("Get");

		final Group groupParameters = new Group(groupLPAParameters, SWT.NONE);
		groupParameters.setLayout(new GridLayout(2, false));
		final GridData gd_groupParameters = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_groupParameters.heightHint = 119;
		gd_groupParameters.widthHint = 277;
		groupParameters.setLayoutData(gd_groupParameters);

		listParameters = new List(groupParameters, SWT.BORDER | SWT.V_SCROLL);
		listParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		final GridData gd_list = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_list.heightHint = 132;
		gd_list.widthHint = 330;
		listParameters.setLayoutData(gd_list);
		new Label(groupParameters, SWT.NONE);
		final Group groupRun = new Group(groupParameters, SWT.NONE);
		groupRun.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		groupRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupRun.setLayout(new GridLayout(3, false));
		groupRun.setText("Perform Best-Cut Partition");

		final Label lblRun = new Label(groupRun, SWT.NONE);
		lblRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblRun.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblRun.setText("Gain/loss parameter");

		textRun = new Text(groupRun, SWT.BORDER);
		textRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		textRun.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textRun.addModifyListener(new ParameterModifyListener());
		btnRun = new Button(groupRun, SWT.NONE);
		btnRun.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRun.setText("Process");
		btnRun.addSelectionListener(new GetAggregationAdapter());
		new Label(groupParameters, SWT.NONE);
		listParameters.addSelectionListener(new SelectSelectionAdapter());

		final Composite compositeQualityView = new Composite(sashFormTimeAggregationParameters, SWT.NONE);
		compositeQualityView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeQualityView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		// compositeVisu.setToolTipText("test");
		final GridLayout gl_compositeQualityView = new GridLayout();
		compositeQualityView.setLayout(gl_compositeQualityView);
		final Canvas canvasQualityView = qualityView.initDiagram(compositeQualityView);
		canvasQualityView.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashFormTimeAggregationParameters.setWeights(new int[] { 97, 153, 332 });

		btnGetParameters.addSelectionListener(new GetParametersAdapter());
		textThreshold.addModifyListener(new ThresholdModifyListener());

		final TabItem tbtmAdvancedParameters = new TabItem(tabFolder, 0);
		tbtmAdvancedParameters.setText("Advanced Parameters");

		final SashForm sashFormAdvancedParameters = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormAdvancedParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		tbtmAdvancedParameters.setControl(sashFormAdvancedParameters);

		final Group grpDivideDbQuery = new Group(sashFormAdvancedParameters, SWT.NONE);
		grpDivideDbQuery.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpDivideDbQuery.setText("Memory Management");
		grpDivideDbQuery.setLayout(new GridLayout(2, false));

		final Label lblDivideDbQueries = new Label(grpDivideDbQuery, SWT.NONE);
		lblDivideDbQueries.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblDivideDbQueries.setText("Divide DB query (Event Producers per query, inactive if 0)");

		spinnerDivideDbQuery = new Spinner(grpDivideDbQuery, SWT.BORDER);
		spinnerDivideDbQuery.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		final GridData gd_spinnerDivideDbQuery = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_spinnerDivideDbQuery.widthHint = 99;
		spinnerDivideDbQuery.setLayoutData(gd_spinnerDivideDbQuery);
		spinnerDivideDbQuery.addModifyListener(new ConfModificationListener());
		spinnerDivideDbQuery.setMinimum(0);
		spinnerDivideDbQuery.setSelection(0);

		final Group grpVisualizationSettings = new Group(sashFormAdvancedParameters, SWT.NONE);
		grpVisualizationSettings.setText("Visualization settings");
		grpVisualizationSettings.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpVisualizationSettings.setLayout(new GridLayout(1, false));

		btnMergeAggregatedParts = new Button(grpVisualizationSettings, SWT.CHECK);
		btnMergeAggregatedParts.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnMergeAggregatedParts.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnMergeAggregatedParts.setText("Merge Aggregated Parts");
		btnMergeAggregatedParts.setSelection(true);

		btnShowNumbers = new Button(grpVisualizationSettings, SWT.CHECK);
		btnShowNumbers.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
			}
		});
		btnShowNumbers.setText("Show Part Numbers");
		btnShowNumbers.setSelection(false);
		btnShowNumbers.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		sashFormAdvancedParameters.setWeights(new int[] { 112, 374 });
		sashFormGlobal.setWeights(new int[] { 172, 286 });
		canvasTimeAxisView.setLayoutData(new GridData(GridData.FILL_BOTH));

		// clean all
		cleanAll();

	}

	public Button getBtnRun() {
		return btnRun;
	}

	public OcelotlCore getCore() {
		return ocelotlCore;
	}

	public List getList() {
		return listParameters;
	}

	public Text getParam() {
		return textRun;
	}

	public OcelotlParameters getParams() {
		return ocelotlParameters;
	}

	public void setConfiguration() {

		ocelotlParameters.setTrace(confDataLoader.getCurrentTrace());
		ocelotlParameters.setEventProducers(producers);
		ocelotlParameters.setEventTypes(types);
		ocelotlParameters.setSleepingStates(idles);
		ocelotlParameters.setNormalize(btnNormalize.getSelection());
		ocelotlParameters.setTimeSlicesNumber(spinnerTSNumber.getSelection());
		ocelotlParameters.setMaxEventProducers(spinnerDivideDbQuery.getSelection());
		ocelotlParameters.setAggOperator(comboAggregationOperator.getText());
		// TODO manage number format exception
		try {
			ocelotlParameters.setThreshold(Double.valueOf(textThreshold.getText()).floatValue());
			ocelotlParameters.setParameter(Double.valueOf(textRun.getText()).floatValue());
			ocelotlParameters.setTimeRegion(new TimeRegion(Long.valueOf(textTimestampStart.getText()), Long.valueOf(textTimestampEnd.getText())));
		} catch (final NumberFormatException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}
}