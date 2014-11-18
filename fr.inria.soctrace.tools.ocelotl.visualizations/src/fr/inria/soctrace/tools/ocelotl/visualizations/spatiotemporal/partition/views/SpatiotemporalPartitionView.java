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
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.Aggregation;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.Part;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.MatrixView;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.partition.VisualAggregation;
import fr.inria.soctrace.tools.ocelotl.visualizations.temporal.partition.views.PartitionColorManager;

public class SpatiotemporalPartitionView extends MatrixView {
	
	public class DrawPartition {

		private double rootHeight;
		private double height;
		private double width;
		private double logicWidth;
		private double logicHeight;
		private int minLogicWeight = 3;
		private PartitionColorManager colors;
		private List<Integer> xendlist;

		public DrawPartition() {
			super();
			xendlist = new ArrayList<Integer>();
			colors = new PartitionColorManager();
		}

		private void initX() {
			xendlist.clear();
			for (int i = 0; i <= hierarchy.getRoot().getParts().size(); i++)
				xendlist.add((int) (i * logicWidth + aBorder - space));
		}

		public void draw() {
			rootHeight = root.getSize().height;
			height = rootHeight - (2 * aBorder);
			width = root.getSize().width - (2 * aBorder);
			logicWidth = width / (hierarchy.getRoot().getParts().size());
			logicHeight = height / hierarchy.getRoot().getWeight();
			initX();
			print(hierarchy.getRoot().getID(), 0, hierarchy.getRoot().getParts()
					.size());
		}

		private List<Part> computeParts(EventProducerNode epn, int start, int end) {
			List<Part> parts = new ArrayList<Part>();
			int oldPart = epn.getParts().get(start);
			parts.add(new Part(start, start + 1, new VisualAggregation(false,
					epn.getParts().get(start) != -1, epn.getParts().get(start),
					true)));
			for (int i = start + 1; i < end; i++) {
				if (epn.getParts().get(i) == oldPart) {
					parts.get(parts.size() - 1).incrSize();

				} else {
					oldPart = epn.getParts().get(i);
					parts.add(new Part(i, i + 1, null));
					parts.get(parts.size() - 1).setData(
							new VisualAggregation(false,
									epn.getParts().get(i) != -1, epn.getParts()
											.get(i), true));

				}
			}
			return parts;
		}

		private void print(int id, int start, int end) {
			EventProducerNode epn = hierarchy.getEventProducerNodes().get(id);
			List<Part> parts = computeParts(epn, start, end);
			for (Part p : parts) {
				if (((VisualAggregation) p.getData()).isAggregated())
					drawStandardAggregate(p.getStartPart(), epn.getIndex(),
							p.getEndPart(), epn.getWeight(),
							((VisualAggregation) p.getData()).getValue(), epn
									.getMe().getName());
				else {
					boolean aggy = false;
					for (EventProducerNode ep : epn.getChildrenNodes()) {
						if ((ep.getWeight() * logicHeight - space) < minLogicWeight) {
							aggy = true;
							break;
						}
					}
					if (aggy == false)
						printChildren(id, p.getStartPart(), p.getEndPart());
					else {
						List<Part> aggParts = computeCommonCuts(epn,
								p.getStartPart(), p.getEndPart());
						for (Part pagg : aggParts) {
							if (((VisualAggregation) pagg.getData())
									.isNoCutInside())
								drawCleanVisualAggregate(pagg.getStartPart(),
										epn.getIndex(), pagg.getEndPart(),
										epn.getWeight(),
										((VisualAggregation) p.getData())
												.getValue(), epn.getMe().getName());
							else
								drawNotCleanVisualAggregate(pagg.getStartPart(),
										epn.getIndex(), pagg.getEndPart(),
										epn.getWeight(),
										((VisualAggregation) p.getData())
												.getValue(), epn.getMe().getName());
						}
					}
				}
			}

		}

		private List<Part> computeCommonCuts(EventProducerNode epn, int start,
				int end) {
			HashMap<EventProducerNode, List<Part>> hm = new HashMap<EventProducerNode, List<Part>>();
			List<Part> commonParts = new ArrayList<Part>();
			List<Part> parts = new ArrayList<Part>();
			for (EventProducerNode child : epn.getChildrenNodes()) {
				if (child.isAggregated() == Aggregation.FULL)
					hm.put(child, computeParts(child, start, end));
				else
					hm.put(child, computeCommonCuts(child, start, end));
			}
			List<Part> testPart = hm.get(epn.getChildrenNodes().get(0));
			for (Part p : testPart) {
				boolean commonCut = false;
				boolean cleanCut = true;
				for (EventProducerNode child : epn.getChildrenNodes()) {
					commonCut = false;
					for (Part p2 : hm.get(child)) {
						if (p.compare(p2)) {
							commonCut = true;
							if (((VisualAggregation) p2.getData())
									.isVisualAggregate()
									&& !((VisualAggregation) p2.getData())
											.isNoCutInside())
								cleanCut = false;
							break;
						}

					}
					if (commonCut == false)
						break;
				}
				if (commonCut) {
					commonParts.add(new Part(p.getStartPart(), p.getEndPart(),
							new VisualAggregation(true, false, -1, cleanCut)));
				}
			}
			if (commonParts.isEmpty()) {
				parts.add(new Part(start, end, new VisualAggregation(true,
						false, -1, false)));
				return parts;
			}
			if (commonParts.get(0).getStartPart() != start)
				parts.add(new Part(start, commonParts.get(0).getStartPart(),
						new VisualAggregation(true, false, -1, false)));
			for (Part ptemp : commonParts) {
				if (parts.size() > 0
						&& parts.get(parts.size() - 1).getEndPart() != ptemp
								.getStartPart())
					parts.add(new Part(parts.get(parts.size() - 1).getEndPart(),
							ptemp.getStartPart(), new VisualAggregation(true,
									false, -1, false)));
				parts.add(ptemp);
			}
			if (parts.get(parts.size() - 1).getEndPart() != end)
				parts.add(new Part(parts.get(parts.size() - 1).getEndPart(), end,
						new VisualAggregation(true, false, -1, false)));
			return parts;
		}

