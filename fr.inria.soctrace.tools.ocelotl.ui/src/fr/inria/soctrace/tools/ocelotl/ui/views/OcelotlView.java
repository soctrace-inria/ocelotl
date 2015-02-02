/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
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
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.framesoc.ui.model.GanttTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.HistogramTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.PieTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TableTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ParameterStrategy;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacacheStrategy;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.HasChanged;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.model.SimpleEventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlDefaultParameterConstants;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.FilenameValidator;
import fr.inria.soctrace.tools.ocelotl.ui.Activator;
import fr.inria.soctrace.tools.ocelotl.ui.loaders.ConfDataLoader;
import fr.inria.soctrace.tools.ocelotl.ui.snapshot.Snapshot;
import fr.inria.soctrace.tools.ocelotl.ui.views.statview.IStatView;
import fr.inria.soctrace.tools.ocelotl.ui.views.statview.StatViewManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.statview.StatViewWrapper;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.IAggregatedView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewWrapper;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.UnitAxisView;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.UnitAxisViewManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.UnitAxisViewWrapper;

/**
 * Main view for Ocelotl
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class OcelotlView extends FramesocPart implements IFramesocBusListener {

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

			Date aDate = new Date(System.currentTimeMillis());
			String convertedDate = new SimpleDateFormat("dd-MM-yyyy HHmmss z").format(aDate);
			String fileName = ocelotlParameters.getTrace().getAlias() + "_" + ocelotlParameters.getTrace().getId() + "_" + convertedDate;
			fileName = FilenameValidator.checkNameValidity(fileName);

			// Set a default file name
			dialog.setFileName(fileName);

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
				comboType.removeAll();
				comboDimension.removeAll();
				comboVisu.removeAll();

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
								throw new OcelotlException(OcelotlException.INVALID_CACHED_TRACE + ": TraceId - " + traceID);

							// Load the trace
							confDataLoader.load(trace);

							monitor.beginTask("Loading cached data...", IProgressMonitor.UNKNOWN);
							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {
									
									// Load the type operators
									for (final String type : ocelotlCore.getMicromodelTypes().getTypes(confDataLoader.getCurrentTrace().getType().getName(), confDataLoader.getCategories())) {
										comboType.add(type);
									}

									comboType.setText("");

									// Search for the corresponding metrics
									for (int i = 0; i < comboType.getItemCount(); i++) {
										if (comboType.getItem(i).equals(ocelotlParameters.getMicroModelType())) {
											comboType.select(i);
											comboType.notifyListeners(SWT.Selection, new Event());
											break;
										}
									}

									// If no operator was found
									if (comboType.getText().isEmpty())
										try {
											throw new OcelotlException(OcelotlException.INVALID_MICRO_DESCRIPTION + ": " + ocelotlParameters.getMicroModelType());
										} catch (OcelotlException e) {
											MessageDialog.openInformation(getSite().getShell(), "Error", e.getMessage());
											return;
										}

									// Set the corresponding parameters
									textTimestampStart.setText(String.valueOf(ocelotlParameters.getTimeRegion().getTimeStampStart()));
									textTimestampEnd.setText(String.valueOf(ocelotlParameters.getTimeRegion().getTimeStampEnd()));
									textDisplayedStart.setText(textTimestampStart.getText());
									textDisplayedEnd.setText(textTimestampEnd.getText());

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

	private class TimestampModificationListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			timestampHasChanged = true;
			
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
	
	private class TimeSliceModificationListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			hasChanged = HasChanged.ALL; 
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

			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.PARAMETER)
				hasChanged = HasChanged.PARAMETER;
			
			if(timestampHasChanged == true)
				hasChanged = HasChanged.ALL;
				
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
								monitor.setTaskName("Initializing Aggregation Operator");
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
							ocelotlParameters.setParameter(parameterPPolicy.computeInitialParameter(ocelotlCore.getLpaggregManager(), ocelotlParameters.getParameterPPolicy()));
						}

						hasChanged = HasChanged.PARAMETER;
						if (monitor.isCanceled()) {
							synchronized (lock) {
								running = false;
							}
							return Status.CANCEL_STATUS;
						}
						monitor.setTaskName("Compute Parts");

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
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							monitor.setTaskName("Draw Diagram");
							hasChanged = HasChanged.NOTHING;
							timeLineView.deleteDiagram();
							timeLineView.createDiagram(ocelotlCore.getLpaggregManager(), ocelotlParameters.getTimeRegion(), ocelotlCore.getVisuOperator());
							timeAxisView.createDiagram(ocelotlParameters.getTimeRegion());
							textRun.setText(String.valueOf(getOcelotlParameters().getParameter()));
							monitor.setTaskName("Draw Quality Curves");
							qualityView.createDiagram();
							monitor.setTaskName("Update Statistics");
							statView.createDiagram();
							monitor.setTaskName("Draw Y Axis");
							ocelotlParameters.setTimeSliceManager(new TimeSliceManager(ocelotlParameters.getTimeRegion(), ocelotlParameters.getTimeSlicesNumber()));
							snapshotAction.setEnabled(true);
							textDisplayedStart.setText(String.valueOf(ocelotlParameters.getTimeRegion().getTimeStampStart()));
							textDisplayedEnd.setText(String.valueOf(ocelotlParameters.getTimeRegion().getTimeStampEnd()));
							unitAxisView.deleteDiagram();
							unitAxisView.createDiagram(ocelotlCore.getVisuOperator());
							updateStatus();
							visuDisplayed = true;
							
							monitor.setTaskName("Launching Overview");
							
							if (ocelotlParameters.isOvervieweEnable()) {
								try {
									overView.updateDiagram(ocelotlParameters.getTimeRegion());
									// Do we need to compute everything
									if (overView.isRedrawOverview())
										overView.getOverviewThread().start();

								} catch (OcelotlException e) {
									MessageDialog.openInformation(getSite().getShell(), "Error", e.getMessage());
								}
							}
							
							history.saveHistory();
							timestampHasChanged = false;
							monitor.done();
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

	private class ComboTypeSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			hasChanged = HasChanged.ALL;
			ocelotlParameters.getUnfilteredEventProducers().clear();

			// Get the available aggregation operators
			comboDimension.setEnabled(true);
			comboDimension.removeAll();

			for (final String op : ocelotlCore.getAggregOperators().getOperators(confDataLoader.getCurrentTrace().getType().getName(), confDataLoader.getCategories())) {
				comboDimension.add(op);
			}

			comboDimension.setText("");
			if (comboDimension.getItems().length != 0) {
				// Items are sorted according to the selection priority
				comboDimension.setText(comboDimension.getItem(0));
				// Set the selected operator as operator in Ocelotl
				comboDimension.notifyListeners(SWT.Selection, new Event());
			}
		}
	}

	private class ComboDimensionSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			hasChanged = HasChanged.ALL;
			ocelotlParameters.getUnfilteredEventProducers().clear();
			history.reset();
			ocelotlCore.getMicromodelTypes().setSelectedMicroModel(comboType.getText());
			ocelotlCore.getAggregOperators().setSelectedOperator(comboDimension.getText());
			ocelotlParameters.setUnit(getCore().getMicromodelTypes().getSelectedOperatorResource().getUnit());
			
			// Set the number of time slice
			spinnerTSNumber.setSelection(ocelotlCore.getAggregOperators().getSelectedOperatorResource().getTs());
			visuDisplayed = false;
			
			if (timeLineView != null) {
				timeLineView.deleteDiagram();
				unitAxisView.deleteDiagram();
				timeAxisView.deleteDiagram();
				qualityView.deleteDiagram();
				statView.deleteDiagram();
			}
			
			comboVisu.setEnabled(true);
			comboVisu.removeAll();

			comboStatistics.setEnabled(true);
			comboStatistics.removeAll();
			// Get visu compatibility from both micro model and aggregation
			// operator
			ArrayList<String> visuCompatibilities = new ArrayList<String>();
			for (String aVisu : ocelotlCore.getAggregOperators().getSelectedOperatorResource().getVisuCompatibility()) {
				if (ocelotlCore.getMicromodelTypes().getSelectedOperatorResource().getVisuCompatibility().contains(aVisu)) {
					visuCompatibilities.add(aVisu);
				}
			}

			for (final String op : ocelotlCore.getVisuOperators().getOperators(visuCompatibilities, ocelotlCore.getAggregOperators().getSelectedOperatorResource().getDimension())) {
				comboVisu.add(op);
			}

			for (final String op : ocelotlCore.getStatOperators().getOperators(ocelotlCore.getMicromodelTypes().getSelectedOperatorResource().getEventCategory(), ocelotlCore.getAggregOperators().getSelectedOperatorResource().getDimension())) {
				comboStatistics.add(op);
			}

			// Since the operators are sorted by priority, set the default
			// choice to the first item
			if (comboVisu.getItems().length != 0) {
				comboVisu.setText(comboVisu.getItem(0));
				// Set the selected operator as operator in Ocelotl
				comboVisu.notifyListeners(SWT.Selection, new Event());
			}

			if (comboStatistics.getItems().length != 0) {
				comboStatistics.setText(comboStatistics.getItem(0));
				// Set the selected operator as operator in Ocelotl
				comboStatistics.notifyListeners(SWT.Selection, new Event());
			}

			// Set default settings
			setDefaultDescriptionSettings();
			
			// Init the overview
			overView.initVisuOperator(ocelotlCore.getVisuOperators().getOperatorResource(comboVisu.getText()).getOverviewVisualization());
		}
	}

	private class ComboVisuSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;

			if (hasChanged != HasChanged.NOTHING || !ocelotlCore.getVisuOperators().getOperatorResource(comboVisu.getText()).getName().equals(ocelotlParameters.getVisuOperator())) {
				if (hasChanged == HasChanged.NOTHING)
					hasChanged = HasChanged.PARAMETER;

				btnRun.setEnabled(true);
				ocelotlCore.getVisuOperators().setSelectedOperator(comboVisu.getText());
				timeLineView = timeLineViewManager.create();
				timeLineViewWrapper.setView(timeLineView);
				unitAxisView = unitAxisViewManager.create();
				unitAxisViewWrapper.setView(unitAxisView);

				if (hasChanged == HasChanged.PARAMETER) {
					btnRun.notifyListeners(SWT.Selection, new Event());
				} else
				// If the overview visu operator is different, then redraw it
				if (!ocelotlCore.getVisuOperators().getOperatorResource(comboVisu.getText()).getOverviewVisualization().equals(overView.getVisuOperatorName()))
					overView.initVisuOperator(ocelotlCore.getVisuOperators().getOperatorResource(comboVisu.getText()).getOverviewVisualization());
			}
		}
	}

	private class ComboStatSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;

			ocelotlCore.getStatOperators().setSelectedOperator(comboStatistics.getText());
			statView = statViewManager.create();
			statViewWrapper.setView(statView);
			
			// If there is a diagram displayed then also update the stat table
			if(ocelotlCore.getLpaggregManager() != null && visuDisplayed == true)
				statView.createDiagram();
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
			if (hasChanged == HasChanged.NOTHING)
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
	
	private class OverviewParameterUpAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (overView != null)
				overView.modifyParameterUp();
		}
	}

	private class OverviewParameterDownAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (overView != null)
				overView.modifyParameterDown();
		}
	}

	private class DefaultParameterAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (ocelotlCore.getLpaggregManager() != null) {
				textRun.setText(Double.toString(parameterPPolicy.computeInitialParameter(ocelotlCore.getLpaggregManager(), ocelotlParameters.getParameterPPolicy())));
				btnRun.notifyListeners(SWT.Selection, new Event());
			}
		}
	}

	private class ResetListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			cancelSelection();
			textTimestampStart.setText(Long.toString(confDataLoader.getMinTimestamp()));
			textTimestampEnd.setText(Long.toString(confDataLoader.getMaxTimestamp()));

			if (timeLineView != null) {
				timeLineView.resizeDiagram();

				// Reset spatial selection
				ocelotlParameters.setSpatialSelection(false);
				ocelotlParameters.updateCurrentProducers();
				
				timestampHasChanged = true;
			}
		}
	}
	
	private class CancelSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			cancelSelection();
		}
	}
	
	
	private class NextZoomListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			history.restoreNextHistory();
		}
	}
	
	private class PrevZoomListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			history.restorePrevHistory();
		}
	}

	/**
	 * Cancel the current selection
	 */
	public void cancelSelection() {
		if (timeLineView != null) {
			// Reset selected time region to displayed time region
			setTimeRegion(getOcelotlParameters().getTimeRegion());

			// Remove the currently drawn selections
			((AggregatedView) timeLineView).deleteSelectFigure();
			getTimeAxisView().unselect();
			getUnitAxisView().unselect();
			getOverView().deleteSelection();

			// Cancel potential spatialselection
			getOcelotlParameters().setSpatialSelection(true);
			getOcelotlParameters().setSpatiallySelectedProducers(getOcelotlParameters().getCurrentProducers());
			
			// Update stats
			statView.updateData();
			
			timestampHasChanged = false;
		}
	}
	
	private class VisualizationSettingsSelectionAdapter extends SelectionAdapter {

		private final OcelotlView	view;

		public VisualizationSettingsSelectionAdapter(final OcelotlView view) {
			this.view = view;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			// hasChanged = HasChanged.ALL;
			if (!comboVisu.getEnabled())
				return;
			final VisuConfigViewManager manager = new VisuConfigViewManager(view);
			manager.openConfigWindows();
		}
	}

	private class AggregationSettingsSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (!comboDimension.getEnabled())
				return;

			hasChanged = HasChanged.ALL;
			aggregationSettingsManager.openConfigWindows();
			// Set the redraw of the overview
			overView.setRedrawOverview(true);
		}
	}
	
	private class ResizeTimeAxisListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			getMainViewTopSashform().setWeights(mainViewBottomSashform.getWeights());
		}
	}
	
	private class ResizeMainViewListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			mainViewBottomSashform.setWeights(getMainViewTopSashform().getWeights());
		}
	}

	/**
	 * Create the settings button for the toolbar
	 * 
	 * @param view
	 * @return the action creating the window settings
	 */
	private Action createSettingWindow(final OcelotlView view) {
		final ImageDescriptor img = ResourceManager.getPluginImageDescriptor("fr.inria.soctrace.framesoc.ui", "icons/management.png");
		final Action showSettings = new Action("Ocelotl Settings", img) {
			@Override
			public void run() {
				final OcelotlSettingsView settingsView = new OcelotlSettingsView(view);
				settingsView.openDialog();
			}
		};
		showSettings.setToolTipText("Ocelotl Settings");
		return showSettings;
	}

	/**
	 * Create the snapshot button for the toolbar
	 * 
	 * @return the action taking a snapshot
	 */
	private Action createSnapshot() {
		final ImageDescriptor img = ResourceManager.getPluginImageDescriptor("fr.inria.soctrace.tools.ocelotl.ui", "icons/snapshot-icon.png");
		final Action takeSnapshot = new Action("Take Snapshot", img) {
			@Override
			public void run() {
				if (confDataLoader.getCurrentTrace() == null || ocelotlParameters.getTrace() == null)
					return;

				snapshot.takeSnapShot();
				playSound("/media/snapshot.wav");
			}
		};
		takeSnapshot.setToolTipText("Take a Snapshot of the Current View");
		return takeSnapshot;
	}

	private class TraceAdapter extends SelectionAdapter {
		private Trace	trace;

		@Override
		public void widgetSelected(final SelectionEvent e) {
			trace = traceMap.get(comboTraces.getSelectionIndex());
			final String title = "Loading Trace";
			comboType.removeAll();
			comboDimension.removeAll();
			comboVisu.removeAll();
			btnRun.setEnabled(false);
			overView.reset();
			
			currentShownTrace = trace;
			setFocus();

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
								textDisplayedStart.setText(textTimestampStart.getText());
								textDisplayedEnd.setText(textTimestampEnd.getText());
								comboType.setEnabled(true);
								comboType.removeAll();
								ocelotlParameters.setTrace(confDataLoader.getCurrentTrace());
								
								for (final String type : ocelotlCore.getMicromodelTypes().getTypes(confDataLoader.getCurrentTrace().getType().getName(), confDataLoader.getCategories())) {
									comboType.add(type);
								}
								// Since the types are sorted by priority, set
								// the default choice to the first item
								if (comboType.getItems().length != 0) {
									comboType.setText(comboType.getItem(0));
									// Set the selected type as operator in
									// Ocelotl
									comboType.notifyListeners(SWT.Selection, new Event());
								}
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

	private class KeyboardEventListener implements Listener {

		@Override
		public void handleEvent(final Event e) {
			switch (e.keyCode) {
			case SWT.ARROW_LEFT:
			case SWT.ARROW_UP:
				// Make sure we are not in an editable field
				if (!(e.widget.getClass().getSimpleName().equals("Text") || e.widget.getClass().getSimpleName().equals("Spinner")))
					buttonDown.notifyListeners(SWT.Selection, new Event());
				break;
			case SWT.ARROW_RIGHT:
			case SWT.ARROW_DOWN:
				// Make sure we are not in an editable field
				if (!(e.widget.getClass().getSimpleName().equals("Text") || e.widget.getClass().getSimpleName().equals("Spinner")))
					buttonUp.notifyListeners(SWT.Selection, new Event());
				break;	
			case 117:// Letter u
				// Make sure we are not in an editable field
				if (!(e.widget.getClass().getSimpleName().equals("Text") || e.widget.getClass().getSimpleName().equals("Spinner")))
					overViewParamDown.notifyListeners(SWT.Selection, new Event());
				break;
			case 105:// Letter i
				// Make sure we are not in an editable field
				if (!(e.widget.getClass().getSimpleName().equals("Text") || e.widget.getClass().getSimpleName().equals("Spinner")))
					overViewParamUp.notifyListeners(SWT.Selection, new Event());
				break;
			case SWT.KEYPAD_CR:
			case SWT.CR:
				if (!e.widget.isListening(e.type))
					btnRun.notifyListeners(SWT.Selection, new Event());
				break;
			case SWT.ESC:
				btnReset.notifyListeners(SWT.Selection, new Event());
				break;
			}
		}
	}

	public static final String			ID				= "fr.inria.soctrace.tools.ocelotl.ui.OcelotlView"; //$NON-NLS-1$
	public static final String			PLUGIN_ID		= Activator.PLUGIN_ID;

	private Button						btnRun;

	private Button						btnSettings;
	private Combo						comboType;
	private Combo						comboDimension;
	private Combo						comboTraces;
	private final ConfDataLoader		confDataLoader	= new ConfDataLoader();
	private HasChanged					hasChanged		= HasChanged.ALL;
	private IAggregatedView				timeLineView;
	private IStatView					statView;
	private final OcelotlCore			ocelotlCore;
	private final OcelotlParameters		ocelotlParameters;
	private Text						textRun;
	private QualityView					qualityView;
	private Spinner						spinnerTSNumber;
	private TimeAxisView				timeAxisView;
	private UnitAxisView				unitAxisView;
	private Text						textTimestampEnd;
	private Text						textTimestampStart;
	final Map<Integer, Trace>			traceMap		= new HashMap<Integer, Trace>();
	private boolean						timestampHasChanged = true;
	
	private Button						buttonDown;
	private Button						buttonUp;
	private Button						btnSaveDataCache;
	private Combo						comboVisu;
	private final TimeLineViewManager	timeLineViewManager;
	private final UnitAxisViewManager	unitAxisViewManager;
	private Composite					compositeMatrixView;
	private SashForm					sashFormView;
	private TimeLineViewWrapper			timeLineViewWrapper;
	private UnitAxisViewWrapper			unitAxisViewWrapper;
	private StatViewManager				statViewManager;
	private StatViewWrapper				statViewWrapper;
	private Button						btnSettings2;
	private Button						btnReset;
	private Button						buttonCancelSelection;
	private Button						btnNextZoom;
	private Button						btnPrevZoom;
	private TabFolder					tabFolder;
	private Action						settings;
	private Action						snapshotAction;
	
	private Snapshot					snapshot;
	private Font						cantarell8;
	private Overview					overView;
	private ParameterStrategy			parameterPPolicy;
	private ActionHistory				history;

	private ConfigViewManager			aggregationSettingsManager;
	private Combo						comboStatistics;
	private Button						buttonHome;
	private Composite					statComposite;
	private Label						textDisplayedStart;
	private Label						textDisplayedEnd;
	private Button						overViewParamUp;
	private Button						overViewParamDown;
	private SashForm					mainViewTopSashform;
	private SashForm					mainViewBottomSashform;
	private Composite					compositeTimeAxisView;
	private SubStatusLineManager		statusLineManager;
	private boolean						visuDisplayed;
	
	/**
	 * Followed topics
	 */
	protected FramesocBusTopicList		topics			= null;

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
		statViewManager = new StatViewManager(this);
		unitAxisViewManager = new UnitAxisViewManager(this);

		try {
			ocelotlParameters.getDataCache().setSettings(ocelotlParameters.getOcelotlSettings());
		} catch (final OcelotlException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}

		snapshot = new Snapshot(ocelotlParameters.getOcelotlSettings().getSnapShotDirectory(), this);
		history = new ActionHistory(this);
		
		// Register update to synchronize traces
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_REFRESH_TRACES_NEEDED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED);
		topics.registerAll();
	}

	private void cleanAll() {
		hasChanged = HasChanged.ALL;
		textTimestampStart.setText(String.valueOf(OcelotlDefaultParameterConstants.TimestampStart));
		textTimestampEnd.setText(String.valueOf(OcelotlDefaultParameterConstants.TimestampEnd));
		spinnerTSNumber.setSelection(OcelotlDefaultParameterConstants.TimeSliceNumber);
		textRun.setText(String.valueOf(OcelotlDefaultParameterConstants.RunParameter));
	}

	@Override
	public void dispose() {
		topics.unregisterAll();
		super.dispose();
	}


	protected TraceIntervalDescriptor getIntervalDescriptor() {
		if (confDataLoader.getCurrentTrace() == null)
			return null;
		TraceIntervalDescriptor des = new TraceIntervalDescriptor();
		des.setTrace(ocelotlParameters.getTrace());
		des.setStartTimestamp(getTimeRegion().getTimeStampStart());
		des.setEndTimestamp(getTimeRegion().getTimeStampEnd());
		return des;
	}

	@Override
	public void createPartControl(final Composite parent) {
		final Display display = Display.getCurrent();
		display.addFilter(SWT.KeyDown, new KeyboardEventListener());

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		final SashForm sashFormGlobal = new SashForm(parent, SWT.VERTICAL);
		sashFormGlobal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		timeAxisView = new TimeAxisView();
		qualityView = new QualityView(this);
		timeLineViewWrapper = new TimeLineViewWrapper(this);
		statViewWrapper = new StatViewWrapper(this);
		unitAxisViewWrapper = new UnitAxisViewWrapper(this);
		overView = new Overview(this);
		parameterPPolicy = new ParameterStrategy();
		cantarell8 = new Font(sashFormGlobal.getDisplay(), new FontData("Cantarell", 8, SWT.NORMAL));

		final SashForm sashForm_1 = new SashForm(sashFormGlobal, SWT.BORDER);
		sashForm_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		// Central view
		sashFormView = new SashForm(sashForm_1, SWT.BORDER | SWT.VERTICAL);
		sashFormView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		// Top bar of the central view
		// Trace settings
		final ScrolledComposite topBarScrollComposite = new ScrolledComposite(sashFormView, SWT.BORDER | SWT.H_SCROLL);
		topBarScrollComposite.setExpandHorizontal(true);
		topBarScrollComposite.setExpandVertical(true);

		final Group groupTraces = new Group(topBarScrollComposite, SWT.NONE);
		groupTraces.setSize(422, 100);
		groupTraces.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		groupTraces.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		groupTraces.setFont(cantarell8);
		groupTraces.setLayout(new GridLayout(8, false));
		
		comboTraces = new Combo(groupTraces, SWT.READ_ONLY);
		 				GridData gd_comboTraces = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		 				gd_comboTraces.widthHint = 170;
		 				comboTraces.setLayoutData(gd_comboTraces);
		 				comboTraces.setFont(cantarell8);
		 				comboTraces.addSelectionListener(new TraceAdapter());
		 				comboTraces.setToolTipText("Trace Selection");
		 		
		 				Button btnLoadDataCache = new Button(groupTraces, SWT.NONE);
		 				btnLoadDataCache.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/etool16/import_wiz.gif"));
		 				btnLoadDataCache.setToolTipText("Load a Microscopic Description");
		 				btnLoadDataCache.setFont(SWTResourceManager.getFont("Cantarell", 7, SWT.NORMAL));
		 				btnLoadDataCache.addSelectionListener(new LoadDataListener());
		 		
		 				comboType = new Combo(groupTraces, SWT.READ_ONLY);
		 				final GridData gd_comboType = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		 				gd_comboType.widthHint = 150;
		 				comboType.setLayoutData(gd_comboType);
		 				comboType.setFont(cantarell8);
		 				comboType.add("Metric");
		 				comboType.setText("Metric");
		 				comboType.addSelectionListener(new ComboTypeSelectionAdapter());
		 				comboType.setToolTipText("Metric Selection");
		 		
		 				comboDimension = new Combo(groupTraces, SWT.READ_ONLY);
		 				final GridData gd_comboAggregationOperator = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		 				gd_comboAggregationOperator.widthHint = 150;
		 				comboDimension.setLayoutData(gd_comboAggregationOperator);
		 				comboDimension.setFont(cantarell8);
		 				comboDimension.add("Dimensions");
		 				comboDimension.setText("Dimensions");
		 				comboDimension.addSelectionListener(new ComboDimensionSelectionAdapter());
		 				comboDimension.setToolTipText("Dimensions Selection");
		 		
		 				btnSettings = new Button(groupTraces, SWT.NONE);
		 				btnSettings.setToolTipText("Settings");
		 				btnSettings.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/management.png"));
		 				btnSettings.setFont(cantarell8);
		 				btnSettings.addSelectionListener(new AggregationSettingsSelectionAdapter());
		 		
		 				btnSaveDataCache = new Button(groupTraces, SWT.NONE);
		 				btnSaveDataCache.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/etool16/save_edit.gif"));
		 				btnSaveDataCache.setToolTipText("Save Current Microscopic Description");
		 				btnSaveDataCache.setFont(SWTResourceManager.getFont("Cantarell", 7, SWT.NORMAL));
		 				btnSaveDataCache.addSelectionListener(new SaveDataListener());
		 		
		 				comboVisu = new Combo(groupTraces, SWT.READ_ONLY);
		 				final GridData gd_comboSpace = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		 				gd_comboSpace.widthHint = 150;
		 				comboVisu.setLayoutData(gd_comboSpace);
						comboVisu.setFont(cantarell8);
		 				comboVisu.add("Visualization");
		 				comboVisu.setText("Visualization");
		 				comboVisu.addSelectionListener(new ComboVisuSelectionAdapter());
		 				comboVisu.setToolTipText("Visualization Selection");
		 		
		 				btnSettings2 = new Button(groupTraces, SWT.NONE);
		 				btnSettings2.setToolTipText("Settings");
		 				btnSettings2.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/management.png"));
						btnSettings2.setFont(cantarell8);
						btnSettings2.addSelectionListener(new VisualizationSettingsSelectionAdapter(this));
		
		topBarScrollComposite.setContent(groupTraces);
		topBarScrollComposite.setMinSize(groupTraces.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// Display of aggregation results
		final SashForm sashForm_4 = new SashForm(sashFormView, SWT.BORDER | SWT.VERTICAL);
		setMainViewTopSashform(new SashForm(sashForm_4, SWT.BORDER | SWT.HORIZONTAL));
		mainViewBottomSashform = new SashForm(sashForm_4, SWT.BORDER | SWT.HORIZONTAL);
		final Composite compositeBlank = new Composite(mainViewBottomSashform, SWT.BORDER);
		compositeBlank.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		// Time axis 
		compositeTimeAxisView = new Composite(mainViewBottomSashform, SWT.BORDER);
		compositeTimeAxisView.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		timeAxisView.initDiagram(compositeTimeAxisView);
		final FillLayout fl_compositeTimeAxisView = new FillLayout(SWT.HORIZONTAL);
		compositeTimeAxisView.setLayout(fl_compositeTimeAxisView);
		compositeTimeAxisView.addListener(SWT.Resize, new ResizeTimeAxisListener());
		mainViewBottomSashform.setWeights(OcelotlConstants.yAxisDefaultWeight);
		
		// Set unit axis
		final Composite compositeUnitAxisView = new Composite(getMainViewTopSashform(), SWT.BORDER);
		compositeUnitAxisView.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		unitAxisViewWrapper.init(compositeUnitAxisView);
		final FillLayout fl_compositeUnitAxisView = new FillLayout(SWT.VERTICAL);
		compositeUnitAxisView.setLayout(fl_compositeUnitAxisView);
		
		compositeMatrixView = new Composite(getMainViewTopSashform(), SWT.BORDER);
		compositeMatrixView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeMatrixView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		compositeMatrixView.setSize(500, 500);
		timeLineViewWrapper.init(compositeMatrixView);
		compositeMatrixView.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeMatrixView.addListener(SWT.Resize, new ResizeMainViewListener());
		
		getMainViewTopSashform().setWeights(OcelotlConstants.yAxisDefaultWeight);
		
		sashForm_4.setWeights(new int[] {206, 16});

		// Bottom bar of the central view
		final ScrolledComposite scrolledComposite = new ScrolledComposite(sashFormView, SWT.BORDER | SWT.H_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		final Group groupTime = new Group(scrolledComposite, SWT.NONE);
		groupTime.setSize(422, 110);
		groupTime.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		groupTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		groupTime.setFont(cantarell8);
		groupTime.setLayout(new GridLayout(20, false));

		Label lblDisplayedStart = new Label(groupTime, SWT.NONE);
		lblDisplayedStart.setFont(cantarell8);
		lblDisplayedStart.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDisplayedStart.setText("Time Bounds:");
		
		textDisplayedStart = new Label(groupTime, SWT.BORDER);
		textDisplayedStart.setText("0");
		final GridData gd_textDisplayedStart = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_textDisplayedStart.widthHint = 100;
		textDisplayedStart.setFont(cantarell8);
		textDisplayedStart.setLayoutData(gd_textDisplayedStart);
		textDisplayedStart.setToolTipText("Starting Timestamp of the Current Display");
		
		Label lblDisplayedEnd = new Label(groupTime, SWT.NONE);
		lblDisplayedEnd.setFont(cantarell8);
		lblDisplayedEnd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDisplayedEnd.setText("-");
		
		textDisplayedEnd = new Label(groupTime, SWT.BORDER);
		textDisplayedEnd.setText("1");
		final GridData gd_textDisplayedEnd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_textDisplayedEnd.widthHint = 100;
		textDisplayedEnd.setFont(cantarell8);
		textDisplayedEnd.setLayoutData(gd_textDisplayedEnd);
		textDisplayedEnd.setToolTipText("Ending Timestamp of the Current Display");
		
		final Label lblStartTimestamp = new Label(groupTime, SWT.NONE);
		lblStartTimestamp.setFont(cantarell8);
		lblStartTimestamp.setText("Selection:");

		textTimestampStart = new Text(groupTime, SWT.BORDER);
		final GridData gd_textTimestampStart = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_textTimestampStart.widthHint = 100;
		textTimestampStart.setLayoutData(gd_textTimestampStart);
		textTimestampStart.setFont(cantarell8);
		textTimestampStart.setToolTipText("Starting Timestamp Value");

		final Label lblEndTimestamp = new Label(groupTime, SWT.NONE);
		lblEndTimestamp.setFont(cantarell8);
		lblEndTimestamp.setText("-");

		textTimestampEnd = new Text(groupTime, SWT.BORDER);
		final GridData gd_textTimestampEnd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_textTimestampEnd.widthHint = 100;
		textTimestampEnd.setLayoutData(gd_textTimestampEnd);
		textTimestampEnd.setFont(cantarell8);
		textTimestampEnd.setToolTipText("Ending Timestamp Value");

		btnReset = new Button(groupTime, SWT.NONE);
		btnReset.setToolTipText("Reset Timestamps");
		btnReset.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/etool16/undo_edit.gif"));
		
		buttonCancelSelection = new Button(groupTime, SWT.NONE);
		buttonCancelSelection.setToolTipText("Cancel the Current Selection");
		buttonCancelSelection.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/etool16/delete_edit.gif"));

		btnPrevZoom = new Button(groupTime, SWT.NONE);
		btnPrevZoom.setToolTipText("Go to the Previous Zooming Value");
		btnPrevZoom.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/dlcl16/backward_nav.gif"));
		btnPrevZoom.setEnabled(false);
		
		btnNextZoom = new Button(groupTime, SWT.NONE);
		btnNextZoom.setToolTipText("Go to the Next Zooming Value");
		btnNextZoom.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/dlcl16/forward_nav.gif"));
		btnNextZoom.setEnabled(false);
		
		final Label lblTSNumber = new Label(groupTime, SWT.NONE);
		lblTSNumber.setFont(cantarell8);
		lblTSNumber.setText("Timeslice Number");

		spinnerTSNumber = new Spinner(groupTime, SWT.BORDER);
		final GridData gd_spinnerTSNumber = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_spinnerTSNumber.widthHint = 55;
		spinnerTSNumber.setLayoutData(gd_spinnerTSNumber);
		spinnerTSNumber.setFont(cantarell8);
		spinnerTSNumber.setMaximum(OcelotlDefaultParameterConstants.maxTimeslice);
		spinnerTSNumber.setMinimum(OcelotlDefaultParameterConstants.minTimeslice);
		spinnerTSNumber.addModifyListener(new TimeSliceModificationListener());
		btnReset.addSelectionListener(new ResetListener());
		buttonCancelSelection.addSelectionListener(new CancelSelectionListener());
		btnNextZoom.addSelectionListener(new NextZoomListener());
		btnPrevZoom.addSelectionListener(new PrevZoomListener());
		textTimestampEnd.addModifyListener(new TimestampModificationListener());
		textTimestampStart.addModifyListener(new TimestampModificationListener());
		scrolledComposite.setContent(groupTime);
		scrolledComposite.setMinSize(groupTime.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sashFormView.setWeights(new int[] {29, 429, 29});

		// Right column
		final SashForm sashForm = new SashForm(sashForm_1, SWT.BORDER | SWT.VERTICAL);
		sashForm.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		// Overview
		final SashForm overviewSashForm = new SashForm(sashForm, SWT.BORDER | SWT.HORIZONTAL);
		overviewSashForm.setLayout(new GridLayout(2, false));
		
		final Composite compositeOverview = new Composite(overviewSashForm, SWT.FILL);
		compositeOverview.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeOverview.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		compositeOverview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		compositeOverview.setLayout(new FillLayout(SWT.FILL));
		overView.init(compositeOverview);
		
		final SashForm buttonSashForm = new SashForm(overviewSashForm, SWT.BORDER | SWT.VERTICAL);
		buttonSashForm.setSashWidth(0);
		overViewParamUp = new Button(buttonSashForm, SWT.NONE);
		overViewParamUp.setText(">");
		overViewParamUp.setToolTipText("Increase Overview Parameter");
		overViewParamUp.setImage(null);
		overViewParamUp.setFont(cantarell8);
		overViewParamUp.addSelectionListener(new OverviewParameterUpAdapter());
		
		overViewParamDown = new Button(buttonSashForm, SWT.NONE);
		overViewParamDown.setText("<");
		overViewParamDown.setToolTipText("Decrease Overview Parameter");
		overViewParamDown.setImage(null);
		overViewParamDown.setFont(cantarell8);
		overViewParamDown.addSelectionListener(new OverviewParameterDownAdapter());

		overviewSashForm.setWeights(new int[] {95, 5});
		
		// Stat and legend
		tabFolder = new TabFolder(sashForm, SWT.NONE);
		tabFolder.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		// Statistics
		final TabItem tbtmStat = new TabItem(tabFolder, SWT.NONE);
		tbtmStat.setText("Statistics");

		SashForm statSashForm = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmStat.setControl(statSashForm);

		Composite composite = new Composite(statSashForm, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		comboStatistics = new Combo(composite, SWT.READ_ONLY);
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_combo.widthHint = 120;
		comboStatistics.setLayoutData(gd_combo);
		comboStatistics.setFont(cantarell8);
		comboStatistics.add("Statistics");
		comboStatistics.setText("Statistics");
		comboStatistics.setToolTipText("Statistics");
		comboStatistics.addSelectionListener(new ComboStatSelectionAdapter());

		statComposite = new Composite(composite, SWT.NONE);
		statComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		statViewWrapper.init(statComposite);

		// Quality curves display
		final Composite compositeQualityView = new Composite(sashForm, SWT.BORDER);
		compositeQualityView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeQualityView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		qualityView.initDiagram(compositeQualityView);
		compositeQualityView.setLayout(new FillLayout(SWT.HORIZONTAL));

		// Bottom bar of the quality view
		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(sashForm, SWT.H_SCROLL);
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);

		Group group = new Group(scrolledComposite_1, SWT.NONE);
		group.setLayout(new GridLayout(6, false));

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

		buttonHome = new Button(group, SWT.NONE);
		buttonHome.setToolTipText("Default Parameter");
		buttonHome.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/elcl16/home_nav.gif"));
		buttonHome.setFont(cantarell8);
		buttonHome.addSelectionListener(new DefaultParameterAdapter());

		btnRun = new Button(group, SWT.NONE);
		btnRun.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		btnRun.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/ocelotl16.png"));
		btnRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.BOLD));
		btnRun.setToolTipText("Launch an Analysis");
		btnRun.setText("RUN!");

		btnRun.addSelectionListener(new GetAggregationAdapter());
		buttonUp.addSelectionListener(new ParameterUpAdapter());
		buttonDown.addSelectionListener(new ParameterDownAdapter());
		textRun.addModifyListener(new ParameterModifyListener());
		scrolledComposite_1.setContent(group);
		scrolledComposite_1.setMinSize(group.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sashForm.setWeights(new int[] { 41, 232, 289, 31 });
		sashForm_1.setWeights(new int[] { 652, 355 });
		sashFormGlobal.setWeights(new int[] { 395 });

		final IActionBars actionBars = getViewSite().getActionBars();
		final IToolBarManager toolBar = actionBars.getToolBarManager();

		TableTraceIntervalAction.add(toolBar, createTableAction());
		GanttTraceIntervalAction.add(toolBar, createGanttAction());
		PieTraceIntervalAction.add(toolBar, createPieAction());
		HistogramTraceIntervalAction.add(toolBar, createHistogramAction());

		toolBar.add(new Separator());

		settings = createSettingWindow(this);
		snapshotAction = createSnapshot();

		toolBar.add(settings);
		toolBar.add(snapshotAction);
		
		statusLineManager = (SubStatusLineManager) actionBars.getStatusLineManager();
	
		refreshTraces();

		cleanAll();
	}

	public Button getBtnRun() {
		return btnRun;
	}

	public Combo getComboAggregationOperator() {
		return comboDimension;
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

	public OcelotlParameters getOcelotlParameters() {
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
		this.comboDimension = comboTime;
	}

	public TimeRegion getTimeRegion() {
		return new TimeRegion(Long.parseLong(textTimestampStart.getText()), Long.parseLong(textTimestampEnd.getText()));
	}

	public Overview getOverView() {
		return overView;
	}

	public HasChanged getHasChanged() {
		return hasChanged;
	}

	public void setHasChanged(HasChanged hasChanged) {
		this.hasChanged = hasChanged;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}

	public Composite getStatComposite() {
		return statComposite;
	}

	public void setStatComposite(Composite statComposite) {
		this.statComposite = statComposite;
	}

	public Text getTextTimestampEnd() {
		return textTimestampEnd;
	}

	public void setTextTimestampEnd(Text textTimestampEnd) {
		this.textTimestampEnd = textTimestampEnd;
	}

	public Text getTextTimestampStart() {
		return textTimestampStart;
	}

	public void setTextTimestampStart(Text textTimestampStart) {
		this.textTimestampStart = textTimestampStart;
	}

	public IStatView getStatView() {
		return statView;
	}

	public void setStatView(IStatView statView) {
		this.statView = statView;
	}

	public Button getNextZoom() {
		return btnNextZoom;
	}

	public Button getPrevZoom() {
		return btnPrevZoom;
	}

	public Combo getComboType() {
		return comboType;
	}

	public void setComboType(Combo comboType) {
		this.comboType = comboType;
	}
	
	public Label getTextDisplayedStart() {
		return textDisplayedStart;
	}

	public Label getTextDisplayedEnd() {
		return textDisplayedEnd;
	}

	public UnitAxisView getUnitAxisView() {
		return unitAxisView;
	}

	public void setUnitAxisView(UnitAxisView unitAxisView) {
		this.unitAxisView = unitAxisView;
	}

	public SashForm getMainViewTopSashform() {
		return mainViewTopSashform;
	}

	public void setMainViewTopSashform(SashForm mainViewTopSashform) {
		this.mainViewTopSashform = mainViewTopSashform;
	}

	public ParameterStrategy getParameterPPolicy() {
		return parameterPPolicy;
	}

	public void setParameterPPolicy(ParameterStrategy parameterPPolicy) {
		this.parameterPPolicy = parameterPPolicy;
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

		comboType.setEnabled(false);
		comboDimension.setEnabled(false);
		comboVisu.setEnabled(false);
		comboStatistics.setEnabled(false);
		btnRun.setEnabled(false);
		snapshotAction.setEnabled(false);
		btnNextZoom.setEnabled(false);
		btnPrevZoom.setEnabled(false);
		ocelotlParameters.getDataCache().buildDictionary(confDataLoader.getTraces());
	}

	public void setComboAggregationOperator(final Combo comboAggregationOperator) {
		comboDimension = comboAggregationOperator;
	}

	public void setConfiguration() {
		ocelotlParameters.setTrace(confDataLoader.getCurrentTrace());
		ocelotlParameters.setNormalize(ocelotlParameters.getOcelotlSettings().isNormalizedCurve());
		ocelotlParameters.setTimeSlicesNumber(spinnerTSNumber.getSelection());
		ocelotlParameters.setMicroModelType(comboType.getText());
		ocelotlParameters.setDataAggOperator(comboDimension.getText());
		ocelotlParameters.setVisuOperator(comboVisu.getText());
		ocelotlParameters.setStatOperator(comboStatistics.getText());
		ocelotlParameters.setEventsPerThread(ocelotlParameters.getOcelotlSettings().getEventsPerThread());
		ocelotlParameters.setThreadNumber(ocelotlParameters.getOcelotlSettings().getNumberOfThread());
		ocelotlParameters.setMaxEventProducers(ocelotlParameters.getOcelotlSettings().getMaxEventProducersPerQuery());
		ocelotlParameters.setThreshold(ocelotlParameters.getOcelotlSettings().getThresholdPrecision());
		ocelotlParameters.updateCurrentProducers();
		
		// If there are aggregated leave, then it is necessary to update the
		// spatial selection
		if (ocelotlParameters.isSpatialSelection())
			ocelotlParameters.setSpatiallySelectedProducers(ocelotlParameters.getCurrentProducers());
	
		ocelotlParameters.setParameterPPolicy(ocelotlParameters.getOcelotlSettings().getParameterPPolicy());
		ocelotlParameters.setOvervieweEnable(ocelotlParameters.getOcelotlSettings().isEnableOverview());
		
		setCachePolicy();
		try {
			ocelotlParameters.setParameter(Double.valueOf(textRun.getText()).floatValue());
			ocelotlParameters.setTimeRegion(new TimeRegion(Long.valueOf(textTimestampStart.getText()), Long.valueOf(textTimestampEnd.getText())));
		} catch (final NumberFormatException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
	}

	/**
	 * Set the cache strategy depending on the selected policy TODO Take the
	 * operator into account
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

	public void setTimeRegion(final TimeRegion time) {
		textTimestampStart.setText(String.valueOf(time.getTimeStampStart()));
		textTimestampEnd.setText(String.valueOf(time.getTimeStampEnd()));
	}

	// When receiving a notification, update the trace list
	@Override
	public void partHandle(FramesocBusTopic topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED) || topic.equals(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED) || topic.equals(FramesocBusTopic.TOPIC_UI_REFRESH_TRACES_NEEDED)) {
			refreshTraces();
		}
		if ((topic.equals(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED))) {
			if (timeLineView != null)
				timeLineView.resizeDiagram();
		}
	}

	/**
	 * Set the default microdescription settings
	 */
	public void setDefaultDescriptionSettings() {
		hasChanged = HasChanged.ALL;

		ocelotlParameters.setAllEventTypes(confDataLoader.getTypes());
		ocelotlParameters.setCatEventTypes(confDataLoader.getTypesByCat());
		ocelotlParameters.setOperatorEventTypes(confDataLoader.getTypes(ocelotlCore.getMicromodelTypes().getSelectedOperatorResource().getType()));
		// Init operator specific configuration
		ocelotlParameters.setAllEventProducers(confDataLoader.getProducers());
		ocelotlParameters.setUnit(getCore().getMicromodelTypes().getSelectedOperatorResource().getUnit());
		
		if (ocelotlParameters.getUnfilteredEventProducers().isEmpty()) {
			ocelotlParameters.getUnfilteredEventProducers().addAll(confDataLoader.getProducers());
			// If there is no current spatial selection
			if (!ocelotlParameters.isSpatialSelection()) {
				// The selected producers are the current producers
				ocelotlParameters.setCurrentProducers(ocelotlParameters.getUnfilteredEventProducers());
			}
		}

		ocelotlParameters.setMaxEventProducers(ocelotlParameters.getOcelotlSettings().getMaxEventProducersPerQuery());
		aggregationSettingsManager = new ConfigViewManager(this);
		aggregationSettingsManager.init();
	}

	/**
	 * Check that inputs are valid
	 * 
	 * @throws OcelotlException
	 *             if one input is not valid
	 */
	public void checkInputs() throws OcelotlException {
		checkTrace();
		checkType();
		checkMicroscopicDescription();
		checkVisualization();
		checkTimeStamp();
	}

	/**
	 * Check that a trace was selected
	 * 
	 * @throws OcelotlException
	 *             if no trace was selected
	 */
	public void checkTrace() throws OcelotlException {
		// If no trace is selected
		if (confDataLoader.getCurrentTrace() == null)
			throw new OcelotlException(OcelotlException.NO_TRACE);
	}

	/**
	 * Check that a type was selected
	 * 
	 * @throws OcelotlException
	 *             if no type was selected
	 */
	public void checkType() throws OcelotlException {
		// If no trace is selected
		if (comboType.getText().equals(""))
			throw new OcelotlException(OcelotlException.NO_TYPE);
	}

	/**
	 * Check that a microscopic description was selected
	 * 
	 * @throws OcelotlException
	 *             if no description was selected
	 */
	public void checkMicroscopicDescription() throws OcelotlException {
		// If no microscopic distribution is selected
		if (comboDimension.getText().equals(""))
			throw new OcelotlException(OcelotlException.NO_MICROSCOPIC_DESCRIPTION);
	}

	/**
	 * Check that visualization was selected
	 * 
	 * @throws OcelotlException
	 *             if no visualization was selected
	 */
	public void checkVisualization() throws OcelotlException {
		// If no visualization is selected
		if (comboVisu.getText().equals(""))
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

	public static synchronized void playSound(final String soundPath) {
		try {
			URL soundFile = OcelotlView.class.getResource(soundPath);
			Clip clip = AudioSystem.getClip();
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(soundFile);
			clip.open(inputStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update the status bar
	 */
	private void updateStatus() {
		final Image img = ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/warn_tsk.gif");
		String message = "";
		boolean messageToDisplay = false;
		statusLineManager.removeAll();

		if (getOcelotlParameters().isHasLeaveAggregated()) {
			message = "Some event producers were aggregated. ";
			messageToDisplay = true;
		}

		if (getOcelotlParameters().isApproximateRebuild()) {
			message = message + "The aggregation was performed using an approximate version of the cache.";
			messageToDisplay = true;
		}

		// If there is a message to display
		if (messageToDisplay) {
			// Set a message and display it
			statusLineManager.setMessage(img, message);
			statusLineManager.setVisible(true);
		} else {
			// Hide the current status
			statusLineManager.setVisible(false);
		}

		statusLineManager.update(true);
	}

	@Override
	protected void createFramesocPartControl(Composite parent) {
		createPartControl(parent);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void showTrace(Trace trace, Object data) {
		// Look for the correct trace among the
		// available traces
		for (int aTraceIndex : traceMap.keySet()) {
			if (traceMap.get(aTraceIndex).getId() == trace.getId()) {
				comboTraces.select(aTraceIndex);
				break;
			}
		}

		comboTraces.notifyListeners(SWT.Selection, new Event());
	}
}
