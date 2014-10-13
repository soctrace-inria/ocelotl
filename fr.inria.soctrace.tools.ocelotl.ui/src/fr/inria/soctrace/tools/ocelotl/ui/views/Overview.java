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
	private double						parameter;
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

	public void createDiagram(IDataAggregManager iMicroDescManager, TimeRegion time) {
		globalTimeRegion = new TimeRegion(time);

		// Save the current parameter
		double tempParam = ocelotlView.getParams().getParameter();

		// Set the parameter to the computed initial parameter
		ocelotlView.getParams().setParameter(ocelotlView.computeInitialParameter());

		///timeLineView.setVisuOperator(visuOperator);
		timeLineView.createDiagram(iMicroDescManager, time, visuOperator);

		// Restore the parameter
		ocelotlView.getParams().setParameter(tempParam);
	}

	/**
	 * 
	 */
	public void resizeDiagram() {
		if (aggregOperator != null && globalTimeRegion != null) {
			canvas.redraw();
			createDiagram(aggregOperator, globalTimeRegion);
			root.repaint();
		}
	}

	/**
	 * Update the overview (the selection, and the drawing)
	 * 
	 * @param iMicroDescManager
	 * @param time
	 * @throws OcelotlException 
	 */
	public void updateDiagram(IDataAggregManager iMicroDescManager, TimeRegion time) throws OcelotlException {
		// Update the selected region with the displayed region
		if (!redrawOverview && newTimeRegionLonger(time)) {
			redrawOverview = true;
			globalTimeRegion = new TimeRegion(time);
		}

		if (redrawOverview) {
			microModel = ocelotlView.getOcelotlCore().getMicroModel();
			
			// Perform a deep copy of the microdescription model

			// Init aggreg
			aggregManager = ocelotlView.getOcelotlCore().getAggregOperators().instantiateOperator(ocelotlView.getParams().getOcelotlSettings().getOverviewAggregOperator());
			aggregOperator = aggregManager.createManager(microModel, new NullProgressMonitor());

			aggregOperator.computeQualities();
			aggregOperator.computeDichotomy();
			
			// Compute the view according to the new parameter value
			aggregOperator.computeParts();

			this.visuOperator.initManager(ocelotlView.getOcelotlCore(), aggregOperator);

			createDiagram(aggregOperator, time);
			redrawOverview = false;
		}

		displayedZone.draw(time);
		updateSelection(time);
	}

	/**
	 * Check if the new time region is larger than the actual displayed one
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

		globalTimeRegion = new TimeRegion(this.ocelotlView.getTimeRegion());
		selectedZone = new SelectFigure(ColorConstants.blue, ColorConstants.blue);
		displayedZone = new SelectFigure(ColorConstants.white, ColorConstants.white);
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

	public void setVisuOperator(String name) {
		// Init visu
		visuOperator = ocelotlView.getOcelotlCore().getVisuOperators().instantiateOperator(name);
		
		final Bundle mybundle = Platform.getBundle(ocelotlView.getCore().getVisuOperators().getSelectedOperatorResource(name).getBundle());

		try {
			this.timeLineView = (AggregatedView) mybundle.loadClass(ocelotlView.getCore().getVisuOperators().getSelectedOperatorResource(name).getVisualization()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlView);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.timeLineView.setRoot(root);
		this.timeLineView.setCanvas(canvas);
		this.redrawOverview = true;
		
		globalTimeRegion = new TimeRegion(this.ocelotlView.getTimeRegion());
		selectedZone = new SelectFigure(ColorConstants.blue, ColorConstants.blue);
		displayedZone = new SelectFigure(ColorConstants.white, ColorConstants.white);
	}

	private class SelectFigure extends RectangleFigure {

		Color	foreground;
		Color	background;

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
