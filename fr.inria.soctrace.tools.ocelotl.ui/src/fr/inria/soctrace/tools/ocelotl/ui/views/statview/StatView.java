package fr.inria.soctrace.tools.ocelotl.ui.views.statview;

import org.eclipse.draw2d.Figure;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class StatView implements IStatView {

	protected Figure	root;
	protected OcelotlView	ocelotlView;

	public StatView(OcelotlView ocelotlView) {
		this.ocelotlView = ocelotlView;
	}

	@Override
	public void createDiagram() {
	}

	@Override
	public void deleteDiagram() {
		// TODO Auto-generated method stub
	}

	@Override
	public void init(StatViewWrapper wrapper) {
		root = wrapper.getRoot();
		wrapper.cleanControlListeners();
		wrapper.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(final ControlEvent arg0) {
				resizeDiagram();
			}

			@Override
			public void controlResized(final ControlEvent arg0) {
				resizeDiagram();
			}
		});
	}

	@Override
	public void resizeDiagram() {
		createDiagram();
		root.repaint();                                                                 
	}

}
