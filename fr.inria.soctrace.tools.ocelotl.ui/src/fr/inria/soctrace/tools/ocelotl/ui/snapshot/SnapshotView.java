package fr.inria.soctrace.tools.ocelotl.ui.snapshot;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.Bundle;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;

public class SnapshotView {

	private int				height;
	private int				width;
	private Shell			dialog;
	private Canvas			canvas;
	private AggregatedView	aggregationView;
	private Figure			root;
	private Composite		compositeOverview;

	public SnapshotView() {

	}

	public void createView(OcelotlView ocelotlview) {
		width = ocelotlview.getOcelotlParameters().getOcelotlSettings().getSnapshotXResolution();
		height = ocelotlview.getOcelotlParameters().getOcelotlSettings().getSnapshotYResolution();
		String name = ocelotlview.getOcelotlParameters().getVisuOperator();

		try {
			final Bundle mybundle = Platform.getBundle(ocelotlview.getCore().getVisuOperators().getOperatorResource(name).getBundle());

			// Instantiate the actual view
			aggregationView = (AggregatedView) mybundle.loadClass(ocelotlview.getCore().getVisuOperators().getOperatorResource(name).getVisualization()).getDeclaredConstructor(OcelotlView.class).newInstance(ocelotlview);

			// New window
			dialog = new Shell(ocelotlview.getSite().getShell().getDisplay());
			dialog.setSize(width, height);

			// Init drawing display zone
			compositeOverview = new Composite(dialog, SWT.BORDER);
			compositeOverview.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			// Make sure we remove the title bar from the size in order to
			// display it fully
			compositeOverview.setSize(dialog.getSize().x, dialog.getSize().y);
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

			// Trigger the display
			aggregationView.createDiagram(ocelotlview.getOcelotlCore().getLpaggregManager(), ocelotlview.getOcelotlParameters().getTimeRegion(), ocelotlview.getOcelotlCore().getVisuOperator());
			
			// Make sure the drawing is up to date
			compositeOverview.layout();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Figure getRoot() {
		return root;
	}

	public void setRoot(Figure root) {
		this.root = root;
	}

	public AggregatedView getAggregationView() {
		return aggregationView;
	}

	public void setAggregationView(AggregatedView aggregationView) {
		this.aggregationView = aggregationView;
	}
}