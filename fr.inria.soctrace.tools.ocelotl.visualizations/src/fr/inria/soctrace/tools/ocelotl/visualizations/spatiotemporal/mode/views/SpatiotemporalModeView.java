package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.views;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.Aggregation;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.Part;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.MatrixView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.SpatioTemporalAggregateView;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.MainState;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.SpatiotemporalMode;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.partition.VisualAggregation;

public class SpatiotemporalModeView extends MatrixView {

	private SpatiotemporalMode spatiotemporalMode;

	public SpatiotemporalModeView(final OcelotlView ocelotlView) {
		super(ocelotlView);
	}

	public class DrawSpatialMode {

		private static final int ColorThreshold = 175;
		private static final int AlphaThreshold = 190;
		int spaceClean = 8;
		int spaceDirty = 10;
		int spaceDirty2 = 1;
		int iterationDirty = 3;
		private int rectangleBorder = 1;
		protected double rootHeight;
		protected double height;
		protected double width;
		protected double logicWidth;
		protected double logicHeight;
		protected int minLogicWeight = 1;
		protected List<Integer> xendlist;
		protected List<Integer> yendlist;

		public DrawSpatialMode() {
			super();
			xendlist = new ArrayList<Integer>();
			yendlist = new ArrayList<Integer>();
			aggregates = new ArrayList<SpatioTemporalAggregateView>();
		}

		/**
		 * Compute the list of X values according to the number of time slices
		 * (parts)
		 */
		protected void initX() {
			xendlist.clear();
			for (int i = 0; i <= hierarchy.getRoot().getParts().size(); i++)
				xendlist.add((int) (i * logicWidth + aBorder - space));
		}

		/**
		 * Compute the list of Y values according to the weight of the root node
		 */
		protected void initY() {
			yendlist.clear();
			for (int i = 0; i <= hierarchy.getRoot().getWeight(); i++)
				yendlist.add((int) (rootHeight - height + i * logicHeight
						- space - aBorder));
		}

		public void draw() {
			rootHeight = root.getSize().height;
			height = rootHeight - (2 * aBorder);
			width = root.getSize().width - (2 * aBorder);
			logicWidth = width / hierarchy.getRoot().getParts().size();
			logicHeight = height / hierarchy.getRoot().getWeight();
			initX();
			initY();
			print(hierarchy.getRoot(), 0, hierarchy.getRoot()
					.getParts().size());
		}

