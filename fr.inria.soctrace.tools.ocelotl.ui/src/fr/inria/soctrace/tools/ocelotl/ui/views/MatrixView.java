/* ===========================================================
 * LPAggreg UI module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to visualize a Paje
 * trace across an aggregated representation.
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

package fr.inria.soctrace.tools.ocelotl.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.color.ColorManager;
import fr.inria.soctrace.tools.ocelotl.ui.color.OcelotlColor;

/**
 * Matrix View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class MatrixView {

	private class PartFigure extends RectangleFigure {

		private TimeRegion			timeRegion;
		private int					index;
		private final int			value;
		private final OcelotlColor	color;
		private final static int	textSize	= 15;

		public PartFigure(final TimeRegion timeRegion, final int index, final int value, final OcelotlColor color) {
			super();
			setTimeRegion(timeRegion);
			setIndex(index);
			this.value = value;
			this.color = color;

		}

		@SuppressWarnings("unused")
		public int getIndex() {
			return index;
		}

		@SuppressWarnings("unused")
		public TimeRegion getTimeRegion() {
			return timeRegion;
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
			// if (Math.min(getSize().height / 2 - 2, getSize().width / 2 - 2) <
			// 8)
			// dim = Math.min(getSize().height / 2, getSize().width / 2);
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

		public void setTimeRegion(final TimeRegion timeRegion) {
			this.timeRegion = timeRegion;
		}

	}

	private Figure					root;
	private Canvas					canvas;
	private final List<PartFigure>	figures		= new ArrayList<PartFigure>();
	private List<Integer>			parts		= null;
	private TimeRegion				time;
	private boolean					aggregated	= false;
	private boolean					numbers		= true;
	// private final static int Height = 100;
	private final static int		Border		= 10;
	private int						Space		= 6;
	private final ColorManager		colors		= new ColorManager();

	public void createDiagram(final List<Integer> parts, final TimeRegion time, final boolean aggregated, final boolean numbers) {
		root.removeAll();
		figures.clear();
		canvas.update();
		this.aggregated = aggregated;
		this.parts = parts;
		this.time = time;
		this.numbers = numbers;
		final int partHeight = (int) (root.getSize().height / 1.1 - Border);
		Space = 6;
		if (parts != null) {
			// if ((root.getSize().width - 2 * Border) / parts.size() <
			// root.getSize().height / 2 - 2 * Border)
			// partHeight = (((root.getSize().width - 2 * Border) /
			// parts.size())>20) ? (root.getSize().width - 2 * Border) /
			// parts.size() : 20;
			while ((root.getSize().width - 2 * Border) / parts.size() - 2 < Space && Space != 0)
				Space = Space - 1;
			if (!aggregated)
				for (int i = 0; i < parts.size(); i++) {
					// TODO manage parts
					final PartFigure part = new PartFigure(new TimeRegion(), i, parts.get(i), colors.getColors().get(parts.get(i) % colors.getColors().size()));
					figures.add(part);
					root.add(part, new Rectangle(new Point(i * (root.getSize().width - 2 * Border) / parts.size() + Border, root.getSize().height / 2 - partHeight / 2), new Point((i + 1) * (root.getSize().width - 2 * Border) / parts.size() + Border - Space,
							root.getSize().height / 2 + partHeight / 2)));
					part.getUpdateManager().performUpdate();
					part.init();
				}
			else {
				final List<Integer> aggParts = new ArrayList<Integer>();
				for (int i = 0; i <= parts.get(parts.size() - 1); i++)
					aggParts.add(0);
				for (int i = 0; i < parts.size(); i++)
					aggParts.set(parts.get(i), aggParts.get(parts.get(i)) + 1);
				int j = 0;
				for (int i = 0; i < aggParts.size(); i++) {
					// TODO manage parts
					final PartFigure part = new PartFigure(new TimeRegion(), i, i, colors.getColors().get(j % colors.getColors().size()));
					figures.add(part);
					root.add(
							part,
							new Rectangle(new Point(j * (root.getSize().width - 2 * Border) / parts.size() + Border, root.getSize().height), new Point((j + aggParts.get(i)) * (root.getSize().width - 2 * Border) / parts.size() - Space + Border, 0 + root
									.getSize().height / 10)));
					j = j + aggParts.get(i);
					part.getUpdateManager().performUpdate();
					part.init();
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private IFigure createPart() {
		final RectangleFigure rectangleFigure = new RectangleFigure();
		rectangleFigure.setBackgroundColor(ColorConstants.lightGray);
		rectangleFigure.setLayoutManager(new ToolbarLayout());
		rectangleFigure.setForegroundColor(ColorConstants.white);
		return rectangleFigure;
	}

	public void deleteDiagram() {
		root.removeAll();
		figures.clear();
		root.repaint();
	}

	public Canvas initDiagram(final Composite parent) {
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
		canvas.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(final ControlEvent arg0) {
				// TODO Auto-generated method stub
				canvas.redraw();
				// root.repaint();
				resizeDiagram();

			}

			@Override
			public void controlResized(final ControlEvent arg0) {
				canvas.redraw();
				// root.repaint();
				resizeDiagram();
			}
		});

		return canvas;
	}

	public void resizeDiagram() {
		createDiagram(parts, time, aggregated, numbers);
		root.repaint();
	}

}
