package mmlib4j;






import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mmlib4j.imagej.plugins.filters.ConnectedFilters;
import mmlib4j.imagej.utils.ImageUtils;


public class Grain_filters implements PlugInFilter {

	private ImagePlus plus;
	
	public void run(ImageProcessor ip) { 
		ImageUtils.initMMorph4J();
		final ConnectedFilters win = new ConnectedFilters( plus);
		win.setVisible(true);			
	}
	
	boolean showDialog() { 
		return true;
	}
	
	public int setup(String arg, ImagePlus imp) {
		this.plus = imp;
		if (imp!=null && !showDialog()) return DONE;
		return DOES_8G;
	}
	
	public static void main(String args[]){
		ImagePlus plus = ImageUtils.openGrayScale();
		Grain_filters plugin = new Grain_filters();
		plugin.setup(null, plus);
		plugin.run(plus.getProcessor());
	}
	
}
