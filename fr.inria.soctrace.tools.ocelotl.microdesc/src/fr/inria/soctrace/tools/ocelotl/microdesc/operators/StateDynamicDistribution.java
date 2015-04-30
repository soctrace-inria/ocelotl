package fr.inria.soctrace.tools.ocelotl.microdesc.operators;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.Microscopic3DDescription;

public class StateDynamicDistribution extends Microscopic3DDescription {

	private Microscopic3DDescription realDesc;
	
	public StateDynamicDistribution() {
		super();
	}
	
	@Override
	//Dynamically select the most appropriate operator
	public void computeSubMatrix(List<EventProducer> eventProducers,
			List<IntervalDesc> time, IProgressMonitor monitor) throws SoCTraceException, InterruptedException, OcelotlException {
		Trace currentTrace = parameters.getTrace();
		Long traceDuration = currentTrace.getMaxTimestamp()
				- currentTrace.getMinTimestamp();
		double percentageTime = (time.get(0).t2 - time.get(0).t1)
				/ traceDuration;

		if (currentTrace.getNumberOfEvents() > 500000000) {
			if (percentageTime < 1.0 && percentageTime > 0.49) {
				realDesc = new StateDistribution();
			} else {
				realDesc = new StateDistributionHybrid();
			}
		} else if (percentageTime >= 1.0) {
			realDesc = new StateDistribution();
		} else if (percentageTime < 1.0 && percentageTime > 0.49) {
			realDesc = new StateDistributionQuery();
		} else {
			realDesc = new StateDistributionHybrid();
		}
		
		realDesc.setOcelotlParameters(parameters, monitor);
		realDesc.computeSubMatrix(eventProducers, time, monitor);
		this.matrix = realDesc.getMatrix();
		eventsNumber = realDesc.getEventsNumber();
		
		parameters.setChosenStateOperator(realDesc.getClass().getSimpleName());
	}

}
