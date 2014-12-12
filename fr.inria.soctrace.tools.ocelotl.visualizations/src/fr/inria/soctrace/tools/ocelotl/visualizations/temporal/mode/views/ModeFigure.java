package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.mode.views;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.MainEvent;

public class ModeFigure extends RectangleFigure {

	public ModeFigure() {
		super();
	}

	// Draw the part visualization of the aggregates
	public void draw(MainEvent mainEvent) {
		removeAll();
		final RectangleFigure roundedRectangle = new RectangleFigure();
		roundedRectangle.setLineWidth(0);
		final ToolbarLayout roundedLayout = new ToolbarLayout();
		roundedRectangle.setLayoutManager(roundedLayout);
		roundedRectangle.setPreferredSize(1000, 1000);
		this.add(roundedRectangle);
		final Label label = new Label(" " + mainEvent.getState() + ": " + mainEvent.getAmplitude100()+"% ");
		roundedRectangle.setToolTip(label);
		final ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
		setConstraint(roundedRectangle, getBounds());
		setLayoutManager(layout);
		setLineWidth(0);
		
		setForegroundColor(FramesocColorManager.getInstance()
				.getEventTypeColor(mainEvent.getState()).getSwtColor());
		setBackgroundColor(FramesocColorManager.getInstance()
				.getEventTypeColor(mainEvent.getState()).getSwtColor());
		setAlpha(mainEvent.getAmplitude255Shifted());
	}
}
