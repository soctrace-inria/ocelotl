package fr.inria.soctrace.tools.ocelotl.ui.views;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.idataaggregop.IDataAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.IAggregatedView;

public class Overview implements IFramesocBusListener {

	private OcelotlView					ocelotlView;

	private MicroscopicDescription		microModel;
	private IDataAggregationOperator	aggregOperator;
	private IDataAggregManager			aggregManager;
	private IVisuOperator				visuOperator;
	private String						visuOperatorName	= "";
	private OcelotlParameters			overviewParameters;

	private Figure						root;
	private Canvas						canvas;
	private AggregatedView				timeLineView;
	private boolean						redrawOverview;
	private double						parameter			= 0.0;
	private int							timeSlice;

	private TimeRegion					displayedTimeRegion;
	private TimeRegion					zoomedTimeRegion;
	private TimeRegion					selectedTimeRegion;

	private Color						displayFGColor		= ColorConstants.black;
	private Color						displayBGColor		= ColorConstants.red;
	private Color						selectFGColor		= ColorConstants.white;
	private Color						selectBGColor		= ColorConstants.black;

	// Show the currently displayed zone
	private SelectFigure				displayedZone;
	// Show the currently selected zone
	private SelectFigure				selectedZone;
	private int							Border				= 3;
	private OverviewThread				overviewThread		= null;

	/**
	 * Followed topics
	 */
	protected FramesocBusTopicList		topics			= null;

