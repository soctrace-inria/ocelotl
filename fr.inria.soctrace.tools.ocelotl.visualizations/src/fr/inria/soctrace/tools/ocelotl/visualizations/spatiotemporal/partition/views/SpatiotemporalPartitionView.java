/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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
import java.util.List;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.Part;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.SpatioTemporalAggregateView;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.partition.VisualAggregation;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.views.SpatioTemporalView;
import fr.inria.soctrace.tools.ocelotl.visualizations.temporal.partition.views.PartitionColorManager;

public class SpatiotemporalPartitionView extends SpatioTemporalView {
	
	public class DrawPartition extends DrawSpatioTemporal {

		protected PartitionColorManager colors;

		public DrawPartition() {
			super();
			colors = new PartitionColorManager();
			aggregates = new ArrayList<SpatioTemporalAggregateView>();
		}

		@Override
		protected RectangleFigure setRectangle(EventProducerNode epn,
				int startTimeSlice, int endTimeSlice,
				boolean isVisualAggregate, int number) {
			RectangleFigure rectangle = new RectangleFigure();

			if (!isVisualAggregate) {
				rectangle.setBackgroundColor(colors.getColors().get(number)
						.getBg());
				rectangle.setForegroundColor(colors.getColors().get(number)
						.getBg());
			} else {
				rectangle.setBackgroundColor(ColorConstants.black);
				rectangle.setForegroundColor(ColorConstants.black);
			}

			rectangle.setLineWidth(2);

			String labelContent =  " " + epn.getMe().getName() + " ";
			rectangle.setToolTip(new Label(labelContent));
			rectangle.setLayoutManager(new BorderLayout());
			rectangle.setPreferredSize(1000, 1000);
			rectangle
			.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.BOLD));
			
			return rectangle;
		}
	}
	
	public SpatiotemporalPartitionView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		
	}

	@Override
	protected void computeDiagram() {
		if (!hierarchy.getRoot().getParts().isEmpty()) {
			DrawPartition hp = new DrawPartition();
			hp.draw();
		}
	}

	@Override
	protected void computeDiagram(EventProducerNode aNode, int start,
			int end) {
		if (!aNode.getParts().isEmpty()) {
			DrawAggregate hp = new DrawAggregate();
			hp.draw(aNode, start, end);
		}
	}

	public class DrawAggregate extends DrawPartition {
		private EventProducerNode theNode;
		
		private int startingSlice; 
		private int endingSlice;
		
		public DrawAggregate() {
			super();
		}

		public void draw(EventProducerNode aNode, int start,
				int end) {
			startingSlice = start;
			endingSlice = end;
			theNode = aNode;
			rootHeight = root.getSize().height;
			height = rootHeight - (2 * aBorder);
			width = root.getSize().width - (2 * aBorder);
			logicWidth = width / (end - start);
			logicHeight = height / theNode.getWeight();
			initX(theNode);
			initY(theNode);
			print(theNode, startingSlice, endingSlice);
		}

		protected void print(EventProducerNode epn, int start, int end) {
			List<Part> parts = computeParts(epn, start, end);
			for (Part p : parts) {
				if (((VisualAggregation) p.getData()).isAggregated())
					drawAggregate(p.getStartPart() - startingSlice,
							epn.getIndex() - theNode.getIndex(), p.getEndPart()
									- startingSlice, epn.getWeight(),
							((VisualAggregation) p.getData()).getValue(), epn, false, false);
				else {
					boolean aggy = false;
					for (EventProducerNode ep : epn.getChildrenNodes()) {
						if ((ep.getWeight() * logicHeight - space) < minLogicWeight) {
							aggy = true;
							break;
						}
					}
					if (aggy == false)
						printChildren(epn, p.getStartPart(), p.getEndPart());
					else {
						List<Part> aggParts = computeCommonCuts(epn,
								p.getStartPart(), p.getEndPart());
						for (Part pagg : aggParts) {
							drawAggregate(pagg.getStartPart() - startingSlice,
									epn.getIndex() - theNode.getIndex(),
									pagg.getEndPart() - startingSlice,
									epn.getWeight(),
									((VisualAggregation) p.getData())
											.getValue(), epn, true,
									((VisualAggregation) pagg.getData())
											.isNoCutInside());
						}
					}
				}
			}
		}
	}
}
