package mmlib4j;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import javax.swing.JFrame;

import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.imagej.utils.ImageUtils;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.segmentation.Labeling;
import mmlib4j.utils.AdjacencyRelation;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Labelings_ extends JFrame implements PlugInFilter {
	
	double raio;
	ImagePlus imgPlus;
	
	public void run(ImageProcessor ip) { 
		ImageUtils.initMMorph4J();
		
		GrayScaleImage img = Labeling.labeling(ImageJAdapter.toGrayScaleImage((ByteProcessor) ip), AdjacencyRelation.getCircular(raio));
		ColorImage imgOut = img.randomColor();
		ImagePlus plus = new ImagePlus("Labeling - number of flat-zone:" + img.maxValue() , ImageJAdapter.toColorProcessor(imgOut));
		plus.show();
		
	}
	
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		GenericDialog tela = new GenericDialog("Labeling");
		tela.addNumericField("Radius", 1.5, 1);
		tela.showDialog();
		if(tela.wasCanceled()){
			return PlugInFilter.DONE;
		}
		imgPlus = imp;
		raio = tela.getNextNumber();
		return PlugInFilter.DOES_8G;
	}
	
	public static void main(String args[]){
		ImagePlus plus = ImageUtils.openGrayScale();
		
		Labelings_ plugin = new Labelings_();
		plugin.setup(null, plus);
		plugin.run(plus.getProcessor());
	}
}
