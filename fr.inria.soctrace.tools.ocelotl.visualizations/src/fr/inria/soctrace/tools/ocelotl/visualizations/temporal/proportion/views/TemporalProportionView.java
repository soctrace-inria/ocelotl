/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
 * overview by using aggregation techniques
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

package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.proportion.views;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.PartMap;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineView;
import fr.inria.soctrace.tools.ocelotl.visualizations.temporal.proportion.TemporalProportion;

public class TemporalProportionView extends TimeLineView {

	TemporalProportion distribution;
	
	public class EventStack {

		private int index;
		//Minimal height for a rectangle to be displayed
		private static final int MinHeight = 6;
		//Minimal size of the icon to be displayed
		private static final int IconMin = 6;
		private static final int IconMax = 32;
		private static final double DrawingMarginRatio = OcelotlConstants.TemporalProportionDrawingMarginRatio;
		private int stackSpace = 2;
		private IconManager iconManager;
		//Minimum value to consider a color too light
		private static final int Light = 225;
		
		public EventStack(final int index, final int space) {
			super();
			setIndex(index);
			this.stackSpace = space / 2;
			this.iconManager = new IconManager();
		}

		public int getIndex() {
			return index;
		}

		public boolean isTooLight(Color color) {
			if (color.getGreen() > Light && color.getBlue() > Light
					&& color.getRed() > Light)
				return true;
			return false;
		}
		
		//Draw the proportion visualization of the aggregates
		public void init() {
			DecimalFormat valueFormat = new DecimalFormat("0.00E0");
			valueFormat.setMaximumIntegerDigits(3);
			double total = 0;
			
			//Height of the drawing area
			final double y0 = root.getSize().height - aBorder;
			final double y1 = DrawingMarginRatio * root.getSize().height - aBorder;
			//Width of the drawing area
			final double x0 = root.getSize().width - 2 * aBorder;
			final double d = distribution.getSliceNumber();
			//Highest value among the aggregates
			final double m = distribution.getMax();
			double agg = 0;
			final List<String> aggList = new ArrayList<String>();
			final List<String> states = new ArrayList<String>();
			states.addAll(distribution.getStates());
			//Sort states alphabetically
			Collections.sort(states, new Comparator<String>() {
				@Override
				public int compare(final String o1, final String o2) {
					return o1.compareTo(o2);
				}
			});
			for (final String state : states) {
				final double value = ((PartMap) distribution.getPart(index)
						.getData()).getElements().get(state);
				if (value > 0) {
					final RectangleFigure rect = new RectangleFigure();
					rect.setBackgroundColor(ColorConstants.white);
					rect.setBackgroundColor(FramesocColorManager.getInstance()
							.getEventTypeColor(state).getSwtColor());
					rect.setForegroundColor(ColorConstants.white);
					rect.setLineWidth(1);
					//If the color is too light add a border
					if (isTooLight(rect.getBackgroundColor())) {
						rect.setForegroundColor(ColorConstants.black);
						rect.setLineWidth(1);
					}
					final Label label = new Label(" " + state + ": "
							+ valueFormat.format(value) + " ");
					rect.setToolTip(label);
					//If the height of the state proportion is big enough
					if (y1 * value / m - stackSpace > MinHeight) {
						//Draw a rectangle
						if (isTooLight(rect.getBackgroundColor()))
							 root.add(rect, new Rectangle(new Point(
							 (int) (distribution.getPart(index)
							 .getStartPart() * x0 / d + aBorder + 1),
							 (int) (y0 - y1 * total / m)), new Point(
							 (int) (distribution.getPart(index).getEndPart()
							 * x0 / d - space + aBorder - 1),
							 (int) (y0 + space - y1 * (total + value) / m))));
						else {
							root.add(rect, new Rectangle(new Point(
								(int) (distribution.getPart(index)
										.getStartPart() * x0 / d + aBorder),
								(int) (y0 - y1 * total / m)), new Point(
								(int) (distribution.getPart(index).getEndPart()
										* x0 / d - stackSpace + aBorder), (int) (y0
										+ stackSpace - y1 * (total + value) / m))));
						}
						total += value;
					} else { // else aggregates it
						agg += value;
						aggList.add(state + ": " + valueFormat.format(value));
					}
					label.getUpdateManager().performUpdate();
					rect.getUpdateManager().performUpdate();
				}
			}
			if (agg != 0) {
				final ImageFigure icon = new ImageFigure();
				final RectangleFigure rectangle = new RectangleFigure();
				icon.setBackgroundColor(ColorConstants.black);
				icon.setForegroundColor(ColorConstants.white);
				rectangle.setBackgroundColor(ColorConstants.black);
				rectangle.setForegroundColor(ColorConstants.white);

				String aggString = " ";
				for (int i = 0; i < aggList.size() - 1; i++)
					aggString = aggString + aggList.get(i) + " \n ";
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
				//If the aggregated state proportion is high enough
				if (y1 * agg / m - stackSpace > MinHeight) {
					//Display a rectangle
					root.add(rectangle, new Rectangle(new Point((int) (distribution
							.getPart(index).getStartPart() * x0 / d + aBorder),
							(int) (y0 - y1 * total / m)), new Point(
							(int) (distribution.getPart(index).getEndPart() * x0
									/ d - stackSpace + aBorder), (int) (y0 + stackSpace - y1
									* (total + agg) / m))));
				} else { // else display as a dash line and an icon
					int size = (int) Math.min(IconMax,
							Math.min(x0 / d - 2 * stackSpace, (y0 - y1 * total / m)));
					if (size > IconMin) {
						icon.setImage(iconManager.getImage(size));

						lineDash.setEndpoints(new Point(
								(int) (distribution.getPart(index).getStartPart()
										* x0 / d + aBorder + 1), (int) (y0 - y1
										* total / m)), new Point(
								(int) (distribution.getPart(index).getEndPart()
										* x0 / d - stackSpace - 1 + aBorder),
								(int) (y0 - y1 * (total) / m)));
						root.add(lineDash);
						root.add(icon, new Rectangle(new Point((int) (distribution
								.getPart(index).getStartPart() * x0 / d + aBorder),
								(int) (y0 - y1 * total / m) - stackSpace), new Point(
								(int) (distribution.getPart(index).getEndPart()
										* x0 / d - stackSpace + aBorder), (int) (y0 - y1
										* (total) / m)
										- size - stackSpace)));
					} else {
						lineDash.setEndpoints(new Point(
								(int) (distribution.getPart(index).getStartPart()
										* x0 / d + aBorder + 1), (int) (y0 - y1
										* total / m)), new Point(
								(int) (distribution.getPart(index).getEndPart()
										* x0 / d - stackSpace - 1 + aBorder),
								(int) (y0 - y1 * (total) / m)));
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
	
	public TemporalProportionView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void computeDiagram() {
		distribution = (TemporalProportion) ocelotlView.getCore()
				.getVisuOperator();
		for (int i = 0; i < distribution.getPartNumber(); i++) {
			final EventStack part = new EventStack(i, space);
			part.init();
		}
	}

}
