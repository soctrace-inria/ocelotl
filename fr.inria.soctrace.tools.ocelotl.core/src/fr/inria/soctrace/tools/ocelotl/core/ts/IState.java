package fr.inria.soctrace.tools.ocelotl.core.ts;

import java.util.Map;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public interface IState {
	public TimeRegion getTimeRegion();

	public String getStateType();

	public int getEventProducerID();

	public Map<Long, Long> getTimeSlicesDistribution();

}
