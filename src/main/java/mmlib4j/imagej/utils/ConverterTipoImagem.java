package mmlib4j.imagej.utils;


import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.io.File;

import mmlib4j.images.ColorImage;
import mmlib4j.utils.ImageBuilder;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class ConverterTipoImagem {

	public static void main(String[] args) {
		
		File dir[] = ImageBuilder.windowOpenDir();
		
		for(File f: dir){
			
			ImagePlus plus = IJ.openImage(f.getAbsolutePath());
			ImageProcessor ip = plus.getProcessor();
			
			
			if(ip instanceof ByteProcessor){
				String path = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-4) + ".png";
				ImageBuilder.saveImage(ImageJAdapter.toGrayScaleImage((ByteProcessor) ip), new File(path));
				System.out.println(path);	
			}
			if(ip instanceof ColorProcessor){
				ColorImage cor = ImageJAdapter.toColorImage((ColorProcessor) ip);
				String path = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-4) + ".png";
				ImageBuilder.saveImage(cor, new File(path));
				System.out.println(path);	
			}
			

			
		}

	}

}
