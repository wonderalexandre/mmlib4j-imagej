package mmlib4j;


import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mmlib4j.imagej.utils.ImageUtils;


public class Replace_level implements PlugInFilter{
	int levelOld;
	int levelNew;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		ImageUtils.initMMorph4J();
		GenericDialog tela = new GenericDialog("Replace: level1 <=> level2");
		tela.addNumericField("level (old)", 0, 0);
		tela.addNumericField("level (new)", 255, 0);
		
		tela.showDialog();
		if(tela.wasCanceled()){
			return PlugInFilter.DONE;
		}
		
		levelOld = (int) tela.getNextNumber();
		levelNew = (int) tela.getNextNumber();
		return PlugInFilter.DOES_8G;
	}

	@Override
	public void run(ImageProcessor img) {
		for(int y=0; y < img.getHeight(); y++){
			for(int x=0; x < img.getWidth(); x++){
				if(img.getPixel(x, y) == levelOld)
					img.putPixel(x, y, levelNew);
			}
		}
	}
}
