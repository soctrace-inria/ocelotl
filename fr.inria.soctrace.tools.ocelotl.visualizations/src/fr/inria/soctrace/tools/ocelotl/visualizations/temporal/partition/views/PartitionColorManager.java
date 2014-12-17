/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
 * overview by using aggregation techniques
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

package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.partition.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;

/**
 * Colors manager class for matrix view Determines which color is each
 * successive part
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class PartitionColorManager {

	private final List<PartitionColor>	colors		= new ArrayList<PartitionColor>();
	private static final int		incrementR	= 47;
	private static final int		incrementG	= 77;
	private static final int		incrementB	= 66;
	private static final int 		incrementW  = 22;
	private static final int		total		= 1000000;
	private int 					watchDog	= 0;
	private static final int 		watchDogThreshold	= 100;

	public PartitionColorManager() {
		super();
		colors.add(new PartitionColor(ColorConstants.red));
		while (colors.size() < total) {
			PartitionColor color = new PartitionColor(colors.get(colors.size() - 1).getBg().getRed() + incrementR, colors.get(colors.size() - 1).getBg().getGreen() + incrementG, colors.get(colors.size() - 1).getBg().getBlue() + incrementB);
			while (color.isTooLight()){
				color = new PartitionColor(color.getBg().getBlue() + incrementG, color.getBg().getRed() + incrementB, color.getBg().getGreen() + incrementR);
				if (watchDog++>watchDogThreshold){
					color = new PartitionColor(color.getBg().getBlue() + incrementW, color.getBg().getRed() + incrementW, color.getBg().getGreen() + incrementW);
					watchDog=0;
				}
					
			}
			colors.add(color);
		}
	}

	public List<PartitionColor> getColors() {
		return colors;
	}

}
