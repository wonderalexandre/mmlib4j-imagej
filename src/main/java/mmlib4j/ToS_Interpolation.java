package mmlib4j;



import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.imagej.utils.ImageUtils;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.tos.BuilderTreeOfShapeByUnionFindParallel;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class ToS_Interpolation  implements PlugInFilter {
	
	public void run(ImageProcessor ip) { 
		ImageUtils.initMMorph4J();

		if(ip instanceof ByteProcessor){
			GrayScaleImage img = ImageJAdapter.toGrayScaleImage( (ByteProcessor) ip);
			
			Object obj[] = BuilderTreeOfShapeByUnionFindParallel.getImageInterpolate(img);
			short interpolation0[] = (short[]) obj[0];
			short interpolation1[] = (short[]) obj[1];
			
			int interpWidth = (img.getWidth()*4-3);
	        int interpHeight = (img.getHeight()*4-3);
	        
	        ByteProcessor imgOut0 = new ByteProcessor(interpWidth, interpHeight);
	        ByteProcessor imgOut1 = new ByteProcessor(interpWidth, interpHeight);
	
			for(int i=0; i < interpolation0.length; i++){
				//imgOut.setPixel(i, (interpolation0[i] + interpolation1[i]) / 2);
				imgOut0.set(i,  (interpolation0[i] & 0xFF));
				imgOut1.set(i,  (interpolation1[i] & 0xFF));
			}
			
			ImagePlus imgPlus0 = new ImagePlus("interpolation - max", imgOut0);
			imgPlus0.show("image interpolation - max");
			ImagePlus imgPlus1 = new ImagePlus("interpolation - min", imgOut1);
			imgPlus1.show("image interpolation - min");
		}
		else if (ip instanceof ColorProcessor){
			ColorImage img = ImageJAdapter.toColorImage( (ColorProcessor) ip);
			
			int interpWidth = (img.getWidth()*4-3);
	        int interpHeight = (img.getHeight()*4-3);
	
			
	        GrayScaleImage imgR = img.getRed();
	        GrayScaleImage imgG = img.getGreen();
	        GrayScaleImage imgB = img.getBlue();
			
	        ColorProcessor imgOut0 = new ColorProcessor(interpWidth, interpHeight);
	        ColorProcessor imgOut1 = new ColorProcessor(interpWidth, interpHeight);
	
	        Object objR[] = BuilderTreeOfShapeByUnionFindParallel.getImageInterpolate(imgR);
			short interpolation0R[] = (short[]) objR[0];
			short interpolation1R[] = (short[]) objR[1];
			
			Object objG[] = BuilderTreeOfShapeByUnionFindParallel.getImageInterpolate(imgG);
			short interpolation0G[] = (short[]) objG[0];
			short interpolation1G[] = (short[]) objG[1];
			
			Object objB[] = BuilderTreeOfShapeByUnionFindParallel.getImageInterpolate(imgB);
			short interpolation0B[] = (short[]) objB[0];
			short interpolation1B[] = (short[]) objB[1];
			
	        for(int i=0; i < interpolation0R.length; i++){
	        	
	        	int valor0 = ((0 & 0xFF) << 24) |
	    					((interpolation0R[i] & 0xFF) << 16) |
	    					((interpolation0G[i] & 0xFF) << 8)  |
	    					((interpolation0B[i] & 0xFF) << 0);
	        	
	        	imgOut0.set(i, valor0);
	        	
	        	int valor1 = ((0 & 0xFF) << 24) |
    					((interpolation1R[i] & 0xFF) << 16) |
    					((interpolation1G[i] & 0xFF) << 8)  |
    					((interpolation1B[i] & 0xFF) << 0);
	        	imgOut1.set(i, valor1);
				
			}
			
			ImagePlus imgPlus0 = new ImagePlus("interpolation - max", imgOut0);
			imgPlus0.show("image interpolation - max");
			ImagePlus imgPlus1 = new ImagePlus("interpolation - min", imgOut1);
			imgPlus1.show("image interpolation - min");
		}
	}
	
	boolean showDialog() { 
		return true;
	}
	
	public int setup(String arg, ImagePlus imp) {

		if (imp!=null && !showDialog()) return DONE;
		return DOES_8G | DOES_RGB;
	}
	
	public static void main(String args[]){
		ImagePlus plus = ImageUtils.openGrayScale();
		ToS_Interpolation plugin = new ToS_Interpolation();
		plugin.setup(null, plus);
		plugin.run(plus.getProcessor());
		
		
	}
	
}
