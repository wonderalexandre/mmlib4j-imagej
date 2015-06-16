package mmlib4j;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import javax.swing.JFrame;

import mmlib4j.imagej.plugins.filters.MorphologicalOperatorBySE;
import mmlib4j.imagej.utils.ImageUtils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Morphological_operator_based_structuring_element extends JFrame implements PlugInFilter {
	
	ImagePlus plus;
	
	public void run(ImageProcessor ip) { 
		ImageUtils.initMMorph4J();
		final MorphologicalOperatorBySE win = new MorphologicalOperatorBySE(plus);
		win.setVisible(true);
		
	}
	
	boolean showDialog() { 
		return true;
	}
	public int setup(String arg, ImagePlus imp) {
		plus = imp;
		if (imp!=null && !showDialog()) return DONE;
		return DOES_8G;
	}
	
	public static void main(String args[]){
		ImagePlus plus = ImageUtils.openGrayScale();
		
		Morphological_operator_based_structuring_element plugin = new Morphological_operator_based_structuring_element();
		plugin.setup(null, plus);
		plugin.run(plus.getProcessor());
	}
}
