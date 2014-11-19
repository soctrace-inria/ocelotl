package fr.inria.soctrace.tools.ocelotl.ui.views.timelineview;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class SpatioTemporalAggregateView {

	private Rectangle aggregateZone;
	private EventProducerNode eventProducerNode;
	private int startingTimeSlice;
	private int endingTimeSlice;
	
	public SpatioTemporalAggregateView(Rectangle aggregateZone, EventProducerNode eventProducerNode, int startingTimeSlice, int endingTimeSlice) {
		super();
		this.aggregateZone = aggregateZone;
		this.eventProducerNode = eventProducerNode;
		this.startingTimeSlice = startingTimeSlice;
		this.endingTimeSlice = endingTimeSlice;
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
			MatrixView newView;

			// Instantiate the actual view
			newView = (MatrixView) mybundle.loadClass(ocelotlview.getCore().getVisuOperators().getSelectedOperatorResource(name).getVisualization()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlview);

			// New dialog
			Shell dialog = new Shell(ocelotlview.getSite().getShell());
			dialog.setText("Aggregation Content");
			dialog.setSize(1000, 700);
			
			// Init drawing display zone
			final Composite compositeOverview = new Composite(dialog, SWT.BORDER);
			compositeOverview.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			compositeOverview.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
			compositeOverview.setSize(1000, 700);
			compositeOverview.setLayout(new FillLayout(SWT.HORIZONTAL));
			
			Figure root;
			Canvas canvas;
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
			
			dialog.open();
			newView.setRoot(root);
			newView.setCanvas(canvas);
			newView.computeDiagram(eventProducerNode, startingTimeSlice, endingTimeSlice);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}
