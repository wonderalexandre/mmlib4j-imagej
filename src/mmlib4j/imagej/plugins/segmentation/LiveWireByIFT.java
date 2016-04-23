package mmlib4j.imagej.plugins.segmentation;


import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.plugin.frame.PlugInFrame;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;

import java.awt.BasicStroke;
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

import javax.swing.JButton;
import javax.swing.JPanel;

import mmlib4j.filtering.MorphologicalOperatorsBasedOnSE;
import mmlib4j.imagej.guj.ComboBoxColor;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.segmentation.LiveWireIFT;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.ImageUtils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class LiveWireByIFT extends PlugInFrame implements MouseListener, ActionListener, MouseMotionListener, WindowListener {

	/* Current mouse coordinates */
	private int mousex = 0;
	private int mousey = 0;
	private int prevx = 0;
	private int prevy = 0;

	/* Initial state falgs for operation */
	private boolean initialPen = true;
	
	private JButton applyButton = new JButton("Cut");
	private JButton reloadButton = new JButton("Reset");
	private JButton viewButton = new JButton("View");
	
	ComboBoxColor comboColor = new ComboBoxColor();

	private JPanel controlPanel = new JPanel(new GridLayout(2, 0, 0, 0));
	private JPanel udefcolPanel = new JPanel(new GridLayout(3, 0, 0, 0));
	private JPanel buttonPanel;
	
	
	private GrayScaleImage imgInput;
	private GrayScaleImage imgMarcador;
	private GrayScaleImage imgGradient;
	private GrayScaleImage imgMapaPredecessores;
	
	private BufferedImage imgOriginal;	
	private ImagePlus imgPlus;
	private long time;
	
	private MouseListener mouseListers[];
	private MouseMotionListener mouseMotionListers[];
	private Cursor hourglassCursor;
	
	private AdjacencyRelation adjPincel = AdjacencyRelation.getCircular(2);
	AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5f);
	
	private Color penColor;
	private int indexPenColor;
	private int lastMouseX;
	private int lastMouseY;
	private int firstMouseX;
	private int firstMouseY;
	private boolean processedIFT;
	private int numPointLivewire;
	
	
	
	public LiveWireByIFT(ImagePlus imgPlus) {
		super("MMLib4J - Livre wire");
		this.imgPlus = imgPlus;
		
		buttonPanel = new JPanel(new GridLayout(4, 0, 0, 0));
		controlPanel.add(udefcolPanel);
		controlPanel.add(buttonPanel);
		
		buttonPanel.add(reloadButton);
		buttonPanel.add(applyButton);
		buttonPanel.add(viewButton);
		
		reloadButton.addActionListener(this);
		viewButton.addActionListener(this);
		applyButton.addActionListener(this);
		
		
        udefcolPanel.add(comboColor);
        comboColor.addActionListener(this);
        controlPanel.setBackground(Color.WHITE);
		
		super.add(controlPanel);
		
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
		super.setSize(260, 250);
			
		initSegmentation();
		
		WindowManager.addWindow(this);
        GUI.center(this);
        super.addWindowListener(this);
        super.setVisible(true);
		
	}
	
	public void initSegmentation(){
		
		this.imgInput = ImageJAdapter.toGrayScaleImage( (ByteProcessor) imgPlus.getProcessor()).duplicate();
		this.imgOriginal = ImageBuilder.convertToImage(imgInput);
		
		Graphics2D g = (Graphics2D) imgPlus.getCanvas().getGraphics();
		g.drawImage(imgOriginal, 0, 0, null);
		
		
		imgMarcador = ImageFactory.createGrayScaleImage(32, imgInput.getWidth(), imgInput.getHeight());;
		imgMarcador.initImage(-1);
		
		initialPen = true;
		processedIFT = true;
		numPointLivewire = 0;
		imgGradient = MorphologicalOperatorsBasedOnSE.gradient(imgInput, adj).getInvert();
		
		lastMouseX = 0;
		lastMouseY = 0;
		firstMouseX = 0;
		firstMouseY = 0;
		indexPenColor = 0;
		comboColor.setSelectedIndex(0);
		penColor = comboColor.getColor(indexPenColor);
		
		mousex = 0;
		mousey = 0;
		prevx = 0;
		prevy = 0;		
	}
	
	
	
	public void windowClosed(WindowEvent e) {
		//imgPlus.setImage( imgCurrent );
		
		imgPlus.getCanvas().removeMouseListener(this);
		imgPlus.getCanvas().removeMouseMotionListener(this);
		imgPlus.getCanvas().setCursor(hourglassCursor);
		
		for(MouseListener ml: mouseListers)
			imgPlus.getCanvas().addMouseListener(ml);
		for(MouseMotionListener mml: mouseMotionListers)
			imgPlus.getCanvas().addMouseMotionListener(mml);
		
		super.close();
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
			penColor = comboColor.getColor();
			indexPenColor = comboColor.getSelectedIndex();
		}else if(e.getSource() == reloadButton){
			penColor = comboColor.getColor();
			indexPenColor = comboColor.getSelectedIndex();
			initSegmentation();
		}else if(e.getSource() == viewButton){
			Graphics2D g = (Graphics2D) imgPlus.getCanvas().getGraphics();
			imgOriginal = ImageBuilder.convertToImage(imgGradient);
			g.drawImage(imgOriginal, 0, 0, null);
			
		}
		else if(e.getSource() == applyButton){
		 
			new ImagePlus("Cut", ImageJAdapter.toByteProcessor( LiveWireIFT.removeBackground(imgInput, imgMarcador) )).show();
			
		}
	}
	
	
	public void mouseReleased(MouseEvent e) {
		initialPen = true;
	}
	
	public void mouseMoved(MouseEvent mouse) {
		if(System.currentTimeMillis() - time > 50)
		if (processedIFT && imgMapaPredecessores != null){
			drawFirstPoint();
			updateMousePoint(mouse);
			if(isPointValid(15)){
				int pixelEnd = mouse.getY() * imgInput.getWidth() + mouse.getX();
				if(imgInput.isPixelValid(pixelEnd)){
					time = System.currentTimeMillis();
					trackingEdges(pixelEnd);
				}
						
			}
		}
	}
	

	//Regra: Se tiver menos de 15 pontos o ponto do mouse atual ate o ultimo 
	//clique entao nao pode rodar a fun+/-+/-o para o usuario ter melhor controle
	public boolean isPointValid(int value){
		if (ImageUtils.distance(mousex, mousey, lastMouseX, lastMouseY, 1) < value){
			Graphics2D g = (Graphics2D) imgPlus.getCanvas().getGraphics();
	        g.setColor(penColor);
			g.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			drawLine(mousex, mousey, lastMouseX, lastMouseY, indexPenColor, false);
			g.drawLine(mousex  , mousey , lastMouseX, lastMouseY);
			return false;
		}
		else{
			return true;
		}
	}
	
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {
		initialPen = true;
		penLiveWire(e);
	}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	
	
	
	

	public void drawFirstPoint(){
		//Pintar o primeiro ponto diferente
		if (firstMouseX != 0 && firstMouseY != 0){
			
			Graphics2D g = (Graphics2D) imgPlus.getCanvas().getGraphics();
			g.drawImage(imgOriginal, 0, 0, null);
			//g.drawImage(imgCurrent, 0, 0, null);
			
			
			drawLine(firstMouseX, firstMouseY, firstMouseX, firstMouseY, indexPenColor, true);
            
            g.setColor(Color.BLACK);
    		g.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    		g.drawLine(firstMouseX, firstMouseY, firstMouseX, firstMouseY);
    		
    		g.setColor(Color.WHITE);
    		g.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    		g.drawLine(firstMouseX, firstMouseY, firstMouseX, firstMouseY);
        }
        
	}

	public void updateMousePoint(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
		prevx = e.getX();
		prevy = e.getY();		
	}


	public void penLiveWire(MouseEvent e){
		updateMousePoint(e);
		if (firstMouseX == 0 && firstMouseY == 0){
			firstMouseX = mousex;
			firstMouseY = mousey;
		}
		//Se estiver perto de 5 pontos e tiver mais de 2 clicks ent+/-o finalizar a imagem
		if (ImageUtils.distance(mousex, mousey, firstMouseX, firstMouseY, 1) <= 15 && numPointLivewire >= 3){
			drawLine(mousex, mousey, firstMouseX, firstMouseY, penColor.getRGB(), false);
			addHistoricalPath(mousex, mousey);
			numPointLivewire = 0;
			imgMapaPredecessores = null;
			firstMouseX = 0;
			firstMouseY = 0;
			drawFirstPoint();
		}else{
			Graphics2D g = (Graphics2D) imgPlus.getCanvas().getGraphics();
			g.setColor(penColor);
			g.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			
			drawLine(prevx, prevy, mousex, mousey,  indexPenColor, false);
			g.drawLine(prevx, prevy, mousex, mousey);
			
			if (ImageUtils.distance(mousex, mousey, lastMouseX, lastMouseY, 1) < 15){ //manual
				drawLine(mousex, mousey, lastMouseX, lastMouseY, indexPenColor, true);
			}else {		
				addHistoricalPath(mousex, mousey);
			}
			
			int pixelMouse = mousex + mousey * imgInput.getWidth();
			imgMapaPredecessores = LiveWireIFT.liveWire(adj, imgGradient, pixelMouse);
			
			lastMouseX = mousex;
			lastMouseY = mousey;
			numPointLivewire += 1; 
		}
	}
	
	public void addHistoricalPath(int x, int y){
		if (processedIFT && imgMapaPredecessores != null){
			int pixelEnd = y * imgMapaPredecessores.getWidth() + x;
			while (imgMapaPredecessores.getPixel(pixelEnd) != -1){
				drawPixelInMarked(pixelEnd % imgInput.getWidth(), pixelEnd / imgInput.getWidth(), indexPenColor);
				pixelEnd = imgMapaPredecessores.getPixel(pixelEnd);
			}
		}
	}
	
	
	/**
	 * Calcular o caminho m+/-nimo a cada movimento do mouse 
	 */
	public void trackingEdges(int pixelEnd){
		while (imgMapaPredecessores.getPixel(pixelEnd) != -1){			
			drawPixel(pixelEnd, 4.0f);
			pixelEnd = imgMapaPredecessores.getPixel(pixelEnd);
		}
	}
	


	public void penMarked(MouseEvent e) {
		Graphics2D g = (Graphics2D) imgPlus.getCanvas().getGraphics();
		g.setColor(penColor);
		g.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		
		if (initialPen) {
			updateMousePoint(e);
			initialPen = false;
			
			drawLine(prevx, prevy, mousex, mousey, indexPenColor, true);
			g.drawLine(prevx, prevy, mousex, mousey);
			
		}

		if (mousex != e.getX() || mousey != e.getY()) {
			mousex = e.getX();
			mousey = e.getY();
			
			drawLine(prevx, prevy, mousex, mousey, indexPenColor, true);
			g.drawLine(prevx, prevy, mousex, mousey);
			
			prevx = mousex;
			prevy = mousey;
		}
	}
	
	public void drawPixelInMarked(int x, int y, int cor){
		for(int p: adjPincel.getAdjacencyPixels(imgInput, x, y)){
			imgMarcador.setPixel(p, cor+1);
			Graphics2D g = (Graphics2D) imgPlus.getCanvas().getGraphics();
			//imgProcessor.putPixel(p % imgInput.getWidth(), p / imgInput.getWidth(), comboColor.getColor(cor).getRGB());
			imgOriginal.setRGB(p % imgInput.getWidth(), p / imgInput.getWidth(), comboColor.getColor(cor).getRGB());
		}
	}
	
	public void drawPixel(int pixel, float widthLine){
		int w = pixel % imgInput.getWidth();
        int h = pixel / imgInput.getWidth();
        
		Graphics2D g = (Graphics2D) imgPlus.getCanvas().getGraphics();
        g.setColor(penColor);
		g.setStroke(new BasicStroke(widthLine, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		drawLine(w, h , w, h, indexPenColor, false);
		g.drawLine(w  , h , w, h);
			
	}
	
	public  void drawLine(int x1, int y1, int x2, int y2, int cor, boolean isMarked){
		if(Math.abs( x2 - x1 ) > Math.abs( y2 - y1 )){
			if(x1 > x2) drawLine(x2, y2, x1, y1, cor, isMarked);
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
			
			if(isMarked) drawPixelInMarked(x, y, cor);
			while (x<= x2){
				if(isMarked) drawPixelInMarked(x, y, cor);
				x= x + 1;
				if(v <= 0){
					v = v + neg;
				}else{
					y = y + inc;
					v = v+ pos;
				}
			}
		}else{
			if(y1 > y2) drawLine(x2, y2, x1, y1, cor, isMarked);
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
			if(isMarked) drawPixelInMarked(x, y, cor);
			while(y <= y2){
				if(isMarked) drawPixelInMarked(x, y, cor);
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


