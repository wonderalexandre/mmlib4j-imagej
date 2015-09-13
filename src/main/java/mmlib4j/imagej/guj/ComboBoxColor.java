package mmlib4j.imagej.guj;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class ComboBoxColor extends JComboBox {

	private static final long serialVersionUID = 1L;
	ImageIcon[] images;
   public static Color[] colors = new Color[]{Color.GREEN, Color.ORANGE, Color.YELLOW, Color.PINK, Color.MAGENTA,  Color.RED, Color.BLUE, Color.LIGHT_GRAY};
   static Integer[] intArray = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7};
  
   public BufferedImage paintImage(int i){
   	Color c = colors[i];
   	BufferedImage img = new BufferedImage(140, 35, BufferedImage.TYPE_INT_RGB);
   	for(int x=0; x < img.getWidth(); x++){
   		for(int y=0; y < img.getHeight(); y++){
   			img.setRGB(x, y, c.getRGB());
   		}
   	}
   	return img;
   }
  
   public ComboBoxColor() {
   	 super(intArray);
   	 images = new ImageIcon[colors.length];
        for (int i = 0; i < colors.length; i++) {
   		 images[i] = new ImageIcon(paintImage(i));
   	 }
       ComboBoxRenderer renderer = new ComboBoxRenderer();
       renderer.setPreferredSize(new Dimension(140, 35));
       super.setRenderer(renderer);
       super.setMaximumRowCount(7);
   }
   
  
   public Color getColor(){
   	return colors[super.getSelectedIndex()];
   }
   
   public static Color getColor(int index){
	   return colors[index];
   }

   class ComboBoxRenderer extends JLabel implements ListCellRenderer {
      
   	
       public ComboBoxRenderer() {
           setOpaque(true);
           setHorizontalAlignment(CENTER);
           setVerticalAlignment(CENTER);
       }

       public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
           int selectedIndex = ((Integer)value).intValue();
           if (isSelected) {
               setBackground(list.getSelectionBackground());
               setForeground(list.getSelectionForeground());
           } else {
               setBackground(list.getBackground());
               setForeground(list.getForeground());
           }
           setIcon(images[selectedIndex]);
           return this;
       }

      
   }
}