	public Overview(OcelotlView aView) {
		super();
		ocelotlView = aView;
		redrawOverview = true;
		displayedTimeRegion = null;
		
		// Set colors according to the settings
		displayFGColor = ocelotlView.getOcelotlParameters().getOcelotlSettings().getOverviewDisplayFgColor();
		displayBGColor = ocelotlView.getOcelotlParameters().getOcelotlSettings().getOverviewDisplayBgColor();
		selectFGColor = ocelotlView.getOcelotlParameters().getOcelotlSettings().getOverviewSelectionFgColor();
		selectBGColor = ocelotlView.getOcelotlParameters().getOcelotlSettings().getOverviewSelectionBgColor();
	
		// Register update to synchronize traces
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED);
		topics.registerAll();
		initSelectionFigure();
	}

	// When receiving a notification, redraw the overview with the new color
	@Override
	public void handle(FramesocBusTopic topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED) && aggregManager != null) {
			createDiagram();
			displayedZone.draw(zoomedTimeRegion, true);
		}
	}

	/**
	 * Initialize the canvas
	 * 
	 * @param parent
	 * @return
	 */
	public Canvas init(final Composite parent) {
		root = new Figure();
		root.setFont(parent.getFont());
		final XYLayout layout = new XYLayout();
		root.setLayoutManager(layout);
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		canvas.setSize(parent.getSize());
		final LightweightSystem lws = new LightweightSystem(canvas);
		lws.setContents(root);
		lws.setControl(canvas);
		canvas.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(final ControlEvent arg0) {
				canvas.redraw();
				root.repaint();
				resizeDiagram();
			}

			@Override
			public void controlResized(final ControlEvent arg0) {
				canvas.redraw();
				root.repaint();
				resizeDiagram();
			}
		});
		root.setFont(SWTResourceManager.getFont("Cantarell", 24, SWT.NORMAL));
		root.setSize(parent.getSize().x, parent.getSize().y);
		return canvas;
	}

	/**
	 * Draw the diagram
	 * 
	 * @param time
	 */
	public void createDiagram() {
		displayedTimeRegion = new TimeRegion(overviewParameters.getTimeRegion());
		timeLineView.setBorder(Border);
		timeLineView.createDiagram(aggregManager, displayedTimeRegion, visuOperator);
	}

	/**
	 * Redraw the diagram to adapt to the new size of the display
	 */
	public void resizeDiagram() {
		root.removeAll();
		canvas.update();
		if (aggregManager != null && displayedTimeRegion != null) {
			createDiagram();
			displayedZone.draw(zoomedTimeRegion, true);
			drawSelection();
		}
	}

	/**
	 * Update the overview (the selection, and the drawing)
	 * 
	 * @param time
	 * @throws OcelotlException
	 */
	public void updateDiagram(TimeRegion time) throws OcelotlException {
		if(overviewThread != null && overviewThread.isAlive())
			return;

		zoomedTimeRegion = new TimeRegion(time);

		// Is it necessary to change completely the computed model
		if (redrawOverview) {
			overviewThread = new OverviewThread(time);
			return;
		}

		displayedZone.draw(time, true);
		deleteSelection();
	}

	/**
	 * Update the selected region with the displayed region
	 * 
	 * @param time
	 *            the new selected time region
	 */
	public void updateSelection(TimeRegion time) {
		selectedTimeRegion = time;
		drawSelection();
	}
	
	/**
	 * Draw the selection
	 */
	protected void drawSelection() {
		if(selectedTimeRegion != null && displayedTimeRegion != null)
			selectedZone.draw(selectedTimeRegion, false);
	}

	/**
	 * Don't the show the selection anymore
	 */
	public void deleteSelection() {
		selectedZone.delete();
		// Avoid further redraw
		selectedTimeRegion = displayedTimeRegion;
	}

	public IAggregatedView getTimeLineView() {
		return timeLineView;
	}

	public void setTimeLineView(AggregatedView timeLineView) {
		this.timeLineView = timeLineView;
	}

	public boolean isRedrawOverview() {
		return redrawOverview;
	}

	public void setRedrawOverview(boolean redrawOverview) {
		this.redrawOverview = redrawOverview;
	}

	public SelectFigure getSelectedZone() {
		return selectedZone;
	}

	public void setSelectedZone(SelectFigure selectedZone) {
		this.selectedZone = selectedZone;
	}

	public OverviewThread getOverviewThread() {
		return overviewThread;
	}

	public void setOverviewThread(OverviewThread overviewThread) {
		this.overviewThread = overviewThread;
	}

	public IVisuOperator getVisuOperator() {
		return visuOperator;
	}

	public void setVisuOperator(IVisuOperator visuOperator) {
		this.visuOperator = visuOperator;
	}

	public String getVisuOperatorName() {
		return visuOperatorName;
	}

	public void setVisuOperatorName(String visuOperatorName) {
		this.visuOperatorName = visuOperatorName;
	}

	public Color getDisplayFGColor() {
		return displayFGColor;
	}

	public void setDisplayFGColor(Color displayFGColor) {
		this.displayFGColor = displayFGColor;
		if (displayedZone != null) {
			displayedZone.setForegroundColor(displayFGColor);
			if(zoomedTimeRegion != null)
				displayedZone.draw(zoomedTimeRegion, true);
		}
	}

	public Color getDisplayBGColor() {
		return displayBGColor;
	}

	public void setDisplayBGColor(Color displayBGColor) {
		this.displayBGColor = displayBGColor;
		if (displayedZone != null) {
			displayedZone.setBackgroundColor(displayBGColor);
			if(zoomedTimeRegion != null)
				displayedZone.draw(zoomedTimeRegion, true);
		}
	}

	public Color getSelectFGColor() {
		return selectFGColor;
	}

	public void setSelectFGColor(Color selectFGColor) {
		this.selectFGColor = selectFGColor;
		if (selectedZone != null) {
			selectedZone.setForegroundColor(selectFGColor);
			drawSelection();
		}
	}

	public Color getSelectBGColor() {
		return selectBGColor;
	}

	public void setSelectBGColor(Color selectBGColor) {
		this.selectBGColor = selectBGColor;
		if (selectedZone != null) {
			selectedZone.setBackgroundColor(selectBGColor);
			drawSelection();
		}
	}

	/**
	 * Initialize the visualization operator and perform additional init
	 * operation
	 * 
	 * @param name
	 *            the name of the visualization operator
	 */
	public void initVisuOperator(String name) {
		// Instantiate the chosen visu operator
		visuOperator = ocelotlView.getOcelotlCore().getVisuOperators().instantiateOperator(name);
		visuOperatorName = name;
		
		final Bundle mybundle = Platform.getBundle(ocelotlView.getCore().getVisuOperators().getOperatorResource(name).getBundle());

		// Instantiate the actual view
		try {
			this.timeLineView = (AggregatedView) mybundle.loadClass(ocelotlView.getCore().getVisuOperators().getOperatorResource(name).getVisualization()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlView);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		root.removeAll();
		canvas.update();

		// Init the view
		timeLineView.setRoot(root);
		timeLineView.setCanvas(canvas);
		redrawOverview = true;

		// Init other stuff
		displayedTimeRegion = new TimeRegion(ocelotlView.getTimeRegion());
		initSelectionFigure();
		parameter = 0.0;
	}

	/**
	 * Set the overview parameters with a set of ocelotlParameters
	 * 
	 * @param ocelotlParameters
	 *            the parameters used to perform the update
	 */
	public void updateOverviewParameters(OcelotlParameters ocelotlParameters) {
		// Copy the given parameters
		overviewParameters = new OcelotlParameters(ocelotlView.getOcelotlParameters());
		
		// Set the time slice number at the current value for overview
		overviewParameters.setTimeSlicesNumber(timeSlice);
		
		// Set the time region at global
		TimeRegion overviewTimeRegion  = new TimeRegion(overviewParameters.getTrace().getMinTimestamp(), overviewParameters.getTrace().getMaxTimestamp());
		overviewParameters.setTimeRegion(overviewTimeRegion);
	}
	
	public void initSelectionFigure() {
		selectedZone = new SelectFigure(selectFGColor, selectBGColor);
		displayedZone = new SelectFigure(displayFGColor, displayBGColor);
	}

	public void reset() {
		if (overviewThread != null && overviewThread.isAlive()) {
			overviewThread.interrupt();
		}
			
		overviewThread = null;
		aggregManager = null;
		displayedTimeRegion = null;
	}
	
	/**
	 * Look for the next smaller parameter value
	 */
	public void modifyParameterDown()
	{
		if (aggregManager != null) {
			for (final double aParam : aggregManager.getParameters()) {
				if (aParam > parameter) {
					parameter = aParam;
					changeParameter();
					break;
				}
			}
		}
	}
	
	/**
	 * Look for the next greater parameter value
	 */
	public void modifyParameterUp()
	{
		if (aggregManager != null) {
			for (int aParam = aggregManager.getParameters().size() - 1; aParam >= 0; aParam--) {
				if (aggregManager.getParameters().get(aParam) < parameter) {
					parameter = aggregManager.getParameters().get(aParam);
					changeParameter();
					break;
				}
			}
		}
	}
	
	/**
	 * If the parameter value has changed then recompute parts and redraw
	 */
	public void changeParameter() {
		// Recompute the parts
		aggregManager.computeParts(parameter);
		// Idem in the view
		visuOperator.initManager(ocelotlView.getOcelotlCore(), aggregManager);
		// Redraw
		createDiagram();
		displayedZone.draw(zoomedTimeRegion, true);
		drawSelection();
	}

	/**
	 * Class for describing and displaying selected zones
	 */
	private class SelectFigure extends RectangleFigure {

		private Color	foreground;
		private Color	background;
		public final static int alphaValue = 110;
		
		// Init with a given color set
		public SelectFigure(Color foreGround, Color backGround) {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			this.foreground = foreGround;
			this.background = backGround;
			setForegroundColor(this.foreground);
			setBackgroundColor(this.background);
			setAlpha(alphaValue);
		}

		/**
		 * Draw the actual selected time region
		 * 
		 * @param timeRegion
		 *            the time region (time boundaries of the region)
		 * @param displayed
		 *            flag to distinguish between the display and the selection
		 *            figure
		 */
		public void draw(final TimeRegion timeRegion, boolean displayed) {
			// If there is no zoom, don't show indicator
			if (timeRegion.compareTimeRegion(displayedTimeRegion)) {
				delete();
				return;
			}

			if (getParent() != root)
				root.add(this);

			// Use to embed the selection figure within the display figure
			int delta = 0;
			if (!displayed) {
				delta = 2;
			}
			
			root.setConstraint(this, new Rectangle(new Point((int) ((timeRegion.getTimeStampStart() - displayedTimeRegion.getTimeStampStart()) * (root.getSize().width - 2 * Border) / displayedTimeRegion.getTimeDuration() + Border), root.getSize().height - delta),
					new Point((int) ((timeRegion.getTimeStampEnd() - displayedTimeRegion.getTimeStampStart()) * (root.getSize().width - 2 * Border) / displayedTimeRegion.getTimeDuration() + Border), delta)));
		}

		/**
		 * Remove the selection from display
		 */
		public void delete() {
			if (getParent() != null)
				root.remove(this);
		}
	}

	/**
	 * Thread use to compute the display of an overview
	 */
	class OverviewThread extends Thread {
		TimeRegion	time;

		public OverviewThread(TimeRegion time) {
			super();
			this.time = time;
		}

		@Override
		public void run() {
			try {
				// Get the number of time slice for the current overview aggregation operator
				timeSlice = ocelotlView.getOcelotlCore().getAggregOperators().getOperatorResource(ocelotlView.getOcelotlParameters().getOcelotlSettings().getOverviewAggregOperator()).getTs();

				//Set the overview parameters with the current ocelotlParameters
				updateOverviewParameters(ocelotlView.getOcelotlParameters());
		
				// Get a new micro model
				microModel = ocelotlView.getOcelotlCore().getMicromodelTypes().instantiateMicroModel(overviewParameters.getMicroModelType());

				// Build the microscopic description
				microModel.setOcelotlParameters(overviewParameters, new NullProgressMonitor());
			    if (Thread.interrupted()) 
			        return;
			    
				// Init the aggregation operator
				aggregOperator = ocelotlView.getOcelotlCore().getAggregOperators().instantiateOperator(ocelotlView.getOcelotlParameters().getOcelotlSettings().getOverviewAggregOperator());
				aggregManager = aggregOperator.createManager(microModel, new NullProgressMonitor());
				aggregManager.computeQualities();
			    if (Thread.interrupted()) 
			        return;
			    
				aggregManager.computeDichotomy();
			    if (Thread.interrupted()) 
			        return;
			     
				parameter = ocelotlView.getOcelotlCore().computeInitialParameter(aggregManager);

				// Compute the view according to the new parameter value
				aggregManager.computeParts(parameter);
			    if (Thread.interrupted()) 
			        return;
			    	    
				visuOperator.initManager(ocelotlView.getOcelotlCore(), aggregManager);
			    if (Thread.interrupted()) 
			        return;
			    
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						createDiagram();
						displayedZone.draw(time, true);
					}
				});
			    if (Thread.interrupted()) 
			        return;
			     
				redrawOverview = false;
			} catch (OcelotlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SoCTraceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
