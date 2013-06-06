package fr.inria.soctrace.tools.paje.lpaggreg.ui.views;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

public class LPColor {
	
	private static int light = 180;
	private static int tooLight = 180;
	private Color bg;
	private Color fg;
	
	public LPColor(Color bg) {
		super();
		this.bg = bg;
		setFg();
	}
	
	public LPColor(int r, int g, int b){
		super();
		Device device = Display.getCurrent ();
		this.bg = new Color(device, r%255, g%255, b%255);
		setFg();
	}
	
	public void setFg(){
		if ((bg.getBlue()>light&&bg.getGreen()>light)||(bg.getBlue()>light&&bg.getRed()>light)||(bg.getGreen()>light&&bg.getRed()>light))
			this.fg=ColorConstants.black;
		else this.fg=ColorConstants.white;
	}
	
	public boolean isTooLight(){
		if (bg.getBlue()>tooLight&&bg.getGreen()>tooLight&&bg.getRed()>tooLight)
			return true;
		else 
			return false;
	}

	
	public static int getLight() {
		return light;
	}

	
	public static void setLight(int light) {
		LPColor.light = light;
	}

	
	public static int getTooLight() {
		return tooLight;
	}

	
	public static void setTooLight(int tooLight) {
		LPColor.tooLight = tooLight;
	}

	
	public Color getBg() {
		return bg;
	}

	
	public void setBg(Color bg) {
		this.bg = bg;
	}

	
	public Color getFg() {
		return fg;
	}

	
	public void setFg(Color fg) {
		this.fg = fg;
	}
	
	
	
	
	

}
