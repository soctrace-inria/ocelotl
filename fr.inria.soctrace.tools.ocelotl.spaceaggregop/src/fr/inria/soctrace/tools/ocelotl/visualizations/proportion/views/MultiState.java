/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.visualizations.proportion.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.PartMap;
import fr.inria.soctrace.tools.ocelotl.visualizations.proportion.Proportion;

public class MultiState {

	private int					index;
	private static final int	Border		= StateProportionTimeLineView.Border;
	private static final int	MinHeight	= 6;
	private static final int			IconMin		= 6;
	private static final int			IconMax		= 32;
	private int					space		= 6;
	private Proportion		distribution;
	private IFigure				root;
	private IconManager 		iconManager;

	public MultiState(final int index, final Proportion distribution, final IFigure root, final int space) {
		super();
		setIndex(index);
		this.distribution = distribution;
		this.root = root;
		this.space = space;
		this.iconManager = new IconManager();

	}

	public int getIndex() {
		return index;
	}

	public void init() {
		double total = 0;
		final double y0 = root.getSize().height;
		final double y1 = 9.0 / 10.0 * root.getSize().height;
		final double x0 = root.getSize().width - 2 * Border;
		final double d = distribution.getSliceNumber();
		final double m = distribution.getMax();
		double agg = 0;
		final List<String> aggList = new ArrayList<String>();
		final List<String> states = new ArrayList<String>();
		states.addAll(distribution.getStates());
		Collections.sort(states, new Comparator<String>() {
			@Override
			public int compare(final String o1, final String o2) {
				return o1.compareTo(o2);
			}
		});
		for (final String state : states) {
			final double value = ((PartMap) distribution.getPart(index).getData()).getElements().get(state);
			if (value > 0) {
				// System.out.println("Part " + index + " " + state + " " +
				// value);
				final RectangleFigure rect = new RectangleFigure();
				rect.setBackgroundColor(ColorConstants.white);
				rect.setBackgroundColor(FramesocColorManager.getInstance().getEventTypeColor(state).getSwtColor());
				rect.setForegroundColor(ColorConstants.white);
				final Label label = new Label(" " + state + " ");
				rect.setToolTip(label);
				if (y1 * value / m > MinHeight) {
					root.add(rect, new Rectangle(new Point((int) (distribution.getPart(index).getStartPart() * x0 / d + Border), (int) (y0 - y1 * total / m)), new Point((int) (distribution.getPart(index).getEndPart() * x0 / d - space + 1 + Border),
							(int) (y0 + 1 - y1 * (total + value) / m))));
					total += value;
				} else {
					agg += value;
					aggList.add(state);
				}
				label.getUpdateManager().performUpdate();
				rect.getUpdateManager().performUpdate();
			}
		}
		if (agg != 0) {
			// System.out.println("Part " + index + " " + "Aggregate" + " " +
			// agg);
			final ImageFigure icon = new ImageFigure();
			final RectangleFigure rectangle = new RectangleFigure();
			icon.setBackgroundColor(ColorConstants.black);
			icon.setForegroundColor(ColorConstants.white);
			rectangle.setBackgroundColor(ColorConstants.black);
			rectangle.setForegroundColor(ColorConstants.white);
			
			
			String aggString = " ";
			for (int i = 0; i < aggList.size() - 1; i++)
				aggString = aggString + aggList.get(i) + " + ";
			aggString = aggString + aggList.get(aggList.size() - 1) + " ";
			final Label label = new Label(aggString);
			icon.setToolTip(label);
			rectangle.setToolTip(label);
			final PolylineConnection lineDash = new PolylineConnection();
			lineDash.setBackgroundColor(ColorConstants.black);
			lineDash.setForegroundColor(ColorConstants.black);
			lineDash.setLineWidth(2);
			lineDash.setLineStyle(SWT.LINE_DASH);
			lineDash.setToolTip(label);
				if (y1 * agg /m > MinHeight){
					root.add(rectangle, new Rectangle(new Point((int) (distribution.getPart(index).getStartPart() * x0 / d + Border), (int) (y0 - y1 * total / m)), new Point((int) (distribution.getPart(index).getEndPart() * x0 / d - space + 1 + Border),
							(int) (y0 + 1 - y1 * (total + agg) / m))));
				}
				else{
					int size = (int) Math.min(IconMax,Math.min(x0/d-2*space, (y0 - y1 * total / m)));
				if (size>IconMin){
					icon.setImage(iconManager.getImage(size));

					lineDash.setEndpoints(new Point((int) (distribution.getPart(index).getStartPart() * x0 / d + Border + 1), (int) (y0 - y1 * total / m) - 1), new Point((int) (distribution.getPart(index).getEndPart() * x0 / d - space + Border), (int) (y0 - y1
							* (total) / m)- 1));
					root.add(lineDash);
				root.add(icon, new Rectangle(new Point((int) (distribution.getPart(index).getStartPart() * x0 / d + Border), (int) (y0 - y1 * total / m) - 2), new Point((int) (distribution.getPart(index).getEndPart() * x0 / d - space + 1 + Border), (int) (y0 - y1
					* (total) / m)- size - 2)));
				}
				else{
					lineDash.setEndpoints(new Point((int) (distribution.getPart(index).getStartPart() * x0 / d + Border + 1), (int) (y0 - y1 * total / m) - 1), new Point((int) (distribution.getPart(index).getEndPart() * x0 / d - space + Border), (int) (y0 - y1
							* (total) / m)- 1));
					root.add(lineDash);
				}
			
				}
			label.getUpdateManager().performUpdate();
			icon.getUpdateManager().performUpdate();
				
		}
	}

	public void setIndex(final int index) {
		this.index = index;
	}

}