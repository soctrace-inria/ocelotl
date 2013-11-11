package fr.inria.soctrace.tools.ocelotl.ui.views.matrixview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import fr.inria.soctrace.tools.ocelotl.core.generic.spaceaggregop.StateDistribution;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.PartMap;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.ITimeAggregationOperator;


public class MultiState {

	private int					index;
	private static final int	Border	= 10;
	private static final double	MinHeight	= 5.0;
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
		double total=0;
		double y0=root.getSize().height;
		double y1=(9.0/10.0)*root.getSize().height;
		double x0=root.getSize().width - 2 * Border;
		double d=distribution.getSliceNumber();
		double m=distribution.getMax();
		double agg=0;
		List<String> aggList=new ArrayList<String>();
		List<String> states=new ArrayList<String>();
		states.addAll(distribution.getStates());
		Collections.sort(states, new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		for (String state : states){
			double value=((PartMap) distribution.getPart(index).getData()).getElements().get(state);
			if (value>0){
				System.out.println("Part " + index + " " + state + " " + value);
				RectangleFigure rect = new RectangleFigure();
				rect.setBackgroundColor(ColorConstants.white);
				if (MPIColors.Colors.containsKey(state))
					rect.setBackgroundColor(MPIColors.Colors.get(state));
				else
					rect.setBackgroundColor(ColorConstants.black);
				rect.setForegroundColor(ColorConstants.white);
				Label label = new Label(state);
				rect.setToolTip(label);
				if ((y1 * (double) (value)/m)>MinHeight){
				root.add(rect, new Rectangle(
						new Point((int) (distribution.getPart(index).getStartPart() * x0 / d + Border), (int) (y0 - ( y1 * (double) (total)/ m))),
						new Point((int) (distribution.getPart(index).getEndPart() * x0 / d - space +1 + Border), (int) (y0 + 1 - ( y1 * (double) (total+ value) / m)))));
				total+=value;
				}
				else{
					agg+=value;
					aggList.add(state);
				}
				label.getUpdateManager().performUpdate();
				rect.getUpdateManager().performUpdate();
			}			
		}
		if (agg!=0){
			System.out.println("Part " + index + " " + "Aggregate" + " " + agg);
			RectangleFigure rect = new RectangleFigure();
			rect.setBackgroundColor(ColorConstants.black);
			rect.setForegroundColor(ColorConstants.white);
			String aggString="";
			for (int i=0; i<aggList.size()-1; i++)
				aggString=aggString + aggList.get(i) + "; ";
			aggString=aggString + aggList.get(aggList.size()-1);
			Label label = new Label(aggString);
			rect.setToolTip(label);
			agg=Math.max((y1 * (double) agg), MinHeight*m)/y1;
			root.add(rect, new Rectangle(
					new Point((int) (distribution.getPart(index).getStartPart() * x0 / d + Border), (int) (y0 - ( y1 * (double) (total)/ m))),
					new Point((int) (distribution.getPart(index).getEndPart() * x0 / d - space +1 + Border), (int) (y0 - ( y1 * (double) (total+ agg) / m)))));
			label.getUpdateManager().performUpdate();
			rect.getUpdateManager().performUpdate();
		}
	}

	public void setIndex(final int index) {
		this.index = index;
	}

}