package mmlib4j;


import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.imagej.utils.ImageUtils;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.segmentation.WatershedByIFT;


public class Watershed_by_HBasin implements PlugInFilter{
	int level;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		ImageUtils.initMMorph4J();
		GenericDialog tela = new GenericDialog("H-basin");
		tela.addNumericField("level", 10, 0);
		
		tela.showDialog();
		if(tela.wasCanceled()){
			return PlugInFilter.DONE;
		}
		
		level = (int) tela.getNextNumber();
		return PlugInFilter.DOES_8G;
	}

	@Override
	public void run(ImageProcessor ip) {
		GrayScaleImage img = ImageJAdapter.toGrayScaleImage((ByteProcessor)ip);
		
		GrayScaleImage imgWS = WatershedByIFT.watershedByHBasin(img, level);
		
		ColorProcessor out = ImageJAdapter.toColorProcessor(imgWS.randomColor());
		ImagePlus plus = new ImagePlus("Watershed by h-basin", out);
		plus.show();  
	}
}
