/* ===========================================================
 * LPAggreg UI module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to visualize a Paje
 * trace across an aggregated representation.
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

package fr.inria.soctrace.tools.ocelotl.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/** 
 * Colors manager class for matrix view
 * Determines which color is each successive part
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>" 
 */
public class ColorManager {

	private List<LPColor>						colors	= new ArrayList<LPColor>();
	private static final int					incrementR	= 47;
	private static final int					incrementG	= 77;
	private static final int					incrementB	= 66;
	private static final int 					total		= 1000;							

	public ColorManager() {
		super();
		colors.add(new LPColor(ColorConstants.red));
		//colors.add(new LPColor(ColorConstants.blue));
		//colors.add(new LPColor(ColorConstants.green));
		//colors.add(new LPColor(ColorConstants.orange));
		while (colors.size()<total){
			LPColor color=new LPColor(colors.get(colors.size()-1).getBg().getRed()+incrementR, colors.get(colors.size()-1).getBg().getGreen()+incrementG, colors.get(colors.size()-1).getBg().getBlue()+incrementB);
			while (color.isTooLight())
				color=new LPColor(color.getBg().getBlue()+incrementG, color.getBg().getRed()+incrementB, color.getBg().getGreen()+incrementR);
			colors.add(color);	
		}
	}

	public List<LPColor> getColors() {
		return colors;
	}

}
