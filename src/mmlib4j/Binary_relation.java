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
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageAlgebra;

public class Binary_relation implements PlugInFilter {
	
	ByteProcessor imgF;
	ByteProcessor imgG;
	String op;
	
	public void run(ImageProcessor ip) {
		//long start = System.currentTimeMillis();
		GrayScaleImage f = ImageJAdapter.toGrayScaleImage(imgF);
		GrayScaleImage g = ImageJAdapter.toGrayScaleImage(imgG);
		
		if("g <is a connected of> f".equals(op)){
			IJ.showMessage("Result", "g <is a connected of> f ==>>> "+ String.valueOf(ImageAlgebra.isPlanning(g, f, AdjacencyRelation.getAdjacency4())));
			
		}else if("g <is a arc-wise connected of> of f".equals(op)){
			IJ.showMessage("Result", "g <is a arc-wise connected of> of f ==>>> "+ String.valueOf(ImageAlgebra.isMonotonePlaning(g, f, AdjacencyRelation.getAdjacency4())));
		}else if("g <is leveling of> f".equals(op)){
			IJ.showMessage("Result", "g <is leveling of> f ==>>> "+ String.valueOf(ImageAlgebra.isLeveling(g, f, AdjacencyRelation.getAdjacency4())));
		}else if("g <is flattening of> f".equals(op)){
			IJ.showMessage("Result", "g <is flattening of> f ==>>> "+ String.valueOf(ImageAlgebra.isFlattening(g, f, AdjacencyRelation.getAdjacency4())));
		}else if("g = f".equals(op)){
			IJ.showMessage("Result", "g = f ==>>> "+ String.valueOf(ImageAlgebra.equals(g, f)));
		}
		else if("g > f".equals(op)){
			IJ.showMessage("Result", "g > f ==>>> "+ String.valueOf(ImageAlgebra.isGreater(g, f)));
		}else if("g <= f".equals(op)){
			IJ.showMessage("Result", "g <= f ==>>> "+ String.valueOf(ImageAlgebra.isLessOrEqual(g, f)));
		}else if("g <is extensive of> f".equals(op)){
			IJ.showMessage("Result", "g <is extensive of> f ==>>> "+ String.valueOf(ImageAlgebra.isExtensive(g, f)));
		}else if("g <is anti-extensive of> f".equals(op)){
			IJ.showMessage("Result", "g <is anti-extensive of> f ==>>> "+ String.valueOf(ImageAlgebra.isAntiExtensive(g, f)));
		}
		
		
	}

	 boolean showDialog() { 
         GenericDialog gd = new GenericDialog("Binary relation");
         gd.addMessage("This plug-in if the image f <is R-related> the image g");


 		int[] wList = WindowManager.getIDList();

 		if (wList == null || wList.length < 2) {
 			IJ.showMessage("Levelings", "There must be at least two windows open");
 			return false;
 		}  
 		
 		String[] titles = new String[wList.length];
 		for (int i = 0, k = 0; i < wList.length; i++) {
 			ImagePlus imp = WindowManager.getImage(wList[i]);
 			if (null != imp)
 				titles[k++] = imp.getTitle();
 		}

 		gd.addChoice("image f:", titles, titles[0]);
 		gd.addChoice("image g:", titles, titles[1]);
         
 		String [] Op={"g <is a connected of> f", 
 					"g <is a arc-wise connected of> of f", 
 					"g <is leveling of> f", 
 					"g <is flattening of> f", 
 					"g <is extensive of> f", 
 					"g <is anti-extensive of> f", 
 					"g = f",
 					"g > f", 
 					"g <= f" };
 		gd.addChoice("Choose an order relation: ", Op, "niv parallel (f, g) [leveling]");
 		
 		
 		   
 		
 		gd.showDialog();
         if (gd.wasCanceled())
             return false;


 		int i1Index = gd.getNextChoiceIndex();
 		int i2Index = gd.getNextChoiceIndex();
 		
 		ImagePlus imp1 = WindowManager.getImage(wList[i1Index]);
 		ImagePlus imp2 = WindowManager.getImage(wList[i2Index]);

 		if (imp1.getBitDepth()!=8 || imp2.getBitDepth()!=8) {
 			IJ.showMessage("Error", "Only 8-bit images are supported");
 			return false;
 		}

 		imgF = (ByteProcessor) imp1.getProcessor();
 		imgG =(ByteProcessor) imp2.getProcessor();
 		op = gd.getNextChoice();
 		return true;
 }

	public int setup(String arg, ImagePlus imp) {
		 if (imp!=null && !showDialog()) return DONE;
		 return DOES_8G;
	}

}

