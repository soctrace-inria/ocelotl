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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
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
	
		private class SaveDataListener extends SelectionAdapter {
		
				@Override
				public void widgetSelected(final SelectionEvent e) {
					FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		
					// Display a warning if the selected file already exists
					dialog.setOverwrite(true);
					// Set a default file name
					dialog.setFileName(ocelotlParameters.getTrace().getAlias() + "_" + ocelotlParameters.getTrace().getId());
		
					String saveCachefile = dialog.open();
		
					if (saveCachefile != null) {
						ocelotlParameters.getDataCache().saveDataCacheTo(ocelotlParameters, saveCachefile);
					}
				}
			}
	
	private class ConfModificationListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			hasChanged = HasChanged.ALL;
			if (confDataLoader.getCurrentTrace() == null || textTimestampStart.getText().isEmpty() || textTimestampEnd.getText().isEmpty())
				return;

			boolean invalidStart = false, invalidEnd = false;

			if (Long.parseLong(textTimestampStart.getText()) >= Long.parseLong(textTimestampEnd.getText())) {
				invalidStart = true;
				invalidEnd = true;
			}

			if (Long.parseLong(textTimestampEnd.getText()) > confDataLoader.getMaxTimestamp() || Long.parseLong(textTimestampEnd.getText()) < confDataLoader.getMinTimestamp())
				invalidEnd = true;

			if (Long.parseLong(textTimestampStart.getText()) < confDataLoader.getMinTimestamp() || Long.parseLong(textTimestampStart.getText()) > confDataLoader.getMaxTimestamp())
				invalidStart = true;

			
			if (invalidStart)
				// Set font color to red
				textTimestampStart.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			else
				// Set font color to normal color
				textTimestampStart.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));

			if (invalidEnd)
				textTimestampEnd.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			else
				textTimestampEnd.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
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
			// Check that inputs are valid
			try {
				checkInputs();
			} catch (OcelotlException exception) {
				// If inputs are wrong, display the reason
				MessageDialog.openInformation(getSite().getShell(), "Error", exception.getMessage());
				return;
			}
					
			// Mutex zone
			synchronized (lock) {
				// If a job is already running
				if (running == true) {
					// reset the displayed value to the actual value to avoid
					// displaying a wrong value
					textRun.setText(Double.toString(ocelotlParameters.getParameter()));

					// and discard the new job
					return;
				}

				// else we are starting a job
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
					if (hasChanged != HasChanged.PARAMETER) {
						try {
							ocelotlCore.computeDichotomy(hasChanged);
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
			if(confDataLoader.getCurrentTrace() == null || comboSpace.getText().equals("") || comboTime.getText().equals(""))
				return;
			
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
	
	private class DeleteDataCache extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			// Ask user confirmation
			if(MessageDialog.openConfirm(getSite().getShell(), "Delete cached data", "This will delete all cached data. Do you want to continue ?"))
				ocelotlParameters.getDataCache().deleteCache();
		}
	}
	
	private class ModifyDatacacheDirectory extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			DirectoryDialog dialog = new DirectoryDialog(getSite().getShell());
		    String newCacheDir = dialog.open();
		    if(newCacheDir != null)
		    {
		    	ocelotlParameters.getDataCache().setCacheDirectory(newCacheDir);
		    	datacacheDirectory.setText(ocelotlParameters.getDataCache().getCacheDirectory());
		    }
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
	private Button						btnDeleteDataCache;
	private Text						datacacheDirectory;
	private Button						btnChangeCacheDirectory;
	
	/**
	 * Followed topics
	 */
	protected FramesocBusTopicList topics = null;
	private Text text;

	private Button	button_1;

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
					// Make sure we are not in an editable field
					if (!(e.widget.getClass().getSimpleName().equals("Text") || e.widget.getClass().getSimpleName().equals("Spinner")))
						buttonDown.notifyListeners(SWT.Selection, new Event());
					break;
				case SWT.ARROW_RIGHT:
					// Make sure we are not in an editable field
					if (!(e.widget.getClass().getSimpleName().equals("Text") || e.widget.getClass().getSimpleName().equals("Spinner")))
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
		groupTime.setLayout(new GridLayout(10, false));

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
		btnReset.setToolTipText("Reset Timestamps");
		btnReset.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/etool16/undo_edit.gif"));
		btnReset.setFont(SWTResourceManager.getFont("Cantarell", 7, SWT.NORMAL));

		final Label lblTSNumber = new Label(groupTime, SWT.NONE);
		lblTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblTSNumber.setText("Timeslice Number");

		spinnerTSNumber = new Spinner(groupTime, SWT.BORDER);
		final GridData gd_spinnerTSNumber = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_spinnerTSNumber.widthHint = 100;
		spinnerTSNumber.setLayoutData(gd_spinnerTSNumber);
		spinnerTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerTSNumber.setMaximum(OcelotlDefaultParameterConstants.maxTimeslice);
		spinnerTSNumber.setMinimum(OcelotlDefaultParameterConstants.minTimeslice);
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

		
		//Trace overview
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
		compositeAggregationOperator.setLayout(new GridLayout(3, false));
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

				// Since the operators are sorted by priority, set the default
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
				btnSettings.setToolTipText("Settings");
				btnSettings.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/management.png"));
				btnSettings.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
				btnSettings.addSelectionListener(new SettingsSelectionAdapter(this));
		
		button_1 = new Button(compositeAggregationOperator, SWT.NONE);
		button_1.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/etool16/save_edit.gif"));
		button_1.setToolTipText("Save Current Microscopic Description");
		button_1.setFont(SWTResourceManager.getFont("Cantarell", 7, SWT.NORMAL));
		button_1.addSelectionListener(new SaveDataListener());
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
		btnSettings2.setToolTipText("Settings");
		btnSettings2.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/management.png"));
		btnSettings2.setFont(org.eclipse.wb.swt.SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		sashFormTSandCurve.setWeights(new int[] { 1, 1, 1 });
		btnSettings2.addSelectionListener(new Settings2SelectionAdapter(this));

		
		//Quality curves settings
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
		btnNormalize.setSelection(true);
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
		gd_textThreshold.widthHint = 100;
		textThreshold.setLayoutData(gd_textThreshold);
		textThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		textThreshold.addModifyListener(new ThresholdModifyListener());
		btnDecreasingQualities.addSelectionListener(new DecreasingQualityRadioSelectionAdapter());
		sashFormAdvancedParameters.setWeights(new int[] { 1 });
		
		

		// Datacache settings
		final TabItem tbtmOcelotlSettings = new TabItem(tabFolder, SWT.NONE);
		tbtmOcelotlSettings.setText("Settings");
		
		final SashForm sashFormSettings = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormSettings.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		tbtmOcelotlSettings.setControl(sashFormSettings);

		final Group groupDataCacheSettings = new Group(sashFormSettings, SWT.NONE);
		groupDataCacheSettings.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupDataCacheSettings.setText("Data Cache Settings");
		groupDataCacheSettings.setLayout(new GridLayout(2, false));
		
		final Label lblDataCacheDirectory = new Label(groupDataCacheSettings, SWT.NONE);
		lblDataCacheDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDataCacheDirectory.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblDataCacheDirectory.setText("Data cache directory:");
		new Label(groupDataCacheSettings, SWT.NONE);
		
				final GridData gd_dataCacheDir = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gd_dataCacheDir.widthHint = 75;
				
				datacacheDirectory = new Text(groupDataCacheSettings, SWT.BORDER);
				datacacheDirectory.setLayoutData(gd_dataCacheDir);
				datacacheDirectory.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
				datacacheDirectory.setEditable(false);
				datacacheDirectory.setText(ocelotlParameters.getDataCache().getCacheDirectory());
		
		btnChangeCacheDirectory = new Button(groupDataCacheSettings, SWT.PUSH);
		btnChangeCacheDirectory.setToolTipText("Change Cache Directory");
		btnChangeCacheDirectory.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/fldr_obj.gif"));
		btnChangeCacheDirectory.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnChangeCacheDirectory.addSelectionListener(new ModifyDatacacheDirectory());
		
				final Label lblDataCacheSize = new Label(groupDataCacheSettings, SWT.NONE);
				lblDataCacheSize.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblDataCacheSize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
				lblDataCacheSize.setText("MB Data cache size (-1=unlimited):");
		
		text = new Text(groupDataCacheSettings, SWT.BORDER);
		text.setText("-1");
		text.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 100;
		text.setLayoutData(gd_text);
		
		btnDeleteDataCache = new Button(groupDataCacheSettings, SWT.PUSH);
		btnDeleteDataCache.setToolTipText("Empty Cache");
		btnDeleteDataCache.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/delete_obj.gif"));
		btnDeleteDataCache.setText("Empty Cache");
		btnDeleteDataCache.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnDeleteDataCache.addSelectionListener(new DeleteDataCache());
		new Label(groupDataCacheSettings, SWT.NONE);
		sashFormSettings.setWeights(new int[] {1});
		
		
		
		// Quality curves display
		final Composite compositeQualityView = new Composite(sashForm, SWT.BORDER);
		compositeQualityView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeQualityView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		qualityView.initDiagram(compositeQualityView);
		compositeQualityView.setLayout(new FillLayout(SWT.HORIZONTAL));

		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(sashForm, SWT.H_SCROLL);
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
		buttonDown.setToolTipText("Increase Parameter");
		buttonDown.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/elcl16/backward_nav.gif"));
		buttonDown.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		buttonUp = new Button(group, SWT.NONE);
		buttonUp.setToolTipText("Decrease Parameter");
		buttonUp.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/elcl16/forward_nav.gif"));
		buttonUp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRun = new Button(group, SWT.NONE);
		btnRun.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		btnRun.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/ocelotl16.png"));
		btnRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.BOLD));
		btnRun.setText("RUN!");
		
		btnRun.addSelectionListener(new GetAggregationAdapter());
		buttonUp.addSelectionListener(new ParameterUpAdapter());
		buttonDown.addSelectionListener(new ParameterDownAdapter());
		textRun.addModifyListener(new ParameterModifyListener());
		scrolledComposite_1.setContent(group);
		scrolledComposite_1.setMinSize(group.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sashForm.setWeights(new int[] {193, 214, 38});
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
		
		if (ocelotlParameters.getTypes().isEmpty())
			ocelotlParameters.getTypes().addAll(confDataLoader.getTypes());
	}
	
	/**
	 * Check that inputs are valid
	 * 
	 * @throws OcelotlException if one input is not valid
	 */
	public void checkInputs() throws OcelotlException {
		checkTrace();
		checkMicroscopicDescription();
		checkVisualization();
		checkTimeStamp();
	}

	/**
	 * Check that a trace was selected
	 * 
	 * @throws OcelotlException if no trace was selected
	 */
	public void checkTrace() throws OcelotlException {
		// If no trace is selected
		if (confDataLoader.getCurrentTrace() == null)
			throw new OcelotlException(OcelotlException.NOTRACE);
	}

	/**
	 * Check that a microscopic description was selected
	 * 
	 * @throws OcelotlException if no description was selected
	 */
	public void checkMicroscopicDescription() throws OcelotlException {
		// If no microscopic distribution is selected
		if (comboTime.getText().equals(""))
			throw new OcelotlException(OcelotlException.NOMICROSCOPICDESCRIPTION);
	}

	/**
	 * Check that visualization was selected
	 * 
	 * @throws OcelotlException if no visualization was selected
	 */
	public void checkVisualization() throws OcelotlException {
		// If no visualization is selected
		if (comboSpace.getText().equals(""))
			throw new OcelotlException(OcelotlException.NOVISUALIZATION);
	}

	/**
	 * Check that the timestamps are valid
	 * 
	 * @throws OcelotlException
	 *             if at least one of the timestamps is not valid
	 */
	public void checkTimeStamp() throws OcelotlException {
		// If the starting timestamp is greater than the ending one
		if (confDataLoader.getCurrentTrace() == null || textTimestampStart.getText().isEmpty() || textTimestampEnd.getText().isEmpty())
			throw new OcelotlException(OcelotlException.NOTIMESTAMP);

		if (Long.parseLong(textTimestampStart.getText()) >= Long.parseLong(textTimestampEnd.getText())) {
			// Reset to default values
			textTimestampEnd.setText(Long.toString(confDataLoader.getMaxTimestamp()));
			textTimestampStart.setText(String.valueOf(confDataLoader.getMinTimestamp()));
			throw new OcelotlException(OcelotlException.INVALIDTIMERANGE);
		}
		if (Long.parseLong(textTimestampEnd.getText()) > confDataLoader.getMaxTimestamp() || Long.parseLong(textTimestampEnd.getText()) < confDataLoader.getMinTimestamp()) {
			textTimestampEnd.setText(String.valueOf(confDataLoader.getMaxTimestamp()));
			throw new OcelotlException(OcelotlException.INVALID_END_TIMESTAMP);
		}

		if (Long.parseLong(textTimestampStart.getText()) < confDataLoader.getMinTimestamp() || Long.parseLong(textTimestampStart.getText()) > confDataLoader.getMaxTimestamp()) {
			textTimestampStart.setText(String.valueOf(confDataLoader.getMinTimestamp()));
			throw new OcelotlException(OcelotlException.INVALID_START_TIMESTAMP);
		}
	}
	
}