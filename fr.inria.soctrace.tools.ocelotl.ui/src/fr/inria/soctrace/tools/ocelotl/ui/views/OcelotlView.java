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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.ViewPart;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
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
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.IAggregatedView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewWrapper;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.custom.StackLayout;

/**
 * Main view for LPAggreg Paje Tool
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class OcelotlView extends ViewPart {

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
								timeLineView.createDiagram(ocelotlCore.getLpaggregManager(), ocelotlParameters.getTimeRegion());
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
			for (final double f : ocelotlCore.getLpaggregManager().getParameters())
				if (f > p) {
					textRun.setText(Double.toString(f));
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
					textRun.setText(Double.toString(ocelotlCore.getLpaggregManager().getParameters().get(f)));
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

	private class Settings2SelectionAdapter extends SelectionAdapter {

		private final OcelotlView	view;

		public Settings2SelectionAdapter(final OcelotlView view) {
			this.view = view;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			// hasChanged = HasChanged.ALL;
			if (comboSpace.getText().equals(""))
				return;
			final VisuConfigViewManager manager = new VisuConfigViewManager(view);
			manager.openConfigWindows();
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
								// btnRemoveEventProducer.notifyListeners(SWT.Selection,
								// new Event());
								// btnAddAllEventProducer.notifyListeners(SWT.Selection,
								// new Event());
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

	private Action createGanttAction() {
		ImageDescriptor img = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID, "icons/gantt.png");
		Action showGantt = new Action("Show Gantt Chart", img) {
			@Override
			public void run() {
				if (confDataLoader.getCurrentTrace() == null)
					return;
				TraceIntervalDescriptor des = new TraceIntervalDescriptor();
				des.setTrace(ocelotlParameters.getTrace());

				des.setStartTimestamp(getTimeRegion().getTimeStampStart());
				des.setEndTimestamp(getTimeRegion().getTimeStampEnd());
				FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_GANTT_DISPLAY_TIME_INTERVAL, des);
			}
		};
		return showGantt;
	}

	public static final String			ID				= "fr.inria.soctrace.tools.ocelotl.ui.OcelotlView"; //$NON-NLS-1$
	public static final String			PLUGIN_ID		= Activator.PLUGIN_ID;
	private Button						btnNormalize;
	private Button						btnRun;
	private Button						btnGrowingQualities;
	private Button						btnDecreasingQualities;
	private Button						btnSettings;
	private Combo						comboTime;
	private Combo						comboTraces;
	private final ConfDataLoader		confDataLoader	= new ConfDataLoader();
	private HasChanged					hasChanged		= HasChanged.ALL;

	private IAggregatedView				timeLineView;
	private final OcelotlCore			ocelotlCore;
	private final OcelotlParameters		ocelotlParameters;
	private Text						textRun;
	private QualityView					qualityView;
	private Spinner						spinnerTSNumber;
	private Text						textThreshold;
	private TimeAxisView				timeAxisView;
	private Text						textTimestampEnd;
	private Text						textTimestampStart;

	final Map<Integer, Trace>			traceMap		= new HashMap<Integer, Trace>();

	private Button						buttonDown;
	private Button						buttonUp;
	private Combo						comboSpace;
	private final TimeLineViewManager	timeLineViewManager;
	private Composite					compositeMatrixView;
	private SashForm					sashFormView;
	private TimeLineViewWrapper			timeLineViewWrapper;
	private Button						btnSettings2;

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
		spinnerTSNumber.setSelection(20);
		textRun.setText("1.0");
		// producers.clear();
		// types.clear();
		// idles.clear();
		// listViewerEventProducers.setInput(producers);
		// TODO config paje
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		final SashForm sashFormGlobal = new SashForm(parent, SWT.VERTICAL);
		sashFormGlobal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		timeAxisView = new TimeAxisView();
		qualityView = new QualityView(this);
		timeLineViewWrapper = new TimeLineViewWrapper(this);

		SashForm sashForm_1 = new SashForm(sashFormGlobal, SWT.BORDER);
		sashForm_1.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		sashFormView = new SashForm(sashForm_1, SWT.BORDER | SWT.VERTICAL);

		SashForm sashForm_4 = new SashForm(sashFormView, SWT.VERTICAL);
		sashForm_4.setSashWidth(0);
		compositeMatrixView = new Composite(sashForm_4, SWT.NONE);
		compositeMatrixView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeMatrixView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		compositeMatrixView.setSize(500, 500);
		timeLineViewWrapper.init(compositeMatrixView);
		compositeMatrixView.setLayout(new FillLayout(SWT.HORIZONTAL));
		final Composite compositeTimeAxisView = new Composite(sashForm_4, SWT.NONE);
		compositeTimeAxisView.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		timeAxisView.initDiagram(compositeTimeAxisView);
		FillLayout fl_compositeTimeAxisView = new FillLayout(SWT.HORIZONTAL);
		compositeTimeAxisView.setLayout(fl_compositeTimeAxisView);
		sashForm_4.setWeights(new int[] {402, 40});

		Composite composite_2 = new Composite(sashFormView, SWT.NONE);
		composite_2.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		composite_2.setLayout(new FillLayout(SWT.HORIZONTAL));

		final Group groupTime = new Group(composite_2, SWT.BORDER);
		groupTime.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		groupTime.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupTime.setLayout(new GridLayout(14, false));

		final Label lblStartTimestamp = new Label(groupTime, SWT.NONE);
		lblStartTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblStartTimestamp.setText("Start");

		textTimestampStart = new Text(groupTime, SWT.BORDER);
		GridData gd_textTimestampStart = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textTimestampStart.minimumWidth = 50;
		textTimestampStart.setLayoutData(gd_textTimestampStart);
		textTimestampStart.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Label lblEndTimestamp = new Label(groupTime, SWT.NONE);
		lblEndTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblEndTimestamp.setText("End");

		textTimestampEnd = new Text(groupTime, SWT.BORDER);
		GridData gd_textTimestampEnd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textTimestampEnd.minimumWidth = 50;
		textTimestampEnd.setLayoutData(gd_textTimestampEnd);
		textTimestampEnd.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Button btnReset = new Button(groupTime, SWT.NONE);
		btnReset.setFont(SWTResourceManager.getFont("Cantarell", 7, SWT.NORMAL));
		btnReset.setText("Reset");

		final Label lblTSNumber = new Label(groupTime, SWT.NONE);
		lblTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblTSNumber.setText("Timeslice Number");

		spinnerTSNumber = new Spinner(groupTime, SWT.BORDER);
		final GridData gd_spinnerTSNumber = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_spinnerTSNumber.minimumWidth = 50;
		spinnerTSNumber.setLayoutData(gd_spinnerTSNumber);
		spinnerTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerTSNumber.setMaximum(10000);
		spinnerTSNumber.setMinimum(1);
		spinnerTSNumber.setSelection(200);

		final Label lblThreshold = new Label(groupTime, SWT.NONE);
		lblThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblThreshold.setText("Threshold");

		textThreshold = new Text(groupTime, SWT.BORDER);
		GridData gd_textThreshold = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textThreshold.minimumWidth = 50;
		textThreshold.setLayoutData(gd_textThreshold);
		textThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Label lblParameter = new Label(groupTime, SWT.NONE);
		lblParameter.setText("Parameter");
		lblParameter.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		// btnGetParameters = new Button(compositeGetParameters, SWT.NONE);
		// btnGetParameters.setFont(SWTResourceManager.getFont("Cantarell", 8,
		// SWT.NORMAL));
		// btnGetParameters.setText("Get");

		textRun = new Text(groupTime, SWT.BORDER);
		GridData gd_textRun = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textRun.minimumWidth = 50;
		textRun.setLayoutData(gd_textRun);
		textRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		buttonDown = new Button(groupTime, SWT.NONE);
		buttonDown.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		buttonDown.setText("<");

		buttonUp = new Button(groupTime, SWT.NONE);
		buttonUp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		buttonUp.setText(">");
		btnRun = new Button(groupTime, SWT.NONE);
		btnRun.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnRun.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/1366759976_white_tiger.png"));
		btnRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRun.setText("RUN!");
		sashFormView.setWeights(new int[] { 442, 47 });

		final SashForm sashForm = new SashForm(sashForm_1, SWT.BORDER | SWT.VERTICAL);
		sashForm.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		// canvasMatrixView.setLayoutData(new GridData(GridData.FILL_BOTH));
		// canvasTimeAxisView.setLayoutData(new GridData(GridData.FILL_BOTH));

		final TabFolder tabFolder = new TabFolder(sashForm, SWT.NONE);
		tabFolder.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		final TabItem tbtmTimeAggregationParameters = new TabItem(tabFolder, SWT.NONE);
		tbtmTimeAggregationParameters.setText("Trace Overview");

		final SashForm sashFormTSandCurve = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmTimeAggregationParameters.setControl(sashFormTSandCurve);
		sashFormTSandCurve.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Group groupTraces = new Group(sashFormTSandCurve, SWT.NONE);
		groupTraces.setFont(org.eclipse.wb.swt.SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupTraces.setText("Trace");
		groupTraces.setLayout(new GridLayout(1, false));

		Composite composite_1 = new Composite(groupTraces, SWT.NONE);
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite_1.widthHint = 285;
		composite_1.setLayoutData(gd_composite_1);
		composite_1.setLayout(new GridLayout(2, false));
		comboTraces = new Combo(composite_1, SWT.READ_ONLY);
		GridData gd_comboTraces = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_comboTraces.widthHint = 179;
		comboTraces.setLayoutData(gd_comboTraces);
		comboTraces.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Button buttonRefresh = new Button(composite_1, SWT.NONE);
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
		final Group groupAggregationOperator = new Group(sashFormTSandCurve, SWT.NONE);
		groupAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupAggregationOperator.setText("Microscopic Description");
		groupAggregationOperator.setLayout(new GridLayout(1, false));

		final Composite compositeAggregationOperator = new Composite(groupAggregationOperator, SWT.NONE);
		compositeAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		compositeAggregationOperator.setLayout(new GridLayout(2, false));
		final GridData gd_compositeAggregationOperator = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
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
				ocelotlParameters.getEventProducers().clear();
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

		final Group grpSpaceAggregationOperator = new Group(sashFormTSandCurve, SWT.NONE);
		grpSpaceAggregationOperator.setText("Visualization");
		grpSpaceAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpSpaceAggregationOperator.setLayout(new GridLayout(1, false));

		final Composite composite = new Composite(grpSpaceAggregationOperator, SWT.NONE);
		final GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_composite.widthHint = 85;
		composite.setLayoutData(gd_composite);
		composite.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		composite.setLayout(new GridLayout(2, false));

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
				btnSettings2.notifyListeners(SWT.Selection, new Event());

			}
		});

		btnSettings2 = new Button(composite, SWT.NONE);
		btnSettings2.setText("Settings");
		btnSettings2.setFont(org.eclipse.wb.swt.SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnSettings2.addSelectionListener(new Settings2SelectionAdapter(this));
		// canvasQualityView.setLayoutData(new GridData(GridData.FILL_BOTH));

		// final TabItem tbtmTraceParameters = new TabItem(tabFolder, SWT.NONE);
		// tbtmTraceParameters.setText("Trace Parameters");

		// final SashForm sashFormTraceParameter = new SashForm(tabFolder,
		// SWT.VERTICAL);
		// tbtmTraceParameters.setControl(sashFormTraceParameter);

		final TabItem tbtmAdvancedParameters = new TabItem(tabFolder, 0);
		tbtmAdvancedParameters.setText("Quality curves");

		final SashForm sashFormAdvancedParameters = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormAdvancedParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		tbtmAdvancedParameters.setControl(sashFormAdvancedParameters);

		final Group groupQualityCurveSettings = new Group(sashFormAdvancedParameters, SWT.NONE);
		groupQualityCurveSettings.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupQualityCurveSettings.setText("Quality Curve Settings");
		groupQualityCurveSettings.setLayout(new GridLayout(2, false));
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnNormalize = new Button(groupQualityCurveSettings, SWT.CHECK);
		btnNormalize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnNormalize.setSelection(false);
		btnNormalize.setText("Normalize Qualities");
		btnNormalize.addSelectionListener(new NormalizeSelectionAdapter());
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnGrowingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnGrowingQualities.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnGrowingQualities.setText("Complexity gain(green), Information gain(red)");
		btnGrowingQualities.setSelection(true);
		btnGrowingQualities.addSelectionListener(new GrowingQualityRadioSelectionAdapter());
		btnGrowingQualities.setSelection(false);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnDecreasingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnDecreasingQualities.setText("Complexity reduction (green), Information loss (red)");
		btnDecreasingQualities.setSelection(false);
		btnDecreasingQualities.setFont(org.eclipse.wb.swt.SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnDecreasingQualities.addSelectionListener(new DecreasingQualityRadioSelectionAdapter());
		sashFormAdvancedParameters.setWeights(new int[] { 1 });

		final Composite compositeQualityView = new Composite(sashForm, SWT.NONE);
		compositeQualityView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeQualityView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		qualityView.initDiagram(compositeQualityView);
		compositeQualityView.setLayout(new FillLayout(SWT.HORIZONTAL));
		sashForm.setWeights(new int[] {196, 293});
		btnRun.addSelectionListener(new GetAggregationAdapter());
		buttonUp.addSelectionListener(new ParameterUpAdapter());
		buttonDown.addSelectionListener(new ParameterDownAdapter());
		textRun.addModifyListener(new ParameterModifyListener());

		// btnGetParameters.addSelectionListener(new GetParametersAdapter());
		textThreshold.addModifyListener(new ThresholdModifyListener());
		spinnerTSNumber.addModifyListener(new ConfModificationListener());
		btnReset.addSelectionListener(new ResetListener());
		textTimestampEnd.addModifyListener(new ConfModificationListener());
		textTimestampStart.addModifyListener(new ConfModificationListener());
		sashForm_1.setWeights(new int[] { 655, 254 });
		sashFormGlobal.setWeights(new int[] { 395 });
		// sashFormAdvancedParameters.setWeights(new int[] { 112, 374 });
		// sashFormGlobal.setWeights(new int[] { 172, 286 });

		// clean all
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(createGanttAction());

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
		// ocelotlParameters.setEventProducers(producers);
		// ocelotlParameters.setEventTypes(types);
		// ocelotlParameters.setSleepingStates(idles);
		ocelotlParameters.setNormalize(btnNormalize.getSelection());
		ocelotlParameters.setTimeSlicesNumber(spinnerTSNumber.getSelection());
		// ocelotlParameters.setMaxEventProducers(spinnerDivideDbQuery.getSelection());
		ocelotlParameters.setTimeAggOperator(comboTime.getText());
		// ocelotlParameters.setEventsPerThread(spinnerEventSize.getSelection());
		// ocelotlParameters.setThread(spinnerThread.getSelection());
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