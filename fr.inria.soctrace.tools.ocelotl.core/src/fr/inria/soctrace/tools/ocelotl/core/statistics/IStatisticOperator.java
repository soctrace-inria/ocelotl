package fr.inria.soctrace.tools.ocelotl.core.statistics;

import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public interface IStatisticOperator {
	void compute(TimeRegion time, List<EventProducer> eps, List<EventType> ets);

	void setOcelotlCore(OcelotlCore ocelotlCore);
}
