package fr.inria.soctrace.tools.ocelotl.ui.views.matrixview;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.core.generic.spaceaggregop.StateDistribution;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.color.OcelotlColor;

public class MultiState {

	private int					index;
	private static final int	Border	= 10;
	private int	space	= 6;
	StateDistribution distribution;
	IFigure root;

	public MultiState(final int index, StateDistribution distribution, IFigure root, int space) {
		super();
		setIndex(index);
		this.distribution = distribution;
		this.root = root;
		this.space=space;

	}

	public int getIndex() {
		return index;
	}


	public void init() {
		RectangleFigure bg = new RectangleFigure();
		bg.setBackgroundColor(ColorConstants.white);
		bg.setForegroundColor(ColorConstants.white);
		double total=0;
		root.add(bg, new Rectangle(
				new Point(distribution.getPart(index).getStartPart() * (root.getSize().width - 2 * Border) / distribution.getSliceNumber() + Border, 0), new Point(
						(distribution.getPart(index).getEndPart()) * (root.getSize().width - 2 * Border) / distribution.getSliceNumber() - space +1 + Border, root.getSize().height)));
		for (String state : distribution.getStates()){
			double value=((PartMap) distribution.getPart(index).getData()).getElements().get(state);
			if (value>0){
				System.out.println(state + " " + value);
				RectangleFigure rect = new RectangleFigure();
				rect.setBackgroundColor(ColorConstants.white);
				if (MPIColors.Colors.containsKey(state))
					rect.setBackgroundColor(MPIColors.Colors.get(state));
				else
					rect.setBackgroundColor(ColorConstants.black);
				rect.setForegroundColor(ColorConstants.white);
				root.add(rect, new Rectangle(
						new Point(distribution.getPart(index).getStartPart() * (root.getSize().width - 2 * Border) / distribution.getSliceNumber() + Border, root.getSize().height - (int)((double) 9*root.getSize().height/10 * total / distribution.getMax())), new Point(
								(distribution.getPart(index).getEndPart()) * (root.getSize().width - 2 * Border) / distribution.getSliceNumber() - space +1 + Border, root.getSize().height - (int)(2 + (double) 9*root.getSize().height/10 * (total+value) / distribution.getMax()))));
				total+=value;
				rect.getUpdateManager().performUpdate();
			}
		}
	}

	public void setIndex(final int index) {
		this.index = index;
	}

}