		/**
		 * Compute the temporal parts for a given EventProducerNode
		 * 
		 * @param epn
		 *            the EventProducerNode
		 * @param start
		 *            the starting time slice
		 * @param end
		 *            the ending time slice
		 * @return a list of temporal TemporalPartition
		 */
		protected List<Part> computeParts(EventProducerNode epn, int start,
				int end) {
			List<Part> parts = new ArrayList<Part>();
			int oldPart = epn.getParts().get(start);
			// Init part
			parts.add(new Part(start, start + 1, new VisualAggregation(false,
					epn.getParts().get(start) != -1, epn.getParts().get(start),
					true)));
			for (int i = start + 1; i < end; i++) {
				// If we are still in the same part, increase its size
				if (epn.getParts().get(i) == oldPart) {
					parts.get(parts.size() - 1).incrSize();
				} else {
					// Create a new part
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

		/**
		 * Print the matrix of data for a given eventproducerNode and for a
		 * given time range
		 * 
		 * @param id
		 *            id of the eventProducerNode
		 * @param start
		 *            starting time slice
		 * @param end
		 *            ending time slice
		 */
		protected void print(EventProducerNode epn, int start, int end) {
			// Compute the parts for the current epn
			List<Part> parts = computeParts(epn, start, end);
			for (Part p : parts) {
				// If p is an aggregation
				if (((VisualAggregation) p.getData()).isAggregated())
					drawStandardAggregate(p.getStartPart(), epn.getIndex(),
							p.getEndPart(), epn.getWeight(),
							((VisualAggregation) p.getData()).getValue(), epn);
				else {
					// Check for each child that we have enough vertical space
					// to display them
					boolean aggy = false;
					for (EventProducerNode ep : epn.getChildrenNodes()) {
						// if the space needed to print an element is smaller
						// than 1 pixel
						if ((ep.getWeight() * logicHeight - space) < minLogicWeight) {
							// Aggregate
							aggy = true;
							break;
						}
					}
					// If enough space
					if (aggy == false)
						// recursively call print() on the children node
						printChildren(epn, p.getStartPart(), p.getEndPart());
					else {
						List<Part> aggParts = computeCommonCuts(epn,
								p.getStartPart(), p.getEndPart());
						for (Part pagg : aggParts) {
							// Does the aggregated data contain some temporal
							// cut
							boolean hasNoCut = ((VisualAggregation) pagg
									.getData()).isNoCutInside();
							drawVisualAggregate(pagg.getStartPart(),
									epn.getIndex(), pagg.getEndPart(),
									epn.getWeight(),
									((VisualAggregation) p.getData())
											.getValue(), epn, hasNoCut);
						}
					}
				}
			}
		}

		/**
		 * Compute the common cut between the event producer node and its
		 * children
		 * 
		 * @param epn
		 *            the event producer node
		 * @param start
		 *            the starting time slice
		 * @param end
		 *            the ending time slice
		 * @return a list of parts
		 */
		protected List<Part> computeCommonCuts(EventProducerNode epn, int start,
				int end) {
			HashMap<EventProducerNode, List<Part>> hm = new HashMap<EventProducerNode, List<Part>>();
			// Contains the parts which are common to all the children
			List<Part> commonParts = new ArrayList<Part>();
			// All parts (results)
			List<Part> parts = new ArrayList<Part>();
			// For each child
			for (EventProducerNode child : epn.getChildrenNodes()) {
				if (child.isAggregated() == Aggregation.FULL)
					// Get parts
					hm.put(child, computeParts(child, start, end));
				else
					// Get common cuts
					hm.put(child, computeCommonCuts(child, start, end));
			}
			List<Part> testPart = hm.get(epn.getChildrenNodes().get(0));
			for (Part p : testPart) {
				boolean commonCut = false;
				boolean cleanCut = true;
				for (EventProducerNode child : epn.getChildrenNodes()) {
					commonCut = false;
					for (Part p2 : hm.get(child)) {
						// If p and p2 has the same starting and ending dates
						if (p.compare(p2)) {
							commonCut = true;
							if (((VisualAggregation) p2.getData())
									.isVisualAggregate()
									&& !((VisualAggregation) p2.getData())
											.isNoCutInside())
								cleanCut = false;
							// Get to the next child
							break;
						}
					}
					// There was at least one child with no common cut
					if (commonCut == false)
						break;
				}
				// If the part is common to all the children
				if (commonCut) {
					// Add it to common parts
					commonParts.add(new Part(p.getStartPart(), p.getEndPart(),
							new VisualAggregation(true, false, -1, cleanCut)));
				}
			}
			// If no common cut were found
			if (commonParts.isEmpty()) {
				// Just add one big part
				parts.add(new Part(start, end, new VisualAggregation(true,
						false, -1, false)));
				return parts;
			}
			// If the common parts do not start at the starting slice
			if (commonParts.get(0).getStartPart() != start)
				parts.add(new Part(start, commonParts.get(0).getStartPart(),
						new VisualAggregation(true, false, -1, false)));

			// For each common part
			for (Part ptemp : commonParts) {
				// If parts is not empty and the last part does not end with
				// the beginning of the current common part
				if (parts.size() > 0
						&& parts.get(parts.size() - 1).getEndPart() != ptemp
								.getStartPart())
					// Add a new part in the gap between the last part and the
					// current common part
					parts.add(new Part(
							parts.get(parts.size() - 1).getEndPart(), ptemp
									.getStartPart(), new VisualAggregation(
									true, false, -1, false)));
				// Add the common part
				parts.add(ptemp);
			}
			// if the last part does not go until the end time slice
			if (parts.get(parts.size() - 1).getEndPart() != end)
				// Add a part to fill the gap
				parts.add(new Part(parts.get(parts.size() - 1).getEndPart(),
						end, new VisualAggregation(true, false, -1, false)));
			return parts;
		}

		/**
		 * Recursively print all the children of the event producer with the
		 * given id
		 * 
		 * @param id
		 *            id of the event producer
		 * @param start
		 *            starting slice
		 * @param end
		 *            ending slice
		 */
		protected void printChildren(EventProducerNode epn , int start, int end) {
			for (EventProducerNode ep : epn.getChildrenNodes())
				print(ep, start, end);
		}

		protected void drawStandardAggregate(int logicX, int logicY, int logicX2,
				int sizeY, int number, EventProducerNode epn) {
			final RectangleFigure rectangle = new RectangleFigure();
			MainState state = spatiotemporalMode.getMainState(epn, logicX,
					logicX2);
			rectangle.setBackgroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			rectangle.setForegroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			rectangle.setAlpha(state.getAmplitude255Shifted());
			rectangle.setLineWidth(1);

			rectangle
					.setToolTip(new Label(" " + epn.getMe().getName() + " ("
							+ state.getState() + ", " + state.getAmplitude100()
							+ "%) "));

			int xa = (int) ((logicX * logicWidth + aBorder));
			int ya = (int) (rootHeight - height + logicY * logicHeight - aBorder);
			int xb = xendlist.get(logicX2);
			int yb = yendlist.get(logicY + sizeY);
			root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb,
					yb)));
		}

