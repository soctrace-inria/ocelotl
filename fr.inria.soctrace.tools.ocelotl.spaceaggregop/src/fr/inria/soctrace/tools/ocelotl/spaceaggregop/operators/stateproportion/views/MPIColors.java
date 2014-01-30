/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.spaceaggregop.operators.stateproportion.views;

import java.io.ObjectInputStream.GetField;
import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

public class MPIColors {

	public static HashMap<String, Color>	Colors	= new HashMap<String, Color>();
	private static Device device;

	static {
		init();
	}
	

	public static Color ocelotlColor(float r, float g, float b) {
			// TODO Auto-generated constructor stub
			return new Color(device, (int)r*255, (int)g*255, (int)b*255);
		}
		

	private static void init() {
		Colors.clear();
		device = Display.getCurrent();
		Colors.put("Execution", new Color(device, (int) (255 * 0.179082), (int) (255 * 0.934619), 255 * 1));
		Colors.put("MPI_Allreduce", new Color(device, 255 * 1, (int) (255 * 0.323597), (int) (255 * 0.7323)));
		Colors.put("MPI_Alltoall", new Color(device, (int) (255 * 0.305274), (int) (255 * 0.454396), 255 * 1));
		Colors.put("MPI_Alltoallv", new Color(device, (int) (255 * 0.321158), (int) (255 * 0.912927), 255 * 1));
		Colors.put("MPI_Barrier", new Color(device, 255 * 0, (int) (255 * 0.991057), 255 * 1));
		Colors.put("MPI_Bcast", new Color(device, 255 * 1, (int) (255 * 0.996891), 255 * 0));
		Colors.put("MPI_Comm_rank", new Color(device, 255 * 1, (int) (255 * 0.559672), (int) (255 * 0.289578)));
		Colors.put("MPI_Comm_size", new Color(device, 255 * 1, (int) (255 * 0.464106), (int) (255 * 0.579677)));
		Colors.put("MPI_Finalize", new Color(device, (int) (255 * 0.915529), 255 * 0, 255 * 1));
		Colors.put("MPI_Init", new Color(device, 255 * 1, (int) (255 * 0.927823), (int) (255 * 0.21797)));
		Colors.put("MPI_Recv", new Color(device, 255 * 1, 255 * 0, 255 * 0));
		Colors.put("MPI_Reduce", new Color(device, (int) (255 * 0.446826), 255 * 1, (int) (255 * 0.280726)));
		Colors.put("MPI_Send", new Color(device, 255 * 0, 255 * 0, 255 * 1));
		Colors.put("MPI_Wait", new Color(device, 255 * 1, (int) (255 * 0.612729), (int) (255 * 0.62382)));
		Colors.put("MPI_Waitall", new Color(device, (int) (255 * 0.886904), (int) (255 * 0.384766), 255 * 1));
		Colors.put("MPI_Allgatherv", new Color(device, 255 * 1, (int) (255 * 0.5), (int) (255 * 0.0)));
		Colors.put("ERROR", new Color(device, 255 * 1, (int) (255 * 0.0), (int) (255 * 0.0)));
		Colors.put("WARN", new Color(device, 255 * 1, (int) (255 * 0.4), (int) (255 * 0.0)));
		Colors.put("FIXME", new Color(device, (int) (255 * 0.0), (int) (255 * 0.0), 255 * 1));
		Colors.put("INFO", new Color(device, (int) (255 * 0.0), 255 * 1, (int) (255 * 0.0)));
		Colors.put("DEBUG", new Color(device, 255 * 1, (int) (255 * 0.8), (int) (255 * 0.0)));
		Colors.put("LOG", new Color(device, (int) (255 * 0.4), (int) (255 * 0.4), (int) (255 * 0.4)));
		Colors.put("TRACE", new Color(device, (int) (255 * 0.3), (int) (255 * 0.3), (int) (255 * 0.0)));
		Colors.put("IDLE", new Color(device, (int) (255 * 0.7), (int) (255 * 0.7), (int) (255 * 0.7)));
//		5 12 11 underloaded "0 1 1"
//		5 13 11 normal "1 1 1"
//		5 14 11 violation "1 0 0"
//		5 15 11 violation-det "0 1 0"
//		5 16 11 violation-out "1 0 0"
//		2 17 2 SERVICE
//		5 18 17 free "1 1 1"
//		5 19 17 booked "0 0 1"
//		5 20 17 compute "1 0 1"
//		5 21 17 reconfigure "1 1 0"
		addColor("12", 0, 1, 1);
		addColor("13", 1, 1, 1);
		addColor("14", 1, 0, 0);
		addColor("15", 0, 1, 0);
		addColor("16", 1, 0, 0);
		addColor("17", 1, 1, 1);
		addColor("18", 1, 0.5, 0.5);
		addColor("19", 0, 0, 1);
		addColor("20", 1, 0, 1);
		addColor("21", 1, 1, 0);

	}
	
	static void addColor(String name, float r, float g, float b){
		Colors.put(name, ocelotlColor(r, g, b));
	}
	
	static void addColor(String name, double r, double g, double b){
		Colors.put(name, ocelotlColor((float)r, (float)g, (float)b));
	}

}
