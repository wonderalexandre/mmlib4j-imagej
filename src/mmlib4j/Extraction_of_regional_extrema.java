package mmlib4j;


import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.segmentation.RegionalMinimaByIFT;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Extraction_of_regional_extrema implements PlugInFilter {
	
	String back;
	
	public void run(ImageProcessor ip) {

		GrayScaleImage img = ImageJAdapter.toGrayScaleImage((ByteProcessor)ip);
		
		GrayScaleImage imgLabelMin = RegionalMinimaByIFT.extractionOfRegionalMinima(img);
		GrayScaleImage imgLabelMax = RegionalMinimaByIFT.extractionOfRegionalMinima(img.getInvert());
		
		int label = 1;
		GrayScaleImage imgLabel = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_32BITS, img.getWidth(), img.getHeight());
		for(int p=0; p < imgLabel.getSize(); p++){
			if(imgLabelMin.getPixel(p) > 0){
				imgLabel.setPixel(p, label++);
			}
			if(imgLabelMax.getPixel(p) > 0){
				imgLabel.setPixel(p, label++);
			}
		}
		
		ColorProcessor out = ImageJAdapter.toColorProcessor(imgLabel.randomColor());
		ImagePlus plus = new ImagePlus("regional extrema: "+ imgLabel.maxValue(), out);
		plus.show();  
		
	}


	public int setup(String arg, ImagePlus imp) {
		 return DOES_8G;
	}

}
