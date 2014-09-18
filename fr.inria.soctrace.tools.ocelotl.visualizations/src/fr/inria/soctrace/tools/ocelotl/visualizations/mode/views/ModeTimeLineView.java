package fr.inria.soctrace.tools.ocelotl.visualizations.mode.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.PartMap;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.matrixproportion.MajState;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.Proportion;

public class ModeTimeLineView extends TimeLineView {

	private Proportion distribution;
	
	public ModeTimeLineView(OcelotlView ocelotlView) {
		super(ocelotlView);
	}

	@Override
	protected void computeDiagram() {
		distribution = (Proportion) ocelotlView.getCore().getSpaceOperator();
		int i;
		final List<Integer> aggParts = new ArrayList<Integer>();
		for (i = 0; i <= parts.get(parts.size() - 1); i++)
			aggParts.add(0);
		for (i = 0; i < parts.size(); i++)
			aggParts.set(parts.get(i), aggParts.get(parts.get(i)) + 1);
		int j = 0;
		for (i = 0; i < aggParts.size(); i++) {
			final ModeFigure part = new ModeFigure();
			figures.add(part);
			root.add(part, new Rectangle(new Point(j
					* (root.getSize().width - 2 * Border) / parts.size()
					+ Border, root.getSize().height - Border), new Point(
					(j + aggParts.get(i)) * (root.getSize().width - 2 * Border)
							/ parts.size() - space + Border, Border)));
			j = j + aggParts.get(i);
			part.getUpdateManager().performUpdate();
			MajState mState = getMajState(i);//distribution.getPart(i).getStartPart(), distribution.getPart(i).getEndPart());
			part.draw(mState);
		}	
	}

	/**
	 * For a given part, compute the state with the maximal proportion or the
	 * event with the maximal occurrences
	 * 
	 * @param index
	 *            the index of the current part
	 * @return the dominant state or event
	 */
	public MajState getMajState(int index) { 
		double max = 0.0;
		double tempMax;
		MajState maj = new MajState("void", max);
		tempMax = 0.0;
		for (String state : distribution.getStates()) {
			tempMax = ((PartMap) distribution.getPart(index).getData())
					.getElements().get(state);
			if (tempMax > max) {
				maj = new MajState(state, tempMax);
				max = tempMax;
			}
		}
		return maj;
	}
	
}
