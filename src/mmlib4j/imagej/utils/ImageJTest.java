package mmlib4j.imagej.utils;
import ij.ImageJ;


public class ImageJTest {

	public static void main(String[] args) {
		
		System.setProperty("plugins.dir", "./bin");
		System.setProperty("user.dir", "./bin");
		
		ImageUtils.initMMorph4J();
		
		ImageJ ij = new ImageJ(null, ImageJ.STANDALONE);
		
	}
}
 