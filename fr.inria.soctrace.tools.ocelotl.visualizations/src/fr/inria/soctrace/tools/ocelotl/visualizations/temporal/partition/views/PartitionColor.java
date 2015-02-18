/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

public class PartitionColor {

	private static int	light		= 180;
	private static int	tooLight	= 180;

	public static int getLight() {
		return light;
	}

	public static int getTooLight() {
		return tooLight;
	}

	public static void setLight(final int light) {
		PartitionColor.light = light;
	}

	public static void setTooLight(final int tooLight) {
		PartitionColor.tooLight = tooLight;
	}

	private Color	bg;

	private Color	fg;

	public PartitionColor(final Color bg) {
		super();
		this.bg = bg;
		setFg();
	}

	public PartitionColor(final int r, final int g, final int b) {
		super();
		final Device device = Display.getCurrent();
		bg = new Color(device, r % 255, g % 255, b % 255);
		setFg();
	}

	public Color getBg() {
		return bg;
	}

	public Color getFg() {
		return fg;
	}

	public boolean isTooLight() {
		if (bg.getBlue() > tooLight && bg.getGreen() > tooLight && bg.getRed() > tooLight)
			return true;
		else
			return false;
	}

	public void setBg(final Color bg) {
		this.bg = bg;
	}

	public void setFg() {
		if (bg.getBlue() > light && bg.getGreen() > light || bg.getBlue() > light && bg.getRed() > light || bg.getGreen() > light && bg.getRed() > light)
			fg = ColorConstants.black;
		else
			fg = ColorConstants.white;
	}

	public void setFg(final Color fg) {
		this.fg = fg;
	}

}
