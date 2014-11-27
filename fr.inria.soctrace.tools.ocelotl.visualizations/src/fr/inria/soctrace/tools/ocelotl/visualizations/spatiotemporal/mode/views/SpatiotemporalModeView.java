package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.views;

import java.util.ArrayList;
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
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.Part;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.SpatioTemporalAggregateView;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.MainState;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.SpatiotemporalMode;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.partition.VisualAggregation;

public class SpatiotemporalModeView extends SpatioTemporalView {

	private SpatiotemporalMode spatiotemporalMode;

	public SpatiotemporalModeView(final OcelotlView ocelotlView) {
		super(ocelotlView);
	}

	public class DrawSpatialMode extends DrawSpatioTemporal {

		private static final int ColorThreshold = 175;
		private static final int AlphaThreshold = 190;
		private int rectangleBorder = 1;

		public DrawSpatialMode() {
			super();
			aggregates = new ArrayList<SpatioTemporalAggregateView>();
		}

		protected MainState getMainState(EventProducerNode epn, int start,
				int end) {
			return spatiotemporalMode.getMainState(epn, start, end);
		}
		
		protected void saveAggregate(int xa, int xb, int ya, int yb,
				EventProducerNode epn, int start, int end, String label,
				boolean isVisualAggregate) {
			aggregates.add(new SpatioTemporalAggregateView(new Rectangle(
					new Point(xa, ya), new Point(xb, yb)), epn, start, end, xb
					- xa, label, isVisualAggregate));
		}
		
		protected void drawStandardAggregate(int logicX, int logicY, int logicX2,
				int sizeY, int number, EventProducerNode epn) {
			final RectangleFigure rectangle = new RectangleFigure();
			MainState state = getMainState(epn, logicX,
					logicX2);
			String label = " " + epn.getMe().getName() + " ("
					+ state.getState() + ", " + state.getAmplitude100() + "%) ";
			rectangle.setBackgroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			rectangle.setForegroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			rectangle.setAlpha(state.getAmplitude255Shifted());
			rectangle.setLineWidth(1);
			rectangle.setToolTip(new Label(label));

			int xa = (int) ((logicX * logicWidth + aBorder));
			int ya = (int) (rootHeight - height + logicY * logicHeight - aBorder);
			int xb = xendlist.get(logicX2);
			int yb = yendlist.get(logicY + sizeY);
			root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb,
					yb)));
			
			saveAggregate(xa, xb, ya, yb, epn, logicX, logicX2, label, false);
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
				int sizeY, EventProducerNode epn, boolean clean) {
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
			
			saveAggregate(xa, xb, ya, yb, epn, logicX, logicX2, label, true);
			
			if (!clean) {
				drawTextureDirty(xa, xb, ya, yb, label);
			} else {
				drawTextureClean(xa, xb, ya, yb, label);
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
									epn.getWeight(), epn, hasNoCut);
						}
					}
				}
			}
		}
		
		@Override
		protected MainState getMainState(EventProducerNode epn, int start,
				int end) {
			return spatiotemporalMode.getMainState(epn, start + startingSlice,
					end + startingSlice);
		}

		@Override
		protected void saveAggregate(int xa, int xb, int ya, int yb,
				EventProducerNode epn, int start, int end, String label,
				boolean isVisualAggregate) {
			// Do nothing, since we don't want to save this aggregate
		}
	}
	
}
