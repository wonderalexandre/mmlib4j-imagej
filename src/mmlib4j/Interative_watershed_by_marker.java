package mmlib4j;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import javax.swing.JFrame;

import mmlib4j.imagej.plugins.segmentation.InterativeWatershedByMarked;
import mmlib4j.imagej.utils.ImageUtils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Interative_watershed_by_marker extends JFrame implements PlugInFilter {

	ImagePlus plus;
	
	public void run(ImageProcessor ip) { 
		ImageUtils.initMMorph4J();
		final InterativeWatershedByMarked win = new InterativeWatershedByMarked( plus);
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
}
