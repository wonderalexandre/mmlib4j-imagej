package mmlib4j;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import mmlib4j.filtering.MorphologicalOperatorsBasedOnSE;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.imagej.utils.ImageUtils;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.segmentation.RegionalMinimaByIFT;
import mmlib4j.segmentation.WatershedByIFT;
import mmlib4j.utils.AdjacencyRelation;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Watershed_based_minima implements PlugInFilter {
	
	boolean gradient;
	
	public void run(ImageProcessor ip) {
		ImageUtils.initMMorph4J();
		long start = System.currentTimeMillis();
		GrayScaleImage img = ImageJAdapter.toGrayScaleImage((ByteProcessor)ip);;
		if(gradient){
			img = MorphologicalOperatorsBasedOnSE.gradient(img, AdjacencyRelation.getAdjacency8());
		}
		
		GrayScaleImage imgMinima = RegionalMinimaByIFT.extractionOfRegionalMinima(img);
		imgMinima.replaceValue(0, -1);
		
		
		GrayScaleImage imgWS = WatershedByIFT.watershedByMarker(img, imgMinima);
		
		ImagePlus plus = new ImagePlus("watershed by minima", ImageJAdapter.toColorProcessor(imgWS.randomColor()));
		plus.show();
		
		IJ.showStatus(IJ.d2s((System.currentTimeMillis() - start) / 1000.0, 2) + " seconds");
	}


	@Override
	public int setup(String arg, ImagePlus imp) {
		ImageUtils.initMMorph4J();
		GenericDialog tela = new GenericDialog("watershed by minima");
		tela.addCheckbox("You want to apply the watershed in the input image gradient?", true);
		
		tela.showDialog();
		if(tela.wasCanceled()){
			return PlugInFilter.DONE;
		}
		
		gradient = tela.getNextBoolean();
		return PlugInFilter.DOES_8G;
	}

}
