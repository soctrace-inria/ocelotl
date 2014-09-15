package fr.inria.soctrace.tools.ocelotl.ui.views;

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

import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.ISpaceAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.IMicroDescManager;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.IAggregatedView;

public class Overview {
	
	private OcelotlView ocelotlView;
	private Figure root;
	private Canvas canvas;
	private AggregatedView timeLineView;
	private IMicroDescManager microDescManager;
	private ISpaceAggregationOperator spaceAggOp;
	private boolean redrawOverview;
	// Show the currently displayed zone
	private SelectFigure displayedZone;
	// Show the currently selected zone
	private SelectFigure selectedZone;

	public Overview(OcelotlView aView) {
		super();
		ocelotlView = aView;
		microDescManager = null;
		spaceAggOp = null;
		redrawOverview = true;
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
	
	public void createDiagram(IMicroDescManager iMicroDescManager, TimeRegion time) {
		TimeRegion aTime = new TimeRegion(ocelotlView.getParams().getTrace().getMinTimestamp(), ocelotlView.getParams().getTrace().getMaxTimestamp());
		
		if(microDescManager == null)
			microDescManager = iMicroDescManager;	
		
		if(spaceAggOp == null)
			spaceAggOp = ocelotlView.getCore().getSpaceOperator();
		
		// Save the current parameter
		double tempParam = ocelotlView.getParams().getParameter();
		// Set the parameter to  zero in order to have a fully desaggregated view
		ocelotlView.getParams().setParameter(0.0);
		// Compute the desaggregated view 
		ocelotlView.getOcelotlCore().computeParts();
		
		timeLineView.createDiagram(microDescManager, aTime);
		
		// Restore the parameter
		ocelotlView.getParams().setParameter(tempParam);
	}

	public void updateDiagram(IMicroDescManager iMicroDescManager, TimeRegion time) {
		// Update the selected region with the displayed region

		if (redrawOverview) {
			createDiagram(iMicroDescManager, time);
			redrawOverview = false;
		}

		displayedZone.draw(time);
		updateSelection(time);
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
		this.timeLineView.setRoot(root);
		this.timeLineView.setCanvas(canvas);
		this.redrawOverview = true;
		selectedZone = new SelectFigure(this.ocelotlView.getTimeRegion(), ColorConstants.blue, ColorConstants.blue);
		displayedZone = new SelectFigure(this.ocelotlView.getTimeRegion(), ColorConstants.white, ColorConstants.white);
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

	private class SelectFigure extends RectangleFigure {

		TimeRegion time;
		Color foreground;
		Color background;
		
		public SelectFigure(final TimeRegion timeRegion, Color foreGround, Color backGround) {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			this.foreground = foreGround;
			this.background = backGround;
			setForegroundColor(this.foreground);
			setBackgroundColor(this.background);
			setAlpha(50);
			time = timeRegion;
		}

		public void draw(final TimeRegion timeRegion) {
			// If there is no zoom, don't show indicator
			if (timeRegion.compareTimeRegion(time)) {
				delete();
				return;
			}

			if (getParent() != root)
				root.add(this);
			root.setConstraint(this, new Rectangle(new Point((int) ((timeRegion.getTimeStampStart() - time.getTimeStampStart()) * (root.getSize().width - 2) / time.getTimeDuration()), root.getSize().height), new Point(
					(int) ((timeRegion.getTimeStampEnd() - time.getTimeStampStart()) * (root.getSize().width - 2) / time.getTimeDuration()), 2)));
			root.repaint();
		}
		
		/**
		 * Remove the selection displayal
		 */
		public void delete() {
			if (getParent() != null)
				root.remove(this);

			root.repaint();
		}
	}
	
}
