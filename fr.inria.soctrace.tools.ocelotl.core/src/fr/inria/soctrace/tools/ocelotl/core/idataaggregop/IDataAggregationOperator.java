package fr.inria.soctrace.tools.ocelotl.core.idataaggregop;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;

public interface IDataAggregationOperator {

	public IDataAggregManager createManager(MicroscopicDescription microMod,
			IProgressMonitor monitor) throws OcelotlException;
}
