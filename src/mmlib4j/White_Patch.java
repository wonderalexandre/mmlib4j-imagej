package mmlib4j;


import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import mmlib4j.filtering.color.ColorConstancy;
import mmlib4j.imagej.utils.ImageJAdapter;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class White_Patch implements PlugInFilter{
	int levelOld;
	int levelNew;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		return PlugInFilter.DOES_RGB;
	}

	@Override
	public void run(ImageProcessor img) {
		new ImagePlus("White patch", ImageJAdapter.toColorProcessor( ColorConstancy.whitePatch(ImageJAdapter.toColorImage((ColorProcessor) img)) )).show();
	}
}
