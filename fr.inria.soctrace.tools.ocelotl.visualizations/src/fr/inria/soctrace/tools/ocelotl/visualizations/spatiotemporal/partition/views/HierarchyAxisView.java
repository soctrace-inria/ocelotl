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

package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.partition.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.UnitAxisView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.VisuSTOperator;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.IVisuOperator;

/**
 * Time Axis View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class HierarchyAxisView extends UnitAxisView {

	private class SelectFigure extends RectangleFigure {

		public SelectFigure() {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			setAlpha(50);
		}

		public void draw(final boolean active) {
			if (active) {
				setForegroundColor(AggregatedView.activeColorFG);
				setBackgroundColor(AggregatedView.activeColorBG);
			} else {
				setForegroundColor(AggregatedView.selectColorFG);
				setBackgroundColor(AggregatedView.selectColorBG);
			}
			root.add(this,
					new Rectangle(new Point(0, 0), new Point(20,
							root.getSize().height - 2)));
		}
	}

	protected EventProducerHierarchy hierarchy;
	protected final static int width = 30;
	// Margin from the frame
	protected final static int border = 10;
	protected final static int unitAxisWidth = 1;
	protected final static long divide = 10;

	protected ArrayList<EventProducerNode> producers;

	// Level of hierarchy to display
	protected int hierarchyLevel;
	// Number of graduations to display
	protected int gradNumber = 10;
	// Value of a grad
	protected int gradDuration = 1;
	protected int producerSkipped;
	// Width of a graduation
	protected double gradWidth = 10;
	// Make sure that grad are centered around a state
	protected double verticalOffset;

	protected final static double gradHeightMin = 5.0;
	protected double gradHeight;
	protected int textWidth;
	protected int textHeight = 15;
	protected int mainLinePosition;
	protected final static double textHeightMin = 16.0;
	protected final static int textPositionOffset = 15;
	protected int areaWidth = 0;
	protected SelectFigure selectFigure;

	public HierarchyAxisView() {
		super();
		selectFigure = new SelectFigure();
	}

	public void createDiagram(EventProducerHierarchy hierarchy) {
		root.removeAll();
		this.hierarchy = hierarchy;
		if (!hierarchy.getEventProducerNodes().isEmpty()) {
			drawMainLine();
			drawGrads();
		}
		canvas.update();
	}

	@Override
	public void createDiagram(IVisuOperator manager) {
		root.removeAll();
		VisuSTOperator propOperator = (VisuSTOperator) manager;
		hierarchy = propOperator.getHierarchy();
		if (!hierarchy.getEventProducerNodes().isEmpty()) {
			drawMainLine();
			drawGrads();
		}
		canvas.update();
	}

	/**
	 * Draw the graduation and the name of the event producers
	 */
	public void drawGrads() {
		hierarchyLevel = hierarchy.getMaxHierarchyLevel();
		producers = new ArrayList<EventProducerNode>();
		producers.addAll(hierarchy
				.getEventProducerNodesFromHierarchyLevel(hierarchyLevel));
		sortPorducers();
		computeGradMeasure();

		areaWidth = root.getClientArea().width();
		textWidth = areaWidth - (areaWidth - mainLinePosition) - textPositionOffset;

		// Draw the graduations
		for (int i = 0; i < gradNumber; i++) {
			final PolylineConnection line = new PolylineConnection();
			line.setForegroundColor(SWTResourceManager
					.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			line.setLineWidth(1);
			line.setEndpoints(new Point((int) (mainLinePosition - gradWidth),
					(int) (i * gradHeight + verticalOffset) + border),
					new Point(mainLinePosition,
							(int) (i * gradHeight + verticalOffset) + border));
			root.add(line);

			// Draw legends
			// Do we have enough place, or should we skip some event producers
			if (producerSkipped == 0 || i % producerSkipped == 0) {
				final String text = producers.get(i).getMe().getName();

				final Label label = new Label(text);
				label.setLabelAlignment(PositionConstants.RIGHT);
				label.setTextAlignment(PositionConstants.RIGHT);
				label.setTextPlacement(PositionConstants.RIGHT);
				label.setForegroundColor(SWTResourceManager
						.getColor(SWT.COLOR_WIDGET_FOREGROUND));
				label.setFont(SWTResourceManager.getFont("Cantarell", 8,
						SWT.NORMAL));
				label.setToolTip(new Label(text));
				label.setSize(textWidth, textHeight);

				root.add(label, new Rectangle(new Point(mainLinePosition
						- textPositionOffset - textWidth,
						(int) (i * gradHeight + textHeight) + border),
						new Point(mainLinePosition - textPositionOffset,
								(int) (i * gradHeight) + border)));
			}
		}
		
		final PolylineConnection line = new PolylineConnection();
		line.setForegroundColor(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		line.setLineWidth(1);
		line.setEndpoints(new Point((int) (mainLinePosition - gradWidth),
				(int) (gradNumber * gradHeight + verticalOffset) + border),
				new Point(mainLinePosition,
						(int) (gradNumber * gradHeight + verticalOffset) + border));
		root.add(line);
	}

	/**
	 * Draw the main line of the axis
	 */
	public void drawMainLine() {
		mainLinePosition = root.getClientArea().width() - border;
		final PolylineConnection line = new PolylineConnection();
		line.setForegroundColor(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		line.setLineWidth(1);
		line.setEndpoints(new Point(mainLinePosition, root.getSize().height()
				- border), new Point(mainLinePosition, border));
		root.add(line);
	}

	/**
	 * Compute the number and size of the graduations to draw
	 */
	public void computeGradMeasure() {
		final int nbProducers = producers.size();

		// Get the normal height for a graduation with the actual number of
		// producers
		gradHeight = (root.getClientArea().height - 2.0 * border)
				/ (double) nbProducers;

		// If we are under the minimal height
		int tmpNbProducer = (int) Math.min(
				nbProducers,
				(root.getClientArea().height - 2 * border - 1)
						/ Math.max(gradHeight, gradHeightMin));

		// Number of producers to display
		gradNumber = nbProducers;

		// Number of producer skip at each time
		gradDuration = (nbProducers / tmpNbProducer);

		textHeight = (int) Math.max(gradHeight, textHeightMin);
		producerSkipped = (int) (textHeight / gradHeight);
	}

	/**
	 * Sort event producer based on their id
	 */
	public void sortPorducers() {
		Collections.sort(producers,
				new Comparator<EventProducerHierarchy.EventProducerNode>() {

					@Override
					public int compare(final EventProducerNode arg0,
							final EventProducerNode arg1) {
						int diff = arg1.getMe().getId() - arg0.getMe().getId();

						return diff;
					}
				});
	}

	public Canvas initDiagram(final Composite parent) {
		root = new Figure();
		root.setFont(parent.getFont());
		final XYLayout layout = new XYLayout();
		root.setLayoutManager(layout);
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.gray);
		canvas.setSize(parent.getSize());
		final LightweightSystem lws = new LightweightSystem(canvas);
		lws.setContents(root);
		lws.setControl(canvas);
		root.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		root.setSize(parent.getSize().x, parent.getSize().y);
		return canvas;
	}

	public void resizeDiagram() {
		createDiagram(hierarchy);
		root.repaint();
	}

	public void select(final boolean active) {
		selectFigure.draw(active);
	}

	public void unselect() {
		resizeDiagram();
	}
}
