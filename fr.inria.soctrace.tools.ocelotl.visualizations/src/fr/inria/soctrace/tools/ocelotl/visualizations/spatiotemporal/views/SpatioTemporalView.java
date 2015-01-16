package fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.Aggregation;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.ivisuop.Part;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.MatrixView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.SpatioTemporalAggregateView;
import fr.inria.soctrace.tools.ocelotl.visualizations.spatiotemporal.partition.VisualAggregation;

public abstract class SpatioTemporalView extends MatrixView {

	public SpatioTemporalView(OcelotlView ocelotlView) {
		super(ocelotlView);
	}

	abstract public class DrawSpatioTemporal {

		protected int spaceClean = 8;
		protected int spaceDirty = 10;
		protected int spaceDirty2 = 1;
		protected int iterationDirty = 3;
		
		protected double rootHeight;
		protected double height;
		protected double width;
		protected double logicWidth;
		protected double logicHeight;
		protected int minLogicWeight = OcelotlConstants.MinimalHeightDrawingThreshold;
		protected List<Integer> xendlist;
		protected List<Integer> yendlist;

		public DrawSpatioTemporal() {
			xendlist = new ArrayList<Integer>();
			yendlist = new ArrayList<Integer>();
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
		
		public void draw() {
			rootHeight = root.getSize().height;
			height = rootHeight - (2 * aBorder);
			width = root.getSize().width - (2 * aBorder);
			logicWidth = width / hierarchy.getRoot().getParts().size();
			logicHeight = height / hierarchy.getRoot().getWeight();
			initX(hierarchy.getRoot());
			initY(hierarchy.getRoot());
			print(hierarchy.getRoot(), 0, hierarchy.getRoot()
					.getParts().size());
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
					drawAggregate(p.getStartPart(), epn.getIndex(),
							p.getEndPart(), epn.getWeight(),
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
							drawAggregate(pagg.getStartPart(),
									epn.getIndex(), pagg.getEndPart(),
									epn.getWeight(), ((VisualAggregation) p.getData()).getValue(), epn, true, hasNoCut);
						}
					}
				}
			}
		}

		/**
		 * Set the drawn rectangle's characteristics and its label
		 * 
		 * @param epn
		 *            the event producer
		 * @param startTimeSlice
		 *            use to get the mainstate in mode view
		 * @param endTimeSlice
		 *            use to get the mainstate in mode view
		 * @param isVisualAggregate
		 *            use to set the color in partition view
		 * @param number
		 *            use to set the color in partition view
		 * @return the created rectanglefigure
		 */
		abstract protected RectangleFigure setRectangle(EventProducerNode epn,
				int startTimeSlice, int endTimeSlice,
				boolean isVisualAggregate, int number);

		/**
		 * Draw the aggregate
		 * 
		 * @param logicX
		 *            starting time slice of the aggregate
		 * @param logicY
		 *            starting height (event producer) of the aggregate
		 * @param logicX2
		 *            end time slice of the aggregate
		 * @param sizeY
		 *            ending height (event producer) of the aggregate
		 * @param number
		 *            value of the aggregate (use to set the color in partition
		 *            view)
		 * @param epn
		 *            the event producer node of the aggregate
		 * @param isVisualAggregate
		 *            is the aggregate a visual aggregate (i.e. an aggregation
		 *            when the resolution is too small to print all the cuts).
		 * @param clean
		 *            is the visual aggregate clean (i.e. does is contains
		 *            temporal cut)
		 */
		protected void drawAggregate(int logicX, int logicY, int logicX2,
				int sizeY, int number, EventProducerNode epn,
				boolean isVisualAggregate, boolean clean) {

			// Set the rectangle characteristics
			final RectangleFigure rectangle = setRectangle(epn, logicX,
					logicX2, isVisualAggregate, number);
			
			// Void state
			if(rectangle == null)
				return;

			String label = ((Label) rectangle.getToolTip()).getText();

			// Draw the rectangle
			int xa = (int) ((logicX * logicWidth + aBorder));
			int ya = (int) (rootHeight - height + logicY * logicHeight - aBorder);
			int xb = xendlist.get(logicX2);
			int yb = yendlist.get(logicY + sizeY);
			root.add(rectangle, new Rectangle(new Point(xa, ya), new Point(xb,
					yb)));

			// Save it in the index if necessary
			saveAggregate(xa, xb, ya, yb, epn, logicX, logicX2, label,
					isVisualAggregate);

			if (isVisualAggregate) {
				if (!clean) {
					drawTextureDirty(xa, xb, ya, yb, label);
				} else {
					drawTextureClean(xa, xb, ya, yb, label);
				}
			}
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
		protected List<Part> computeCommonCuts(EventProducerNode epn,
				int start, int end) {
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
		protected void printChildren(EventProducerNode epn, int start, int end) {
			for (EventProducerNode ep : epn.getChildrenNodes())
				print(ep, start, end);
		}
		
		/**
		 * Draw the texture for visual aggregate (add left to right lines) that
		 * do not contains temporal cut (i.e. clean)
		 * 
		 * @param xa
		 * @param xb
		 * @param ya
		 * @param yb
		 * @param label
		 */
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

		/**
		 * Draw the texture for visual aggregate (add right to left lines) that
		 * contains temporal cut (i.e. dirty)
		 * 
		 * @param xa
		 * @param xb
		 * @param ya
		 * @param yb
		 * @param label
		 */
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
		
		/**
		 * Save the aggregate with its characteristics in the index 
		 * @param xa
		 * @param xb
		 * @param ya
		 * @param yb
		 * @param epn
		 * @param start
		 * @param end
		 * @param label
		 * @param isVisualAggregate
		 */
		protected void saveAggregate(int xa, int xb, int ya, int yb,
				EventProducerNode epn, int start, int end, String label,
				boolean isVisualAggregate) {
			aggregates.add(new SpatioTemporalAggregateView(new Rectangle(
					new Point(xa, ya), new Point(xb, yb)), epn, start, end, xb
					- xa, label, isVisualAggregate));
		}
	}
}
