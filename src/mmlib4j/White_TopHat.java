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
public class White_TopHat implements PlugInFilter {
	
	double raio;
	ImagePlus imgPlus;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		GenericDialog tela = new GenericDialog("White top-hat");
		tela.addNumericField("Radius", 5, 1);
		
		tela.showDialog();
		if(tela.wasCanceled()){
			return PlugInFilter.DONE;
		}
		imgPlus = imp;
		raio = tela.getNextNumber();
		return PlugInFilter.DOES_8G;
	}

	@Override
	public void run(ImageProcessor img) {
		ImageUtils.initMMorph4J();
		GrayScaleImage gimg = ImageJAdapter.toGrayScaleImage((ByteProcessor) img);
		GrayScaleImage imgOut = MorphologicalOperatorsBasedOnSE.openingTopHat(gimg, AdjacencyRelation.getCircular(raio));
		imgPlus.setProcessor("White top-hat", ImageJAdapter.toByteProcessor(imgOut));
		//ImagePlus plus = new ImagePlus("White top-hat", ImageJAdapter.toByteProcessor(imgOut));
		//plus.show();
		
	}

}
