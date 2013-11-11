package fr.inria.soctrace.tools.ocelotl.ui.views.matrixview;

import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

public class MPIColors {
	
	public static HashMap<String, Color> Colors = new HashMap<String, Color>();

	static{
		init();
	}
	
	private static void init(){
		Colors.clear();
		final Device device = Display.getCurrent();
		Colors.put("Execution", new Color(device, (int)(255*0.179082), (int)(255*0.934619), (int)(255*1)));
		Colors.put("MPI_Allreduce", new Color(device, (int)(255*1), (int)(255*0.323597), (int)(255*0.7323)));
		Colors.put("MPI_Alltoall", new Color(device, (int)(255*0.305274), (int)(255*0.454396), (int)(255*1)));
		Colors.put("MPI_Alltoallv", new Color(device, (int)(255*0.321158), (int)(255*0.912927), (int)(255*1)));
		Colors.put("MPI_Barrier", new Color(device, (int)(255*0), (int)(255*0.991057), (int)(255*1)));
		Colors.put("MPI_Bcast", new Color(device, (int)(255*1), (int)(255*0.996891), (int)(255*0)));
		Colors.put("MPI_Comm_rank", new Color(device, (int)(255*1), (int)(255*0.559672 ), (int)(255*0.289578)));
		Colors.put("MPI_Comm_size", new Color(device, (int)(255*1), (int)(255*0.464106), (int)(255*0.579677)));
		Colors.put("MPI_Finalize", new Color(device, (int)(255*0.915529), (int)(255*0), (int)(255*1)));
		Colors.put("MPI_Init", new Color(device, (int)(255*1), (int)(255*0.927823), (int)(255*0.21797)));
		Colors.put("MPI_Recv", new Color(device, (int)(255*1), (int)(255*0), (int)(255*0)));
		Colors.put("MPI_Reduce", new Color(device, (int)(255*0.446826), (int)(255*1), (int)(255*0.280726)));
		Colors.put("MPI_Send", new Color(device, (int)(255*0), (int)(255*0), (int)(255*1)));
		Colors.put("MPI_Wait", new Color(device, (int)(255*1), (int)(255*0.612729), (int)(255*0.62382)));
		Colors.put("MPI_Waitall", new Color(device, (int)(255*0.886904), (int)(255*0.384766), (int)(255*1)));
		Colors.put("MPI_Allgatherv", new Color(device, (int)(255*1), (int)(255*0.5), (int)(255*0.0)));
		Colors.put("ERROR", new Color(device, (int) (255 * 1), (int) (255 * 0.0), (int) (255 * 0.0)));
		Colors.put("WARN", new Color(device, (int) (255 * 1), (int) (255 * 0.4), (int) (255 * 0.0)));
		Colors.put("FIXME", new Color(device, (int) (255 * 0.0), (int) (255 * 0.0), (int) (255 * 1)));
		Colors.put("INFO", new Color(device, (int) (255 * 0.0), (int) (255 * 1), (int) (255 * 0.0)));
		Colors.put("DEBUG", new Color(device, (int) (255 * 1), (int) (255 * 0.8), (int) (255 * 0.0)));
		Colors.put("LOG", new Color(device, (int) (255 * 0.4), (int) (255 * 0.4), (int) (255 * 0.4)));
		Colors.put("TRACE", new Color(device, (int) (255 * 0.3), (int) (255 * 0.3), (int) (255 * 0.0)));
		Colors.put("IDLE", new Color(device, (int) (255 * 0.7), (int) (255 * 0.7), (int) (255 * 0.7)));
		
	}


}
