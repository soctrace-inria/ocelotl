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

package fr.inria.soctrace.tools.ocelotl.spaceaggregop.operators.stateproportion.views;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.PartMap;
import fr.inria.soctrace.tools.ocelotl.spaceaggregop.operators.stateproportion.StateProportion;

public class MultiState {

	private int					index;
	private static final int	Border		= 10;
	private static final double	MinHeight	= 5.0;
	private int					MIN		= 6;
	private int					MAX		= 32;
	private int					space		= 6;
	private StateProportion				distribution;
	private IFigure						root;
	private final String				DANGER;
	private Device device;
	private Image image;

	public MultiState(final int index, final StateProportion distribution, final IFigure root, final int space) {
		super();
		setIndex(index);
		this.distribution = distribution;
		this.root = root;
		this.space = space;
		this.device=Display.getCurrent();
		Bundle bundle = Platform.getBundle("fr.inria.soctrace.tools.ocelotl.spaceaggregop");
		URL fileURL = bundle.getEntry("icons/warning.ico");
		File file = null;
		try {
		    file = new File(FileLocator.resolve(fileURL).toURI());
		} catch (URISyntaxException e1) {
		    e1.printStackTrace();
		} catch (IOException e1) {
		    e1.printStackTrace();
		}
		DANGER=file.getAbsolutePath();
		image = new Image(device, DANGER);
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
			final ImageFigure rect = new ImageFigure();
			final RectangleFigure rectAlt = new RectangleFigure();
			rect.setBackgroundColor(ColorConstants.black);
			rect.setForegroundColor(ColorConstants.white);
			rectAlt.setBackgroundColor(ColorConstants.black);
			rectAlt.setForegroundColor(ColorConstants.white);
			
			
			String aggString = " ";
			for (int i = 0; i < aggList.size() - 1; i++)
				aggString = aggString + aggList.get(i) + " + ";
			aggString = aggString + aggList.get(aggList.size() - 1) + " ";
			final Label label = new Label(aggString);
			rect.setToolTip(label);
			rectAlt.setToolTip(label);
				agg = Math.min(MAX,Math.min(Math.min(x0/d-2*space, (y0 - y1 * total / m)), Math.min(image.getBounds().height, image.getBounds().width)));
				rect.setImage(new Image(device, image.getImageData().scaledTo((int)agg, (int)agg)));
				rect.setSize((int)agg, (int)agg);
				rectAlt.setSize((int)(x0/d-2*space), (int)MinHeight);
				if (agg>MIN)
				root.add(rect, new Rectangle(new Point((int) (distribution.getPart(index).getStartPart() * x0 / d + Border), (int) (y0 - y1 * total / m)), new Point((int) (distribution.getPart(index).getEndPart() * x0 / d - space + 1 + Border), (int) (y0 - y1
					* (total) / m)-(int)agg)));		
				else
					root.add(rectAlt, new Rectangle(new Point((int) (distribution.getPart(index).getStartPart() * x0 / d + Border), (int) (y0 - y1 * total / m)), new Point((int) (distribution.getPart(index).getEndPart() * x0 / d - space + 1+ Border), (int) (y0 - y1
							* (total) / m)-(int) MinHeight)));
			label.getUpdateManager().performUpdate();
			rect.getUpdateManager().performUpdate();
		}
	}

	public void setIndex(final int index) {
		this.index = index;
	}

}