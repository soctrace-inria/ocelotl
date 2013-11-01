package fr.inria.soctrace.tools.ocelotl.core.state;

import java.util.Map;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public interface IState {
	public int getEventProducerID();

	public String getStateType();

	public TimeRegion getTimeRegion();

	public Map<Long, Long> getTimeSlicesDistribution();

}
