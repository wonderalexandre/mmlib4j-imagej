package mmlib4j;


import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.utils.ImageBuilder;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Colorspace_Chong implements PlugInFilter{
	int levelOld;
	int levelNew;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		return PlugInFilter.DOES_RGB;
	}

	@Override
	public void run(ImageProcessor img) {
		new ImagePlus("colorspace", ImageJAdapter.toByteProcessor( ImageBuilder.getConvertColorSpaceRGBtoChong( ImageJAdapter.toColorImage((ColorProcessor) img) ) )).show();
	}
}
