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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
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
import org.eclipse.ui.part.ViewPart;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.HasChanged;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.Activator;
import fr.inria.soctrace.tools.ocelotl.ui.com.eclipse.wb.swt.ResourceManager;
import fr.inria.soctrace.tools.ocelotl.ui.com.eclipse.wb.swt.SWTResourceManager;
import fr.inria.soctrace.tools.ocelotl.ui.loaders.ConfDataLoader;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.ITimeLineView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewWrapper;

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

	private class DecreasingQualityRadioSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (btnDecreasingQualities.getSelection()) {
				btnGrowingQualities.setSelection(false);
				ocelotlParameters.setGrowingQualities(false);
				qualityView.createDiagram();
			}
		}
	}

	private class EventProducerLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((EventProducer) element).getName();
		}
	}

	private class GetAggregationAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			if (comboTime.getText().equals(""))
				return;
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ || hasChanged == HasChanged.PARAMETER)
				hasChanged = HasChanged.PARAMETER;
			else
				textRun.setText("1.0");
			setConfiguration();
			final String title = "Computing Aggregated View";
			final Job job = new Job(title) {

				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					monitor.beginTask(title, IProgressMonitor.UNKNOWN);
					try {
						if (hasChanged != HasChanged.PARAMETER)
							ocelotlCore.computeDichotomy(hasChanged);
						// textRun.setText(String.valueOf(ocelotlCore.getLpaggregManager().getParameters().get(ocelotlCore.getLpaggregManager().getParameters().size()
						// - 1)));
						// setConfiguration();
						hasChanged = HasChanged.PARAMETER;
						ocelotlCore.computeParts(hasChanged);
						monitor.done();
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								// MessageDialog.openInformation(getSite().getShell(),
								// "Parts", "Parts processing finished");
								hasChanged = HasChanged.NOTHING;
								timeLineView.deleteDiagram();
								timeLineView.createDiagram(ocelotlCore.getLpaggregManager().getParts(), ocelotlParameters.getTimeRegion(), btnMergeAggregatedParts.getSelection(), btnShowNumbers.getSelection());
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

	private class GrowingQualityRadioSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (btnGrowingQualities.getSelection()) {
				btnDecreasingQualities.setSelection(false);
				ocelotlParameters.setGrowingQualities(true);
				qualityView.createDiagram();
			}
		}
	}

	private class NormalizeSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (hasChanged != HasChanged.ALL)
				hasChanged = HasChanged.NORMALIZE;
			btnRun.notifyListeners(SWT.Selection, new Event());
		}
	}

	private class ParameterDownAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final float p = Float.parseFloat(textRun.getText());
			for (final float f : ocelotlCore.getLpaggregManager().getParameters())
				if (f > p) {
					textRun.setText(Float.toString(f));
					break;
				}
			btnRun.notifyListeners(SWT.Selection, new Event());

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

	private class ParameterUpAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final float p = Float.parseFloat(textRun.getText());
			for (int f = ocelotlCore.getLpaggregManager().getParameters().size() - 1; f >= 0; f--)
				if (ocelotlCore.getLpaggregManager().getParameters().get(f) < p) {
					textRun.setText(Float.toString(ocelotlCore.getLpaggregManager().getParameters().get(f)));
					break;
				}
			btnRun.notifyListeners(SWT.Selection, new Event());
		}

	}

	private class ResetListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			textTimestampStart.setText(Long.toString(confDataLoader.getMinTimestamp()));
			textTimestampEnd.setText(Long.toString(confDataLoader.getMaxTimestamp()));
			timeLineView.resizeDiagram();
			timeAxisView.resizeDiagram();
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

	private class SettingsSelectionAdapter extends SelectionAdapter {

		private final OcelotlView	view;

		public SettingsSelectionAdapter(final OcelotlView view) {
			this.view = view;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			hasChanged = HasChanged.ALL;
			if (comboTime.getText().equals(""))
				return;
			final ConfigViewManager manager = new ConfigViewManager(view);
			manager.openConfigWindows();
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
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ || hasChanged == HasChanged.PARAMETER)
				hasChanged = HasChanged.THRESHOLD;
		}
	}

	private class TraceAdapter extends SelectionAdapter {
		private Trace	trace;

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final String title = "Loading Trace";
			comboTime.removeAll();
			comboSpace.removeAll();
			trace = traceMap.get(comboTraces.getSelectionIndex());
			final Job job = new Job(title) {

				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					monitor.beginTask(title, IProgressMonitor.UNKNOWN);
					try {
						try {
							confDataLoader.load(trace);
						} catch (final SoCTraceException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						monitor.done();
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {

								textTimestampStart.setText(String.valueOf(confDataLoader.getMinTimestamp()));
								textTimestampEnd.setText(String.valueOf(confDataLoader.getMaxTimestamp()));
								for (final String op : ocelotlCore.getTimeOperators().getOperators(confDataLoader.getCurrentTrace().getType().getName()))
									comboTime.add(op);
								comboTime.setText("");
								btnRemoveEventProducer.notifyListeners(SWT.Selection, new Event());
								btnAddAllEventProducer.notifyListeners(SWT.Selection, new Event());
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

	public static final String					ID				= "fr.inria.soctrace.tools.ocelotl.ui.OcelotlView"; //$NON-NLS-1$
	public static final String					PLUGIN_ID		= Activator.PLUGIN_ID;
	private Button								btnMergeAggregatedParts;
	private Button								btnNormalize;
	private Button								btnRun;
	private Button								btnShowNumbers;
	private Button								btnGrowingQualities;
	private Button								btnDecreasingQualities;
	private Button								btnSettings;
	private Combo								comboTime;
	private Combo								comboTraces;
	private final ConfDataLoader				confDataLoader	= new ConfDataLoader();
	private HasChanged							hasChanged		= HasChanged.ALL;

	private ListViewer							listViewerEventProducers;
	private ITimeLineView						timeLineView;
	private final OcelotlCore					ocelotlCore;
	private final OcelotlParameters				ocelotlParameters;
	private Text								textRun;
	private final java.util.List<EventProducer>	producers		= new LinkedList<EventProducer>();
	private QualityView							qualityView;
	private Spinner								spinnerDivideDbQuery;
	private Spinner								spinnerThread;
	private Spinner								spinnerEventSize;
	private Spinner								spinnerTSNumber;
	private Text								textThreshold;
	private TimeAxisView						timeAxisView;
	private Text								textTimestampEnd;
	private Text								textTimestampStart;

	final Map<Integer, Trace>					traceMap		= new HashMap<Integer, Trace>();

	private Button								buttonDown;
	private Button								buttonUp;
	private Combo								comboSpace;
	private Button								btnRemoveEventProducer;
	private Canvas								canvasMatrixView;
	private final TimeLineViewManager			timeLineViewManager;
	private Composite							compositeMatrixView;
	private SashForm							sashFormView;
	private TimeLineViewWrapper					timeLineViewWrapper;
	private Button								btnAddAllEventProducer;

	/** @throws SoCTraceException */
	public OcelotlView() throws SoCTraceException {
		try {
			confDataLoader.loadTraces();
		} catch (final SoCTraceException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
		ocelotlParameters = new OcelotlParameters();
		ocelotlCore = new OcelotlCore(ocelotlParameters);
		timeLineViewManager = new TimeLineViewManager(this);
	}

	private void cleanAll() {
		hasChanged = HasChanged.ALL;
		textThreshold.setText("0.001");
		textTimestampStart.setText("0");
		textTimestampEnd.setText("0");
		btnNormalize.setSelection(false);
		btnGrowingQualities.setSelection(true);
		btnDecreasingQualities.setSelection(false);
		spinnerTSNumber.setSelection(100);
		spinnerDivideDbQuery.setSelection(0);
		spinnerEventSize.setSelection(10000);
		spinnerThread.setSelection(8);
		textRun.setText("1.0");
		btnMergeAggregatedParts.setSelection(true);
		producers.clear();
		// types.clear();
		// idles.clear();
		listViewerEventProducers.setInput(producers);
		// TODO config paje
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		final SashForm sashFormGlobal = new SashForm(parent, SWT.VERTICAL);
		sashFormGlobal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		timeAxisView = new TimeAxisView();
		qualityView = new QualityView(this);
		sashFormView = new SashForm(sashFormGlobal, SWT.VERTICAL);
		sashFormView.setSashWidth(0);
		compositeMatrixView = new Composite(sashFormView, SWT.NONE);
		compositeMatrixView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeMatrixView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		final GridLayout gl_compositeMatrixView = new GridLayout();
		gl_compositeMatrixView.horizontalSpacing = 0;
		gl_compositeMatrixView.marginHeight = 0;
		compositeMatrixView.setLayout(gl_compositeMatrixView);
		compositeMatrixView.setSize(500, 500);
		timeLineViewWrapper = new TimeLineViewWrapper(this);
		canvasMatrixView = timeLineViewWrapper.init(compositeMatrixView);
		canvasMatrixView.setLayoutData(new GridData(GridData.FILL_BOTH));
		final Composite compositeTimeAxisView = new Composite(sashFormView, SWT.NONE);
		compositeTimeAxisView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		final GridLayout gl_compositeTimeAxisView = new GridLayout();
		gl_compositeTimeAxisView.horizontalSpacing = 0;
		gl_compositeTimeAxisView.marginHeight = 0;
		compositeTimeAxisView.setLayout(gl_compositeTimeAxisView);
		final Canvas canvasTimeAxisView = timeAxisView.initDiagram(compositeTimeAxisView);
		canvasTimeAxisView.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashFormView.setWeights(new int[] { 220, 57 });

		final Group groupTime = new Group(sashFormGlobal, SWT.NONE);
		groupTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		groupTime.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupTime.setLayout(new GridLayout(7, false));

		final Label lblStartTimestamp = new Label(groupTime, SWT.NONE);
		lblStartTimestamp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		lblStartTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblStartTimestamp.setText("Start");

		textTimestampStart = new Text(groupTime, SWT.BORDER);
		textTimestampStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		textTimestampStart.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Label lblEndTimestamp = new Label(groupTime, SWT.NONE);
		lblEndTimestamp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		lblEndTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblEndTimestamp.setText("End");

		textTimestampEnd = new Text(groupTime, SWT.BORDER);
		textTimestampEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		textTimestampEnd.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Button btnReset = new Button(groupTime, SWT.NONE);
		btnReset.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		btnReset.setFont(SWTResourceManager.getFont("Cantarell", 7, SWT.NORMAL));
		btnReset.setText("Reset");

		final Label lblTSNumber = new Label(groupTime, SWT.NONE);
		lblTSNumber.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		lblTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblTSNumber.setText("Timeslice Number");

		spinnerTSNumber = new Spinner(groupTime, SWT.BORDER);
		spinnerTSNumber.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		spinnerTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerTSNumber.setMaximum(10000);
		spinnerTSNumber.setMinimum(1);
		spinnerTSNumber.setSelection(200);
		spinnerTSNumber.addModifyListener(new ConfModificationListener());
		btnReset.addSelectionListener(new ResetListener());
		textTimestampEnd.addModifyListener(new ConfModificationListener());
		textTimestampStart.addModifyListener(new ConfModificationListener());

		final TabFolder tabFolder = new TabFolder(sashFormGlobal, SWT.NONE);
		tabFolder.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		final TabItem tbtmTimeAggregationParameters = new TabItem(tabFolder, SWT.NONE);
		tbtmTimeAggregationParameters.setText("Trace Overview");

		final SashForm sashFormTimeAggregationParameters = new SashForm(tabFolder, SWT.NONE);
		tbtmTimeAggregationParameters.setControl(sashFormTimeAggregationParameters);

		final SashForm sashFormTSandCurve = new SashForm(sashFormTimeAggregationParameters, SWT.VERTICAL);
		sashFormTSandCurve.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Group groupTSParameters = new Group(sashFormTSandCurve, SWT.NONE);
		groupTSParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupTSParameters.setLayout(new GridLayout(1, false));

		final Group groupTraces = new Group(groupTSParameters, SWT.NONE);
		groupTraces.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		groupTraces.setLayout(new GridLayout(2, false));
		comboTraces = new Combo(groupTraces, SWT.READ_ONLY);
		final GridData gd_comboTraces = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_comboTraces.widthHint = 180;
		comboTraces.setLayoutData(gd_comboTraces);
		comboTraces.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Button buttonRefresh = new Button(groupTraces, SWT.NONE);
		buttonRefresh.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/load.png"));
		buttonRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				refreshTraces();
			}
		});
		comboTraces.addSelectionListener(new TraceAdapter());

		int index = 0;
		for (final Trace t : confDataLoader.getTraces()) {
			comboTraces.add(t.getAlias(), index);
			traceMap.put(index, t);
			index++;
		}
		;

		final SashForm sashAggreg = new SashForm(groupTSParameters, SWT.NONE);
		sashAggreg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		final Group groupAggregationOperator = new Group(sashAggreg, SWT.NONE);
		groupAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupAggregationOperator.setText("Microscopic Description");
		groupAggregationOperator.setLayout(new GridLayout(1, false));

		final Composite compositeAggregationOperator = new Composite(groupAggregationOperator, SWT.NONE);
		compositeAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		compositeAggregationOperator.setLayout(new GridLayout(2, false));
		final GridData gd_compositeAggregationOperator = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_compositeAggregationOperator.widthHint = 85;
		compositeAggregationOperator.setLayoutData(gd_compositeAggregationOperator);

		comboTime = new Combo(compositeAggregationOperator, SWT.READ_ONLY);
		final GridData gd_comboAggregationOperator = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_comboAggregationOperator.widthHint = 170;
		comboTime.setLayoutData(gd_comboAggregationOperator);
		comboTime.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		comboTime.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (confDataLoader.getCurrentTrace() == null)
					return;
				hasChanged = HasChanged.ALL;
				ocelotlCore.getTimeOperators().setSelectedOperator(comboTime.getText());
				comboSpace.removeAll();
				for (final String op : ocelotlCore.getSpaceOperators().getOperators(ocelotlCore.getTimeOperators().getSelectedOperatorResource().getSpaceCompatibility()))
					comboSpace.add(op);
				comboSpace.setText("");
				btnSettings.notifyListeners(SWT.Selection, new Event());

			}
		});
		comboTime.setText("");

		btnSettings = new Button(compositeAggregationOperator, SWT.NONE);
		btnSettings.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnSettings.setText("Settings");
		btnSettings.addSelectionListener(new SettingsSelectionAdapter(this));
		comboTime.setText("");

		final Group grpSpaceAggregationOperator = new Group(sashAggreg, SWT.NONE);
		grpSpaceAggregationOperator.setText("Visualization");
		grpSpaceAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpSpaceAggregationOperator.setLayout(new GridLayout(1, false));

		final Composite composite = new Composite(grpSpaceAggregationOperator, SWT.NONE);
		final GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_composite.widthHint = 85;
		composite.setLayoutData(gd_composite);
		composite.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		composite.setLayout(new GridLayout(1, false));

		comboSpace = new Combo(composite, SWT.READ_ONLY);
		comboSpace.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		final GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_combo.widthHint = 170;
		comboSpace.setLayoutData(gd_combo);
		comboSpace.setText("");

		comboSpace.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (confDataLoader.getCurrentTrace() == null)
					return;
				if (hasChanged == HasChanged.NOTHING)
					hasChanged = HasChanged.PARAMETER;
				ocelotlCore.getSpaceOperators().setSelectedOperator(comboSpace.getText());
				timeLineView = timeLineViewManager.create();
				timeLineViewWrapper.setView(timeLineView);

			}
		});
		sashAggreg.setWeights(new int[] { 1, 1 });

		final Group groupEventProducers = new Group(groupTSParameters, SWT.NONE);
		groupEventProducers.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
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
		scrCompositeEventProducerButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		scrCompositeEventProducerButtons.setExpandHorizontal(true);
		scrCompositeEventProducerButtons.setExpandVertical(true);

		final Composite compositeEventProducerButtons = new Composite(scrCompositeEventProducerButtons, SWT.NONE);
		compositeEventProducerButtons.setLayout(new GridLayout(1, false));
		final Button btnAddEventProducer = new Button(compositeEventProducerButtons, SWT.NONE);
		btnAddEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddEventProducer.setText("Add");
		btnAddEventProducer.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddEventProducer.setImage(null);

		btnAddAllEventProducer = new Button(compositeEventProducerButtons, SWT.NONE);
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
		btnRemoveEventProducer = new Button(compositeEventProducerButtons, SWT.NONE);
		btnRemoveEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnRemoveEventProducer.setText("Reset");
		btnRemoveEventProducer.addSelectionListener(new ResetSelectionAdapter(listViewerEventProducers));
		btnRemoveEventProducer.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRemoveEventProducer.setImage(null);
		scrCompositeEventProducerButtons.setContent(compositeEventProducerButtons);
		scrCompositeEventProducerButtons.setMinSize(compositeEventProducerButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnAddEventProducer.addSelectionListener(new AddEventProducersAdapter());

		final Group groupQualityCurveSettings = new Group(sashFormTSandCurve, SWT.NONE);
		groupQualityCurveSettings.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupQualityCurveSettings.setText("Quality Curve Settings");
		groupQualityCurveSettings.setLayout(new GridLayout(3, false));
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnNormalize = new Button(groupQualityCurveSettings, SWT.CHECK);
		btnNormalize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnNormalize.setSelection(false);
		btnNormalize.setText("Normalize Qualities");
		btnNormalize.addSelectionListener(new NormalizeSelectionAdapter());
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnGrowingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnGrowingQualities.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnGrowingQualities.setText("Complexity (green), Information (red)");
		btnGrowingQualities.setSelection(true);
		btnGrowingQualities.addSelectionListener(new GrowingQualityRadioSelectionAdapter());
		btnGrowingQualities.setSelection(false);

		btnDecreasingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnDecreasingQualities.setText("Complexity reduction (green), Information loss (red)");
		btnDecreasingQualities.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		sashFormTSandCurve.setWeights(new int[] { 222, 67 });
		btnDecreasingQualities.addSelectionListener(new DecreasingQualityRadioSelectionAdapter());

		final SashForm sashForm = new SashForm(sashFormTimeAggregationParameters, SWT.VERTICAL);

		final Composite compositeQualityView = new Composite(sashForm, SWT.NONE);
		compositeQualityView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeQualityView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		final GridLayout gl_compositeQualityView = new GridLayout();
		compositeQualityView.setLayout(gl_compositeQualityView);
		final Canvas canvasQualityView = qualityView.initDiagram(compositeQualityView);

		final Group groupLPAParameters = new Group(sashForm, SWT.NONE);
		groupLPAParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupLPAParameters.setLayout(new GridLayout(1, false));

		final Composite compositeGetParameters = new Composite(groupLPAParameters, SWT.NONE);
		compositeGetParameters.setLayout(new GridLayout(8, false));
		compositeGetParameters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		final Label lblThreshold = new Label(compositeGetParameters, SWT.NONE);
		lblThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblThreshold.setText("Threshold");

		textThreshold = new Text(compositeGetParameters, SWT.BORDER);
		final GridData gd_textThreshold = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textThreshold.widthHint = 82;
		textThreshold.setLayoutData(gd_textThreshold);
		textThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Label lblParameter = new Label(compositeGetParameters, SWT.NONE);
		lblParameter.setText("Parameter");
		lblParameter.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblParameter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		// btnGetParameters = new Button(compositeGetParameters, SWT.NONE);
		// btnGetParameters.setFont(SWTResourceManager.getFont("Cantarell", 8,
		// SWT.NORMAL));
		// btnGetParameters.setText("Get");

		textRun = new Text(compositeGetParameters, SWT.BORDER);
		textRun.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		buttonDown = new Button(compositeGetParameters, SWT.NONE);
		buttonDown.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		buttonDown.setText("<");
		buttonDown.addSelectionListener(new ParameterDownAdapter());

		buttonUp = new Button(compositeGetParameters, SWT.NONE);
		buttonUp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		buttonUp.setText(">");
		buttonUp.addSelectionListener(new ParameterUpAdapter());
		btnRun = new Button(compositeGetParameters, SWT.NONE);
		btnRun.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/1366759976_white_tiger.png"));
		btnRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRun.setText("RUN!");
		new Label(compositeGetParameters, SWT.NONE);
		sashForm.setWeights(new int[] { 249, 46 });
		sashFormTimeAggregationParameters.setWeights(new int[] { 227, 361 });
		btnRun.addSelectionListener(new GetAggregationAdapter());
		textRun.addModifyListener(new ParameterModifyListener());

		// btnGetParameters.addSelectionListener(new GetParametersAdapter());
		textThreshold.addModifyListener(new ThresholdModifyListener());
		canvasQualityView.setLayoutData(new GridData(GridData.FILL_BOTH));

		// final TabItem tbtmTraceParameters = new TabItem(tabFolder, SWT.NONE);
		// tbtmTraceParameters.setText("Trace Parameters");

		// final SashForm sashFormTraceParameter = new SashForm(tabFolder,
		// SWT.VERTICAL);
		// tbtmTraceParameters.setControl(sashFormTraceParameter);

		final TabItem tbtmAdvancedParameters = new TabItem(tabFolder, 0);
		tbtmAdvancedParameters.setText("Advanced Parameters");

		final SashForm sashFormAdvancedParameters = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormAdvancedParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		tbtmAdvancedParameters.setControl(sashFormAdvancedParameters);

		final Group grpCacheManagement = new Group(sashFormAdvancedParameters, SWT.NONE);
		grpCacheManagement.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpCacheManagement.setText("Iterator Management");
		grpCacheManagement.setLayout(new GridLayout(2, false));

		final Label lblPageSize = new Label(grpCacheManagement, SWT.NONE);
		lblPageSize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblPageSize.setText("Event Number Retrieved by Threads");

		spinnerEventSize = new Spinner(grpCacheManagement, SWT.BORDER);
		spinnerEventSize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerEventSize.addModifyListener(new ConfModificationListener());
		spinnerEventSize.setMinimum(100);
		spinnerEventSize.setMaximum(10000000);
		spinnerEventSize.setSelection(100000);

		final Group grpDivideDbQuery = new Group(sashFormAdvancedParameters, SWT.NONE);
		grpDivideDbQuery.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpDivideDbQuery.setText("Query Management");
		grpDivideDbQuery.setLayout(new GridLayout(2, false));

		final Label lblDivideDbQueries = new Label(grpDivideDbQuery, SWT.NONE);
		lblDivideDbQueries.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblDivideDbQueries.setText("Event Producers per Query (0=All)");

		spinnerDivideDbQuery = new Spinner(grpDivideDbQuery, SWT.BORDER);
		spinnerDivideDbQuery.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerDivideDbQuery.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		spinnerDivideDbQuery.addModifyListener(new ConfModificationListener());
		spinnerDivideDbQuery.setMinimum(0);
		spinnerDivideDbQuery.setMaximum(1000000);
		spinnerDivideDbQuery.setSelection(0);

		final Group grpMultiThread = new Group(sashFormAdvancedParameters, SWT.NONE);
		grpMultiThread.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpMultiThread.setText("Multi Threading");
		grpMultiThread.setLayout(new GridLayout(2, false));

		final Label lblThread = new Label(grpMultiThread, SWT.NONE);
		lblThread.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblThread.setText("Working Threads");

		spinnerThread = new Spinner(grpMultiThread, SWT.BORDER);
		spinnerThread.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerThread.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		spinnerThread.addModifyListener(new ConfModificationListener());
		spinnerThread.setMinimum(1);
		spinnerThread.setMaximum(1000000);
		spinnerThread.setSelection(5);

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
		sashFormGlobal.setWeights(new int[] { 254, 41, 368 });
		// sashFormAdvancedParameters.setWeights(new int[] { 112, 374 });
		// sashFormGlobal.setWeights(new int[] { 172, 286 });

		// clean all
		cleanAll();

	}

	public Button getBtnRun() {
		return btnRun;
	}

	public Combo getComboAggregationOperator() {
		return comboTime;
	}

	public ConfDataLoader getConfDataLoader() {
		return confDataLoader;
	}

	public OcelotlCore getCore() {
		return ocelotlCore;
	}

	public Text getParam() {
		return textRun;
	}

	public OcelotlParameters getParams() {
		return ocelotlParameters;
	}

	public TimeAxisView getTimeAxisView() {
		return timeAxisView;
	}

	public TimeRegion getTimeRegion() {
		return new TimeRegion(Long.parseLong(textTimestampStart.getText()), Long.parseLong(textTimestampEnd.getText()));
	}

	private void refreshTraces() {
		try {
			confDataLoader.loadTraces();
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int index = 0;
		comboTraces.removeAll();
		for (final Trace t : confDataLoader.getTraces()) {
			comboTraces.add(t.getAlias(), index);
			traceMap.put(index, t);
			index++;
		}
		;
	}

	public void setComboAggregationOperator(final Combo comboAggregationOperator) {
		comboTime = comboAggregationOperator;
	}

	public void setConfiguration() {

		ocelotlParameters.setTrace(confDataLoader.getCurrentTrace());
		ocelotlParameters.setEventProducers(producers);
		// ocelotlParameters.setEventTypes(types);
		// ocelotlParameters.setSleepingStates(idles);
		ocelotlParameters.setNormalize(btnNormalize.getSelection());
		ocelotlParameters.setTimeSlicesNumber(spinnerTSNumber.getSelection());
		ocelotlParameters.setMaxEventProducers(spinnerDivideDbQuery.getSelection());
		ocelotlParameters.setTimeAggOperator(comboTime.getText());
		ocelotlParameters.setEventsPerThread(spinnerEventSize.getSelection());
		ocelotlParameters.setThread(spinnerThread.getSelection());
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

	public void setTimeRegion(final TimeRegion time) {
		textTimestampStart.setText(String.valueOf(time.getTimeStampStart()));
		textTimestampEnd.setText(String.valueOf(time.getTimeStampEnd()));
	}
}