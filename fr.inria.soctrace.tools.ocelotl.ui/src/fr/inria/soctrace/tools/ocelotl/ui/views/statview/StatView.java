package fr.inria.soctrace.tools.ocelotl.ui.views.statview;

import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public abstract class StatView implements IStatView {

	protected OcelotlView	ocelotlView;

	public StatView(OcelotlView ocelotlView) {
		super();
		this.ocelotlView = ocelotlView;
	}

	@Override
	public void createDiagram() {

	}

	@Override
	public void deleteDiagram() {

	}

	@Override
	public void init(StatViewWrapper wrapper) {

	}

	@Override
	public void resizeDiagram() {
		createDiagram();                                                             
	}

}
