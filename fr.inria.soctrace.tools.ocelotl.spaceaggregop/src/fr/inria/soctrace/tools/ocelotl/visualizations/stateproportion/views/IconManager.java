package fr.inria.soctrace.tools.ocelotl.visualizations.stateproportion.views;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

public class IconManager {
	
	
	private Device device;
	private Bundle bundle;

	public IconManager() {
		super();
		this.device=Display.getCurrent();
		bundle = Platform.getBundle("fr.inria.soctrace.tools.ocelotl.visualizations");
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
		URL fileURL = bundle.getEntry("icons/info"+s+".png");
		if (size<16)
			s=size;
		File file = null;
		try {
		    file = new File(FileLocator.resolve(fileURL).toURI());
		} catch (URISyntaxException e1) {
		    e1.printStackTrace();
		} catch (IOException e1) {
		    e1.printStackTrace();
		}
		file.getAbsolutePath();
		Image image = new Image(device, file.getAbsolutePath());
		return new Image(device, image.getImageData().scaledTo(s, s));
	}

	public Image getBackupImage() {
		URL fileURL = bundle.getEntry("icons/backup.png");
		File file = null;
		try {
		    file = new File(FileLocator.resolve(fileURL).toURI());
		} catch (URISyntaxException e1) {
		    e1.printStackTrace();
		} catch (IOException e1) {
		    e1.printStackTrace();
		}
		file.getAbsolutePath();
		return new Image(device, file.getAbsolutePath());
	}
	
	
}
