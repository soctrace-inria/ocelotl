package fr.inria.soctrace.tools.ocelotl.ui.views;

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

import fr.inria.soctrace.tools.ocelotl.core.config.ISpaceConfig;
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
	private TimeRegion globalTimeRegion;
	private IMicroDescManager md;
	private ISpaceAggregationOperator spaceOperator;
	private boolean redrawOverview;
	// Show the currently displayed zone
	private SelectFigure displayedZone;
	// Show the currently selected zone
	private SelectFigure selectedZone;
	private int Border = 10;

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
	
	public void createDiagram(IMicroDescManager iMicroDescManager, TimeRegion time) {
		globalTimeRegion = new TimeRegion(time);

		// Save the current parameter
		double tempParam = ocelotlView.getParams().getParameter();

		// Set the parameter to the computed initial parameter
		ocelotlView.getParams().setParameter(ocelotlView.computeInitialParameter());
		
		timeLineView.setDistribution(spaceOperator);

		timeLineView.createDiagram(iMicroDescManager, time);

		// Restore the parameter
		ocelotlView.getParams().setParameter(tempParam);
	}

	/**
	 * 
	 */
	public void resizeDiagram() {
		if (md != null && globalTimeRegion != null) {
			canvas.redraw();
			createDiagram(md, globalTimeRegion);
			root.repaint();
		}
	}

	public void updateDiagram(IMicroDescManager iMicroDescManager, TimeRegion time) {
		// Update the selected region with the displayed region
		if (!redrawOverview && newTimeRegionLonger(time)) {
			redrawOverview = true;
			globalTimeRegion = new TimeRegion(time);
		}

		if (redrawOverview) {
			// Perform a deep copy of the microdescription model
			md = iMicroDescManager.copy();

			// Compute the view according to the new parameter value
			iMicroDescManager.computeParts();
			
			this.spaceOperator.setOcelotlCore(ocelotlView.getOcelotlCore());

			//spaceOperator = ocelotlView.getOcelotlCore().getSpaceOperator().copy();
			createDiagram(iMicroDescManager, time);
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
	public boolean newTimeRegionLonger(TimeRegion time)
	{
		if(time.getTimeStampStart() < globalTimeRegion.getTimeStampStart() || time.getTimeStampEnd() > globalTimeRegion.getTimeStampEnd())
		{
			long newDuration = time.getTimeStampEnd() - time.getTimeStampStart();
			long currentDuration = globalTimeRegion.getTimeStampEnd() - globalTimeRegion.getTimeStampStart();
			
			if(newDuration > currentDuration)
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
		this.timeLineView.setRoot(root);
		this.timeLineView.setCanvas(canvas);
		this.redrawOverview = true;
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

	public ISpaceAggregationOperator getSpaceOperator() {
		return spaceOperator;
	}

	public void setSpaceOperator(ISpaceAggregationOperator spaceOperator) {
		this.spaceOperator = spaceOperator;
	}

	
	public void setSelectedOperator(String spaceOperator) {
		final Bundle mybundle = Platform.getBundle(ocelotlView.getOcelotlCore().getSpaceOperators().getOperatorList().get(
				spaceOperator).getBundle());
		try {
			this.spaceOperator = (ISpaceAggregationOperator) mybundle.loadClass(
					ocelotlView.getOcelotlCore().getSpaceOperators().getOperatorList().get(spaceOperator).getOperatorClass())
					.newInstance();

		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	//	this.spaceOperator.setOcelotlCore(ocelotlView.getOcelotlCore());
	}
	
	private class SelectFigure extends RectangleFigure {

		Color foreground;
		Color background;
		
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
			root.setConstraint(this,
					new Rectangle(new Point((int) ((timeRegion.getTimeStampStart() - globalTimeRegion.getTimeStampStart()) * (root.getSize().width - 2 * Border) / globalTimeRegion.getTimeDuration() + Border), root.getSize().height), new Point(
							(int) ((timeRegion.getTimeStampEnd() - globalTimeRegion.getTimeStampStart()) * (root.getSize().width - 2 * Border) / globalTimeRegion.getTimeDuration() + Border), 2)));
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
