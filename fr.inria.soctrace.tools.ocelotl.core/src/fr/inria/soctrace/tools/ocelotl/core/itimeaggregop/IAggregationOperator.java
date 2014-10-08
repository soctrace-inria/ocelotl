package fr.inria.soctrace.tools.ocelotl.core.itimeaggregop;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.micromodel.MicroscopicModel;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.IMicroDescManager;

public interface IAggregationOperator {

	public IMicroDescManager createManager(MicroscopicModel microMod, IProgressMonitor monitor) throws OcelotlException;

}
