/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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

package fr.inria.soctrace.tools.ocelotl.ui.color;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;

/**
 * Colors manager class for matrix view Determines which color is each
 * successive part
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class ColorManager {

	private final List<OcelotlColor>	colors		= new ArrayList<OcelotlColor>();
	private static final int			incrementR	= 47;
	private static final int			incrementG	= 77;
	private static final int			incrementB	= 66;
	private static final int			total		= 1000;

	public ColorManager() {
		super();
		colors.add(new OcelotlColor(ColorConstants.red));
		// colors.add(new OcelotlColor(ColorConstants.blue));
		// colors.add(new OcelotlColor(ColorConstants.green));
		// colors.add(new OcelotlColor(ColorConstants.orange));
		while (colors.size() < total) {
			OcelotlColor color = new OcelotlColor(colors.get(colors.size() - 1).getBg().getRed() + incrementR, colors.get(colors.size() - 1).getBg().getGreen() + incrementG, colors.get(colors.size() - 1).getBg().getBlue() + incrementB);
			while (color.isTooLight())
				color = new OcelotlColor(color.getBg().getBlue() + incrementG, color.getBg().getRed() + incrementB, color.getBg().getGreen() + incrementR);
			colors.add(color);
		}
	}

	public List<OcelotlColor> getColors() {
		return colors;
	}

}
