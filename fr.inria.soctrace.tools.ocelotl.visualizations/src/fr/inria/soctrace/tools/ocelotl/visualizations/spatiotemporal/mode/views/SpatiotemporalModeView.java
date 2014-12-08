package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.Part;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.SpatioTemporalAggregateView;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.MainState;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.mode.SpatiotemporalMode;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.partition.VisualAggregation;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.views.SpatioTemporalView;

public class SpatiotemporalModeView extends SpatioTemporalView {

	private SpatiotemporalMode spatiotemporalMode;

	public SpatiotemporalModeView(final OcelotlView ocelotlView) {
		super(ocelotlView);
	}

	public class DrawSpatialMode extends DrawSpatioTemporal {

		public DrawSpatialMode() {
			super();
			aggregates = new ArrayList<SpatioTemporalAggregateView>();
		}

		protected MainState getMainState(EventProducerNode epn, int start,
				int end) {
			return spatiotemporalMode.getMainState(epn, start, end);
		}
		
		@Override
		protected RectangleFigure setRectangle(EventProducerNode epn,
				int startTimeSlice, int endTimeSlice,
				boolean isVisualAggregate, int number) {
			RectangleFigure rectangle = new RectangleFigure();

			MainState state = getMainState(epn, startTimeSlice, endTimeSlice);
			String label = " " + epn.getMe().getName() + " ("
					+ state.getState() + ", " + state.getAmplitude100() + "%) ";
			rectangle.setBackgroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			rectangle.setForegroundColor(FramesocColorManager.getInstance()
					.getEventTypeColor(state.getState()).getSwtColor());
			rectangle.setToolTip(new Label(label));

			// Set the alpha transparency according to the spatiotemporalMode
			rectangle.setAlpha(state.getAmplitude255Shifted());
			rectangle.setLineWidth(1);
			rectangle.setLayoutManager(new BorderLayout());
			rectangle.setPreferredSize(1000, 1000);
			rectangle.setFont(SWTResourceManager.getFont("Cantarell", 11,
					SWT.BOLD));

			return rectangle;
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
					drawAggregate(p.getStartPart() - startingSlice, epn.getIndex()- theNode.getIndex(),
							p.getEndPart()  - startingSlice, epn.getWeight(),
							((VisualAggregation) p.getData()).getValue(), epn, false, false);
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
							drawAggregate(pagg.getStartPart() - startingSlice,
									epn.getIndex() - theNode.getIndex(),
									pagg.getEndPart() - startingSlice,
									epn.getWeight(),
									((VisualAggregation) p.getData())
											.getValue(), epn, true, hasNoCut);
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
