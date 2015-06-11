package mmlib4j;






import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import mmlib4j.filtering.MorphologicalOperators;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.imagej.utils.ImageUtils;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.AdjacencyRelation;


public class Morphological_external_gradient implements PlugInFilter {
	
	double raio;
	ImagePlus imgPlus;
	
	public int setup(String arg, ImagePlus imp) {
		GenericDialog tela = new GenericDialog("Morphological Gradient (external)");
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
		GrayScaleImage imgOut = MorphologicalOperators.gradientExternal(ImageJAdapter.toGrayScaleImage((ByteProcessor) ip), AdjacencyRelation.getCircular(raio));
		imgPlus.setProcessor("Morphological Gradient (external)", ImageJAdapter.toByteProcessor(imgOut));
		//ImagePlus plus = new ImagePlus("Morphological Gradient", ImageJAdapter.toByteProcessor(imgOut));
		//plus.show();
		
	}
	
	public static void main(String args[]){
		ImagePlus plus = ImageUtils.openGrayScale();
		Morphological_external_gradient plugin = new Morphological_external_gradient();
		plugin.setup(null, plus);
		plugin.run(plus.getProcessor());
	}
}
