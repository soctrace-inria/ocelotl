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

package fr.inria.soctrace.tools.ocelotl.visualizations.temporal.proportion.views;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventColorManager {

	static final String FILE = "configuration" + File.separator + ".ocelotl";
	static final String SEP = "sp@ce";

	Map<String, Color> colors = new HashMap<String, Color>();
	final Device device;

	private static final Logger logger = LoggerFactory
			.getLogger(EventColorManager.class);

	public EventColorManager() {
		super();
		this.device = Display.getCurrent();
		openFile();
		parseFile();
	}

	private void openFile() {
		try {
			File file = new File(FILE);
			if (file.createNewFile()) {
				logger.debug("Ocelotl state color configuration not found, creating file");
			} else {
				logger.debug("Ocelotl state color configuration found: "
						+ file.getAbsolutePath());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseFile() {

		BufferedReader br = null;
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(FILE));

			while ((sCurrentLine = br.readLine()) != null) {
				String tokens[] = sCurrentLine.split(SEP);
				String state = tokens[0];
				int red = Integer.parseInt(tokens[1]);
				int green = Integer.parseInt(tokens[2]);
				int blue = Integer.parseInt(tokens[3]);
				colors.put(state, new Color(device, red, green, blue));
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void updateFile() {
		try {

			File file = new File(FILE);

			file.delete();
			file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (String state : colors.keySet())
				bw.write(state + SEP + colors.get(state).getRed() + SEP
						+ colors.get(state).getGreen() + SEP
						+ colors.get(state).getBlue() + "\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RGB getRGB(String state) {
		return colors.get(state).getRGB();
	}

	public Color getColor(String state) {
		return colors.get(state);
	}

	public void setRGB(String state, RGB rgb) {
		colors.put(state, new Color(device, rgb));
	}

	public void testState(String name) {
		if (!colors.containsKey(name))
			colors.put(name, new Color(device, (int) (Math.random() * 255),
					(int) (Math.random() * 255), (int) (Math.random() * 255)));
	}

}
