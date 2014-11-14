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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.idataaggregop.IDataAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.IAggregatedView;

public class Overview implements IFramesocBusListener {

	private OcelotlView					ocelotlView;

	private MicroscopicDescription		microModel;
	private IDataAggregationOperator	aggregOperator;
	private IDataAggregManager			aggregManager;
	private IVisuOperator				visuOperator;

	private Figure						root;
	private Canvas						canvas;
	private AggregatedView				timeLineView;
	private boolean						redrawOverview;
	private double						parameter	= 0.0;
	private int							timeSlice;

	private TimeRegion					globalTimeRegion;
	private TimeRegion					zoomedTimeRegion;

	// Show the currently displayed zone
	private SelectFigure				displayedZone;
	// Show the currently selected zone
	private SelectFigure				selectedZone;
	private int							Border		= 3;

	/**
	 * Followed topics
	 */
	protected FramesocBusTopicList		topics		= null;

	public Overview(OcelotlView aView) {
		super();
		ocelotlView = aView;
		redrawOverview = true;
		globalTimeRegion = null;

		// Register update to synchronize traces
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED);
		topics.registerAll();
	}

	// When receiving a notification, update the trace list
	@Override
	public void handle(FramesocBusTopic topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED) && aggregManager != null) {
			createDiagram(globalTimeRegion);
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
		root.setFont(SWTResourceManager.getFont("Cantarell", 24, SWT.NORMAL));
		root.setSize(parent.getSize().x, parent.getSize().y);
		return canvas;
	}

	/**
	 * Draw the diagram
	 * 
	 * @param time
	 */
	public void createDiagram(TimeRegion time) {
		globalTimeRegion = new TimeRegion(time);
		timeLineView.setBorder(Border);
		timeLineView.createDiagram(aggregManager, time, visuOperator);
	}

	/**
	 * Redraw the diagram to adapt to the new size of the display
	 */
	public void resizeDiagram() {
		root.removeAll();
		canvas.update();
		if (aggregManager != null && globalTimeRegion != null) {
			createDiagram(globalTimeRegion);
			displayedZone.draw(zoomedTimeRegion, true);
		}
		root.repaint();
	}

	/**
	 * Update the overview (the selection, and the drawing)
	 * 
	 * @param time
	 * @throws OcelotlException
	 */
	public void updateDiagram(TimeRegion time) throws OcelotlException {
		// Update the selected region with the displayed region
		if (!redrawOverview && newTimeRegionLonger(time)) {
			redrawOverview = true;
			globalTimeRegion = new TimeRegion(time);
		}

		zoomedTimeRegion = new TimeRegion(time);

		// is it necessary to change completely the computed model
		if (redrawOverview) {
			// Get the same micro model
			microModel = ocelotlView.getOcelotlCore().getMicroModel();

			// Init the aggregation operator
			// if
			// (!ocelotlView.getParams().getOcelotlSettings().getOverviewAggregOperator().equals(ocelotlView.getParams().getTimeAggOperator()))
			// {
			aggregOperator = ocelotlView.getOcelotlCore().getAggregOperators().instantiateOperator(ocelotlView.getParams().getOcelotlSettings().getOverviewAggregOperator());
			aggregManager = aggregOperator.createManager(microModel, new NullProgressMonitor());
			aggregManager.computeQualities();
			aggregManager.computeDichotomy();
			// } else {
			// aggregManager =
			// ocelotlView.getOcelotlCore().getLpaggregManager();
			// }
			parameter = ocelotlView.getOcelotlCore().computeInitialParameter();

			// Compute the view according to the new parameter value
			aggregManager.computeParts(parameter);

			this.visuOperator.initManager(ocelotlView.getOcelotlCore(), aggregManager);

			redrawOverview = false;
			createDiagram(time);
		}

		displayedZone.draw(time, true);
		updateSelection(time);
	}

	/**
	 * Check if the new time region is larger than the actual displayed one.
	 * Used to know if it is necessary to recompute a new view of the overview
	 * 
	 * @param time
	 *            the tested time region
	 * @return true if it is larger, false otherwise
	 */
	public boolean newTimeRegionLonger(TimeRegion time) {
		if (time.getTimeStampStart() < globalTimeRegion.getTimeStampStart() || time.getTimeStampEnd() > globalTimeRegion.getTimeStampEnd()) {
			long newDuration = time.getTimeStampEnd() - time.getTimeStampStart();
			long currentDuration = globalTimeRegion.getTimeStampEnd() - globalTimeRegion.getTimeStampStart();

			if (newDuration > currentDuration)
				return true;
		}

		return false;
	}

	public void updateSelection(TimeRegion time) {
		// Update the selected region with the displayed region
		selectedZone.draw(time, false);
	}

	/**
	 * Don't the show the selection anymore
	 */
	public void deleteSelection() {
		selectedZone.delete();
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

	public IVisuOperator getVisuOperator() {
		return visuOperator;
	}

	public void setVisuOperator(IVisuOperator visuOperator) {
		this.visuOperator = visuOperator;
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

		final Bundle mybundle = Platform.getBundle(ocelotlView.getCore().getVisuOperators().getSelectedOperatorResource(name).getBundle());

		// Instantiate the actual view
		try {
			this.timeLineView = (AggregatedView) mybundle.loadClass(ocelotlView.getCore().getVisuOperators().getSelectedOperatorResource(name).getVisualization()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlView);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Init the view
		this.timeLineView.setRoot(root);
		this.timeLineView.setCanvas(canvas);
		this.redrawOverview = true;

		// Init other stuff
		globalTimeRegion = new TimeRegion(this.ocelotlView.getTimeRegion());
		initSelectionFigure();
	}

	public void initSelectionFigure() {
		selectedZone = new SelectFigure(ColorConstants.black, ColorConstants.black);
		displayedZone = new SelectFigure(ColorConstants.black, ColorConstants.darkBlue);
	}

	public void reset() {
		aggregManager = null;
		globalTimeRegion = null;
	}

	/**
	 * Class for describing and displaying selected zones
	 */
	private class SelectFigure extends RectangleFigure {

		Color	foreground;
		Color	background;

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
			setAlpha(90);
		}

		/**
		 * Draw the actual selected time region
		 * 
		 * @param timeRegion
		 *            the time region (time boundaries of the region)
		 * @param b
		 */
		public void draw(final TimeRegion timeRegion, boolean displayed) {
			// If there is no zoom, don't show indicator
			if (timeRegion.compareTimeRegion(globalTimeRegion)) {
				delete();
				return;
			}

			if (getParent() != root)
				root.add(this);

			int delta = 0;
			if (!displayed) {
				delta = 2;
			}
			root.setConstraint(this, new Rectangle(new Point((int) ((timeRegion.getTimeStampStart() - globalTimeRegion.getTimeStampStart()) * (root.getSize().width - 2 * Border) / globalTimeRegion.getTimeDuration() + Border), root.getSize().height - delta),
					new Point((int) ((timeRegion.getTimeStampEnd() - globalTimeRegion.getTimeStampStart()) * (root.getSize().width - 2 * Border) / globalTimeRegion.getTimeDuration() + Border), delta)));
			root.repaint();
		}

		/**
		 * Remove the selection from display
		 */
		public void delete() {
			if (getParent() != null)
				root.remove(this);

			root.repaint();
		}
	}

}
