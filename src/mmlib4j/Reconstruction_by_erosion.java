package mmlib4j;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
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
public class Reconstruction_by_erosion implements PlugInFilter {
	
	ByteProcessor imgF;
	ByteProcessor imgG;
	double raio;
	
	public void run(ImageProcessor ip) {
		GrayScaleImage f = ImageJAdapter.toGrayScaleImage(imgF);
		GrayScaleImage g = ImageJAdapter.toGrayScaleImage(imgG);
		ReconstructionMorphological rec = new ReconstructionMorphological(f, AdjacencyRelation.getCircular(raio), false);
		GrayScaleImage imgOut = rec.reconstructionByErosion(g);
		ImagePlus imgPlus = new ImagePlus("Reconstruction by erosion", ImageJAdapter.toByteProcessor(imgOut));
		imgPlus.show();
		
	}

	 boolean showDialog() { 
         GenericDialog gd = new GenericDialog("Morphological reconstruction based on marked image");
         gd.addMessage("This plug-in calculates reconstruction by erosion");


 		int[] wList = WindowManager.getIDList();

 		if (wList == null || wList.length < 2) {
 			IJ.showMessage("Morphological reconstruction based on marked image", "There must be at least two windows open");
 			return false;
 		}  
 		
 		String[] titles = new String[wList.length];
 		for (int i = 0, k = 0; i < wList.length; i++) {
 			ImagePlus imp = WindowManager.getImage(wList[i]);
 			if (null != imp)
 				titles[k++] = imp.getTitle();
 		}

 		gd.addChoice("mask image:", titles, titles[0]);
 		gd.addChoice("marked image:", titles, titles[1]);
 		gd.addNumericField("Adjacency relation (Radius)", 1.5, 1);
 		
 		gd.showDialog();
         if (gd.wasCanceled())
             return false;


 		int i1Index = gd.getNextChoiceIndex();
 		int i2Index = gd.getNextChoiceIndex();
 		raio = gd.getNextNumber();
 		
 		ImagePlus imp1 = WindowManager.getImage(wList[i1Index]);
 		ImagePlus imp2 = WindowManager.getImage(wList[i2Index]);

 		if (imp1.getBitDepth()!=8 || imp2.getBitDepth()!=8) {
 			IJ.showMessage("Error", "Only 8-bit images are supported");
 			return false;
 		}

 		imgF = (ByteProcessor) imp1.getProcessor();
 		imgG =(ByteProcessor) imp2.getProcessor();
 		return true;
 }

	public int setup(String arg, ImagePlus imp) {
		 if (imp!=null && !showDialog()) return DONE;
		 return DOES_8G;
	}

}

