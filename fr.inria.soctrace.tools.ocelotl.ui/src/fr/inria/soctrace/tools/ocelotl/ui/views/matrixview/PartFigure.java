package fr.inria.soctrace.tools.ocelotl.ui.views.matrixview;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.color.OcelotlColor;

public class PartFigure extends RectangleFigure {

	private int					index;
	private final int			value;
	private final OcelotlColor	color;
	private final static int	textSize	= 15;
	private boolean 			numbers;

	public PartFigure(final int index, final int value, final OcelotlColor color, boolean numbers) {
		super();
		setIndex(index);
		this.value = value;
		this.color = color;
		this.numbers = numbers;

	}

	public int getIndex() {
		return index;
	}


	public void init() {
		removeAll();
		final RoundedRectangle roundedRectangle = new RoundedRectangle();
		roundedRectangle.setBackgroundColor(color.getBg());
		roundedRectangle.setForegroundColor(color.getBg());
		roundedRectangle.setLineWidth(15);
		final ToolbarLayout roundedLayout = new ToolbarLayout();
		roundedRectangle.setLayoutManager(roundedLayout);
		roundedRectangle.setPreferredSize(1000, 1000);
		this.add(roundedRectangle);
		final int dim = 0;
		roundedRectangle.setCornerDimensions(new Dimension(dim, dim));
		final Label label = new Label("" + value);
		label.setLabelAlignment(SWT.CENTER);
		label.setForegroundColor(color.getFg());
		roundedRectangle.setFont(SWTResourceManager.getFont("Cantarell", textSize, SWT.BOLD));
		if (numbers)
			if (getSize().width / 2 - 3 > textSize && getSize().height / 2 - 3 > textSize)
				roundedRectangle.add(label);
		final ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
		setConstraint(roundedRectangle, getBounds());
		setLayoutManager(layout);
		setForegroundColor(ColorConstants.white);
		setBackgroundColor(ColorConstants.white);
	}

	public void setIndex(final int index) {
		this.index = index;
	}

}