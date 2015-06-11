package mmlib4j.imagej.utils;

import ij.IJ;
import ij.ImagePlus;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.Image2D;
import mmlib4j.images.RealImage;

public class ImageUtils {
	
	private static WindowImages win = null;
	
	public static ImagePlus openGrayScale(){
		ImagePlus plus = IJ.openImage();
		plus.setProcessor( plus.getProcessor().convertToByteProcessor() );
		plus.show();
		return plus;
	}
	static{
		initMMorph4J();
	}
	
	public static void initMMorph4J(){
		
		if(win == null){
			win = new WindowImages() {
				@Override
				public void showImpl(Image2D[] img, String[] titles) {
					ImagePlus plus[] = new ImagePlus[img.length];
					for(int i=0; i < img.length; i++){
						if(img[i] instanceof GrayScaleImage)
							plus[i] = new ImagePlus(titles[i], ImageJAdapter.toByteProcessor( (GrayScaleImage) img[i]));
						else if(img[i] instanceof ColorImage)
							plus[i] = new ImagePlus(titles[i], ImageJAdapter.toColorProcessor( (ColorImage) img[i]));
						else if(img[i] instanceof BinaryImage)
							plus[i] = new ImagePlus(titles[i], ImageJAdapter.toByteProcessor( (BinaryImage) img[i]));
						else if(img[i] instanceof RealImage)
							plus[i] = new ImagePlus(titles[i], ImageJAdapter.toFloatProcessor( (RealImage) img[i]));
						
						plus[i].show();
					}
				}
			};
			WindowImages.instance = win;
		}
		
	}
	
}
