package mmlib4j;






import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import javax.swing.JFrame;

import mmlib4j.imagej.plugins.residual.UltimateLevelings;
import mmlib4j.imagej.utils.ImageUtils;


public class Ultimate_levelings extends JFrame implements PlugInFilter {

	private static final long serialVersionUID = 1L;
	ImagePlus plus;
	
	public void run(ImageProcessor ip) {
		ImageUtils.initMMorph4J();
		final UltimateLevelings win = new UltimateLevelings(plus);
		win.setVisible(true);
		
				
	}
	
	boolean showDialog() { 
		return true;
	}
	public int setup(String arg, ImagePlus imp) {
		this.plus = imp;
		if (imp!=null && !showDialog()){
			return DONE;
		}
		
		//if(imp.getHeight() * imp.getWidth() > 1000000){
		//	new MessageDialog(this, "Limitation", "the input image resolution needs to be less than one megapixel");
		//	return PlugInFilter.DONE;			
		//}
		return DOES_8G;
	}
	
	
	public static void main(String args[]){
		ImagePlus plus = ImageUtils.openGrayScale();
		
		Ultimate_levelings plugin = new Ultimate_levelings();
		plugin.setup(null, plus);
		plugin.run(plus.getProcessor());
	}
}
