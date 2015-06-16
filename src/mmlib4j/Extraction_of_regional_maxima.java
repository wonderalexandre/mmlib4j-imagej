package mmlib4j;


import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.segmentation.RegionalMinimaByIFT;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Extraction_of_regional_maxima implements PlugInFilter {
	
	
	public void run(ImageProcessor ip) {
		GrayScaleImage img = ImageJAdapter.toGrayScaleImage((ByteProcessor)ip);
		
		GrayScaleImage imgLabel = RegionalMinimaByIFT.extractionOfRegionalMinima(img.getInvert());
		
		ColorProcessor out = ImageJAdapter.toColorProcessor(imgLabel.randomColor());
		ImagePlus plus = new ImagePlus("regional maxima: "+ imgLabel.maxValue(), out);
		plus.show();
		
	}


	public int setup(String arg, ImagePlus imp) {
		 return DOES_8G;
	}

}
