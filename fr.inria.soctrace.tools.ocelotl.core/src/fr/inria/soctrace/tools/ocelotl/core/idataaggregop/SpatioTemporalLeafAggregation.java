package fr.inria.soctrace.tools.ocelotl.core.idataaggregop;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.SpaceTimeAggregationLeavesManager;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;

public class SpatioTemporalLeafAggregation implements IDataAggregationOperator {

	@Override
	public SpaceTimeAggregationLeavesManager createManager(
			MicroscopicDescription microMod, IProgressMonitor monitor)
			throws OcelotlException {
		return new SpaceTimeAggregationLeavesManager(microMod, monitor);
	}
}
