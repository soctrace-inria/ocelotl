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

import java.util.ArrayList;
import java.util.Date;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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

import fr.inria.lpaggreg.quality.DLPQuality;
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
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacachePolicy;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacacheStrategy;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.HasChanged;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.model.SimpleEventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlDefaultParameterConstants;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceStateManager;
import fr.inria.soctrace.tools.ocelotl.ui.Activator;
import fr.inria.soctrace.tools.ocelotl.ui.Snapshot;
import fr.inria.soctrace.tools.ocelotl.ui.loaders.ConfDataLoader;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.IAggregatedView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewWrapper;

/**
 * Main view for Ocelotl
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class OcelotlView extends ViewPart implements IFramesocBusListener {
	
	private class SaveDataListener extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null) {
				MessageDialog.openInformation(getSite().getShell(), "Error", OcelotlException.NO_TRACE);
				return;
			}

			FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);

			// Display a warning if the selected file already exists
			dialog.setOverwrite(true);
			
			Date date = new Date(System.currentTimeMillis());
			
			// Set a default file name
			dialog.setFileName(ocelotlParameters.getTrace().getAlias() + "_" + ocelotlParameters.getTrace().getId() + "_" + date);

			String saveCachefile = dialog.open();

			if (saveCachefile != null) {
				ocelotlParameters.getDataCache().saveDataCacheTo(ocelotlParameters, saveCachefile);
			}
		}
	}

	private class LoadDataListener extends SelectionAdapter {
		private Trace	trace;
		private String	loadCachefile;
		private int		traceID;

		@Override
		public void widgetSelected(final SelectionEvent e) {

			FileDialog dialog = new FileDialog(getSite().getShell(), SWT.OPEN);
			dialog.setFilterPath(ocelotlParameters.getDataCache().getCacheDirectory());
			loadCachefile = dialog.open();

			if (loadCachefile != null) {
				comboTime.removeAll();
				comboSpace.removeAll();

				final Job job = new Job("Loading trace from micro description") {

					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						monitor.beginTask("Gathering data from trace...", IProgressMonitor.UNKNOWN);
						try {
							traceID = ocelotlParameters.getDataCache().loadDataCache(loadCachefile, ocelotlParameters);
							trace = null;

							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {

									// Look for the correct trace among the
									// available traces
									for (int aTraceIndex : traceMap.keySet()) {
										if (traceMap.get(aTraceIndex).getId() == traceID) {
											comboTraces.select(aTraceIndex);
											trace = traceMap.get(comboTraces.getSelectionIndex());
											break;
										}
									}
								}
							});

							// If no trace was found
							if (trace == null)
								throw new OcelotlException(OcelotlException.INVALID_CACHED_TRACE);

							// Load the trace
							confDataLoader.load(trace);

							monitor.beginTask("Loading cached data...", IProgressMonitor.UNKNOWN);
							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {
									// Load the aggregation operators
									for (final String op : ocelotlCore.getAggregOperators().getOperators(confDataLoader.getCurrentTrace().getType().getName(), confDataLoader.getCategories())) {
										comboTime.add(op);
									}

									comboTime.setText("");

									// Search for the corresponding operator
									for (int i = 0; i < comboTime.getItemCount(); i++) {
										if (comboTime.getItem(i).equals(ocelotlParameters.getTimeAggOperator())) {
											comboTime.select(i);
											comboTime.notifyListeners(SWT.Selection, new Event());
											break;
										}
									}

									// If no operator was found
									if (comboTime.getText().isEmpty())
										try {
											throw new OcelotlException(OcelotlException.INVALID_CACHED_OPERATOR);
										} catch (OcelotlException e) {
											MessageDialog.openInformation(getSite().getShell(), "Error", e.getMessage());
											return;
										}

									// Set the corresponding parameters
									spinnerTSNumber.setSelection(ocelotlParameters.getTimeSlicesNumber());
									textTimestampStart.setText(String.valueOf(ocelotlParameters.getTimeRegion().getTimeStampStart()));
									textTimestampEnd.setText(String.valueOf(ocelotlParameters.getTimeRegion().getTimeStampEnd()));

									// And launch the display
									btnRun.notifyListeners(SWT.Selection, new Event());
								}
							});

						} catch (final OcelotlException exception) {
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									// If inputs are wrong, display the reason
									MessageDialog.openInformation(getSite().getShell(), "Error", exception.getMessage());
								}
							});
							return Status.CANCEL_STATUS;
						} catch (SoCTraceException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						return Status.OK_STATUS;
					}
				};

				job.setUser(true);
				job.schedule();
			}
		}
	}
	
	private class TakeSnapshotAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(confDataLoader.getCurrentTrace() == null || ocelotlParameters.getTrace() == null)
				return;
				
			snapshot.takeSnapShot();
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

		// Prevent the simultaneous execution of multiple threads leading to
		// random crashes
		private Object	lock	= new Object();
		// Global flag signaling that a job is already running
		private boolean	running	= false;

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

			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ || hasChanged == HasChanged.PARAMETER) {
				hasChanged = HasChanged.PARAMETER;
			} else {
				textRun.setText("1.0");
			}
			setConfiguration();
			final String title = "Computing Aggregated View";
			final Job job = new Job(title) {

				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					monitor.beginTask(title, 4);
					try {
						if (hasChanged != HasChanged.PARAMETER) {
							if (hasChanged == HasChanged.ALL) {
								if (monitor.isCanceled()) {
									synchronized (lock) {
										running = false;
									}
									return Status.CANCEL_STATUS;
								}
								monitor.setTaskName("Initializing Time Operator");
								ocelotlCore.initAggregOperator(monitor);
								monitor.worked(1);
							}
							if (hasChanged == HasChanged.ALL || hasChanged == HasChanged.NORMALIZE) {
								if (monitor.isCanceled()) {
									synchronized (lock) {
										running = false;
									}
									return Status.CANCEL_STATUS;
								}
								monitor.setTaskName("Compute Qualities");
								monitor.subTask("");
								ocelotlCore.computeQualities();
								monitor.worked(1);
							}
							if (hasChanged == HasChanged.ALL || hasChanged == HasChanged.NORMALIZE || hasChanged == HasChanged.THRESHOLD) {
								if (monitor.isCanceled()) {
									synchronized (lock) {
										running = false;
									}
									return Status.CANCEL_STATUS;
								}
								monitor.setTaskName("Compute Dichotomy");
								ocelotlCore.computeDichotomy();
								monitor.worked(1);
							}
							
							// Compute the parameter value
							ocelotlParameters.setParameter(computeInitialParameter());
						}

						hasChanged = HasChanged.PARAMETER;
						if (monitor.isCanceled()) {
							synchronized (lock) {
								running = false;
							}
							return Status.CANCEL_STATUS;
						}
						monitor.setTaskName("Compute Parts");
						// if (hasChanged == HasChanged.ALL || hasChanged ==
						// HasChanged.NORMALIZE || hasChanged ==
						// HasChanged.PARAMETER)

						ocelotlCore.computeParts();
						monitor.worked(1);

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
							textRun.setText(String.valueOf(getParams().getParameter()));
							qualityView.createDiagram();
							tabFolder.setSelection(1);
							overView.updateDiagram(ocelotlCore.getLpaggregManager(), ocelotlParameters.getTimeRegion());
							ocelotlParameters.setTimeSliceManager(new TimeSliceStateManager(ocelotlParameters.getTimeRegion(), ocelotlParameters.getTimeSlicesNumber()));
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

private class ComboTypeSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			hasChanged = HasChanged.ALL;
			ocelotlParameters.getEventProducers().clear();
	
			// Get the available aggregation operators
			comboTime.removeAll();
			comboSpace.removeAll();
			
			for (final String op : ocelotlCore.getAggregOperators().getOperators(confDataLoader.getCurrentTrace().getType().getName(), confDataLoader.getCategories())) {
				comboTime.add(op);
			}
			
			comboTime.setText("");

		}
	}
	
	private class ComboTimeSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			hasChanged = HasChanged.ALL;
			ocelotlParameters.getEventProducers().clear();
			ocelotlCore.getMicromodelTypes().setSelectedMicroModel(comboType.getText());
			ocelotlCore.getAggregOperators().setSelectedOperator(comboTime.getText());
			// Set a list of all the events
			spinnerTSNumber.setSelection(ocelotlCore.getAggregOperators().getSelectedOperatorResource().getTs());
			
			// Get the available visualizations
			comboSpace.removeAll();
			for (final String op : ocelotlCore.getVisuOperators().getOperators(ocelotlCore.getAggregOperators().getSelectedOperatorResource().getSpaceCompatibility())) {
				comboSpace.add(op);
			}

			// Since the operators are sorted by priority, set the default
			// choice to the first item
			if (comboSpace.getItems().length != 0) {
				comboSpace.setText(comboSpace.getItem(0));
				// Set the selected operator as operator in Ocelotl
				comboSpace.notifyListeners(SWT.Selection, new Event());
			}

			// Set default settings
			setDefaultDescriptionSettings();
		}
	}

	private class ComboSpaceSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			if (hasChanged == HasChanged.NOTHING)
				hasChanged = HasChanged.PARAMETER;
			ocelotlCore.getVisuOperators().setSelectedOperator(comboSpace.getText());
			timeLineView = timeLineViewManager.create();
			timeLineViewWrapper.setView(timeLineView);
			overView.setTimeLineView((AggregatedView)timeLineViewManager.create("Simple Mode"));
			overView.setSelectedOperator("Simple Mode");
		}
	}
	
	@SuppressWarnings("unused")
	private class OcelotlKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(final KeyEvent e) {
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
	
	private class EnableCacheListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			ocelotlParameters.getDataCache().setCacheActive(btnCacheEnabled.getSelection());
			boolean cacheActivation = ocelotlParameters.getDataCache().isCacheActive();
				btnDeleteDataCache.setEnabled(cacheActivation);
				datacacheDirectory.setEnabled(cacheActivation);
				btnChangeCacheDirectory.setEnabled(cacheActivation);
				btnRadioButton.setEnabled(cacheActivation);
				btnRadioButton_1.setEnabled(cacheActivation);
				btnRadioButton_2.setEnabled(cacheActivation);
				btnRadioButton_3.setEnabled(cacheActivation);
				cacheTimeSliceValue.setEnabled(cacheActivation);
				dataCacheSize.setEnabled(cacheActivation);		
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

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (comboTime.getText().equals(""))
				return;
			
			hasChanged = HasChanged.ALL;
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
		    	//Update the current datacache path
		    	ocelotlParameters.getDataCache().setCacheDirectory(newCacheDir);
		    	//Update the displayed path
		    	datacacheDirectory.setText(ocelotlParameters.getDataCache().getCacheDirectory());
		    }
		}
	}

	private class DataCacheSizeListener implements ModifyListener {
	
		@Override
		public void modifyText(final ModifyEvent e) {
			try {
				if (Integer.valueOf(dataCacheSize.getText()) < 0) {
					ocelotlParameters.getDataCache().setCacheMaxSize(-1);
				} else {
					// Set the cache size at the entered value converted from
					// Megabytes to bytes
					ocelotlParameters.getDataCache().setCacheMaxSize(Long.valueOf(dataCacheSize.getText()) * 1000000);
				}
			} catch (final NumberFormatException err) {
				dataCacheSize.setSelection((int) ocelotlParameters.getDataCache().getCacheMaxSize());
			} catch (OcelotlException e1) {
				MessageDialog.openInformation(getSite().getShell(), "Error", e1.getMessage());
			}
		}
	}

	private class CacheTimeSliceListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			ocelotlParameters.getOcelotlSettings().setCacheTimeSliceNumber(Integer.valueOf(cacheTimeSliceValue.getText()));
		}
	}
	
	private class ThreadNumberListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			ocelotlParameters.getOcelotlSettings().setNumberOfThread(Integer.valueOf(spinnerThread.getText()));
		}
	}
	
	private class MaxEventProducerListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			ocelotlParameters.getOcelotlSettings().setMaxEventProducersPerQuery(Integer.valueOf(spinnerDivideDbQuery.getText()));
		}
	}
	
	private class EventPerThreadListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			ocelotlParameters.getOcelotlSettings().setEventsPerThread(Integer.valueOf(spinnerEventSize.getText()));
		}
	}
	
	private class cachePolicyListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (btnRadioButton.getSelection()) {
				ocelotlParameters.getOcelotlSettings().setCachePolicy(DatacachePolicy.CACHEPOLICY_SLOW);
			}
			if (btnRadioButton_1.getSelection()) {
				ocelotlParameters.getOcelotlSettings().setCachePolicy(DatacachePolicy.CACHEPOLICY_FAST);
			}
			if (btnRadioButton_2.getSelection()) {
				ocelotlParameters.getOcelotlSettings().setCachePolicy(DatacachePolicy.CACHEPOLICY_ASK);
			}
			if (btnRadioButton_3.getSelection()) {
				ocelotlParameters.getOcelotlSettings().setCachePolicy(DatacachePolicy.CACHEPOLICY_AUTO);
			}
		}
	}
	
	private class TraceAdapter extends SelectionAdapter {
		private Trace	trace;

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final String title = "Loading Trace";
comboType.removeAll();
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
							ocelotlParameters.setEventProducerHierarchy(new SimpleEventProducerHierarchy(confDataLoader.getProducers()));
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
								for (final String type : ocelotlCore.getMicromodelTypes().getTypes(confDataLoader.getCurrentTrace().getType().getName(), confDataLoader.getCategories()))
								{
									comboType.add(type);
								}
								
								comboType.setText("");
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
	private Combo						comboType;
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
	private Button 						btnCacheEnabled;
	private Snapshot							snapshot;
	private Button								btnRadioButton, btnRadioButton_1, btnRadioButton_2, btnRadioButton_3;
	private HashMap<DatacachePolicy, Button>	cachepolicy		= new HashMap<DatacachePolicy, Button>();
	private Spinner								cacheTimeSliceValue;
	private Font								cantarell8;
	private Overview							overView;
	private TabFolder							tabFolder;

	/**
	 * Followed topics
	 */
	protected FramesocBusTopicList topics = null;
	private Spinner dataCacheSize;
	private Button	btnSaveDataCache;
	private ConfigViewManager	manager;

	private Spinner	spinnerEventSize;

	private Spinner	spinnerDivideDbQuery;

	private Spinner	spinnerThread;

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

		try {
			ocelotlParameters.getDataCache().setSettings(ocelotlParameters.getOcelotlSettings());
		} catch (final OcelotlException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}

		snapshot = new Snapshot(ocelotlParameters.getOcelotlSettings().getSnapShotDirectory(), this);

		// Register update to synchronize traces
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
		overView = new Overview(this);

		final SashForm sashForm_1 = new SashForm(sashFormGlobal, SWT.BORDER);
		sashForm_1.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		sashFormView = new SashForm(sashForm_1, SWT.BORDER | SWT.VERTICAL);
		sashFormView.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		final SashForm sashForm_4 = new SashForm(sashFormView, SWT.BORDER | SWT.VERTICAL);
		compositeMatrixView = new Composite(sashForm_4, SWT.BORDER);
		compositeMatrixView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		cantarell8 = new Font(compositeMatrixView.getDisplay(), new FontData("Cantarell", 8, SWT.NORMAL));
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
		groupTime.setFont(cantarell8);
		groupTime.setLayout(new GridLayout(11, false));

		final Label lblStartTimestamp = new Label(groupTime, SWT.NONE);
		lblStartTimestamp.setFont(cantarell8);
		lblStartTimestamp.setText("Start");

		textTimestampStart = new Text(groupTime, SWT.BORDER);
		final GridData gd_textTimestampStart = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_textTimestampStart.widthHint = 150;
		textTimestampStart.setLayoutData(gd_textTimestampStart);
		textTimestampStart.setFont(cantarell8);

		final Label lblEndTimestamp = new Label(groupTime, SWT.NONE);
		lblEndTimestamp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblEndTimestamp.setFont(cantarell8);
		lblEndTimestamp.setText("End");

		textTimestampEnd = new Text(groupTime, SWT.BORDER);
		final GridData gd_textTimestampEnd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_textTimestampEnd.widthHint = 150;
		textTimestampEnd.setLayoutData(gd_textTimestampEnd);
		textTimestampEnd.setFont(cantarell8);

		btnReset = new Button(groupTime, SWT.NONE);
		btnReset.setToolTipText("Reset Timestamps");
		btnReset.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/etool16/undo_edit.gif"));

		final Label lblTSNumber = new Label(groupTime, SWT.NONE);
		lblTSNumber.setFont(cantarell8);
		lblTSNumber.setText("Timeslice Number");

		spinnerTSNumber = new Spinner(groupTime, SWT.BORDER);
		final GridData gd_spinnerTSNumber = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_spinnerTSNumber.widthHint = 100;
		spinnerTSNumber.setLayoutData(gd_spinnerTSNumber);
		spinnerTSNumber.setFont(cantarell8);
		spinnerTSNumber.setMaximum(OcelotlDefaultParameterConstants.maxTimeslice);
		spinnerTSNumber.setMinimum(OcelotlDefaultParameterConstants.minTimeslice);
		
		Button btnTakeSnapshot = new Button(groupTime, SWT.NONE);
		btnTakeSnapshot.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/snapshot-icon.png"));
		btnTakeSnapshot.setFont(cantarell8);
		btnTakeSnapshot.setToolTipText("Take a snapshot of the current view.");
		btnTakeSnapshot.addSelectionListener(new TakeSnapshotAdapter());
		spinnerTSNumber.addModifyListener(new ConfModificationListener());
		btnReset.addSelectionListener(new ResetListener());
		textTimestampEnd.addModifyListener(new ConfModificationListener());
		textTimestampStart.addModifyListener(new ConfModificationListener());
		scrolledComposite.setContent(groupTime);
		scrolledComposite.setMinSize(groupTime.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sashFormView.setWeights(new int[] {418, 36});

		final SashForm sashForm = new SashForm(sashForm_1, SWT.BORDER | SWT.VERTICAL);
		sashForm.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		tabFolder = new TabFolder(sashForm, SWT.NONE);
		tabFolder.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		
		//Trace overview
		final TabItem tbtmTimeAggregationParameters = new TabItem(tabFolder, SWT.NONE);
		tbtmTimeAggregationParameters.setText("Trace Overview");

		final SashForm sashFormTSandCurve = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmTimeAggregationParameters.setControl(sashFormTSandCurve);
		sashFormTSandCurve.setFont(cantarell8);

		final Group groupTraces = new Group(sashFormTSandCurve, SWT.NONE);
		groupTraces.setFont(cantarell8);
		groupTraces.setText("Trace");
		groupTraces.setLayout(new GridLayout(1, false));

		final Composite composite_1 = new Composite(groupTraces, SWT.NONE);
		final GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_1.minimumHeight = 20;
		gd_composite_1.widthHint = 285;
		composite_1.setLayoutData(gd_composite_1);
		composite_1.setLayout(new GridLayout(2, false));
		comboTraces = new Combo(composite_1, SWT.READ_ONLY);
		final GridData gd_comboTraces = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_comboTraces.widthHint = 179;
		comboTraces.setLayoutData(gd_comboTraces);
		comboTraces.setFont(cantarell8);
		
		Button btnLoadDataCache = new Button(composite_1, SWT.NONE);
		btnLoadDataCache.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/etool16/import_wiz.gif"));
		btnLoadDataCache.setToolTipText("Load a Microscopic Description");
		btnLoadDataCache.setFont(SWTResourceManager.getFont("Cantarell", 7, SWT.NORMAL));
		btnLoadDataCache.addSelectionListener(new LoadDataListener());
		
		comboTraces.addSelectionListener(new TraceAdapter());
		int index = 0;
		// Fill with the available traces
		for (final Trace t : confDataLoader.getTraces()) {
			comboTraces.add(t.getAlias(), index);
			traceMap.put(index, t);
			index++;
		}

	
		final Group groupTypes = new Group(sashFormTSandCurve, SWT.NONE);
		groupTypes.setFont(cantarell8);
		groupTypes.setText("Metrics");
		groupTypes.setLayout(new GridLayout(1, false));
		
		final Composite compositeType = new Composite(groupTypes, SWT.NONE);
		compositeType.setFont(cantarell8);
		compositeType.setLayout(new GridLayout(1, false));
		
		comboType = new Combo(compositeType, SWT.READ_ONLY);
		final GridData gd_comboType = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_comboType.widthHint = 170;
		comboType.setLayoutData(gd_comboType);
		comboType.setFont(cantarell8);
		comboType.addSelectionListener(new ComboTypeSelectionAdapter());
		comboType.setText("");
		
		final Group groupAggregationOperator = new Group(sashFormTSandCurve, SWT.NONE);
		groupAggregationOperator.setFont(cantarell8);
		groupAggregationOperator.setText("Analysis Type");
		groupAggregationOperator.setLayout(new GridLayout(1, false));

		final Composite compositeAggregationOperator = new Composite(groupAggregationOperator, SWT.NONE);
		compositeAggregationOperator.setFont(cantarell8);
		compositeAggregationOperator.setLayout(new GridLayout(3, false));
		final GridData gd_compositeAggregationOperator = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_compositeAggregationOperator.minimumHeight = 20;
		gd_compositeAggregationOperator.widthHint = 85;
		compositeAggregationOperator.setLayoutData(gd_compositeAggregationOperator);

		comboTime = new Combo(compositeAggregationOperator, SWT.READ_ONLY);
		final GridData gd_comboAggregationOperator = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_comboAggregationOperator.widthHint = 170;
		comboTime.setLayoutData(gd_comboAggregationOperator);
		comboTime.setFont(cantarell8);
		comboTime.addSelectionListener(new ComboTimeSelectionAdapter());
		comboTime.setText("");
		
				btnSettings = new Button(compositeAggregationOperator, SWT.NONE);
				btnSettings.setToolTipText("Settings");
				btnSettings.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/management.png"));
				btnSettings.setFont(cantarell8);
				btnSettings.addSelectionListener(new SettingsSelectionAdapter());
		
		btnSaveDataCache = new Button(compositeAggregationOperator, SWT.NONE);
		btnSaveDataCache.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/etool16/save_edit.gif"));
		btnSaveDataCache.setToolTipText("Save Current Microscopic Description");
		btnSaveDataCache.setFont(SWTResourceManager.getFont("Cantarell", 7, SWT.NORMAL));
		btnSaveDataCache.addSelectionListener(new SaveDataListener());

		final Group grpSpaceAggregationOperator = new Group(sashFormTSandCurve, SWT.NONE);
		grpSpaceAggregationOperator.setText("Visualization");
		grpSpaceAggregationOperator.setFont(cantarell8);
		grpSpaceAggregationOperator.setLayout(new GridLayout(1, false));

		final Composite composite = new Composite(grpSpaceAggregationOperator, SWT.NONE);
		final GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.minimumHeight = 20;
		gd_composite.widthHint = 85;
		composite.setLayoutData(gd_composite);
		composite.setFont(cantarell8);
		composite.setLayout(new GridLayout(2, false));

		comboSpace = new Combo(composite, SWT.READ_ONLY);
		comboSpace.setFont(cantarell8);
		final GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_combo.widthHint = 170;
		comboSpace.setLayoutData(gd_combo);
		comboSpace.setText("");
		comboSpace.addSelectionListener(new ComboSpaceSelectionAdapter());

		btnSettings2 = new Button(composite, SWT.NONE);
		btnSettings2.setToolTipText("Settings");
		btnSettings2.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/management.png"));
		btnSettings2.setFont(cantarell8);
		sashFormTSandCurve.setWeights(new int[] { 1, 1, 1, 1 });
		btnSettings2.addSelectionListener(new Settings2SelectionAdapter(this));

		
		//Overview
		final TabItem tbtmOverview = new TabItem(tabFolder, SWT.NONE);
		tbtmOverview.setText("Overview");
	
		
		SashForm overviewSashForm = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmOverview.setControl(overviewSashForm);
		final Composite compositeOverview = new Composite(overviewSashForm, SWT.BORDER);
		compositeOverview.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeOverview.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		compositeOverview.setLayout(new FillLayout(SWT.HORIZONTAL));
		overView.init(compositeOverview);

		//Quality curves settings
		final TabItem tbtmAdvancedParameters = new TabItem(tabFolder, 0);
		tbtmAdvancedParameters.setText("Quality curves");

		final SashForm sashFormAdvancedParameters = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormAdvancedParameters.setFont(cantarell8);
		tbtmAdvancedParameters.setControl(sashFormAdvancedParameters);

		final Group groupQualityCurveSettings = new Group(sashFormAdvancedParameters, SWT.NONE);
		groupQualityCurveSettings.setFont(cantarell8);
		groupQualityCurveSettings.setText("Quality Curve Settings");
		groupQualityCurveSettings.setLayout(new GridLayout(4, false));
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnNormalize = new Button(groupQualityCurveSettings, SWT.CHECK);
		btnNormalize.setFont(cantarell8);
		btnNormalize.setSelection(true);
		btnNormalize.setText("Normalize Qualities");
		btnNormalize.addSelectionListener(new NormalizeSelectionAdapter());
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnGrowingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnGrowingQualities.setFont(cantarell8);
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
		btnDecreasingQualities.setFont(cantarell8);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		final Label lblThreshold = new Label(groupQualityCurveSettings, SWT.NONE);
		lblThreshold.setFont(cantarell8);
		lblThreshold.setText("X Axis Maximal Precision");

		textThreshold = new Text(groupQualityCurveSettings, SWT.BORDER);
		GridData gd_textThreshold = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textThreshold.widthHint = 100;
		textThreshold.setLayoutData(gd_textThreshold);
		textThreshold.setFont(cantarell8);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		textThreshold.addModifyListener(new ThresholdModifyListener());
		btnDecreasingQualities.addSelectionListener(new DecreasingQualityRadioSelectionAdapter());
		sashFormAdvancedParameters.setWeights(new int[] { 1 });
		
		// Datacache settings
		final TabItem tbtmOcelotlSettings = new TabItem(tabFolder, SWT.NONE);
		tbtmOcelotlSettings.setText("Cache");
		
		final SashForm sashFormSettings = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormSettings.setFont(cantarell8);
		tbtmOcelotlSettings.setControl(sashFormSettings);

		final Group groupDataCacheSettings = new Group(sashFormSettings, SWT.NONE);
		groupDataCacheSettings.setFont(cantarell8);
		groupDataCacheSettings.setText("Data Cache Settings");
		groupDataCacheSettings.setLayout(new GridLayout(3, false));
		
		btnCacheEnabled = new Button(groupDataCacheSettings, SWT.CHECK);
		btnCacheEnabled.setFont(cantarell8);
		btnCacheEnabled.setText("Cache Enabled");
		btnCacheEnabled.setSelection(ocelotlParameters.getOcelotlSettings().isCacheActivated());
		btnCacheEnabled.addSelectionListener(new EnableCacheListener());
		
		btnDeleteDataCache = new Button(groupDataCacheSettings, SWT.PUSH);
		btnDeleteDataCache.setToolTipText("Empty Cache");
		btnDeleteDataCache.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/delete_obj.gif"));
		btnDeleteDataCache.setText("Empty Cache");
		btnDeleteDataCache.setFont(cantarell8);
		btnDeleteDataCache.addSelectionListener(new DeleteDataCache());
		new Label(groupDataCacheSettings, SWT.NONE);
				
				final Label lblDataCacheDirectory = new Label(groupDataCacheSettings, SWT.NONE);
				lblDataCacheDirectory.setFont(cantarell8);
				lblDataCacheDirectory.setText("Data cache directory:");
				
						final GridData gd_dataCacheDir = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
						gd_dataCacheDir.widthHint = 100;
						
						datacacheDirectory = new Text(groupDataCacheSettings, SWT.BORDER);
						datacacheDirectory.setLayoutData(gd_dataCacheDir);
						datacacheDirectory.setFont(cantarell8);
						datacacheDirectory.setEditable(false);
						datacacheDirectory.setText(ocelotlParameters.getDataCache().getCacheDirectory());
				
				btnChangeCacheDirectory = new Button(groupDataCacheSettings, SWT.PUSH);
				btnChangeCacheDirectory.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				btnChangeCacheDirectory.setToolTipText("Change Cache Directory");
				btnChangeCacheDirectory.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/fldr_obj.gif"));
				btnChangeCacheDirectory.setFont(cantarell8);
				btnChangeCacheDirectory.addSelectionListener(new ModifyDatacacheDirectory());
		
				final Label lblDataCacheSize = new Label(groupDataCacheSettings, SWT.NONE);
				lblDataCacheSize.setFont(cantarell8);
				lblDataCacheSize.setText("MB Data cache size (-1=unlimited):");
		
		dataCacheSize = new Spinner(groupDataCacheSettings, SWT.BORDER);
		dataCacheSize.setValues(0, -1, 99999999, 0, 1, 10);
		dataCacheSize.setFont(cantarell8);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 100;
		dataCacheSize.setLayoutData(gd_text);
		dataCacheSize.addModifyListener(new DataCacheSizeListener());
		new Label(groupDataCacheSettings, SWT.NONE);
		
		Label lblCacheTimeSlices = new Label(groupDataCacheSettings, SWT.NONE);
		lblCacheTimeSlices.setText("Cache time slices:");
		lblCacheTimeSlices.setFont(cantarell8);
		lblCacheTimeSlices.setToolTipText("Number of time slices used when generating cache");

		cacheTimeSliceValue = new Spinner(groupDataCacheSettings, SWT.BORDER);
		cacheTimeSliceValue.setValues(0, 0, 99999999, 0, 1, 10);
		cacheTimeSliceValue.setFont(cantarell8);
		GridData gd_cacheTimeSliceValue = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_cacheTimeSliceValue.widthHint = 100;
		cacheTimeSliceValue.setLayoutData(gd_cacheTimeSliceValue);
		cacheTimeSliceValue.setSelection(ocelotlParameters.getOcelotlSettings().getCacheTimeSliceNumber());
		cacheTimeSliceValue.addModifyListener(new CacheTimeSliceListener());
		new Label(groupDataCacheSettings, SWT.NONE);
		
		Label lblCachePolicy = new Label(groupDataCacheSettings, SWT.NONE);
		lblCachePolicy.setText("Cache policy");
		lblCachePolicy.setFont(cantarell8);
		new Label(groupDataCacheSettings, SWT.NONE);
		new Label(groupDataCacheSettings, SWT.NONE);
		
		btnRadioButton = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton.addSelectionListener(new cachePolicyListener());
		btnRadioButton.setText("Precise (slow)");
		btnRadioButton.setFont(cantarell8);
		
		btnRadioButton_1 = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton_1.addSelectionListener(new cachePolicyListener());
		btnRadioButton_1.setText("Fast");
		btnRadioButton_1.setFont(cantarell8);
		new Label(groupDataCacheSettings, SWT.NONE);
		
		btnRadioButton_2 = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton_2.addSelectionListener(new cachePolicyListener());
		btnRadioButton_2.setText("Ask me");
		btnRadioButton_2.setFont(cantarell8);
		
		btnRadioButton_3 = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton_3.addSelectionListener(new cachePolicyListener());
		btnRadioButton_3.setText("Auto");
		btnRadioButton_3.setFont(cantarell8);
		new Label(groupDataCacheSettings, SWT.NONE);
		
		cachepolicy.put(DatacachePolicy.CACHEPOLICY_SLOW, btnRadioButton);
		cachepolicy.put(DatacachePolicy.CACHEPOLICY_FAST, btnRadioButton_1);
		cachepolicy.put(DatacachePolicy.CACHEPOLICY_ASK, btnRadioButton_2);
		cachepolicy.put(DatacachePolicy.CACHEPOLICY_AUTO, btnRadioButton_3);
		
		cachepolicy.get(ocelotlParameters.getOcelotlSettings().getCachePolicy()).setSelection(true);
		
		if (ocelotlParameters.getOcelotlSettings().getCacheSize() > 0) {
			dataCacheSize.setSelection((int) (ocelotlParameters.getOcelotlSettings().getCacheSize() / 1000000));
		} else {
			dataCacheSize.setSelection((int) ocelotlParameters.getOcelotlSettings().getCacheSize());
		}
		sashFormSettings.setWeights(new int[] {1});	
		
		
		//Thread settings
		final TabItem tbtmAdvancedSettings = new TabItem(tabFolder, SWT.NONE);
		tbtmAdvancedSettings.setText("Advanced");
	
		SashForm advancedSettingsSashForm = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmAdvancedSettings.setControl(advancedSettingsSashForm);
		final Group grpCacheManagement = new Group(advancedSettingsSashForm, SWT.NONE);
		grpCacheManagement.setFont(cantarell8);
		grpCacheManagement.setText("Iterator Management");
		grpCacheManagement.setLayout(new GridLayout(2, false));

		final Label lblPageSize = new Label(grpCacheManagement, SWT.NONE);
		lblPageSize.setFont(cantarell8);
		lblPageSize.setText("Event Number Retrieved by Threads");

		spinnerEventSize = new Spinner(grpCacheManagement, SWT.BORDER);
		spinnerEventSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		spinnerEventSize.setFont(cantarell8);
		spinnerEventSize.setMinimum(OcelotlDefaultParameterConstants.MIN_EVENTS_PER_THREAD);
		spinnerEventSize.setMaximum(OcelotlDefaultParameterConstants.MAX_EVENTS_PER_THREAD);
		spinnerEventSize.setSelection(ocelotlParameters.getOcelotlSettings().getEventsPerThread());
		spinnerEventSize.addModifyListener(new EventPerThreadListener());

		final Group grpDivideDbQuery = new Group(advancedSettingsSashForm, SWT.NONE);
		grpDivideDbQuery.setFont(cantarell8);
		grpDivideDbQuery.setText("Query Management");
		grpDivideDbQuery.setLayout(new GridLayout(2, false));

		final Label lblDivideDbQueries = new Label(grpDivideDbQuery, SWT.NONE);
		lblDivideDbQueries.setFont(cantarell8);
		lblDivideDbQueries.setText("Event Producers per Query (0=All)");

		spinnerDivideDbQuery = new Spinner(grpDivideDbQuery, SWT.BORDER);
		spinnerDivideDbQuery.setFont(cantarell8);
		spinnerDivideDbQuery.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		spinnerDivideDbQuery.setMinimum(OcelotlDefaultParameterConstants.MIN_EVENT_PRODUCERS_PER_QUERY);
		spinnerDivideDbQuery.setMaximum(OcelotlDefaultParameterConstants.MAX_EVENT_PRODUCERS_PER_QUERY);
		spinnerDivideDbQuery.setSelection(ocelotlParameters.getOcelotlSettings().getMaxEventProducersPerQuery());
		spinnerDivideDbQuery.addModifyListener(new MaxEventProducerListener());
		
		final Group grpMultiThread = new Group(advancedSettingsSashForm, SWT.NONE);
		grpMultiThread.setFont(cantarell8);
		grpMultiThread.setText("Multi Threading");
		grpMultiThread.setLayout(new GridLayout(2, false));

		final Label lblThread = new Label(grpMultiThread, SWT.NONE);
		lblThread.setFont(cantarell8);
		lblThread.setText("Working Threads");

		spinnerThread = new Spinner(grpMultiThread, SWT.BORDER);
		spinnerThread.setFont(cantarell8);
		spinnerThread.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		spinnerThread.setMinimum(OcelotlDefaultParameterConstants.MIN_NUMBER_OF_THREAD);
		spinnerThread.setMaximum(OcelotlDefaultParameterConstants.MAX_NUMBER_OF_THREAD);
		spinnerThread.setSelection(ocelotlParameters.getOcelotlSettings().getNumberOfThread());
		spinnerThread.addModifyListener(new ThreadNumberListener());
		advancedSettingsSashForm.setWeights(new int[] { 1, 1, 1 });
			
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
		lblParameter.setFont(cantarell8);

		textRun = new Text(group, SWT.BORDER);
		GridData gd_textRun = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textRun.widthHint = 100;
		textRun.setLayoutData(gd_textRun);
		textRun.setFont(cantarell8);

		buttonDown = new Button(group, SWT.NONE);
		buttonDown.setToolTipText("Increase Parameter");
		buttonDown.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/elcl16/backward_nav.gif"));
		buttonDown.setFont(cantarell8);

		buttonUp = new Button(group, SWT.NONE);
		buttonUp.setToolTipText("Decrease Parameter");
		buttonUp.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/elcl16/forward_nav.gif"));
		buttonUp.setFont(cantarell8);
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
		sashForm.setWeights(new int[] {190, 221, 34});
		sashForm_1.setWeights(new int[] {635, 265});
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
	

	public IAggregatedView getTimeLineView() {
		return timeLineView;
	}
	
	public QualityView getQualityView() {
		return qualityView;
	}

	public int getTimeSliceNumber() {
		return spinnerTSNumber.getSelection();
	}

	public void setComboTime(Combo comboTime) {
		this.comboTime = comboTime;
	}

	public TimeRegion getTimeRegion() {
		return new TimeRegion(Long.parseLong(textTimestampStart.getText()), Long.parseLong(textTimestampEnd.getText()));
	}

	public Overview getOverView() {
		return overView;
	}

	public OcelotlCore getOcelotlCore() {
		return ocelotlCore;
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

		ocelotlParameters.getDataCache().buildDictionary(confDataLoader.getTraces());	
	}

	public void setComboAggregationOperator(final Combo comboAggregationOperator) {
		comboTime = comboAggregationOperator;
	}

	public void setConfiguration() {
		ocelotlParameters.setTrace(confDataLoader.getCurrentTrace());
		ocelotlParameters.setNormalize(btnNormalize.getSelection());
		ocelotlParameters.setTimeSlicesNumber(spinnerTSNumber.getSelection());
	ocelotlParameters.setMicroModelType(comboType.getText());
		ocelotlParameters.setTimeAggOperator(comboTime.getText());
		ocelotlParameters.setSpaceAggOperator(comboSpace.getText());
		
		ocelotlParameters.setEventsPerThread(spinnerEventSize.getSelection());
		ocelotlParameters.setThreadNumber(spinnerThread.getSelection());
		ocelotlParameters.setMaxEventProducers(spinnerDivideDbQuery.getSelection());
		
		setCachePolicy();
		try {
			ocelotlParameters.setThreshold(Double.valueOf(textThreshold.getText()).floatValue());
			ocelotlParameters.setParameter(Double.valueOf(textRun.getText()).floatValue());
			ocelotlParameters.setTimeRegion(new TimeRegion(Long.valueOf(textTimestampStart.getText()), Long.valueOf(textTimestampEnd.getText())));
			// Set a list of all the events
		} catch (final NumberFormatException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
	}

	/**
	 * Set the cache strategy depending on the selected policy
	 * TODO Take the operator into account
	 */
	public void setCachePolicy() {
		if (hasChanged != HasChanged.ALL)
			return;

		switch (ocelotlParameters.getOcelotlSettings().getCachePolicy()) {
		case CACHEPOLICY_FAST:
			ocelotlParameters.getDataCache().setBuildingStrategy(DatacacheStrategy.DATACACHE_PROPORTIONAL);
			break;

		case CACHEPOLICY_SLOW:
			ocelotlParameters.getDataCache().setBuildingStrategy(DatacacheStrategy.DATACACHE_DATABASE);
			break;

		case CACHEPOLICY_ASK:
			String[] dialogButtonLabels = { "Precise", "Fast", "Automatic" };
			MessageDialog choosePolicy = new MessageDialog(getSite().getShell(), "Choose a cache policy", null, "Please choose one of the following methods for cache rebuilding:", MessageDialog.NONE, dialogButtonLabels, 0);
			int choice = choosePolicy.open();

			if (choice == 0) {
				ocelotlParameters.getDataCache().setBuildingStrategy(DatacacheStrategy.DATACACHE_DATABASE);
				break;
			} else if (choice == 1) {
				ocelotlParameters.getDataCache().setBuildingStrategy(DatacacheStrategy.DATACACHE_PROPORTIONAL);
				break;
			}

		case CACHEPOLICY_AUTO:
			// TODO implement auto (decision taken when computing ratio)
			ocelotlParameters.getDataCache().setBuildingStrategy(DatacacheStrategy.DATACACHE_PROPORTIONAL);
			break;

		default:
			break;
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

		ocelotlParameters.setAllEventTypes(confDataLoader.getTypes());
		ocelotlParameters.setCatEventTypes(confDataLoader.getTypesByCat());
		ocelotlParameters.setOperatorEventTypes(confDataLoader.getTypes(ocelotlCore.getAggregOperators().getSelectedOperatorResource().getEventCategory()));
		// Init operator specific configuration
		ocelotlParameters.setAllEventProducers(confDataLoader.getProducers());
		if (ocelotlParameters.getEventProducers().isEmpty())
			ocelotlParameters.getEventProducers().addAll(confDataLoader.getProducers());
		
		ocelotlParameters.setMaxEventProducers(ocelotlParameters.getOcelotlSettings().getMaxEventProducersPerQuery());
		manager = new ConfigViewManager(this);
		manager.init();
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
			throw new OcelotlException(OcelotlException.NO_TRACE);
	}

	/**
	 * Check that a microscopic description was selected
	 * 
	 * @throws OcelotlException if no description was selected
	 */
	public void checkMicroscopicDescription() throws OcelotlException {
		// If no microscopic distribution is selected
		if (comboTime.getText().equals(""))
			throw new OcelotlException(OcelotlException.NO_MICROSCOPIC_DESCRIPTION);
	}

	/**
	 * Check that visualization was selected
	 * 
	 * @throws OcelotlException if no visualization was selected
	 */
	public void checkVisualization() throws OcelotlException {
		// If no visualization is selected
		if (comboSpace.getText().equals(""))
			throw new OcelotlException(OcelotlException.NO_VISUALIZATION);
	}

	/**
	 * Check that the timestamps are valid
	 * 
	 * @throws OcelotlException
	 *             if at least one of the timestamps is not valid
	 */
	public void checkTimeStamp() throws OcelotlException {
		// If the starting timestamp is greater than the ending one
		if (textTimestampStart.getText().isEmpty() || textTimestampEnd.getText().isEmpty())
			throw new OcelotlException(OcelotlException.NO_TIMESTAMP);

		if (Long.parseLong(textTimestampStart.getText()) >= Long.parseLong(textTimestampEnd.getText())) {
			// Reset to default values
			textTimestampEnd.setText(Long.toString(confDataLoader.getMaxTimestamp()));
			textTimestampStart.setText(String.valueOf(confDataLoader.getMinTimestamp()));
			throw new OcelotlException(OcelotlException.INVALID_TIMERANGE);
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

	/**
	 * Search for the parameter that has the largest gap (sum of the differences
	 * in gain and loss values) between two consecutive gain and loss values
	 * 
	 * @return the corresponding parameter value, or 1.0 as default
	 */
	public double computeInitialParameter() {
		double diffG = 0.0, diffL = 0.0;
		double sumDiff = 0.0;
		double maxDiff = 0.0;
		int indexMaxQual = -1;
		int i;
		ArrayList<DLPQuality> qual = (ArrayList<DLPQuality>) ocelotlCore.getLpaggregManager().getQualities();
		for (i = 1; i < qual.size(); i++) {
			// Compute the difference for the gain and the loss
			diffG = Math.abs(qual.get(i - 1).getGain() - qual.get(i).getGain());
			diffL = Math.abs(qual.get(i - 1).getLoss() - qual.get(i).getLoss());

			// Compute sum of both
			sumDiff = diffG + diffL;

			if (sumDiff > maxDiff) {
				maxDiff = sumDiff;
				indexMaxQual = i;
			}
		}
		if (indexMaxQual > 0 && indexMaxQual < ocelotlCore.getLpaggregManager().getParameters().size())
			return ocelotlCore.getLpaggregManager().getParameters().get(indexMaxQual -1);

		// No index found or the value is invalid, return 1.0 as default
		return 1.0;
	}
	
}