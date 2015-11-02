package mmlib4j;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import mmlib4j.gui.WindowImages;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.imagej.utils.ImageUtils;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.Image2D;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.AdjacencyRelationByAmoebas;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Filter_Means_by_Amebas implements PlugInFilter {
	
	double raio;
	ImagePlus imgPlus;
	
	public int setup(String arg, ImagePlus imp) {
		GenericDialog tela = new GenericDialog("Means by amebas");
		tela.addNumericField("Radius", 1.5, 10);
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
		
		GrayScaleImage img = ImageJAdapter.toGrayScaleImage((ByteProcessor) ip);
		
		
		//filtro da media sem amebas
		GrayScaleImage imgMedia = ImageFactory.createGrayScaleImage(img);
		AdjacencyRelation adj = AdjacencyRelation.getCircular(raio);
		for(int pixelP=0; pixelP < img.getSize(); pixelP++){
			int cont = 0;
			int soma = 0; 
			for(int pixelQ: adj.getAdjacencyPixels(img, pixelP)){
				soma += img.getPixel(pixelQ);
				cont++;
			}
			imgMedia.setPixel(pixelP, soma / cont);
		}
		
		//filtro da media com amebas
		int threshold = 50;
		GrayScaleImage imgMediaAboema = ImageFactory.createGrayScaleImage(img);
		AdjacencyRelationByAmoebas adjAmoeba = AdjacencyRelationByAmoebas.getCircular(raio);
		for(int pixelP=0; pixelP < img.getSize(); pixelP++){
			int cont = 0;
			int soma = 0; 
			for(int pixelQ: adjAmoeba.getAdjacencyPixels(img, threshold, pixelP)){
				soma += img.getPixel(pixelQ);
				cont++;
			}
			imgMediaAboema.setPixel(pixelP, soma / cont);
		}
		
		WindowImages.show(new Image2D[]{imgMedia, imgMediaAboema});
		
	}
	
	public static void main(String args[]){
		ImagePlus plus = ImageUtils.openGrayScale();
		Filter_Means_by_Amebas plugin = new Filter_Means_by_Amebas();
		plugin.setup(null, plus);
		plugin.run(plus.getProcessor());
	}
}
