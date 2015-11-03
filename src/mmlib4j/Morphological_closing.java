package mmlib4j;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import mmlib4j.filtering.MorphologicalOperatorsBasedOnSE;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.imagej.utils.ImageUtils;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.AdjacencyRelation;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Morphological_closing implements PlugInFilter {
	
	double raio;
	ImagePlus imgPlus;
	
	public int setup(String arg, ImagePlus imp) {
		
		GenericDialog tela = new GenericDialog("Closing");
		tela.addNumericField("Radius", 1.5, 1);
		tela.showDialog();
		if(tela.wasCanceled()){
			return PlugInFilter.DONE;
		}
		imgPlus = imp;
		raio = tela.getNextNumber();
		return PlugInFilter.DOES_8G;
	}
	
	public void run(ImageProcessor ip) { 
		ImageUtils.initMMorph4J();
		GrayScaleImage imgOut = MorphologicalOperatorsBasedOnSE.closing(ImageJAdapter.toGrayScaleImage((ByteProcessor) ip), AdjacencyRelation.getCircular(raio));
		
		imgPlus.setProcessor("Closing", ImageJAdapter.toByteProcessor(imgOut));
		//ImagePlus plus = new ImagePlus("Morphological Closing", ImageJAdapter.toByteProcessor(imgOut));
		//plus.show();
		
		
	}
	
	public static void main(String args[]){
		ImagePlus plus = ImageUtils.openGrayScale();
		Morphological_closing plugin = new Morphological_closing();
		plugin.setup(null, plus);
		plugin.run(plus.getProcessor());
	}
}
