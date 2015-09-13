package mmlib4j.imagej.utils;




import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.RealImage;
import mmlib4j.images.impl.BitImage;
import mmlib4j.images.impl.ImageFactory;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class ImageJAdapter {
	
	public static GrayScaleImage toGrayScaleImage(ByteProcessor ip){
		return ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_8BITS, ip.getPixels(), ip.getWidth(), ip.getHeight()); 
	}
	
	public static GrayScaleImage toGrayScaleImage(ShortProcessor ip){
		return ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_16BITS, ip.getPixels(), ip.getWidth(), ip.getHeight()); 
	}
	
	public static BinaryImage toBinaryImage(ByteProcessor ip){
		BinaryImage img = new BitImage(ip.getWidth(), ip.getHeight());
		for(int x=0; x < img.getWidth(); x++){
			for(int y=0; y < img.getHeight(); y++){
				img.setPixel(x, y, ip.getPixel(x, y)!=0);
			}
		}
		return img; 
	}
	
	public static ColorImage toColorImage(ColorProcessor ip){
		return ImageFactory.createColorImage((int[])ip.getPixels(), ip.getWidth(), ip.getHeight());
	}  
	
	
	public static ByteProcessor toByteProcessor(GrayScaleImage img){
		ByteProcessor ip = new ByteProcessor(img.getWidth(), img.getHeight(), (byte[]) img.getPixels(), null);
		return ip;
	}
	
	public static ByteProcessor toByteProcessor(BinaryImage img){
		ByteProcessor ip = new ByteProcessor(img.getWidth(), img.getHeight());
		for(int x=0; x < img.getWidth(); x++){
			for(int y=0; y < img.getHeight(); y++){
				ip.putPixel(x, y, img.getPixel(x, y)?255:0);
			}
		}
		return ip;
	}
	public static FloatProcessor toFloatProcessor(RealImage img){
		FloatProcessor ip = new FloatProcessor(img.getWidth(), img.getHeight(), (float[]) img.getPixels(), null);
		return ip;
	}
	
	
	public static ColorProcessor toColorProcessor(ColorImage img){
		ColorProcessor ip = new ColorProcessor(img.getWidth(), img.getHeight(), img.getPixels());
		return ip;
	}
	
	public static ShortProcessor toShortProcessor(GrayScaleImage img){
		ShortProcessor ip = new ShortProcessor(img.getWidth(), img.getHeight(), (short[]) img.getPixels(), null);
		return ip;
	}
	
}
