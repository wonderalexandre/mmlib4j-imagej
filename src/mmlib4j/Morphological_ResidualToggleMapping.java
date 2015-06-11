package mmlib4j;






import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import mmlib4j.filtering.ToggleMapping;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.imagej.utils.ImageUtils;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.AdjacencyRelation;


public class Morphological_ResidualToggleMapping implements PlugInFilter {
	
	double raio;
	ImagePlus imgPlus;
	
	public int setup(String arg, ImagePlus imp) {
		
		GenericDialog tela = new GenericDialog("Residual toggle mapping (erosion/dilation)");
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
		GrayScaleImage imgOut = ToggleMapping.toggleMappingResidue(ImageJAdapter.toGrayScaleImage((ByteProcessor) ip), AdjacencyRelation.getCircular(raio));
		
		imgPlus.setProcessor("Residual toggle mapping", ImageJAdapter.toByteProcessor(imgOut));
		//ImagePlus plus = new ImagePlus("Morphological Closing", ImageJAdapter.toByteProcessor(imgOut));
		//plus.show();
		
		
	}
	
	public static void main(String args[]){
		ImagePlus plus = ImageUtils.openGrayScale();
		Morphological_ResidualToggleMapping plugin = new Morphological_ResidualToggleMapping();
		plugin.setup(null, plus);
		plugin.run(plus.getProcessor());
	}
}
