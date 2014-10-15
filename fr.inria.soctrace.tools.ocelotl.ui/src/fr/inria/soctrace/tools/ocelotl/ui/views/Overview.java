package fr.inria.soctrace.tools.ocelotl.ui.views;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

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

import fr.inria.lpaggreg.quality.DLPQuality;
import fr.inria.soctrace.tools.ocelotl.core.idataaggregop.IDataAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.IAggregatedView;

public class Overview {

	private OcelotlView					ocelotlView;

	private MicroscopicDescription		microModel;
	private IDataAggregationOperator	aggregManager;
	private IDataAggregManager			aggregOperator;
	private IVisuOperator				visuOperator;

	private Figure						root;
	private Canvas						canvas;
	private AggregatedView				timeLineView;
	private boolean						redrawOverview;
	private double						parameter = 0.0;
	private int							timeSlice;

	private TimeRegion					globalTimeRegion;

	// Show the currently displayed zone
	private SelectFigure				displayedZone;
	// Show the currently selected zone
	private SelectFigure				selectedZone;
	private int							Border	= 10;

	public Overview(OcelotlView aView) {
		super();
		ocelotlView = aView;
		redrawOverview = true;
		globalTimeRegion = null;
	}

	/**
	 * Initialize the canvas
	 * @param parent
	 * @return
	 */
	public Canvas init(final Composite parent) {
		root = new Figure();
		root.setFont(parent.getFont());
		final XYLayout layout = new XYLayout();
		root.setLayoutManager(layout);
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
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
	 * @param time
	 */
	public void createDiagram(TimeRegion time) {
		globalTimeRegion = new TimeRegion(time);
		timeLineView.createDiagram(aggregOperator, time, visuOperator);
	}

	/**
	 * Redraw the diagram to adapt to the new size of the display
	 */
	public void resizeDiagram() {
		if (aggregOperator != null && globalTimeRegion != null) {
			canvas.redraw();
			createDiagram(globalTimeRegion);
			root.repaint();
		}
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

		// is it necessary to change completely the computed model
		if (redrawOverview) {
			// Get the same micro model
			microModel = ocelotlView.getOcelotlCore().getMicroModel();

			// Init the aggregation operator
			aggregManager = ocelotlView.getOcelotlCore().getAggregOperators().instantiateOperator(ocelotlView.getParams().getOcelotlSettings().getOverviewAggregOperator());
			aggregOperator = aggregManager.createManager(microModel, new NullProgressMonitor());

			aggregOperator.computeQualities();
			aggregOperator.computeDichotomy();
			
			parameter = computeInitialParameter();

			// Compute the view according to the new parameter value
			aggregOperator.computeParts(parameter);

			this.visuOperator.initManager(ocelotlView.getOcelotlCore(), aggregOperator);

			createDiagram(time);
			redrawOverview = false;
		}

		displayedZone.draw(time);
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
		selectedZone.draw(time);
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
		selectedZone = new SelectFigure(ColorConstants.blue, ColorConstants.blue);
		displayedZone = new SelectFigure(ColorConstants.white, ColorConstants.white);
	}

	/**
	 * Search for the parameter that has the largest gap (sum of the differences
	 * in gain and loss values) between two consecutive gain and loss values
	 * 
	 * @return the corresponding parameter value, or 0.0 as default
	 */
	public double computeInitialParameter() {
		double diffG = 0.0, diffL = 0.0;
		double sumDiff = 0.0;
		double maxDiff = 0.0;
		int indexMaxQual = -1;
		int i;
		ArrayList<DLPQuality> qual = (ArrayList<DLPQuality>) aggregOperator.getQualities();
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
		if (indexMaxQual > 0 && indexMaxQual < aggregOperator.getParameters().size())
			return aggregOperator.getParameters().get(indexMaxQual -1);

		// No index found or the value is invalid, return 1.0 as default
		return 0.0;
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
			setAlpha(50);
		}

		/**
		 * Draw the actual selected time region
		 * 
		 * @param timeRegion
		 *            the time region (time boundaries of the region)
		 */
		public void draw(final TimeRegion timeRegion) {
			// If there is no zoom, don't show indicator
			if (timeRegion.compareTimeRegion(globalTimeRegion)) {
				delete();
				return;
			}

			if (getParent() != root)
				root.add(this);
			root.setConstraint(this, new Rectangle(new Point((int) ((timeRegion.getTimeStampStart() - globalTimeRegion.getTimeStampStart()) * (root.getSize().width - 2 * Border) / globalTimeRegion.getTimeDuration() + Border), root.getSize().height),
					new Point((int) ((timeRegion.getTimeStampEnd() - globalTimeRegion.getTimeStampStart()) * (root.getSize().width - 2 * Border) / globalTimeRegion.getTimeDuration() + Border), 2)));
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
