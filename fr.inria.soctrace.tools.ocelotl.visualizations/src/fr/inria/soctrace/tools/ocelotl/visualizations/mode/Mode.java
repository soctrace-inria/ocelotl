package fr.inria.soctrace.tools.ocelotl.visualizations.mode;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.ISpaceAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.Part;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.IMicroDescManager;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.spacetime.SpaceTimeAggregationManager;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.TimeAggregationManager;
import fr.inria.soctrace.tools.ocelotl.visualizations.matrixproportion.MajState;

public class Mode implements ISpaceAggregationOperator {

	protected OcelotlCore ocelotlCore;
	protected HashMap<Integer, MajState> majStates;
	protected IMicroDescManager lpaggregManager;
	protected List<Part> parts;

	private static final Logger logger = LoggerFactory.getLogger(Mode.class);

	@Override
	public OcelotlCore getOcelotlCore() {
		return ocelotlCore;
	}

	@Override
	public void setOcelotlCore(OcelotlCore ocelotlCore) {
		this.ocelotlCore = ocelotlCore;
		lpaggregManager = (IMicroDescManager) ocelotlCore.getLpaggregManager();
		computeParts();
	}

	public Mode(OcelotlCore ocelotlCore) {
		super();
		setOcelotlCore(ocelotlCore);
	}
	
	public Mode() {
		super();
	}
	
	/**
	 * Check whether the operator is temporal or spatio-temporal and then
	 * compute the majstates with appropriate class
	 */
	public void computeParts() {
		if (SpaceTimeAggregationManager.class.isAssignableFrom(ocelotlCore
				.getLpaggregManager().getClass())) {
			majStates = new SpaceTimeMode(ocelotlCore, lpaggregManager).getMajStates();
		} else if (TimeAggregationManager.class.isAssignableFrom(ocelotlCore
				.getLpaggregManager().getClass())) {
			majStates = new TimeMode(ocelotlCore, ocelotlCore.getLpaggregManager()).getMajStates();
		} else {
			logger.error("Non supported class type: "
					+ ocelotlCore.getLpaggregManager().getClass().getName());
		}
	}

	public HashMap<Integer, MajState> getMajStates() {
		return majStates;
	}

	public void setMajStates(HashMap<Integer, MajState> majStates) {
		this.majStates = majStates;
	}

}