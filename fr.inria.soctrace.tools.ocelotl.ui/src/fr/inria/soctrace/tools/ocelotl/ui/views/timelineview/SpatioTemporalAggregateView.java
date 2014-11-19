package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView.MouseState;

public class SpatioTemporalAggregateView {
	
	private class SpatioTemporalAggregateMouseListener implements MouseListener{



		@Override
		public void mouseDoubleClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			dialog.close();
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}

	public final static int Height=700;
	private int width;
	private Rectangle aggregateZone;
	private EventProducerNode eventProducerNode;
	private int startingTimeSlice;
	private int endingTimeSlice;
	private Shell	dialog;
	private Canvas	canvas;
	private MatrixView	newView;
	private Figure	root;
	private String label;
	
	public SpatioTemporalAggregateView(Rectangle aggregateZone, EventProducerNode eventProducerNode, int startingTimeSlice, int endingTimeSlice, int width) {
		super();
		this.aggregateZone = aggregateZone;
		this.eventProducerNode = eventProducerNode;
		this.startingTimeSlice = startingTimeSlice;
		this.endingTimeSlice = endingTimeSlice;
		this.width=width;
		label="Aggregate Content";
	}

	public SpatioTemporalAggregateView(Rectangle aggregateZone, EventProducerNode eventProducerNode, int startingTimeSlice, int endingTimeSlice, int width, String label) {
		super();
		this.aggregateZone = aggregateZone;
		this.eventProducerNode = eventProducerNode;
		this.startingTimeSlice = startingTimeSlice;
		this.endingTimeSlice = endingTimeSlice;
		this.width=width;
		this.label=label;
	}

	public Rectangle getAggregateZone() {
		return aggregateZone;
	}

	public void setAggregateZone(Rectangle aggregateZone) {
		this.aggregateZone = aggregateZone;
	}

	public EventProducerNode getEventProducerNode() {
		return eventProducerNode;
	}

	public void setEventProducerNode(EventProducerNode eventProducerNode) {
		this.eventProducerNode = eventProducerNode;
	}

	public int getStartingTimeSlice() {
		return startingTimeSlice;
	}

	public void setStartingTimeSlice(int startingTimeSlice) {
		this.startingTimeSlice = startingTimeSlice;
	}

	public int getEndingTimeSlice() {
		return endingTimeSlice;
	}

	public void setEndingTimeSlice(int endingTimeSlice) {
		this.endingTimeSlice = endingTimeSlice;
	}
	
	/**
	 * Display the content of the aggregation in a new window
	 * 
	 * @param ocelotlview
	 *            the current ocelotl view
	 */
	public void display(OcelotlView ocelotlview)
	{
		String name = ocelotlview.getOcelotlParameters().getVisuOperator();

		try {
			final Bundle mybundle = Platform.getBundle(ocelotlview.getCore().getVisuOperators().getSelectedOperatorResource(name).getBundle());

			// Instantiate the actual view
			newView = (MatrixView) mybundle.loadClass(ocelotlview.getCore().getVisuOperators().getSelectedOperatorResource(name).getVisualization()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlview);

			// New dialog
			dialog = new Shell(ocelotlview.getSite().getShell(), SWT.SHELL_TRIM|SWT.RESIZE);
			dialog.setText(label);
			dialog.setSize(width+(newView.getBorder()*3), Height);
			dialog.setLayout(new FillLayout());

			
			// Init drawing display zone
			final Composite compositeOverview = new Composite(dialog, SWT.BORDER);
			compositeOverview.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			compositeOverview.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
			compositeOverview.setSize(width+(newView.getBorder()*3), Height);
			compositeOverview.setLayout(new FillLayout());
			
			root = new Figure();
			root.setFont(compositeOverview.getFont());
			final XYLayout layout = new XYLayout();
			root.setLayoutManager(layout);
			canvas = new Canvas(compositeOverview, SWT.DOUBLE_BUFFERED);
			canvas.setSize(compositeOverview.getSize());
			final LightweightSystem lws = new LightweightSystem(canvas);
			lws.setContents(root);
			lws.setControl(canvas);
			root.setFont(SWTResourceManager.getFont("Cantarell", 24, SWT.NORMAL));
			root.setSize(compositeOverview.getSize().x, compositeOverview.getSize().y);
			newView.setRoot(root);
			newView.setCanvas(canvas);
			dialog.addControlListener(new ControlListener() {

				@Override
				public void controlMoved(final ControlEvent arg0) {
					canvas.redraw();
					newView.createDiagram(eventProducerNode, startingTimeSlice, endingTimeSlice);
					//root.repaint();
				}

				@Override
				public void controlResized(final ControlEvent arg0) {
					canvas.redraw();
					newView.createDiagram(eventProducerNode, startingTimeSlice, endingTimeSlice);
					//root.repaint();
				}
			});
			dialog.open();
			newView.setRoot(root);
			newView.setCanvas(canvas);
			root.addMouseListener(new SpatioTemporalAggregateMouseListener());
			newView.createDiagram(eventProducerNode, startingTimeSlice, endingTimeSlice);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}	
}
