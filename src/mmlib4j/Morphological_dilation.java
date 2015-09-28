package mmlib4j;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import mmlib4j.filtering.MorphologicalOperators;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.imagej.utils.ImageUtils;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.AdjacencyRelation;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Morphological_dilation implements PlugInFilter {
	
	double raio;
	ImagePlus imgPlus;
	
	public int setup(String arg, ImagePlus imp) {
		GenericDialog tela = new GenericDialog("Dilation");
		tela.addNumericField("Radius", 1.5, 1);
		tela.showDialog();
		if(tela.wasCanceled()){
			return PlugInFilter.DONE;
		}
		imgPlus = imp;
		raio = tela.getNextNumber();
		return PlugInFilter.DOES_8G | PlugInFilter.DOES_RGB;
	}
	
	public void run(ImageProcessor ip) { 
		ImageUtils.initMMorph4J();
		
		if(ip instanceof ColorProcessor){
			ColorProcessor imgRGB = (ColorProcessor) ip;
			AdjacencyRelation adj = AdjacencyRelation.getCircular(raio);
			
			GrayScaleImage imageR = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_8BITS, imgRGB.getChannel(1), ip.getWidth(), ip.getHeight());
			GrayScaleImage imageG = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_8BITS, imgRGB.getChannel(2), ip.getWidth(), ip.getHeight());
			GrayScaleImage imageB = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_8BITS, imgRGB.getChannel(3), ip.getWidth(), ip.getHeight());
			
			GrayScaleImage imgOutR = MorphologicalOperators.dilation(imageR, adj);
			GrayScaleImage imgOutG = MorphologicalOperators.dilation(imageG, adj);
			GrayScaleImage imgOutB = MorphologicalOperators.dilation(imageB, adj);
			
			ColorProcessor rgbOutput = new ColorProcessor(ip.getWidth(), ip.getHeight());
			rgbOutput.setRGB((byte[])imgOutR.getPixels(), (byte[])imgOutG.getPixels(), (byte[])imgOutB.getPixels());
			
			imgPlus.setProcessor("Dilation", rgbOutput);
		}else{
			
			GrayScaleImage imgOut = MorphologicalOperators.dilation(ImageJAdapter.toGrayScaleImage((ByteProcessor) ip), AdjacencyRelation.getCircular(raio));
			imgPlus.setProcessor("Dilation", ImageJAdapter.toByteProcessor(imgOut));
			//ImagePlus plus = new ImagePlus("Morphological Dilation", ImageJAdapter.toByteProcessor(imgOut));
			//plus.show();
			
		}
		
	}
	
	public static void main(String args[]){
		ImagePlus plus = ImageUtils.openGrayScale();
		Morphological_dilation plugin = new Morphological_dilation();
		plugin.setup(null, plus);
		plugin.run(plus.getProcessor());
	}
}
