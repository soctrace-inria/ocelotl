package fr.inria.soctrace.tools.ocelotl.visualizations.mode.views;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.mode.MajState;

public class ModeFigure extends RectangleFigure {

	public ModeFigure() {
		super();
	}

	// Draw the part visualization of the aggregates
	public void draw(MajState majState) {
		removeAll();
		final RectangleFigure roundedRectangle = new RectangleFigure();
		roundedRectangle.setLineWidth(1);
		final ToolbarLayout roundedLayout = new ToolbarLayout();
		roundedRectangle.setLayoutManager(roundedLayout);
		roundedRectangle.setPreferredSize(1000, 1000);
		this.add(roundedRectangle);
		final Label label = new Label(" " + majState.getState() + ": " + majState.getAmplitude());
		roundedRectangle.setToolTip(label);
		final ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
		setConstraint(roundedRectangle, getBounds());
		setLayoutManager(layout);
		setLineWidth(0);
		setForegroundColor(ColorConstants.black);
		setBackgroundColor(FramesocColorManager.getInstance()
				.getEventTypeColor(majState.getState()).getSwtColor());
	}
}
