package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class SpatioTemporalAggregateView {

	private class SpatioTemporalAggregateMouseListener implements MouseListener {

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

	public final static int		Height	= 700;
	private int					width;
	private Rectangle			aggregateZone;
	private EventProducerNode	eventProducerNode;
	private int					startingTimeSlice;
	private int					endingTimeSlice;
	private Shell				dialog;
	private Canvas				canvas;
	private MatrixView			aggregationView;
	private Figure				root;
	private String				label;
	private Composite			compositeOverview;
	private OcelotlView			ocelotlView;
	private boolean 			visualAggregate;

	public SpatioTemporalAggregateView(Rectangle aggregateZone, EventProducerNode eventProducerNode, int startingTimeSlice, int endingTimeSlice, int width, boolean visualAggregate) {
		super();
		this.aggregateZone = aggregateZone;
		this.eventProducerNode = eventProducerNode;
		this.startingTimeSlice = startingTimeSlice;
		this.endingTimeSlice = endingTimeSlice;
		this.width = width;
		this.visualAggregate = visualAggregate;
		label = "Aggregate Content";
	}

	public SpatioTemporalAggregateView(Rectangle aggregateZone, EventProducerNode eventProducerNode, int startingTimeSlice, int endingTimeSlice, int width, String label, boolean visualAggregate) {
		super();
		this.aggregateZone = aggregateZone;
		this.eventProducerNode = eventProducerNode;
		this.startingTimeSlice = startingTimeSlice;
		this.endingTimeSlice = endingTimeSlice;
		this.width = width;
		this.visualAggregate = visualAggregate;
		this.label = label;
	}

	public boolean isVisualAggregate() {
		return visualAggregate;
	}

	public void setVisualAggregate(boolean visualAggregate) {
		this.visualAggregate = visualAggregate;
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
	public void display(OcelotlView ocelotlview) {
		if (!visualAggregate)
			return;
		
		this.ocelotlView = ocelotlview;
		String name = ocelotlview.getOcelotlParameters().getVisuOperator();

		try {
			final Bundle mybundle = Platform.getBundle(ocelotlview.getCore().getVisuOperators().getSelectedOperatorResource(name).getBundle());

			// Instantiate the actual view
			aggregationView = (MatrixView) mybundle.loadClass(ocelotlview.getCore().getVisuOperators().getSelectedOperatorResource(name).getVisualization()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlview);

			// New window
			dialog = new Shell(ocelotlview.getSite().getShell().getDisplay());
			dialog.setText(label);
			dialog.setSize(width + (aggregationView.getBorder() * 3), Height);
			// Set location of the new window centered around the centered of the eclipse window
			dialog.setLocation(ocelotlview.getSite().getShell().getLocation().x + ocelotlview.getSite().getShell().getSize().x / 2 - width / 2, ocelotlview.getSite().getShell().getLocation().y + ocelotlview.getSite().getShell().getSize().y / 2 - Height / 2);
			dialog.setLayout(new FillLayout());

			// Init drawing display zone
			compositeOverview = new Composite(dialog, SWT.BORDER);
			compositeOverview.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			compositeOverview.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
			// Make sure we remove the title bar from the size in order not
			// display it fully
			compositeOverview.setSize(dialog.getSize().x, dialog.getSize().y - (dialog.getSize().y - dialog.getClientArea().height));
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

			aggregationView.setRoot(root);
			aggregationView.setCanvas(canvas);
			dialog.addControlListener(new dialogControlListener());
			dialog.addShellListener(new DialogShellListener());
			
			dialog.open();
			root.addMouseListener(new SpatioTemporalAggregateMouseListener());

			// Trigger the display
			aggregationView.createDiagram(eventProducerNode, startingTimeSlice, endingTimeSlice);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Listener for resize event of the window
	private class dialogControlListener implements ControlListener {
		@Override
		public void controlMoved(final ControlEvent arg0) {
			canvas.redraw();
			aggregationView.createDiagram(eventProducerNode, startingTimeSlice, endingTimeSlice);
		}

		@Override
		public void controlResized(final ControlEvent arg0) {
			// Prevent bad redraw when maximizing the window 
			compositeOverview.setSize(dialog.getSize().x, dialog.getSize().y - (dialog.getSize().y - dialog.getClientArea().height));
			canvas.redraw();
			aggregationView.createDiagram(eventProducerNode, startingTimeSlice, endingTimeSlice);
		}
	}
	
	private class DialogShellListener implements ShellListener {

		@Override
		public void shellActivated(ShellEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void shellClosed(ShellEvent e) {
			// Remove the highlight rectangle
			AggregatedView graphDisplayView = (AggregatedView) ocelotlView.getTimeLineView();

			if (graphDisplayView.getHighLightAggregateFigure() != null)
				graphDisplayView.getHighLightAggregateFigure().delete();
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void shellDeiconified(ShellEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void shellIconified(ShellEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
