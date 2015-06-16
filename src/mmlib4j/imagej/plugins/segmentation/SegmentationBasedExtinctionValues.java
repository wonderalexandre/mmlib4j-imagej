package mmlib4j.imagej.plugins.segmentation;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.IMorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueComponentTree;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueTreeOfShapes;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValue;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.segmentation.Labeling;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class SegmentationBasedExtinctionValues  extends JFrame implements ActionListener, ChangeListener, MouseMotionListener, ItemListener, MouseListener {

		
		private BufferedImage imgCurrent2;
		private GrayScaleImage imgInput;
		private GrayScaleImage imgCurrent;
		
		private boolean imagej = false;
		
		private JPanel drawPanel = new JPanel();
		private JPanel controlPanel= new JPanel();
		private JPanel appPanelEV; 
		
		private IMorphologicalTreeFiltering tree;
		private ComputerExtinctionValue extinctionValue;
		private AdjacencyRelation adj4 = AdjacencyRelation.getCircular(1);
		private AdjacencyRelation adj8 = AdjacencyRelation.getCircular(1.5);
		private boolean flagLabel = false;
		
		
		
		public SegmentationBasedExtinctionValues(GrayScaleImage imgInput) {
			super("Morphological operators by trees");
			
			
			this.imgInput = imgInput;
			tree = new ConnectedFilteringByComponentTree(imgInput, adj8, true);
			
			//montando o painel da aplicacao
			controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));  
			controlPanel.setBackground(Color.WHITE);
			createPanelEV(true);
	        
	        //montando o painel da imagem
			drawPanel.setBackground(Color.WHITE);
			drawPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			drawPanel.setSize(imgInput.getWidth(), imgInput.getHeight());
			
			drawPanel.addMouseMotionListener(this);
			drawPanel.addMouseListener(this);
			super.addMouseListener(this);
			super.addMouseMotionListener(this);
			
			JScrollPane scrollPane = new JScrollPane();
	        scrollPane.getViewport().add( drawPanel );
	        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	        super.setLayout(new BorderLayout());
			super.add(controlPanel, BorderLayout.WEST);
			super.add(scrollPane,  BorderLayout.CENTER);
			super.setSize(imgInput.getWidth() + 240, imgInput.getHeight() > 670? imgInput.getHeight(): 670);
			super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			super.setVisible(true);	
			super.setResizable(false);
	        this.updateImage(imgInput);
	        
		}
		
		public void setImageJ(boolean b){
			imagej = b;
			super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}
		
		public JButton getButtonSaveEv(){
			return saveButtonEV;
		}
		
		public BufferedImage getImageCurrent(){
			return imgCurrent2;
		}
		
		public void updateImage(GrayScaleImage img){
			this.imgCurrent2 = ImageBuilder.convertToImage(img);
			this.imgCurrent= img;
			if(flagLabel){
				updateImage(Labeling.labeling(imgCurrent, adj8).randomColor());
				
			}else{
				Graphics2D g = (Graphics2D) drawPanel.getGraphics();
				g.drawImage(imgCurrent2, 0, 0, null);
				
			}
		}
		
		public void updateImage(ColorImage img){
			Graphics2D g = (Graphics2D) drawPanel.getGraphics();
			imgCurrent2 = ImageBuilder.convertToImage(img);
			g.drawImage(imgCurrent2, 0, 0, null);	
		}
		
		@Override
		public void itemStateChanged(ItemEvent arg0) {
			if(chkSegEV == arg0.getSource()){
				if(chkSegEV.getState()){
					if(comboChoiceEV.getSelectedItem().equals("k-max"))
						applySegEV(true);
					else 
						applySegEV(false);
				}else{
					if(comboChoiceEV.getSelectedItem().equals("k-max"))
						applyEV(true);
					else 
						applyEV(false);
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent event) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			if(imgCurrent != null && imgCurrent.isPixelValid(x, y))
				pixelValueLabelEV.setText("pixel: ("+ x + ", " + y +") = " + imgCurrent.getPixel(x, y));
			else
				pixelValueLabelEV.setText("pixel: ");
		}


		

		@Override
		public void mouseClicked(MouseEvent e) {
					
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
		}

		
		
		
		
	/**
	 ********************************************************************************	
	 ********************************EXTINCTION VALUE***************************************
	 ********************************************************************************
	 */
		private JComboBox comboAttributoEV;
		private JComboBox comboTreeEV;
		private JComboBox comboChoiceEV;
		private JButton reloadButtonEV;
		private JButton applyButtonEV;
		private JButton labelButtonEV;
		private JButton saveButtonEV;
		private JSlider attributeValueEV;
		private JSlider attributeValueEV2;
		private JLabel limiarLabelEV2;
		private JLabel limiarLabelEV;
		private JLabel pixelValueLabelEV;
		private Checkbox chkSegEV;
		public void createPanelEV(boolean visib){
			appPanelEV = new JPanel(new GridLayout(7, 1, 0, 0));
			
			//1
			comboTreeEV = new JComboBox();
			comboTreeEV.setBorder(BorderFactory.createTitledBorder("Tree"));
			comboTreeEV.addItem("Max-tree");
			comboTreeEV.addItem("Min-tree");
			comboTreeEV.addItem("Tree of shapes");
			comboTreeEV.addActionListener(this);
			
			//2
			comboAttributoEV = new JComboBox();
			comboAttributoEV.setBorder(BorderFactory.createTitledBorder("Attribute type"));
			comboAttributoEV.addItem("---Increasing---");
			comboAttributoEV.addItem("Area");
			comboAttributoEV.addItem("Volume");
			comboAttributoEV.addItem("Altitude");
			comboAttributoEV.addItem("Height");
			comboAttributoEV.addItem("Width");
			comboAttributoEV.addItem("---Not increasing---");
			comboAttributoEV.addItem("Perimeter");
			comboAttributoEV.addItem("Circularity");
			comboAttributoEV.addActionListener(this);
			comboAttributoEV.setSelectedIndex(1);

			//3
			comboChoiceEV = new JComboBox();
			comboChoiceEV.setBorder(BorderFactory.createTitledBorder("Choice strategies"));
			comboChoiceEV.addItem("Atrribute value");
			comboChoiceEV.addItem("k-max");
			comboChoiceEV.addActionListener(this);
			
			chkSegEV = new Checkbox("Sementation");
			chkSegEV.addItemListener(this);
			
			//4
			attributeValueEV = new JSlider(JSlider.HORIZONTAL, 0,	100, 0);
			attributeValueEV.setValue(0);
			attributeValueEV.setBorder(BorderFactory.createTitledBorder("Attribute value"));
			attributeValueEV.setMajorTickSpacing(25);
			attributeValueEV.setMinorTickSpacing(1);
			attributeValueEV.setPaintTicks(true);
			attributeValueEV.addChangeListener(this);
			
			attributeValueEV2 = new JSlider(JSlider.HORIZONTAL, 0,	100, 0);
			attributeValueEV2.setValue(0);
			attributeValueEV2.setBorder(BorderFactory.createTitledBorder("Attribute value 2"));
			attributeValueEV2.setMajorTickSpacing(25);
			attributeValueEV2.setMinorTickSpacing(1);
			attributeValueEV2.setPaintTicks(true);
			attributeValueEV2.addChangeListener(this);
			
			
			//5 e 6
			limiarLabelEV = new JLabel("value: (to) :");
			limiarLabelEV2 = new JLabel("value: (from) :");
			pixelValueLabelEV = new JLabel("pixel:");
			JPanel panelLabels = new JPanel(new GridLayout(4, 1, 0, 0));
			panelLabels.add(limiarLabelEV);
			panelLabels.add(limiarLabelEV2);
			panelLabels.add(pixelValueLabelEV);
			panelLabels.add(chkSegEV);
			
			appPanelEV.add(comboTreeEV);
			appPanelEV.add(comboAttributoEV);
			appPanelEV.add(comboChoiceEV);
			appPanelEV.add(attributeValueEV);
			appPanelEV.add(attributeValueEV2);
			appPanelEV.add(panelLabels);
			
			//5
			reloadButtonEV = new JButton("Reload");
			applyButtonEV = new JButton("Apply");
			labelButtonEV = new JButton("Labeling");
			saveButtonEV = new JButton("Save");
			reloadButtonEV.addActionListener(this);
			applyButtonEV.addActionListener(this);
			labelButtonEV.addActionListener(this);
			saveButtonEV.addActionListener(this);
			
			JPanel panelButtons = new JPanel(new GridLayout(2, 1, 0, 0));
			panelButtons.add(reloadButtonEV);
			panelButtons.add(applyButtonEV);
			panelButtons.add(labelButtonEV);
			panelButtons.add(saveButtonEV);
			

			appPanelEV.add(panelButtons);
			controlPanel.add(appPanelEV);
			appPanelEV.setVisible(visib);
			changeTreeEV();
		}

		
		public void initEV(){
			//tree = new ComponentTree(imgInput, adj4, true);
			comboChoiceEV.setSelectedIndex(0);
			comboAttributoEV.setSelectedIndex(1);
			comboTreeEV.setSelectedIndex(0);
			attributeValueEV.setValue(0);
			attributeValueEV2.setValue(0);
			imgCurrent = imgInput.duplicate();
			imgCurrent2 = ImageBuilder.convertToImage(imgInput);
			//drawPanel.getGraphics().drawImage(imgCurrent2, 0, 0, null); 
			changeTreeEV();
		}
		
		public void applyEV(boolean isKmax){
			int attributeValue = 0;
			int attributeValueIni = 0;
			if(isKmax){
				if(comboAttributoEV.getSelectedItem().equals("Area")){ //area
					attributeValue = attributeValueEV.getValue();
					updateImage(extinctionValue.extinctionByKmax(attributeValue, Attribute.AREA));
				}
				else if(comboAttributoEV.getSelectedItem().equals("Volume")){ //volume
					attributeValue = attributeValueEV.getValue();
					updateImage(extinctionValue.extinctionByKmax(attributeValue, Attribute.VOLUME));
				}
				else if(comboAttributoEV.getSelectedItem().equals("Height")){ //Height
					attributeValue = attributeValueEV.getValue();
					updateImage(extinctionValue.extinctionByKmax(attributeValue, Attribute.HEIGHT));
				}
				else if(comboAttributoEV.getSelectedItem().equals("Width")){ //Width
					attributeValue = attributeValueEV.getValue();
					updateImage(extinctionValue.extinctionByKmax(attributeValue, Attribute.WIDTH));
				}
				else if(comboAttributoEV.getSelectedItem().equals("Altitude")){ //Width
					attributeValue = attributeValueEV.getValue();
					updateImage(extinctionValue.extinctionByKmax(attributeValue, Attribute.ALTITUDE));
				}
			}else{
				if(comboAttributoEV.getSelectedItem().equals("Area")){ //area
					attributeValue = (int) ((attributeValueEV.getValue() / 100.0) * imgInput.getSize());
					attributeValue = (int) Math.floor(Math.pow(imgInput.getSize(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
					attributeValueIni = (int) ((attributeValueEV2.getValue() / 100.0) * imgInput.getSize()/5);
					attributeValueIni = (int) Math.floor(Math.pow(imgInput.getSize()/5, 1-2) * Math.pow(attributeValueIni, 2)); //funcao potencia
					updateImage(extinctionValue.extinctionByAttribute(attributeValueIni, attributeValue, Attribute.AREA));
				}
				else if(comboAttributoEV.getSelectedItem().equals("Volume")){ //volume
					attributeValue = (int) ((attributeValueEV.getValue() / 100.0) * imgInput.getSize());
					attributeValue = (int) Math.floor(Math.pow(imgInput.getSize(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
					attributeValueIni = (int) ((attributeValueEV2.getValue() / 100.0) * imgInput.getSize());
					attributeValueIni = (int) Math.floor(Math.pow(imgInput.getSize(), 1-2) * Math.pow(attributeValueIni, 2)); //funcao potencia
					updateImage(extinctionValue.extinctionByAttribute(attributeValueIni, attributeValue, Attribute.VOLUME));
				}
				else if(comboAttributoEV.getSelectedItem().equals("Height")){ //Height
					attributeValue = (int) ((attributeValueEV.getValue() / 100.0) * imgInput.getHeight());
					attributeValue = (int) Math.floor(Math.pow(imgInput.getHeight(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
					attributeValueIni = (int) ((attributeValueEV2.getValue() / 100.0) * imgInput.getHeight());
					attributeValueIni = (int) Math.floor(Math.pow(imgInput.getHeight(), 1-2) * Math.pow(attributeValueIni, 2)); //funcao potencia
					updateImage(extinctionValue.extinctionByAttribute(attributeValueIni, attributeValue, Attribute.HEIGHT));
				}
				else if(comboAttributoEV.getSelectedItem().equals("Width")){ //Width
					attributeValue = (int) ((attributeValueEV.getValue() / 100.0) * imgInput.getWidth());
					attributeValue = (int) Math.floor(Math.pow(imgInput.getWidth(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
					attributeValueIni = (int) ((attributeValueEV2.getValue() / 100.0) * imgInput.getWidth());
					attributeValueIni = (int) Math.floor(Math.pow(imgInput.getWidth(), 1-2) * Math.pow(attributeValueIni, 2)); //funcao potencia
					updateImage(extinctionValue.extinctionByAttribute(attributeValueIni, attributeValue, Attribute.WIDTH));
				}
				else if(comboAttributoEV.getSelectedItem().equals("Altitude")){ //Width
					attributeValue = (int) ((attributeValueEV.getValue() / 100.0) * 200);
					attributeValue = (int) Math.floor(Math.pow(200, 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
					attributeValueIni = (int) ((attributeValueEV2.getValue() / 100.0) * 200);
					attributeValueIni = (int) Math.floor(Math.pow(200, 1-2) * Math.pow(attributeValueIni, 2)); //funcao potencia
					updateImage(extinctionValue.extinctionByAttribute(attributeValueIni, attributeValue, Attribute.ALTITUDE));
				}	
			}
			limiarLabelEV.setText("value: (to) :" + attributeValue);
			limiarLabelEV2.setText("value (from): " + attributeValueIni);
			
		}
		
		public void applySegEV(boolean isKmax){
			int attributeValue = 0;
			int attributeValueIni = 0;
			if(isKmax){
				if(comboAttributoEV.getSelectedItem().equals("Area")){ //area
					attributeValue = attributeValueEV.getValue();
					updateImage(extinctionValue.segmentationByKmax(attributeValue, Attribute.AREA).randomColor());
				}
				else if(comboAttributoEV.getSelectedItem().equals("Volume")){ //volume
					attributeValue = attributeValueEV.getValue();
					updateImage(extinctionValue.segmentationByKmax(attributeValue, Attribute.VOLUME).randomColor());
				}
				else if(comboAttributoEV.getSelectedItem().equals("Height")){ //Height
					attributeValue = attributeValueEV.getValue();
					updateImage(extinctionValue.segmentationByKmax(attributeValue, Attribute.HEIGHT).randomColor());
				}
				else if(comboAttributoEV.getSelectedItem().equals("Width")){ //Width
					attributeValue = attributeValueEV.getValue();
					updateImage(extinctionValue.segmentationByKmax(attributeValue, Attribute.WIDTH).randomColor());
				}
				else if(comboAttributoEV.getSelectedItem().equals("Altitude")){ //Width
					attributeValue = attributeValueEV.getValue();
					updateImage(extinctionValue.segmentationByKmax(attributeValue, Attribute.ALTITUDE).randomColor());
				}
			}else{
				if(comboAttributoEV.getSelectedItem().equals("Area")){ //area
					attributeValue = (int) ((attributeValueEV.getValue() / 100.0) * imgInput.getSize());
					attributeValue = (int) Math.floor(Math.pow(imgInput.getSize(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
					attributeValueIni = (int) ((attributeValueEV2.getValue() / 100.0) * imgInput.getSize()/5);
					attributeValueIni = (int) Math.floor(Math.pow(imgInput.getSize()/5, 1-2) * Math.pow(attributeValueIni, 2)); //funcao potencia
					
					updateImage(extinctionValue.segmentationByAttribute(attributeValueIni, attributeValue, Attribute.AREA).randomColor());
				}
				else if(comboAttributoEV.getSelectedItem().equals("Volume")){ //volume
					attributeValue = (int) ((attributeValueEV.getValue() / 100.0) * imgInput.getSize());
					attributeValue = (int) Math.floor(Math.pow(imgInput.getSize(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
					attributeValueIni = (int) ((attributeValueEV2.getValue() / 100.0) * imgInput.getSize());
					attributeValueIni = (int) Math.floor(Math.pow(imgInput.getSize(), 1-2) * Math.pow(attributeValueIni, 2)); //funcao potencia
					updateImage(extinctionValue.segmentationByAttribute(attributeValueIni, attributeValue, Attribute.VOLUME).randomColor());
				}
				else if(comboAttributoEV.getSelectedItem().equals("Height")){ //Height
					attributeValue = (int) ((attributeValueEV.getValue() / 100.0) * imgInput.getHeight());
					attributeValue = (int) Math.floor(Math.pow(imgInput.getHeight(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
					attributeValueIni = (int) ((attributeValueEV2.getValue() / 100.0) * imgInput.getHeight());
					attributeValueIni = (int) Math.floor(Math.pow(imgInput.getHeight(), 1-2) * Math.pow(attributeValueIni, 2)); //funcao potencia
					updateImage(extinctionValue.segmentationByAttribute(attributeValueIni, attributeValue, Attribute.HEIGHT).randomColor());
				}
				else if(comboAttributoEV.getSelectedItem().equals("Width")){ //Width
					attributeValue = (int) ((attributeValueEV.getValue() / 100.0) * imgInput.getWidth());
					attributeValue = (int) Math.floor(Math.pow(imgInput.getWidth(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
					attributeValueIni = (int) ((attributeValueEV2.getValue() / 100.0) * imgInput.getWidth());
					attributeValueIni = (int) Math.floor(Math.pow(imgInput.getWidth(), 1-2) * Math.pow(attributeValueIni, 2)); //funcao potencia
					updateImage(extinctionValue.segmentationByAttribute(attributeValueIni, attributeValue, Attribute.WIDTH).randomColor());
				}
				else if(comboAttributoEV.getSelectedItem().equals("Altitude")){ //Width
					attributeValue = (int) ((attributeValueEV.getValue() / 100.0) * 200);
					attributeValue = (int) Math.floor(Math.pow(200, 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
					attributeValueIni = (int) ((attributeValueEV2.getValue() / 100.0) * 200);
					attributeValueIni = (int) Math.floor(Math.pow(200, 1-2) * Math.pow(attributeValueIni, 2)); //funcao potencia
					updateImage(extinctionValue.segmentationByAttribute(attributeValueIni, attributeValue, Attribute.ALTITUDE).randomColor());
				}	
			}
			limiarLabelEV.setText("value: (to) :" + attributeValue);
			limiarLabelEV2.setText("value (from): " + attributeValueIni);
			
		}
		
		public void changeTreeEV(){
			if(comboTreeEV.getSelectedItem().equals("Max-tree"))
				extinctionValue = new ComputerExtinctionValueComponentTree(new ConnectedFilteringByComponentTree(imgInput, adj8, true));
			else if(comboTreeEV.getSelectedItem().equals("Min-tree"))
				extinctionValue = new ComputerExtinctionValueComponentTree(new ConnectedFilteringByComponentTree(imgInput, adj8, false));
			else if(comboTreeEV.getSelectedItem().equals("Tree of shapes"))
				extinctionValue = new ComputerExtinctionValueTreeOfShapes(new ConnectedFilteringByTreeOfShape(imgInput));
			
			attributeValueEV.setValue(0);
		}

		
		public static void main(String args[]) {
			
			
			GrayScaleImage img = ImageBuilder.openGrayImage(ImageBuilder.windowOpenFile());  
			SegmentationBasedExtinctionValues win = new SegmentationBasedExtinctionValues(img);
			win.setVisible(true);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void stateChanged(ChangeEvent event) {
			if(event.getSource() == attributeValueEV || event.getSource() == attributeValueEV2){
				if(chkSegEV.getState()){
					if(comboChoiceEV.getSelectedItem().equals("k-max"))
						applySegEV(true);
					else 
						applySegEV(false);
				}else{
					if(comboChoiceEV.getSelectedItem().equals("k-max"))
						applyEV(true);
					else 
						applyEV(false);
				}
			}
			else if(event.getSource() == attributeValueEV2){
				if(chkSegEV.getState())
					applySegEV(true);
				else	
					applyEV(false);
				
			}
			
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			if(event.getSource() == comboTreeEV){ //alterando a arvore da filtragem
				changeTreeEV();
			}
			else if(event.getSource() == applyButtonEV){
				this.imgInput = imgCurrent.duplicate();
				changeTreeEV();
				
			}
			else if(event.getSource() == reloadButtonEV){
				flagLabel = false;
				attributeValueEV.setValue(0);
				updateImage(this.imgInput);
			}
			else if(event.getSource() == labelButtonEV){
				flagLabel = !flagLabel;
				if(flagLabel)
					updateImage( Labeling.labeling(imgCurrent, adj8).randomColor() );
				else
					updateImage( imgCurrent );
			}
			
		}
		
	

}