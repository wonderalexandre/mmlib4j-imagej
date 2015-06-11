package mmlib4j;






import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.util.HashMap;

import mmlib4j.filtering.MorphologicalOperators;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.imagej.utils.ImageUtils;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.segmentation.WatershedByIFT;
import mmlib4j.utils.AdjacencyRelation;

public class Watershed_by_Marked implements PlugInFilter {
	
	ByteProcessor imgF;
	ColorProcessor imgG;
	String op;
	ImagePlus plus;
	boolean gradient;
	
	public void run(ImageProcessor ip) {
		ImageUtils.initMMorph4J();
		long start = System.currentTimeMillis();
		

		GrayScaleImage img = ImageJAdapter.toGrayScaleImage((ByteProcessor)imgF);;
		if(gradient){
			img = MorphologicalOperators.gradient(img, AdjacencyRelation.getAdjacency8());
		}
		
		ColorImage markedC =ImageJAdapter.toColorImage(imgG); 
		GrayScaleImage imgMinima = ImageFactory.createGrayScaleImage(32,  markedC.getWidth(), markedC.getHeight());
		HashMap<Integer, Integer> labels = new HashMap<Integer, Integer>();
		int label = 0;
		
		for(int x=1; x < img.getWidth(); x++){
			for(int y=1; y < img.getHeight(); y++){
				if(markedC.getRed(x, y) == 0 && markedC.getBlue(x,y) == 0 && markedC.getGreen(x,y) == 0){
					imgMinima.setPixel(x-1,y-1, -1);
				}
				else{
					if(labels.containsKey(markedC.getPixel(x,y))){
						imgMinima.setPixel(x-1,y-1, labels.get(markedC.getPixel(x,y)));
					}else{
						//System.out.println(value);
						imgMinima.setPixel(x-1,y-1, label);
						labels.put(markedC.getPixel(x,y), label);
						label += 1;
					}
				}
			}
			
		}
		
		
	
		
		GrayScaleImage imgWS = WatershedByIFT.watershedByMarker(img, imgMinima);
		ImagePlus plus = new ImagePlus("watershed by marked", ImageJAdapter.toColorProcessor(imgWS.randomColor()));
		plus.show();
		
		IJ.showStatus(IJ.d2s((System.currentTimeMillis() - start) / 1000.0, 2) + " seconds");
		
	}

	 boolean showDialog() { 
         GenericDialog gd = new GenericDialog("Levelings");
         gd.addMessage("This plug-in calculates levelings");


 		int[] wList = WindowManager.getIDList();

 		if (wList == null || wList.length < 2) {
 			IJ.showMessage("watershed by marked", "There must be at least two windows open");
 			return false;
 		}  
 		
 		String[] titles = new String[wList.length];
 		for (int i = 0, k = 0; i < wList.length; i++) {
 			ImagePlus imp = WindowManager.getImage(wList[i]);
 			if (null != imp)
 				titles[k++] = imp.getTitle();
 		}

 		gd.addChoice("input image:", titles, titles[0]);
 		gd.addChoice("marked (color) image :", titles, titles[1]);
        gd.addCheckbox("You want to apply the watershed in the input image gradient?", true);
		
		
		
		
 		gd.showDialog();
         if (gd.wasCanceled())
             return false;


 		int i1Index = gd.getNextChoiceIndex();
 		int i2Index = gd.getNextChoiceIndex();
 		gradient = gd.getNextBoolean();
 		
 		ImagePlus imp1 = WindowManager.getImage(wList[i1Index]);
 		ImagePlus imp2 = WindowManager.getImage(wList[i2Index]);

 		if (imp1.getBitDepth()!=8) {
 			IJ.showMessage("Error", "Only 8-bit images are supported");
 			return false;
 		}

 		imgF = (ByteProcessor) imp1.getProcessor();
 		imgG = (ColorProcessor) imp2.getProcessor();
 		this.plus = imp1;
 		return true;
 }

	public int setup(String arg, ImagePlus imp) {
		 if (imp!=null && !showDialog()) return DONE;
		 return DOES_ALL;
	}

}

