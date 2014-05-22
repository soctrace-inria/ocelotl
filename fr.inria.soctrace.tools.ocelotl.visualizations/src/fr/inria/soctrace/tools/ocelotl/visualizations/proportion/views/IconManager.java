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

package fr.inria.soctrace.tools.ocelotl.visualizations.proportion.views;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.ResourceManager;
import org.osgi.framework.Bundle;


import fr.inria.soctrace.tools.ocelotl.visualizations.Activator;

public class IconManager {
	
	
	private Device device;

	public IconManager() {
		super();
		this.device=Display.getCurrent();
	}
	
	public Image getImage(int size){
		int s=16;
		if(size>=256)
			s=256;
		else if (size>=48)
			s=48;
		else if (size>=32)
			s=32;
		else if (size>=24)
			s=24;
		Image image = ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/info"+s+".png");
		if (size<16)
			s=size;
	return new Image(device, image.getImageData().scaledTo(s, s));
	}

	public Image getBackupImage() {
		return ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/backup.png");
	}
	
	
}
