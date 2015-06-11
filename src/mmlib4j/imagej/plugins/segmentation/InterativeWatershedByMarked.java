package mmlib4j.imagej.plugins.segmentation;


import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.plugin.frame.PlugInFrame;
import ij.process.ByteProcessor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mmlib4j.filtering.MorphologicalOperators;
import mmlib4j.gui.ComboBoxColor;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.segmentation.Labeling;
import mmlib4j.segmentation.RegionalMinimaByIFT;
import mmlib4j.segmentation.WatershedByIFT;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.ImageUtils;

public class InterativeWatershedByMarked extends PlugInFrame implements MouseListener, ActionListener, MouseMotionListener, WindowListener, ChangeListener {
	private final int PEN_OP = 1;

	/* Current mouse coordinates */
	private int mousex = 0;
	private int mousey = 0;

	/* Previous mouse coordinates */
	private int prevx = 0;
	private int prevy = 0;

	/* Initial state falgs for operation */
	private boolean initialPen = true;
	private int flagView = 1;
	
	private int udefAlphaValue = 25;
	
	/* Primitive status & color variables */
	private int opStatus = PEN_OP;
	private Color userDefinedColor = new Color(0, 255, 20);

	/* User defined Color variables */
	private JSlider alphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, udefAlphaValue);
	Border borda = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK,3));
	
	private JButton applyButton = new JButton("Apply");
	private JButton reloadButton = new JButton("Reload");
	private JButton viewButton = new JButton("View");
	private JButton labelButton = new JButton("Labeling");
	private JButton filterMinimaButton = new JButton("Regional minima");
	
	ComboBoxColor comboColor = new ComboBoxColor();
	/* Sub panels of the main applet */
	private JPanel controlPanel = new JPanel(new GridLayout(2, 0, 0, 0));
	private JPanel udefcolPanel = new JPanel(new GridLayout(3, 0, 0, 0));
	private JPanel buttonPanel;
	
	private GrayScaleImage imgInput;
	private BufferedImage imgCurrent;
	private GrayScaleImage imgLabel;
	int imgMarcador[][];
	AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5f);
	
	private ImagePlus imgPlus;
	private MouseListener mouseListers[];
	private MouseMotionListener mouseMotionListers[];
	private Cursor hourglassCursor;
	
	public InterativeWatershedByMarked(ImagePlus imgPlus) {
		super("MMorph4J - Watershed");
		this.imgPlus = imgPlus;
		this.imgInput = ImageJAdapter.toGrayScaleImage( (ByteProcessor) imgPlus.getProcessor());
		
		this.imgCurrent = new BufferedImage(imgInput.getWidth(), imgInput.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		for(int x=0; x < imgInput.getWidth(); x++)
			for(int y=0; y < imgInput.getHeight(); y++)
				imgCurrent.setRGB(x, y, imgInput.getPixel(x, y));
		
		buttonPanel = new JPanel(new GridLayout(5, 0, 0, 0));
		
		controlPanel.add(udefcolPanel);
		controlPanel.add(buttonPanel);
		
		buttonPanel.add(reloadButton);
		buttonPanel.add(applyButton);
		buttonPanel.add(viewButton);
		buttonPanel.add(labelButton);
		buttonPanel.add(filterMinimaButton);
		
		
		reloadButton.addActionListener(this);
		viewButton.addActionListener(this);
		applyButton.addActionListener(this);
		filterMinimaButton.addActionListener(this);
		labelButton.addActionListener(this);
		
		alphaSlider.setBorder(BorderFactory.createTitledBorder("Alpha"));
		alphaSlider.setMajorTickSpacing(25);
		alphaSlider.setMinorTickSpacing(10);
		alphaSlider.setPaintTicks(true);
		alphaSlider.setPaintLabels(true);
		udefcolPanel.add(alphaSlider, BorderLayout.NORTH);
		
        udefcolPanel.add(comboColor);
        comboColor.addActionListener(this);
        controlPanel.setBackground(Color.WHITE);
		
        imgMarcador = new int[imgInput.getWidth()][imgInput.getHeight()];
		for(int x=0; x < imgInput.getWidth(); x++)
			for(int y=0; y < imgInput.getHeight(); y++)
				imgMarcador[x][y] = -1;
		
		
		super.add(controlPanel);
		
		alphaSlider.addChangeListener(this);
		hourglassCursor = imgPlus.getCanvas().getCursor();
		imgPlus.getCanvas().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		
		mouseListers = imgPlus.getCanvas().getMouseListeners();
		mouseMotionListers = imgPlus.getCanvas().getMouseMotionListeners();
		
		for(MouseListener ml: mouseListers)
			imgPlus.getCanvas().removeMouseListener(ml);
		for(MouseMotionListener mml: mouseMotionListers)
			imgPlus.getCanvas().removeMouseMotionListener(mml);
		
		imgPlus.getCanvas().addMouseMotionListener(this);
		imgPlus.getCanvas().addMouseListener(this);
		super.setSize(260, 450);
		//super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
		
		WindowManager.addWindow(this);
        GUI.center(this);
        super.addWindowListener(this);
        super.setVisible(true);
		
	}
	
	public void windowClosed(WindowEvent e) {
		imgPlus.setImage( imgCurrent );
		
		imgPlus.getCanvas().removeMouseListener(this);
		imgPlus.getCanvas().removeMouseMotionListener(this);
		imgPlus.getCanvas().setCursor(hourglassCursor);
		
		for(MouseListener ml: mouseListers)
			imgPlus.getCanvas().addMouseListener(ml);
		for(MouseMotionListener mml: mouseMotionListers)
			imgPlus.getCanvas().addMouseMotionListener(mml);
		
		this.setVisible(false);	
		this.setEnabled(false);
	}
	public void windowOpened(WindowEvent e) { }
	public void windowClosing(WindowEvent e) {	
        super.windowClosing(e);
	}
	public void windowIconified(WindowEvent e) { }
	public void windowDeiconified(WindowEvent e) { }
	public void windowActivated(WindowEvent e) { }
	public void windowDeactivated(WindowEvent e) { }
	
	
	
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == comboColor){
			updateRGBValues();
		}else if(e.getSource() == reloadButton){
			updateRGBValues();
			updateImage();
		}else if(e.getSource() == viewButton){
			viewMarcador();
		}
		else if(e.getSource() == applyButton){
			apply();
		}else if(e.getSource() == filterMinimaButton){
			filterMinima();
		}else if(e.getSource() == labelButton){
			labeling();
		}
	}
	
	public void labeling(){
		GrayScaleImage imgLabeling = Labeling.labeling(imgInput, adj);
		ColorImage imgM = imgLabeling.randomColor();
		for(int x=0; x < imgInput.getWidth(); x++){
			for(int y=0; y < imgInput.getHeight(); y++){
				setSuperPixel(x, y, imgM.getPixel(x, y), 1);
			
			}
		}
		flagView = 0;
		viewMarcador();
	}
	
	public void filterMinima(){
		GrayScaleImage imgM = RegionalMinimaByIFT.extractionOfRegionalMinima(imgInput); 
		for(int x=0; x < imgCurrent.getWidth(); x++){
			for(int y=0; y < imgCurrent.getHeight(); y++){
				if(imgM.getPixel(x, y) > 0){
					setSuperPixel(x, y, new Color(ImageUtils.randomInteger(0, 255), ImageUtils.randomInteger(0, 255), ImageUtils.randomInteger(0, 255) ).getRGB(), 0);
				}
			}
		}
		flagView = 0;
		viewMarcador();
	}
	
	public void apply(){
		GrayScaleImage imgGrad = MorphologicalOperators.gradient(imgInput, adj);
	
		HashMap<Integer, Integer> labels = new HashMap<Integer, Integer>();
		int label = 1;
		GrayScaleImage imgM = ImageFactory.createGrayScaleImage(32, imgInput.getWidth(), imgInput.getHeight());
		for(int x=0; x < imgInput.getWidth(); x++){
			for(int y=0; y < imgInput.getHeight(); y++){
				if(imgMarcador[x][y] != -1){
					if(labels.containsKey(imgMarcador[x][y])){
						imgM.setPixel(x, y, labels.get(imgMarcador[x][y]));
					}else{
						labels.put(imgMarcador[x][y], imgMarcador[x][y]);
						imgM.setPixel(x, y, labels.get(imgMarcador[x][y]));
					}
				}else{
					imgM.setPixel(x,  y, -1);
				}
			}
		}
		
		GrayScaleImage imgWS = WatershedByIFT.watershedByMarker(imgGrad, imgM);
		imgLabel = imgWS;
		
		updateWS();
	}
	
	public void setMarked(ColorImage img){
		for(int x=0; x < img.getWidth(); x++){
			for(int y=0; y < img.getHeight(); y++){
				if(img.getRed(x, y) == 0 && img.getBlue(x, y) == 0 && img.getGreen(x, y) == 0)
					imgMarcador[x][y] = -1;
				else
					imgMarcador[x][y] = img.getPixel(x, y);
			}
		}
	}
	
	public void updateWS(){
		double alpha = alphaSlider.getValue();
		alpha = 255 - (alphaSlider.getValue() / 100.0) * 255; 
		if(alpha > 255) alpha = 255;
		for(int x=0; x < imgInput.getWidth(); x++){
			for(int y=0; y < imgInput.getHeight(); y++){
				Color c = new Color(imgLabel.getPixel(x, y));
				imgCurrent.setRGB(x, y, new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) alpha).getRGB());
			}
		}
		Graphics2D g = (Graphics2D) imgPlus.getCanvas().getGraphics();
		g.drawImage(ImageBuilder.convertToImage(imgInput), 0, 0, null);
		g.drawImage(imgCurrent, 0, 0, null);
		
	}
	
	public void viewMarcador(){
		if(flagView == 1){
			for(int x=0; x < imgCurrent.getWidth(); x++){
				for(int y=0; y < imgCurrent.getHeight(); y++){
					if(imgMarcador[x][y] == -1)
						imgCurrent.setRGB(x, y, Color.BLACK.getRGB());
					else
						imgCurrent.setRGB(x, y, imgMarcador[x][y]);
				}
			}
			
		}else if(flagView == 0){
			for(int x=0; x < imgCurrent.getWidth(); x++){
				for(int y=0; y < imgCurrent.getHeight(); y++){
					if(imgMarcador[x][y] != -1){
						imgCurrent.setRGB(x, y, imgMarcador[x][y]);
					}else{
						imgCurrent.setRGB(x, y, imgCurrent.getRGB(x, y));
					}
				}
			}
			
		}
		else{
			GrayScaleImage imgGrad = MorphologicalOperators.gradient(imgInput, adj);
			for(int x=0; x < imgCurrent.getWidth(); x++){
				for(int y=0; y < imgCurrent.getHeight(); y++){
					imgCurrent.setRGB(x, y, new Color(imgGrad.getPixel(x, y), imgGrad.getPixel(x, y), imgGrad.getPixel(x, y)).getRGB() );
				}
			}
		}
		
		
		//imgPlus.getCanvas().getGraphics().drawImage(imgCurrent, 0, 0, null);
		imgPlus.setImage( imgCurrent );
		flagView += 1;
		if(flagView > 2)
			flagView = 0;
	}
	
	public void updateImage(){
		imgMarcador = new int[imgInput.getWidth()][imgInput.getHeight()];
		for(int x=0; x < imgInput.getWidth(); x++)
			for(int y=0; y < imgInput.getHeight(); y++)
				imgMarcador[x][y] = -1;
		
		//this.imgCurrent = ImageBuilder.convertToImage(imgInput);
		
		//imgPlus.setProcessor( ImageJAdapter.toByteProcessor(imgInput) );
		//imgPlus.getCanvas().getGraphics().drawImage( ImageBuilder.convertToImage(imgInput), 0, 0, null);
		imgPlus.setImage( ImageBuilder.convertToImage(imgInput) );
	}


	public void penOperation(MouseEvent e) {
		Graphics2D g = (Graphics2D) imgPlus.getCanvas().getGraphics();
		g.setColor(userDefinedColor);
		g.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		
		if (initialPen) {
			setGraphicalDefaults(e);
			initialPen = false;
			
			drawLine(prevx, prevy, mousex, mousey, userDefinedColor.getRGB());
			g.drawLine(prevx, prevy, mousex, mousey);
			
		}

		if (mousex != e.getX() || mousey != e.getY()) {
			mousex = e.getX();
			mousey = e.getY();
			
			drawLine(prevx, prevy, mousex, mousey, userDefinedColor.getRGB());
			g.drawLine(prevx, prevy, mousex, mousey);
			
			prevx = mousex;
			prevy = mousey;
		}
	}

	public void setGraphicalDefaults(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
		prevx = e.getX();
		prevy = e.getY();
	}

	public void mouseDragged(MouseEvent e) {
		switch (opStatus) {
		case PEN_OP:
				penOperation(e);
				break;

		}
	}
	
	

	public void mouseReleased(MouseEvent e) {
		switch (opStatus) {
		case PEN_OP:
			releasedPen();
			break;

		}
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}

	
	
	public void releasedPen() {
		initialPen = true;
	}

	public void stateChanged(ChangeEvent e) {
		updateRGBValues();		
	}


	public void updateRGBValues() {
		userDefinedColor = comboColor.getColor();
		if(udefAlphaValue != alphaSlider.getValue()){
			udefAlphaValue = alphaSlider.getValue();
			if(imgLabel != null)
				updateWS();
		}
	}


	
	public void setSuperPixel(int x, int y, int cor, int espessura){
		if(espessura == 1){
			if(x-2 >= 0 && x-2 < imgInput.getWidth() && y >= 0 && y < imgInput.getHeight() )
				imgMarcador[x-2][y] = cor;
			if(x-2 >= 0 && x-2 < imgInput.getWidth() && y-1 >= 0 && y-1 < imgInput.getHeight() )
				imgMarcador[x-2][y-1] = cor;
			if(x-2 >= 0 && x-2 < imgInput.getWidth() && y+1 >= 0 && y+1 < imgInput.getHeight() )
				imgMarcador[x-2][y+1] = cor;
			
			if(x-1 >= 0 && x-1 < imgInput.getWidth() && y-2 >= 0 && y-2 < imgInput.getHeight() )
				imgMarcador[x-1][y-2] = cor;
			if(x-1 >= 0 && x-1 < imgInput.getWidth() && y+2 >= 0 && y+2 < imgInput.getHeight() )
				imgMarcador[x-1][y+2] = cor;
			
			
			if(x >= 0 && x < imgInput.getWidth() && y-2 >= 0 && y-2 < imgInput.getHeight() )
				imgMarcador[x][y-2] = cor;
			if(x >= 0 && x < imgInput.getWidth() && y+2 >= 0 && y+2 < imgInput.getHeight() )
				imgMarcador[x][y+2] = cor;
			
			if(x+1 >= 0 && x+1 < imgInput.getWidth() && y-2 >= 0 && y-2 < imgInput.getHeight() )
				imgMarcador[x+1][y-2] = cor;
			if(x+1 >= 0 && x+1 < imgInput.getWidth() && y+2 >= 0 && y+2 < imgInput.getHeight() )
				imgMarcador[x+1][y+2] = cor;
			
	
			if(x+2 >= 0 && x+2 < imgInput.getWidth() && y >= 0 && y < imgInput.getHeight() )
				imgMarcador[x+2][y] = cor;
			if(x+2 >= 0 && x+2 < imgInput.getWidth() && y-1 >= 0 && y-1 < imgInput.getHeight() )
				imgMarcador[x+2][y-1] = cor;
			if(x+2 >= 0 && x+2 < imgInput.getWidth() && y+1 >= 0 && y+1 < imgInput.getHeight() )
				imgMarcador[x+2][y+1] = cor;
		
		}
		
		if(x-1 >= 0 && x-1 < imgInput.getWidth() && y >= 0 && y < imgInput.getHeight() )
			imgMarcador[x-1][y] = cor;
		if(x-1 >= 0 && x-1 < imgInput.getWidth() && y-1 >= 0 && y-1 < imgInput.getHeight() )
			imgMarcador[x-1][y-1] = cor;
		if(x-1 >= 0 && x-1 < imgInput.getWidth() && y+1 >= 0 && y+1 < imgInput.getHeight() )
			imgMarcador[x-1][y+1] = cor;
		
		if(x >= 0 && x < imgInput.getWidth() && y >= 0 && y < imgInput.getHeight() )
			imgMarcador[x][y] = cor;
		if(x >= 0 && x < imgInput.getWidth() && y-1 >= 0 && y-1 < imgInput.getHeight() )
			imgMarcador[x][y-1] = cor;
		if(x >= 0 && x < imgInput.getWidth() && y+1 >= 0 && y+1 < imgInput.getHeight() )
			imgMarcador[x][y+1] = cor;
		
		if(x+1 >= 0 && x+1 < imgInput.getWidth() && y >= 0 && y < imgInput.getHeight() )
			imgMarcador[x+1][y] = cor;
		if(x+1 >= 0 && x+1 < imgInput.getWidth() && y-1 >= 0 && y-1 < imgInput.getHeight() )
			imgMarcador[x+1][y-1] = cor;
		if(x+1 >= 0 && x+1 < imgInput.getWidth() && y+1 >= 0 && y+1 < imgInput.getHeight() )
			imgMarcador[x+1][y+1] = cor;
		
		
	}
	
	public  void drawLine(int x1, int y1, int x2, int y2, int cor){
		//algoritmo de bresenham
		if(Math.abs( x2 - x1 ) > Math.abs( y2 - y1 )){
			if(x1 > x2) drawLine(x2, y2, x1, y1, cor);
			int a = x2 - x1;
			int b = y2 -y1;
			
			int inc = 1;
			if(b<0){
				inc = -1;
				b = -b;
			}
			int v = 2 * a + b;
			int neg = 2 * b;
			int pos = 2 * (b - a);
			int x = x1;
			int y = y1;
			setSuperPixel(x, y, cor, 1);	
			while (x<= x2){
				 
				setSuperPixel(x, y, cor, 1);	
				x= x + 1;
				if(v <= 0){
					v = v + neg;
				}else{
					y = y + inc;
					v = v+ pos;
				}
			}
		}else{
			if(y1 > y2) drawLine(x2, y2, x1, y1, cor);
			int b = x2 - x1;
			int a = y2 - y1;
			int inc = 1;
			if( b < 0){
				inc = -1;
				b = -b;
			}
			int v = 2 * b - a;
			int neg = 2 * b;
			int pos = 2 * (b - a);
			int x = x1;
			int y = y1;
			setSuperPixel(x, y, cor, 1);	
			while(y <= y2){
				
				setSuperPixel(x, y, cor, 1);
				y = y + 1;
				if(v <= 0){
					v = v + neg;
				}else{
					x = x + inc;
					v = v + pos;
				}
			}
		}
	}

	
}