		/**
		 * Draw an aggregation when the resolution is too small to print all the
		 * cuts. If the aggregated area contains temporal cut (clean parameter),
		 * then it is drawn with a cross else it is drawn with a single diagnoal
		 * line
		 * 
		 * @param logicX
		 * @param logicY
		 * @param logicX2
		 * @param sizeY
		 *	Height of the aggregate (number of represented leave nodes)
		 * @param number
		 *            ?? (not used: to delete ?)
		 * @param epn
		 *            the event producer
		 * @param clean
		 *            Does the aggregated area contains temporal cut ?
		 */
		protected void drawVisualAggregate(int logicX, int logicY, int logicX2,
				int sizeY, int number, EventProducerNode epn, boolean clean) {
			final RectangleFigure rectangle = new RectangleFigure();
			MainState state = spatiotemporalMode.getMainState(epn, logicX,
					logicX2);
			String label = " " + epn.getMe().getName() + " ("
					+ state.getState() + ", " + state.getAmplitude100() + "%) ";
			rectangle.setBackgroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			rectangle.setForegroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			// Set the alpha transparency according to the spatiotemporalMode
			rectangle.setAlpha(state.getAmplitude255Shifted());
			rectangle.setLineWidth(1);
			rectangle.setToolTip(new Label(label));
			rectangle.setLayoutManager(new BorderLayout());
			rectangle.setPreferredSize(1000, 1000);

			Label lab = new Label("?");
			lab.setTextAlignment(PositionConstants.CENTER);
			lab.setLabelAlignment(SWT.CENTER);
			lab.setForegroundColor(ColorConstants.black);
			rectangle.setFont(SWTResourceManager.getFont("Cantarell", 11,
					SWT.BOLD));

			int xa = (int) ((logicX * logicWidth + aBorder));
			int ya = (int) (rootHeight - height + logicY * logicHeight - aBorder);
			int xb = xendlist.get(logicX2);
			int yb = yendlist.get(logicY + sizeY);
			root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb,
					yb)));
			
			aggregates.add(new SpatioTemporalAggregateView(new Rectangle(new Point(xa, ya), new Point(xb,
					yb)), epn, logicX, logicX2, xb-xa, label));
			
			if (!clean) {
				drawTextureDirty(xa, xb, ya, yb, label);
			} else {
				drawTextureClean(xa, xb, ya, yb, label);
			}
		}

		protected void drawTextureClean(int xa, int xb, int ya, int yb,
				String label) {
			for (int x = xa + spaceClean - yb + ya; x < (xb); x = x
					+ spaceClean + 1) {
				final PolylineConnection line = new PolylineConnection();
				int xinit = x;
				int yinit = ya;
				int xfinal = Math.min(xb, (xinit + (yb - ya)));
				int yfinal = Math.min(yb, ya + xfinal - xinit);
				if (xa > xinit) {
					yinit = Math.min(yb, ya - xinit + xa);
					xinit = xa;
				}
				line.setBackgroundColor(ColorConstants.white);
				line.setForegroundColor(ColorConstants.white);
				line.setEndpoints(new Point(xinit, yinit), new Point(xfinal,
						yfinal));
				line.setLineWidth(1);
				line.setAntialias(SWT.ON);
				line.setToolTip(new Label(label));
				root.add(line);
			}
		}

		protected void drawTextureDirty(int xa, int xb, int ya, int yb,
				String label) {
			int i = 0;
			for (int x = xa + spaceDirty2; x < (xb + yb - ya); x = x
					+ spaceDirty2 + 1) {
				i++;
				if (i > iterationDirty - 1) {
					i = 0;
					x += spaceDirty;
				}
				if (x >= (xb + yb - ya)){
					break;
				}
				final PolylineConnection line = new PolylineConnection();
				int xinit = x;
				int yinit = ya;
				int xfinal = Math.max(xa, (xinit - (yb - ya)));
				int yfinal = Math.min(yb, ya + xinit - xfinal);
				if (xb < xinit) {
					yinit = Math.min(yb, ya + xinit - xb);
					xinit = xb;
				}
				line.setBackgroundColor(ColorConstants.white);
				line.setForegroundColor(ColorConstants.white);
				line.setEndpoints(new Point(xinit, yinit), new Point(xfinal,
						yfinal));
				line.setLineWidth(1);
				line.setAntialias(SWT.ON);
				line.setToolTip(new Label(label));
				root.add(line);
			}

		}

		@SuppressWarnings("unused")
		private void drawRectangleBorder(int xa, int xb, int ya, int yb) {
			final PolylineConnection rect1 = new PolylineConnection();
			rect1.setBackgroundColor(ColorConstants.black);
			rect1.setForegroundColor(ColorConstants.black);
			rect1.setLineWidth(rectangleBorder);
			rect1.setEndpoints(new Point(xa, ya), new Point(xb, ya));
			final PolylineConnection rect2 = new PolylineConnection();
			rect2.setBackgroundColor(ColorConstants.black);
			rect2.setForegroundColor(ColorConstants.black);
			rect2.setLineWidth(rectangleBorder);
			rect2.setEndpoints(new Point(xa, yb), new Point(xb, yb));
			final PolylineConnection rect3 = new PolylineConnection();
			rect3.setBackgroundColor(ColorConstants.black);
			rect3.setForegroundColor(ColorConstants.black);
			rect3.setLineWidth(rectangleBorder);
			rect3.setEndpoints(new Point(xa, ya), new Point(xa, yb));
			final PolylineConnection rect4 = new PolylineConnection();
			rect4.setBackgroundColor(ColorConstants.black);
			rect4.setForegroundColor(ColorConstants.black);
			rect4.setLineWidth(rectangleBorder);
			rect4.setEndpoints(new Point(xb, ya), new Point(xb, yb));
			root.add(rect1);
			root.add(rect2);
			root.add(rect3);
			root.add(rect4);
		}

		@SuppressWarnings("unused")
		private boolean isColorLight(Color color, int alpha) {
			return (alpha < AlphaThreshold || ((color.getBlue() < ColorThreshold)
					&& (color.getRed() < ColorThreshold) && (color.getGreen() < ColorThreshold)));
		}

	}

	@Override
	protected void computeDiagram(EventProducerNode aNode, int start, int end) {
		if (!aNode.getParts().isEmpty()) {
			spatiotemporalMode = (SpatiotemporalMode) ocelotlView.getCore()
					.getVisuOperator();
			DrawAggregate hp = new DrawAggregate();
			hp.draw(aNode, start, end);
		}
	}
	
	@Override
	protected void computeDiagram() {
		if (!hierarchy.getRoot().getParts().isEmpty()) {
			spatiotemporalMode = (SpatiotemporalMode) ocelotlView.getCore()
					.getVisuOperator();
			DrawSpatialMode hp = new DrawSpatialMode();
			hp.draw();
		}
	}

	public class DrawAggregate extends DrawSpatialMode {
		private EventProducerNode theNode;
		
		private int startingSlice; 
		private int endingSlice;
		
		public DrawAggregate() {
			super();
		}
		
		/**
		 * Compute the list of X values according to the number of time slices
		 * (parts)
		 */
		protected void initX(EventProducerNode epn) {
			xendlist.clear();
			for (int i = 0; i <= epn.getParts().size(); i++)
				xendlist.add((int) (i * logicWidth + aBorder - space));
		}

		/**
		 * Compute the list of Y values according to the weight of the root node
		 */
		protected void initY(EventProducerNode epn) {
			yendlist.clear();
			for (int i = 0; i <= epn.getWeight(); i++)
				yendlist.add((int) (rootHeight - height + i * logicHeight
						- space - aBorder));
		}

		public void draw(EventProducerNode epn, int start, int end) {
			startingSlice = start;
			endingSlice = end;
			theNode = epn;
			rootHeight = root.getSize().height;
			height = rootHeight - (2 * aBorder);
			width = root.getSize().width - (2 * aBorder);
			logicWidth = width / (end - start);
			logicHeight = height / theNode.getWeight();
			initX(theNode);
			initY(theNode);
			print(theNode, startingSlice, endingSlice);
		}

		/**
		 * Print the matrix of data for a given eventproducerNode and for a
		 * given time range
		 * 
		 * @param id
		 *            id of the eventProducerNode
		 * @param start
		 *            starting time slice
		 * @param end
		 *            ending time slice
		 */
		@Override
		protected void print(EventProducerNode epn, int start, int end) {
			// Compute the parts for the current epn
			List<Part> parts = computeParts(epn, start, end);
			for (Part p : parts) {
				// If p is an aggregation
				if (((VisualAggregation) p.getData()).isAggregated())
					drawStandardAggregate(p.getStartPart() - startingSlice, epn.getIndex()- theNode.getIndex(),
							p.getEndPart()  - startingSlice, epn.getWeight(),
							((VisualAggregation) p.getData()).getValue(), epn);
				else {
					// Check for each child that we have enough vertical space
					// to display them
					boolean aggy = false;
					for (EventProducerNode ep : epn.getChildrenNodes()) {
						// if the space needed to print an element is smaller
						// than 1 pixel
						if ((ep.getWeight() * logicHeight - space) < minLogicWeight) {
							// Aggregate
							aggy = true;
							break;
						}
					}
					// If enough space
					if (aggy == false)
						// recursively call print() on the children node
						printChildren(epn, p.getStartPart(), p.getEndPart());
					else {
						List<Part> aggParts = computeCommonCuts(epn,
								p.getStartPart(), p.getEndPart());
						for (Part pagg : aggParts) {
							// Does the aggregated data contain some temporal
							// cut
							boolean hasNoCut = ((VisualAggregation) pagg
									.getData()).isNoCutInside();
							drawVisualAggregate(pagg.getStartPart() - startingSlice,
									epn.getIndex() - theNode.getIndex(), pagg.getEndPart() - startingSlice,
									epn.getWeight(),
									((VisualAggregation) p.getData())
											.getValue(), epn, hasNoCut);
						}
					}
				}
			}
		}
		
		@Override
		protected void drawStandardAggregate(int logicX, int logicY, int logicX2,
				int sizeY, int number, EventProducerNode epn) {
			final RectangleFigure rectangle = new RectangleFigure();
			MainState state = spatiotemporalMode.getMainState(epn, logicX + startingSlice,
					logicX2 + startingSlice);
			rectangle.setBackgroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			rectangle.setForegroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			rectangle.setAlpha(state.getAmplitude255Shifted());
			rectangle.setLineWidth(1);

			rectangle
					.setToolTip(new Label(" " + epn.getMe().getName() + " ("
							+ state.getState() + ", " + state.getAmplitude100()
							+ "%) "));

			int xa = (int) ((logicX * logicWidth + aBorder));
			int ya = (int) (rootHeight - height + logicY * logicHeight - aBorder);
			int xb = xendlist.get(logicX2);
			int yb = yendlist.get(logicY + sizeY);
			root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb,
					yb)));
		}

		/**
		 * Draw an aggregation when the resolution is too small to print all the
		 * cuts. If the aggregated area contains temporal cut (clean parameter),
		 * then it is drawn with a cross else it is drawn with a single diagnoal
		 * line
		 * 
		 * @param logicX
		 * @param logicY
		 * @param logicX2
		 * @param sizeY
		 *	Height of the aggregate (number of represented leave nodes)
		 * @param number
		 *            ?? (not used: to delete ?)
		 * @param epn
		 *            the event producer
		 * @param clean
		 *            Does the aggregated area contains temporal cut ?
		 */
		@Override
		protected void drawVisualAggregate(int logicX, int logicY, int logicX2,
				int sizeY, int number, EventProducerNode epn, boolean clean) {
			final RectangleFigure rectangle = new RectangleFigure();
			MainState state = spatiotemporalMode.getMainState(epn, logicX + startingSlice,
					logicX2 + startingSlice);
			String label = " " + epn.getMe().getName() + " ("
					+ state.getState() + ", " + state.getAmplitude100() + "%) ";
			rectangle.setBackgroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			rectangle.setForegroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			// Set the alpha transparency according to the spatiotemporalMode
			rectangle.setAlpha(state.getAmplitude255Shifted());
			rectangle.setLineWidth(1);
			rectangle.setToolTip(new Label(label));
			rectangle.setLayoutManager(new BorderLayout());
			rectangle.setPreferredSize(1000, 1000);

			Label lab = new Label("?");
			lab.setTextAlignment(PositionConstants.CENTER);
			lab.setLabelAlignment(SWT.CENTER);
			lab.setForegroundColor(ColorConstants.black);
			rectangle.setFont(SWTResourceManager.getFont("Cantarell", 11,
					SWT.BOLD));

			int xa = (int) ((logicX * logicWidth + aBorder));
			int ya = (int) (rootHeight - height + logicY * logicHeight - aBorder);
			int xb = xendlist.get(logicX2);
			int yb = yendlist.get(logicY + sizeY);
			root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb,
					yb)));
			
			if (!clean) {
				drawTextureDirty(xa, xb, ya, yb, label);
			} else {
				drawTextureClean(xa, xb, ya, yb, label);
			}
		}
	}
	
}
