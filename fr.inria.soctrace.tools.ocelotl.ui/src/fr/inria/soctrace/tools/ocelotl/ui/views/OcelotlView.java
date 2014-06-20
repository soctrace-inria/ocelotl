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

package fr.inria.soctrace.tools.ocelotl.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.HasChanged;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.Activator;
import fr.inria.soctrace.tools.ocelotl.ui.loaders.ConfDataLoader;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.IAggregatedView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewWrapper;

/**
 * Main view for LPAggreg Paje Tool
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class OcelotlView extends ViewPart implements IFramesocBusListener {
	
	
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

	private class GetAggregationAdapter extends SelectionAdapter {
		
		//Prevent the simultaneous execution of multiple threads leading to random crashes
		private Object lock = new Object();
		//Global flag signaling that a job already is running 
		private boolean running = false;

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			if (comboTime.getText().equals(""))
				return;
			if (comboSpace.getText().equals(""))
				return;
			
			//Mutex zone
			synchronized (lock) {
				//If a job is already running
				if (running == true)
				{
					//reset the displayed value to the actual value to avoid displaying a wrong value
					textRun.setText(Double.toString(ocelotlParameters.getParameter()));
			
					//and discard the new job
					return;
				}
				
				//else we are starting a job
				running = true;
			}
			
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ || hasChanged == HasChanged.PARAMETER)
			{
				hasChanged = HasChanged.PARAMETER;
			}
			else
			{
				textRun.setText("1.0");
			}
			setConfiguration();
			final String title = "Computing Aggregated View";
			final Job job = new Job(title) {

				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					monitor.beginTask(title, IProgressMonitor.UNKNOWN);
					if (hasChanged != HasChanged.PARAMETER)
						try {
							ocelotlCore.computeDichotomy(hasChanged);
						} catch (final SoCTraceException e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						} catch (final OcelotlException e) {
							monitor.done();
							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {
									hasChanged = HasChanged.ALL;
									MessageDialog.openInformation(getSite().getShell(), "Error", e.getMessage());
								}
							});
							synchronized (lock) {
								running = false;
							}
							return Status.CANCEL_STATUS;
						}

					hasChanged = HasChanged.PARAMETER;
					try {
						ocelotlCore.computeParts(hasChanged);
					} catch (final SoCTraceException e) {
						e.printStackTrace();
						synchronized (lock) {
							running = false;
						}
						return Status.CANCEL_STATUS;
					} catch (final OcelotlException e) {
						monitor.done();
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								hasChanged = HasChanged.ALL;
								MessageDialog.openInformation(getSite().getShell(), "Error", e.getMessage());
							}
						});
						synchronized (lock) {
							running = false;
						}
						return Status.CANCEL_STATUS;
					}
					monitor.done();
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							hasChanged = HasChanged.NOTHING;
							timeLineView.deleteDiagram();
							timeLineView.createDiagram(ocelotlCore.getLpaggregManager(), ocelotlParameters.getTimeRegion());
							timeAxisView.createDiagram(ocelotlParameters.getTimeRegion());
							qualityView.createDiagram();
						}
					});
				
					synchronized (lock) {
						running = false;
					}
					return Status.OK_STATUS;	
				}
			};
			job.setUser(true);	
			job.schedule();
			
			if(job.getResult() == Status.OK_STATUS)
			{
				//Setting the actual computed value
				textRun.setText(Double.toString(ocelotlParameters.getParameter()));
			}
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

	@SuppressWarnings("unused")
	private class OcelotlKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(final KeyEvent e) {
			switch (e.keyCode) {
			case SWT.ARROW_LEFT:
				//Make sure we are not in an editable field
				if(!(e.widget.getClass().getSimpleName().equals("Text") || e.widget.getClass().getSimpleName().equals("Spinner")))
					buttonDown.notifyListeners(SWT.Selection, new Event());
				break;
			case SWT.ARROW_RIGHT:
				//Make sure we are not in an editable field
				if(!(e.widget.getClass().getSimpleName().equals("Text") || e.widget.getClass().getSimpleName().equals("Spinner")))
					buttonUp.notifyListeners(SWT.Selection, new Event());
				break;
			case SWT.KEYPAD_CR:
			case SWT.CR:
				btnRun.notifyListeners(SWT.Selection, new Event());
				break;
			case SWT.ESC:
				btnReset.notifyListeners(SWT.Selection, new Event());
				break;
			}
		}
	}

	private class ParameterDownAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final float p = Float.parseFloat(textRun.getText());
			if (ocelotlCore.getLpaggregManager() != null) {
				for (final double f : ocelotlCore.getLpaggregManager().getParameters()) {
					if (f > p) {
						textRun.setText(Double.toString(f));
						btnRun.notifyListeners(SWT.Selection, new Event());
						break;
					}
				}
			}
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
			if (ocelotlCore.getLpaggregManager() != null) {
				for (int f = ocelotlCore.getLpaggregManager().getParameters().size() - 1; f >= 0; f--) {
					if (ocelotlCore.getLpaggregManager().getParameters().get(f) < p) {
						textRun.setText(Double.toString(ocelotlCore.getLpaggregManager().getParameters().get(f)));
						btnRun.notifyListeners(SWT.Selection, new Event());
						break;
					}
				}

			}
		}
	}

	private class ResetListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			textTimestampStart.setText(Long.toString(confDataLoader.getMinTimestamp()));
			textTimestampEnd.setText(Long.toString(confDataLoader.getMaxTimestamp()));
			if(timeLineView != null)
			{
				timeLineView.resizeDiagram();
				timeAxisView.resizeDiagram();
			}
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
								for (final String op : ocelotlCore.getTimeOperators().getOperators(confDataLoader.getCurrentTrace().getType().getName(), confDataLoader.getCategories()))
								{
									comboTime.add(op);
								}
								
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
	private Button						btnReset;
	
	/**
	 * Followed topics
	 */
	protected FramesocBusTopicList topics = null;

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
		
		//Register update to synchronize traces
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_REFRESH_TRACES_NEEDED);
		topics.registerAll();
	}

	private void cleanAll() {
		hasChanged = HasChanged.ALL;
		textThreshold.setText(String.valueOf(OcelotlDefaultParameterConstants.Threshold));
		textTimestampStart.setText(String.valueOf(OcelotlDefaultParameterConstants.TimestampStart));
		textTimestampEnd.setText(String.valueOf(OcelotlDefaultParameterConstants.TimestampEnd));
		btnNormalize.setSelection(OcelotlDefaultParameterConstants.Normalize);
		btnGrowingQualities.setSelection(OcelotlDefaultParameterConstants.GrowingQualities);
		spinnerTSNumber.setSelection(OcelotlDefaultParameterConstants.TimeSliceNumber);
		textRun.setText(String.valueOf(OcelotlDefaultParameterConstants.RunParameter));
	}
	
	@Override
	public void dispose() {
		topics.unregisterAll();
		super.dispose();
	}

	private Action createGanttAction() {
		final ImageDescriptor img = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID, "icons/gantt.png");
		final Action showGantt = new Action("Show Gantt Chart", img) {
			@Override
			public void run() {
				if (confDataLoader.getCurrentTrace() == null)
					return;
				final TraceIntervalDescriptor des = new TraceIntervalDescriptor();
				des.setTrace(ocelotlParameters.getTrace());

				des.setStartTimestamp(getTimeRegion().getTimeStampStart());
				des.setEndTimestamp(getTimeRegion().getTimeStampEnd());
				FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_GANTT_DISPLAY_TIME_INTERVAL, des);
			}
		};
		return showGantt;
	}

	private Action createTableAction() {
		final ImageDescriptor img = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID, "icons/table.png");
		final Action showTable = new Action("Show Event Table", img) {
			@Override
			public void run() {
				if (confDataLoader.getCurrentTrace() == null)
					return;
				final TraceIntervalDescriptor des = new TraceIntervalDescriptor();
				des.setTrace(ocelotlParameters.getTrace());

				des.setStartTimestamp(getTimeRegion().getTimeStampStart());
				des.setEndTimestamp(getTimeRegion().getTimeStampEnd());
				FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_TABLE_DISPLAY_TIME_INTERVAL, des);
			}
		};
		return showTable;
	}

	@Override
	public void createPartControl(final Composite parent) {
		final Display display = Display.getCurrent();

		display.addFilter(SWT.KeyDown, new Listener() {

			@Override
			public void handleEvent(final Event e) {
				switch (e.keyCode) {
				case SWT.ARROW_LEFT:
					//Make sure we are not in an editable field
					if(!(e.widget.getClass().getSimpleName().equals("Text") || e.widget.getClass().getSimpleName().equals("Spinner")))
						buttonDown.notifyListeners(SWT.Selection, new Event());
					break;
				case SWT.ARROW_RIGHT:
					//Make sure we are not in an editable field
					if(!(e.widget.getClass().getSimpleName().equals("Text") || e.widget.getClass().getSimpleName().equals("Spinner")))
						buttonUp.notifyListeners(SWT.Selection, new Event());
					break;
				case SWT.KEYPAD_CR:
				case SWT.CR:
					btnRun.notifyListeners(SWT.Selection, new Event());
					break;
				case SWT.ESC:
					btnReset.notifyListeners(SWT.Selection, new Event());
					break;
				}

			}
		});
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		final SashForm sashFormGlobal = new SashForm(parent, SWT.VERTICAL);
		sashFormGlobal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		timeAxisView = new TimeAxisView();
		qualityView = new QualityView(this);
		timeLineViewWrapper = new TimeLineViewWrapper(this);

		final SashForm sashForm_1 = new SashForm(sashFormGlobal, SWT.BORDER);
		sashForm_1.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		sashFormView = new SashForm(sashForm_1, SWT.BORDER | SWT.VERTICAL);
		sashFormView.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		final SashForm sashForm_4 = new SashForm(sashFormView, SWT.BORDER | SWT.VERTICAL);
		compositeMatrixView = new Composite(sashForm_4, SWT.BORDER);
		compositeMatrixView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeMatrixView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		compositeMatrixView.setSize(500, 500);
		timeLineViewWrapper.init(compositeMatrixView);
		compositeMatrixView.setLayout(new FillLayout(SWT.HORIZONTAL));
		final Composite compositeTimeAxisView = new Composite(sashForm_4, SWT.BORDER);
		compositeTimeAxisView.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		timeAxisView.initDiagram(compositeTimeAxisView);
		final FillLayout fl_compositeTimeAxisView = new FillLayout(SWT.HORIZONTAL);
		compositeTimeAxisView.setLayout(fl_compositeTimeAxisView);
		sashForm_4.setWeights(new int[] {388, 24});

		final ScrolledComposite scrolledComposite = new ScrolledComposite(sashFormView, SWT.BORDER | SWT.H_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		final Group groupTime = new Group(scrolledComposite, SWT.NONE);
		groupTime.setSize(422, 110);
		groupTime.setForeground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		groupTime.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		groupTime.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupTime.setLayout(new GridLayout(11, false));
		new Label(groupTime, SWT.NONE);

		final Label lblStartTimestamp = new Label(groupTime, SWT.NONE);
		lblStartTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblStartTimestamp.setText("Start");

		textTimestampStart = new Text(groupTime, SWT.BORDER);
		final GridData gd_textTimestampStart = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_textTimestampStart.widthHint = 150;
		textTimestampStart.setLayoutData(gd_textTimestampStart);
		textTimestampStart.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Label lblEndTimestamp = new Label(groupTime, SWT.NONE);
		lblEndTimestamp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblEndTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblEndTimestamp.setText("End");

		textTimestampEnd = new Text(groupTime, SWT.BORDER);
		final GridData gd_textTimestampEnd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_textTimestampEnd.widthHint = 150;
		textTimestampEnd.setLayoutData(gd_textTimestampEnd);
		textTimestampEnd.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		btnReset = new Button(groupTime, SWT.NONE);
		btnReset.setFont(SWTResourceManager.getFont("Cantarell", 7, SWT.NORMAL));
		btnReset.setText("Reset");

		final Label lblTSNumber = new Label(groupTime, SWT.NONE);
		lblTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblTSNumber.setText("Timeslice Number");

		spinnerTSNumber = new Spinner(groupTime, SWT.BORDER);
		final GridData gd_spinnerTSNumber = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_spinnerTSNumber.widthHint = 100;
		spinnerTSNumber.setLayoutData(gd_spinnerTSNumber);
		spinnerTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerTSNumber.setMaximum(10000);
		spinnerTSNumber.setMinimum(1);
		spinnerTSNumber.addModifyListener(new ConfModificationListener());
		btnReset.addSelectionListener(new ResetListener());
		textTimestampEnd.addModifyListener(new ConfModificationListener());
		textTimestampStart.addModifyListener(new ConfModificationListener());
		scrolledComposite.setContent(groupTime);
		scrolledComposite.setMinSize(groupTime.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sashFormView.setWeights(new int[] {418, 36});

		final SashForm sashForm = new SashForm(sashForm_1, SWT.BORDER | SWT.VERTICAL);
		sashForm.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

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

		final Composite composite_1 = new Composite(groupTraces, SWT.NONE);
		final GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_1.minimumHeight = 20;
		gd_composite_1.widthHint = 285;
		composite_1.setLayoutData(gd_composite_1);
		composite_1.setLayout(new GridLayout(1, false));
		comboTraces = new Combo(composite_1, SWT.READ_ONLY);
		final GridData gd_comboTraces = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_comboTraces.widthHint = 179;
		comboTraces.setLayoutData(gd_comboTraces);
		comboTraces.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
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
		final GridData gd_compositeAggregationOperator = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_compositeAggregationOperator.minimumHeight = 20;
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
				for (final String op : ocelotlCore.getSpaceOperators().getOperators(ocelotlCore.getTimeOperators().getSelectedOperatorResource().getSpaceCompatibility())) {
					comboSpace.add(op);
				}

				// Since the operator are sorted by priority, set the default
				// choice to the first item
				if (comboSpace.getItems().length != 0) {
					comboSpace.setText(comboSpace.getItem(0));
					// Set the selected operator as operator in Ocelotl
					comboSpace.notifyListeners(SWT.Selection, new Event());
				}
				
				setDefaultDescriptionSettings();
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
		final GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.minimumHeight = 20;
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
			}
		});

		btnSettings2 = new Button(composite, SWT.NONE);
		btnSettings2.setText("Settings");
		btnSettings2.setFont(org.eclipse.wb.swt.SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		sashFormTSandCurve.setWeights(new int[] { 1, 1, 1 });
		btnSettings2.addSelectionListener(new Settings2SelectionAdapter(this));

		final TabItem tbtmAdvancedParameters = new TabItem(tabFolder, 0);
		tbtmAdvancedParameters.setText("Quality curves");

		final SashForm sashFormAdvancedParameters = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormAdvancedParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		tbtmAdvancedParameters.setControl(sashFormAdvancedParameters);

		final Group groupQualityCurveSettings = new Group(sashFormAdvancedParameters, SWT.NONE);
		groupQualityCurveSettings.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupQualityCurveSettings.setText("Quality Curve Settings");
		groupQualityCurveSettings.setLayout(new GridLayout(4, false));
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnNormalize = new Button(groupQualityCurveSettings, SWT.CHECK);
		btnNormalize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnNormalize.setSelection(false);
		btnNormalize.setText("Normalize Qualities");
		btnNormalize.addSelectionListener(new NormalizeSelectionAdapter());
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnGrowingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnGrowingQualities.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnGrowingQualities.setText("Complexity gain (green)\nInformation gain (red)");
		btnGrowingQualities.setSelection(true);
		btnGrowingQualities.addSelectionListener(new GrowingQualityRadioSelectionAdapter());
		btnGrowingQualities.setSelection(false);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnDecreasingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnDecreasingQualities.setText("Complexity reduction (green)\nInformation loss (red)");
		btnDecreasingQualities.setSelection(false);
		btnDecreasingQualities.setFont(org.eclipse.wb.swt.SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
				new Label(groupQualityCurveSettings, SWT.NONE);
				new Label(groupQualityCurveSettings, SWT.NONE);
						new Label(groupQualityCurveSettings, SWT.NONE);
				
						final Label lblThreshold = new Label(groupQualityCurveSettings, SWT.NONE);
						lblThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
						lblThreshold.setText("X Axis Maximal Precision");
						
								textThreshold = new Text(groupQualityCurveSettings, SWT.BORDER);
								GridData gd_textThreshold = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
								gd_textThreshold.widthHint = 89;
								textThreshold.setLayoutData(gd_textThreshold);
								textThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
								
										textThreshold.addModifyListener(new ThresholdModifyListener());
		btnDecreasingQualities.addSelectionListener(new DecreasingQualityRadioSelectionAdapter());
		sashFormAdvancedParameters.setWeights(new int[] { 1 });
		

		final Composite compositeQualityView = new Composite(sashForm, SWT.NONE);
		compositeQualityView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeQualityView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		qualityView.initDiagram(compositeQualityView);
		compositeQualityView.setLayout(new FillLayout(SWT.HORIZONTAL));

		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(sashForm, SWT.BORDER | SWT.H_SCROLL);
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);

		Group group = new Group(scrolledComposite_1, SWT.NONE);
		group.setLayout(new GridLayout(5, false));

		final Label lblParameter = new Label(group, SWT.NONE);
		lblParameter.setText("Parameter");
		lblParameter.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		textRun = new Text(group, SWT.BORDER);
		GridData gd_textRun = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_textRun.widthHint = 100;
		textRun.setLayoutData(gd_textRun);
		textRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		buttonDown = new Button(group, SWT.NONE);
		buttonDown.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		buttonDown.setText("<");

		buttonUp = new Button(group, SWT.NONE);
		buttonUp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		buttonUp.setText(">");
		btnRun = new Button(group, SWT.NONE);
		btnRun.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		btnRun.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/1366759976_white_tiger.png"));
		btnRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.BOLD));
		btnRun.setText("RUN!");
		
		btnRun.addSelectionListener(new GetAggregationAdapter());
		buttonUp.addSelectionListener(new ParameterUpAdapter());
		buttonDown.addSelectionListener(new ParameterDownAdapter());
		textRun.addModifyListener(new ParameterModifyListener());
		scrolledComposite_1.setContent(group);
		scrolledComposite_1.setMinSize(group.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sashForm.setWeights(new int[] { 148, 267, 35 });
		sashForm_1.setWeights(new int[] { 678, 222 });
		sashFormGlobal.setWeights(new int[] { 395 });

		final IActionBars actionBars = getViewSite().getActionBars();
		final IToolBarManager toolBar = actionBars.getToolBarManager();
		if (FramesocPartManager.getInstance().isFramesocPartExisting(FramesocViews.GANTT_CHART_VIEW_ID))
			toolBar.add(createGanttAction());
		if (FramesocPartManager.getInstance().isFramesocPartExisting(FramesocViews.EVENT_TABLE_VIEW_ID))
			toolBar.add(createTableAction());	

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
		ocelotlParameters.setNormalize(btnNormalize.getSelection());
		ocelotlParameters.setTimeSlicesNumber(spinnerTSNumber.getSelection());
		ocelotlParameters.setTimeAggOperator(comboTime.getText());
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

	//When receiving a notification, update the trace list
	@Override
	public void handle(String topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED) || topic.equals(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED) || topic.equals(FramesocBusTopic.TOPIC_UI_REFRESH_TRACES_NEEDED)) {
			refreshTraces();
		}
	}
	
	/**
	 * Set the default microdescription settings
	 */
	public void setDefaultDescriptionSettings() {
		hasChanged = HasChanged.ALL;

		// Init operator specific configuration
		ocelotlParameters.getTraceTypeConfig().init(confDataLoader);

		if (ocelotlParameters.getEventProducers().isEmpty())
			ocelotlParameters.getEventProducers().addAll(confDataLoader.getProducers());

		ocelotlParameters.setMaxEventProducers(OcelotlDefaultParameterConstants.EventProducersPerQuery);
	}
}