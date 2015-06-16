package mmlib4j;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import mmlib4j.filtering.MorphologicalOperators;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.componentTree.ReconstructionMorphological;
import mmlib4j.utils.AdjacencyRelation;
/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Opening_by_reconstruction implements PlugInFilter {
	
	double raio;
	double raioEE;
	ImagePlus imgPlus;
	
	public void run(ImageProcessor ip) {
		GrayScaleImage f = ImageJAdapter.toGrayScaleImage((ByteProcessor) ip);
		GrayScaleImage g = MorphologicalOperators.opening(f, AdjacencyRelation.getCircular(raioEE));
		ReconstructionMorphological rec = new ReconstructionMorphological(f, AdjacencyRelation.getCircular(raio), true);
		GrayScaleImage imgOut = rec.reconstructionByDilation(g);
		
		imgPlus.setProcessor("Opening by reconstruction", ImageJAdapter.toByteProcessor(imgOut));
		//ImagePlus imgPlus = new ImagePlus("Opening by reconstruction", ImageJAdapter.toByteProcessor(imgOut));
		//imgPlus.show();
		
	}

	 boolean showDialog() { 
         GenericDialog gd = new GenericDialog("Morphological reconstruction based on marked image");

         gd.addNumericField("Adjacency relation [rec] (Radius)", 1.5, 1);
 		 gd.addNumericField("Structuring element [opening] (Radius)", 1.5, 1);
 		
 		 gd.showDialog();
          if (gd.wasCanceled())
             return false;


 		raio = gd.getNextNumber();
 		raioEE = gd.getNextNumber();
 		 		
 		return true;
 }

	public int setup(String arg, ImagePlus imp) {
		 if (imp!=null && !showDialog()) return DONE;
		 imgPlus = imp;
		 return DOES_8G;
	}

}