		private void printChildren(int id, int start, int end) {
			for (EventProducerNode ep : hierarchy.getEventProducerNodes().get(id)
					.getChildrenNodes())
				print(ep.getID(), start, end);
		}

		private void drawStandardAggregate(int logicX, int logicY, int logicX2,
				int sizeY, int number, String name) {
			final RectangleFigure rectangle = new RectangleFigure();
			rectangle.setBackgroundColor(colors.getColors().get(number).getBg());
			rectangle.setForegroundColor(colors.getColors().get(number).getBg());
			rectangle.setLineWidth(2);

			rectangle.setToolTip(new Label(" " + name + " "));
			int xa = (int) ((logicX * logicWidth + aBorder));
			int ya = (int) (rootHeight - height + logicY * logicHeight - aBorder);
			int xb = xendlist.get(logicX2);
			int yb = (int) (ya + sizeY * logicHeight) - space;
			root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb, yb)));
		}

		private void drawNotCleanVisualAggregate(int logicX, int logicY,
				int logicX2, int sizeY, int number, String name) {
			final RectangleFigure rectangle = new RectangleFigure();
			rectangle.setBackgroundColor(ColorConstants.white);
			rectangle.setForegroundColor(ColorConstants.black);

			rectangle.setLineWidth(2);

			rectangle.setToolTip(new Label(" " + name + " "));
			rectangle.setLayoutManager(new BorderLayout());
			rectangle.setPreferredSize(1000, 1000);
			Label lab = new Label("?");
			lab.setTextAlignment(PositionConstants.CENTER);
			lab.setLabelAlignment(SWT.CENTER);
			lab.setForegroundColor(ColorConstants.red);
			rectangle
					.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.BOLD));
			// rectangle.add(lab, BorderLayout.CENTER);
			int xa = (int) ((logicX * logicWidth + aBorder));
			int ya = (int) (rootHeight - height + logicY * logicHeight - aBorder);
			int xb = xendlist.get(logicX2);
			int yb = (int) (ya + sizeY * logicHeight) - space;
			root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb, yb)));
			final PolylineConnection line = new PolylineConnection();
			line.setBackgroundColor(ColorConstants.black);
			line.setForegroundColor(ColorConstants.black);
			line.setEndpoints(new Point(xa, ya), new Point(xb, yb));
			root.add(line);
			final PolylineConnection line2 = new PolylineConnection();
			line2.setBackgroundColor(ColorConstants.black);
			line2.setForegroundColor(ColorConstants.black);
			line2.setEndpoints(new Point(xa, yb), new Point(xb, ya));
			root.add(line2);
		}

		private void drawCleanVisualAggregate(int logicX, int logicY, int logicX2,
				int sizeY, int number, String name) {
			final RectangleFigure rectangle = new RectangleFigure();
			rectangle.setBackgroundColor(ColorConstants.white);
			rectangle.setForegroundColor(ColorConstants.black);

			rectangle.setLineWidth(2);

			rectangle.setToolTip(new Label(" " + name + " "));
			rectangle.setLayoutManager(new BorderLayout());
			rectangle.setPreferredSize(1000, 1000);
			Label lab = new Label("?");
			lab.setTextAlignment(PositionConstants.CENTER);
			lab.setLabelAlignment(SWT.CENTER);
			lab.setForegroundColor(ColorConstants.black);
			rectangle
					.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.BOLD));
			// rectangle.add(lab, BorderLayout.CENTER);
			int xa = (int) ((logicX * logicWidth + aBorder));
			int ya = (int) (rootHeight - height + logicY * logicHeight - aBorder);
			int xb = xendlist.get(logicX2);
			int yb = (int) (ya + sizeY * logicHeight) - space;
			root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb, yb)));
			final PolylineConnection line = new PolylineConnection();
			line.setBackgroundColor(ColorConstants.black);
			line.setForegroundColor(ColorConstants.black);
			line.setEndpoints(new Point(xa, yb), new Point(xb, ya));
			root.add(line);

		}


	}
	

	public SpatiotemporalPartitionView(final OcelotlView ocelotlView) {
		super(ocelotlView);
		
	}

	@Override
	protected void computeDiagram() {
		if (!hierarchy.getRoot().getParts().isEmpty()) {
			DrawPartition hp=new DrawPartition();
			hp.draw();
		}
	}

}
