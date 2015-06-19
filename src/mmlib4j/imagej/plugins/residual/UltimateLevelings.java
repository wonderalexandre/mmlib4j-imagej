package mmlib4j.imagej.plugins.residual;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.plugin.frame.PlugInFrame;
import ij.process.ByteProcessor;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mmlib4j.filtering.MorphologicalOperators;
import mmlib4j.filtering.residual.UltimateAttributeOpenClose;
import mmlib4j.filtering.residual.UltimateAttributeOpening;
import mmlib4j.filtering.residual.UltimateGrainFilter;
import mmlib4j.filtering.residual.UltimateLevelingByReconstruction;
import mmlib4j.gui.WindowImages;
import mmlib4j.imagej.guj.EvolutionResidue;
import mmlib4j.imagej.guj.Granulometry;
import mmlib4j.imagej.guj.HistogramOfBranch;
import mmlib4j.imagej.guj.VisualizationComponentTree;
import mmlib4j.imagej.guj.VisualizationTreeOfShape;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.pruningStrategy.MappingStrategyOfPruning;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedAttribute;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedExtinctionValue;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedGradualTransition;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedMSER;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedMSERWithCircularity;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedMSERWithElongation;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedMSERWithTextLocation;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedTBMR;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.segmentation.Labeling;
import mmlib4j.segmentation.WatershedByIFT;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageAlgebra;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class UltimateLevelings  extends PlugInFrame implements ActionListener, ChangeListener,  WindowListener, MouseListener {
	
	private static final long serialVersionUID = 1L;
	private GrayScaleImage imgInput;
	private GrayScaleImage imgInputOriginal;
	private GrayScaleImage imgCurrent;
	
	private MorphologicalTreeFiltering tree;
	private MorphologicalTreeFiltering maxTree;
	private MorphologicalTreeFiltering minTree;
	
	private AdjacencyRelation adj8 = AdjacencyRelation.getCircular(1.5);
	private boolean flagLabel = false;
	private boolean flagColor = false;
	private MappingStrategyOfPruning pruning = null;
	private ImagePlus imgPlus;
	
	private String titleImg;
	
	private class ParamElongation{
		double elong;
		int areaMin;
		int areaMax;
		ParamElongation(double e, int min, int max){elong = e; areaMin=min; areaMax=max;}
	}
	private ParamElongation paramElongation;
	
	public UltimateLevelings(ImagePlus plus) {
		super("Ultimate Levelings - " + plus.getTitle());
		super.setSize(335, 720);
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		this.imgPlus = plus;
		
		imgPlus.addImageListener(new ImageListener() {
			public void imageUpdated(ImagePlus imp) {}
			public void imageOpened(ImagePlus imp) {}
			public void imageClosed(ImagePlus imp) {
				if(imp == imgPlus)
					close();
			}
		});
		
		this.titleImg = plus.getTitle();
		this.imgInput = ImageJAdapter.toGrayScaleImage( (ByteProcessor) plus.getProcessor());
		this.setMenuBar( IJ.getInstance().getMenuBar() );
		
		paramElongation = new ParamElongation(0.7, 50, imgInput.getSize());
		
		imgInputOriginal = imgInput.duplicate();
		
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				minTree = new ConnectedFilteringByComponentTree(imgInput, adj8, false);		
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				maxTree = new ConnectedFilteringByComponentTree(imgInput, adj8, true);		
			}
		});
		
		final Thread[] threads = new Thread[]{t1, t2}; 
		threads[0].setPriority(Thread.currentThread().getPriority());
		threads[0].start();
		threads[1].setPriority(Thread.currentThread().getPriority());
		threads[1].start();
		for (final Thread thread : threads){
			try {
				if (thread != null) 
					thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();	  
			}
		}
		
		this.tree = maxTree;
		this.imgCurrent = imgInput.duplicate();
		//imgPlus.getCanvas().addMouseListener(this);
		
		Panel appPanelResiduo = createPanelResidualOperator(true);
        super.add(appPanelResiduo);

        WindowManager.addWindow(this);
        GUI.center(this);
        imgPlus.getCanvas().addMouseListener(this);
		super.addWindowListener(this);
        super.setVisible(true);
	}
	
	public void updateImage(GrayScaleImage img){
		this.imgCurrent = img;
		if(flagLabel){
			if(chkLabel.isSelected()){
				imgPlus.setTitle(titleImg + " - Label");	
				imgPlus.setProcessor( ImageJAdapter.toColorProcessor(Labeling.labeling(imgCurrent, adj8).randomColor()) );
			}if(chkWS.isSelected()){
				imgPlus.setTitle(titleImg + " - watershed");
				GrayScaleImage imgGrad = MorphologicalOperators.gradient(imgInputOriginal, adj8);
				GrayScaleImage imgM = imgCurrent.duplicate();
				imgM.replaceValue(0, imgM.maxValue()+1);
				imgPlus.setProcessor( ImageJAdapter.toColorProcessor( WatershedByIFT.watershedByMarker(imgGrad, imgM).randomColor() ) );
				
			}
		}else{
			if(flagColor){
				int cc = imgCurrent.maxValue();
				imgPlus.setTitle(titleImg + " - Associeted index  (Amount: "+ cc+")");
				imgPlus.setProcessor( ImageJAdapter.toColorProcessor(imgCurrent.randomColor()) );
			}
			else{
				imgPlus.setTitle(titleImg + " - Residues");
				imgPlus.setProcessor( ImageJAdapter.toByteProcessor(imgCurrent) );
			}
			//imgPlus.getCanvas().getGraphics().drawImage(ImageBuilder.convertToImage(imgCurrent), 0, 0, null);
		}
	}
	
	/*public void updateImage(ColorImage img){
		imgPlus.setProcessor( ImageJAdapter.toColorProcessor(img) );
		//imgPlus.getCanvas().getGraphics().drawImage(ImageBuilder.convertToImage(img), 0, 0, null);
	}
	*/
	
	
	@Override
	public void stateChanged(ChangeEvent event) {
		stateChangedExtractionResidues(event);
	}
	
	
	
	@Override
	public void mouseEntered(MouseEvent e) { }
	@Override
	public void mouseExited(MouseEvent e) {	}
	@Override
	public void mousePressed(MouseEvent e) { }
	@Override
	public void mouseReleased(MouseEvent e) { }
	//@Override
	public void mouseClicked(MouseEvent e) {
		if(analisysButton.isSelected()){
			int mousex = e.getX();
			int mousey = e.getY();
			
			int attr = 0;
			if(comboAttributoResiduo.getSelectedItem().equals("Area"))
				attr = 0;
			else if(comboAttributoResiduo.getSelectedItem().equals("Volume"))
				attr = 1;				
			else if(comboAttributoResiduo.getSelectedItem().equals("Altitude"))
				attr = 4;
			else if(comboAttributoResiduo.getSelectedItem().equals("Height"))
				attr = 2;
			else if(comboAttributoResiduo.getSelectedItem().equals("Width"))
				attr = 3;
			
			String pruningSelected = (String) this.comboPruningStrategy.getSelectedItem();
			int typePruning = MorphologicalTreeFiltering.PRUNING;
			if(pruningSelected.equals("Gradual transition")){
				typePruning = MorphologicalTreeFiltering.PRUNING_GRADUAL_TRANSITION;
			}
			else if(pruningSelected.equals("MSER")){
				typePruning = MorphologicalTreeFiltering.PRUNING_MSER;
			}
			
			else if(pruningSelected.equals("Extinction value")){
				typePruning = MorphologicalTreeFiltering.EXTINCTION_VALUE;
			}
			
			if(comboResiduo.getSelectedItem().equals("Max{ ultimate Attribute opening and closing }")){
				new HistogramOfBranch(attr, mousex, mousey).run(this.minTree, typePruning, paramDeltaOfPruningStrategies.getValue());
				new HistogramOfBranch(attr, mousex, mousey).run(this.maxTree, typePruning, paramDeltaOfPruningStrategies.getValue());
			}
			
			else if(comboResiduo.getSelectedItem().equals("Ultimate leveling by reconstruction")){

				int type = 0;
				ComponentTree tree1 = null;
				if(comboAttributoResiduo.getSelectedItem().equals("Dilation")){
					tree1 = ((ComponentTree) minTree).getClone(); 
					type = UltimateLevelingByReconstruction.DILATION;
				}
				else if(comboAttributoResiduo.getSelectedItem().equals("Opening")){
					tree1 = ((ComponentTree) maxTree).getClone();
					type = UltimateLevelingByReconstruction.OPENING;
				}
				else if(comboAttributoResiduo.getSelectedItem().equals("Closing")){
					tree1 = ((ComponentTree) minTree).getClone(); 
					type = UltimateLevelingByReconstruction.CLOSING;
				}
				else if(comboAttributoResiduo.getSelectedItem().equals("Erosion")){
					tree1 = ((ComponentTree) maxTree).getClone();
					type = UltimateLevelingByReconstruction.EROSION;
				}
				
				UltimateLevelingByReconstruction ulr;
				ulr = new UltimateLevelingByReconstruction(tree1.getInputImage(), tree1, type);
				ulr.enableComputerDistribution(true);
				ulr.computeUAO(attributeValueMaxResiduo.getValue(), 1);
				new EvolutionResidue(ulr.getNodeDistribuition(), tree1,  mousex, mousey);
			}
			else{
				ComponentTree treeCT = ((ComponentTree) tree).getClone();
				UltimateAttributeOpening r = new UltimateAttributeOpening( treeCT );
				r.enableComputerDistribution(true);
				r.computeUAO( getAttributeValue(), getAttributeType(), getPruningSelected(), getFilteringResidues());
				new EvolutionResidue(r.getNodeDistribuition(), treeCT,  mousex, mousey);
				
			}
			analisysButton.setSelected(false);
			analisysButton.setText("Residual evolution");
			setCursor(DEFAULT_CURSOR);
		}
	}
	
/**
 ********************************************************************************	
 ********************************RESIDUOS***************************************
 ********************************************************************************
 */
	private JComboBox comboAttributoResiduo;
	private JComboBox comboPruningStrategy;
	private JComboBox comboFilterRegion;
	private JComboBox comboResiduo;
	private JComboBox comboInfoResiduo;
	private JButton reloadButtonResiduo;
	private JButton applyButtonFilter;
	private JButton applyButtonGrad;
	private JButton applyButtonContrast;
	private JButton applyButtonGranulometry;
	private JToggleButton analisysButton;
	private JCheckBox chkLabel;
	private JCheckBox chkInteractive;
	private JCheckBox chkWS;
	private JSlider attributeValueMaxResiduo;
	private JSlider paramDeltaOfPruningStrategies;
	private JSlider paramDeltaOfFilter;
	private JLabel limiarLabelResiduo;
	public Panel createPanelResidualOperator(boolean visib){
		Panel appPanelResiduo = new Panel(new GridLayout(10, 1, 0, 0));
		
		//1
		comboResiduo = new JComboBox();
		comboResiduo.setBorder(BorderFactory.createTitledBorder("Residual operator"));
		comboResiduo.addItem("Ultimate Attribute opening");
		comboResiduo.addItem("Ultimate Attribute closing");
		comboResiduo.addItem("Ultimate grain filter");
		comboResiduo.addItem("Max{ ultimate Attribute opening and closing }");
		comboResiduo.addItem("Ultimate leveling by reconstruction");
		comboResiduo.addActionListener(this);
		
		//2
		comboAttributoResiduo = new JComboBox();
		comboAttributoResiduo.setBorder(BorderFactory.createTitledBorder("Primitive type"));
		comboAttributoResiduo.addItem("--- primitive based on attribute ---");
		comboAttributoResiduo.addItem("Area");
		comboAttributoResiduo.addItem("Height");
		comboAttributoResiduo.addItem("Width");
		comboAttributoResiduo.addItem("Altitude");
		comboAttributoResiduo.setSelectedIndex(1);
		comboAttributoResiduo.addItem("--- primitive based on marked ---");
		comboAttributoResiduo.addItem("Dilation");
		comboAttributoResiduo.addItem("Closing");
		comboAttributoResiduo.addItem("Erosion");
		comboAttributoResiduo.addItem("Opening");
		comboAttributoResiduo.addItem("Dilation / Erosion");
		comboAttributoResiduo.addItem("Opening / Closing");
		comboAttributoResiduo.addItem("Median");
		comboAttributoResiduo.addItem("Gaussian");
		
		comboAttributoResiduo.addActionListener(this);
		
		//3
		comboInfoResiduo = new JComboBox();
		comboInfoResiduo.setBorder(BorderFactory.createTitledBorder("Display"));
		comboInfoResiduo.addItem("Residues (+/-)");
		comboInfoResiduo.addItem("Residues (+)");
		comboInfoResiduo.addItem("Residues (-)");
		comboInfoResiduo.addItem("Inf. residues (+/-)");
		comboInfoResiduo.addItem("Inf. residues (+)");
		comboInfoResiduo.addItem("Inf. residues (-)");
		comboInfoResiduo.addActionListener(this);
		
		//4
		comboPruningStrategy = new JComboBox();
		comboPruningStrategy.setBorder(BorderFactory.createTitledBorder("Pruning strategy"));
		comboPruningStrategy.addItem("Simple pruning");
		comboPruningStrategy.addItem("Extinction value");
		comboPruningStrategy.addItem("Gradual transition");
		//comboPruningStrategy.addItem("MSER");
		//comboPruningStrategy.addItem("TBMR");
		
		comboPruningStrategy.addActionListener(this);
		
		
		comboFilterRegion = new JComboBox();
		comboFilterRegion.setBorder(BorderFactory.createTitledBorder("Filtering of residues"));
		comboFilterRegion.addItem("--- no filtering ---");
		comboFilterRegion.addItem("MSER");
		comboFilterRegion.addItem("MSER by rank");
		comboFilterRegion.addItem("TBMR");
		comboFilterRegion.addItem("-- filtering by classifier --");
		comboFilterRegion.addItem("---- Text location ----");
		comboFilterRegion.addItem("---- Circularity ----");
		comboFilterRegion.addItem("---- Retangularity ----");
		comboFilterRegion.addItem("---- Elongation ----");
		comboFilterRegion.addActionListener(this);
		
		//5
		attributeValueMaxResiduo = new JSlider(JSlider.HORIZONTAL, 0,	100, 0);
		attributeValueMaxResiduo.setValue(0);
		attributeValueMaxResiduo.setBorder(BorderFactory.createTitledBorder("Attribute value (max)"));
		attributeValueMaxResiduo.setMajorTickSpacing(25);
		attributeValueMaxResiduo.setMinorTickSpacing(1);
		attributeValueMaxResiduo.setPaintTicks(true);
		attributeValueMaxResiduo.addChangeListener(this);
		
		
		//5
		paramDeltaOfPruningStrategies = new JSlider(JSlider.HORIZONTAL, 0,	100, 0);
		paramDeltaOfPruningStrategies.setValue(0);
		paramDeltaOfPruningStrategies.setBorder(BorderFactory.createTitledBorder("Delta (pruning strategy)"));
		paramDeltaOfPruningStrategies.setMajorTickSpacing(10);
		paramDeltaOfPruningStrategies.setMinorTickSpacing(1);
		paramDeltaOfPruningStrategies.setPaintTicks(true);
		paramDeltaOfPruningStrategies.addChangeListener(this);
				
		
		//6
		paramDeltaOfFilter = new JSlider(JSlider.HORIZONTAL, 0,	50, 0);
		paramDeltaOfFilter.setValue(0);
		paramDeltaOfFilter.setBorder(BorderFactory.createTitledBorder("Delta (filtering of residues)"));
		paramDeltaOfFilter.setMajorTickSpacing(10);
		paramDeltaOfFilter.setMinorTickSpacing(1);
		paramDeltaOfFilter.setPaintTicks(true);
		paramDeltaOfFilter.addChangeListener(this);
		//graudalTrasitionResiduo.setPaintLabels(true);
		
		
		
		//7
		limiarLabelResiduo = new JLabel("value: 0");
		chkInteractive = new JCheckBox("Interactive update");
		chkInteractive.addActionListener(this);
		chkWS = new JCheckBox("Watershed");
		chkWS.addActionListener(this);
		chkLabel = new JCheckBox("Labeling");
		chkLabel.addActionListener(this);
		
		JPanel panelLabels = new JPanel(new GridLayout(4, 1, 0, 0)); 
		panelLabels.add(limiarLabelResiduo);
		panelLabels.add(chkLabel);
		panelLabels.add(chkInteractive);
		panelLabels.add(chkWS);
		
		appPanelResiduo.add(comboResiduo);
		appPanelResiduo.add(comboAttributoResiduo);
		appPanelResiduo.add(comboPruningStrategy);
		appPanelResiduo.add(comboFilterRegion);
		appPanelResiduo.add(comboInfoResiduo);
		appPanelResiduo.add(attributeValueMaxResiduo);
		appPanelResiduo.add(paramDeltaOfPruningStrategies);
		appPanelResiduo.add(paramDeltaOfFilter);
		
		appPanelResiduo.add(panelLabels);
		
		//8
		reloadButtonResiduo = new JButton("Reset");
		applyButtonFilter = new JButton("Visualization tree");
		analisysButton = new JToggleButton("Residual evolution");
		applyButtonContrast = new JButton("Contrast Enhancement");
		applyButtonGranulometry = new JButton("Primitives"); //Granulometries
		applyButtonGrad = new JButton("Parameters");
		applyButtonFilter.addActionListener(this);
		reloadButtonResiduo.addActionListener(this);
		analisysButton.addActionListener(this);
		applyButtonGrad.addActionListener(this);
		applyButtonContrast.addActionListener(this);
		applyButtonGranulometry.addActionListener(this);
		
		JPanel panelButtons = new JPanel(new GridLayout(3, 1, 0, 0));
		panelButtons.add(reloadButtonResiduo);
		panelButtons.add(applyButtonFilter);
		panelButtons.add(analisysButton);
		panelButtons.add(applyButtonGrad);
		panelButtons.add(applyButtonContrast);
		panelButtons.add(applyButtonGranulometry);
		
		appPanelResiduo.add(panelButtons);
		return appPanelResiduo;
	}

	
	public void initResiduo(){
		tree = new ConnectedFilteringByComponentTree(imgInput, adj8, true);
		comboAttributoResiduo.setSelectedIndex(1);
		attributeValueMaxResiduo.setValue(0);
		paramDeltaOfPruningStrategies.setValue(0);
		paramDeltaOfFilter.setValue(1);
		flagLabel = false;
		//chkInteractive.setSelected(true);
	}	
	
	public void changeTreeFiltering(GrayScaleImage imgCurrent){
		if(comboResiduo.getSelectedItem().equals("Ultimate Attribute opening")){
			tree = maxTree;
			updateExtractionResidues();
		}
		else if(comboResiduo.getSelectedItem().equals("Ultimate Attribute closing")){
			tree = minTree;
			updateExtractionResidues();
		}
		else if(comboResiduo.getSelectedItem().equals("Ultimate grain filter")){
			tree = new ConnectedFilteringByTreeOfShape(imgCurrent);
			updateExtractionResidues();
		}
		
		
	}
	
	public MappingStrategyOfPruning getPruningSelected(){
		if(this.tree instanceof ComponentTree){
			//ComponentTree treeCT = (ComponentTree) this.tree;
			String pruningSelected = (String) this.comboPruningStrategy.getSelectedItem();
			if(pruningSelected.equals("Gradual transition")){
				System.out.println("ComponentTree - Gradual transition");
				int gradualTrans = paramDeltaOfPruningStrategies.getValue();
				return new PruningBasedGradualTransition(this.tree, getAttributeType(), gradualTrans);
			}
			else if(pruningSelected.equals("MSER")){
				System.out.println("ComponentTree - MSER");
				int delta = paramDeltaOfPruningStrategies.getValue();
				return new PruningBasedMSER(tree, delta);
			}
			else if(pruningSelected.equals("TBMR")){
				System.out.println("ComponentTree - TBMR");
				int tMin = 100;
				int tMax = (int) (tree.getInputImage().getSize() * 0.80);
				return new PruningBasedTBMR(tree, tMin, tMax);
			}
			else if(pruningSelected.equals("Extinction value")){
				System.out.println("ComponentTree - Extinction value");
				int delta = paramDeltaOfPruningStrategies.getValue();
				return new PruningBasedExtinctionValue(tree, getAttributeType(), delta);
			}
		}else{
			//TreeOfShape tree = (TreeOfShape) this.tree;
			String pruningSelected = (String) this.comboPruningStrategy.getSelectedItem();
			if(pruningSelected.equals("Gradual transition")){
				System.out.println("TreeOfShape - Gradual transition");
				int gradualTrans = paramDeltaOfPruningStrategies.getValue();
				return new PruningBasedGradualTransition(this.tree, getAttributeType(), gradualTrans);
			}
			else if(pruningSelected.equals("MSER")){
				System.out.println("TreeOfShape - MSER");
				int delta = paramDeltaOfPruningStrategies.getValue();
				return new PruningBasedMSER(tree, delta);
			}
			else if(pruningSelected.equals("TBMR")){
				System.out.println("TreeOfShape - TBMR");
				//int gradualTrans = graudalTrasitionResiduo.getValue();
				//return new TbmrToS(tree).getSelectedNode(50, gradualTrans);
			}
			else if(pruningSelected.equals("Extinction value")){
				System.out.println("TreeOfShape - Extinction value");
				int delta = paramDeltaOfPruningStrategies.getValue();
				return new PruningBasedExtinctionValue(tree, getAttributeType(), delta);
			}
			
		}
		return new PruningBasedAttribute(tree, getAttributeType());
		
	}
	
	
	
	public boolean[] getFilteringResidues(){
		if(this.tree instanceof ComponentTree){
			//ComponentTree tree = (ComponentTree) this.tree;
			String pruningSelected = (String) this.comboFilterRegion.getSelectedItem();
			
			if(pruningSelected.equals("MSER")){
				System.out.println("ComponentTree - MSER");
				int delta = paramDeltaOfFilter.getValue();
				
				return new PruningBasedMSER(tree, delta).getMappingSelectedNodes(); //new MserCT(tree).getMappingNodesByMSER(delta);
			}
			if(pruningSelected.equals("MSER by rank")){
				System.out.println("ComponentTree - MSER by rank");
				int delta = paramDeltaOfFilter.getValue();
				return new PruningBasedMSER(tree, delta).getMappingSelectedNodesRank();
			}
			else if(pruningSelected.equals("---- Text location ----")){
				System.out.println("ComponentTree - MSER with Text location");
				int delta = paramDeltaOfFilter.getValue();
				return new PruningBasedMSERWithTextLocation(this.tree, delta).getMappingSelectedNodes();
			}
			else if(pruningSelected.equals("---- Circularity ----")){
				System.out.println("ComponentTree - MSER with circularity");
				int delta = paramDeltaOfFilter.getValue();
				return new PruningBasedMSERWithCircularity(this.tree, delta).getMappingSelectedNodes();
			}
			else if(pruningSelected.equals("---- Elongation ----")){
				System.out.println("ComponentTree - MSER with elongation");
				int delta = paramDeltaOfFilter.getValue();
				pruning = new PruningBasedMSERWithElongation(this.tree, delta);
				((PruningBasedMSERWithElongation)pruning).setParametersElongationFunction(paramElongation.areaMin, paramElongation.areaMax, paramElongation.elong);
				return pruning.getMappingSelectedNodes();
			}
			
			
			else if(pruningSelected.equals("TBMR")){
				System.out.println("ComponentTree - TBMR");
				int tMin = paramDeltaOfFilter.getValue();;
				int tMax = tree.getInputImage().getSize();
				return new PruningBasedTBMR(this.tree, tMin, tMax).getMappingSelectedNodes();
			}
			
		}else{
			TreeOfShape tree = (TreeOfShape) this.tree;
			
			String pruningSelected = (String) this.comboFilterRegion.getSelectedItem();
			if(pruningSelected.equals("MSER")){
				System.out.println("TreeOfShape - MSER");
				int delta = paramDeltaOfFilter.getValue();
				return new PruningBasedMSER((MorphologicalTreeFiltering) tree, delta).getMappingSelectedNodes(); 
				//return new MSER(tree).getMappingNodesByMSER(delta);
			}
			if(pruningSelected.equals("MSER by rank")){
				System.out.println("TreeOfShape - MSER by rank");
				int delta = paramDeltaOfFilter.getValue();
				return new PruningBasedMSER((MorphologicalTreeFiltering) tree, delta).getMappingSelectedNodesRank();
			}
			else if(pruningSelected.equals("---- Text location ----")){
				System.out.println("TreeOfShape - MSER with Text location");
				int delta = paramDeltaOfFilter.getValue();
				return new PruningBasedMSERWithTextLocation(this.tree, delta).getMappingSelectedNodes();
			}
			else if(pruningSelected.equals("---- Circularity ----")){
				System.out.println("TreeOfShape - MSER with circularity");
				int delta = paramDeltaOfFilter.getValue();
				return new PruningBasedMSERWithCircularity(this.tree, delta).getMappingSelectedNodes();
			}
			else if(pruningSelected.equals("---- Circularity ----")){
				System.out.println("TreeOfShape - MSER with circularity");
				int delta = paramDeltaOfFilter.getValue();
				return new PruningBasedMSERWithElongation(this.tree, delta).getMappingSelectedNodes();
			}
			else if(pruningSelected.equals("TBMR")){
				System.out.println("TreeOfShape - TBMR");
				int tMin = paramDeltaOfFilter.getValue();;
				int tMax = tree.getInputImage().getSize();
				return new PruningBasedTBMR(this.tree, tMin, tMax).getMappingSelectedNodes();
			}
				
		}
		return null;
		
	}
	
	
	public void actionPerformed(ActionEvent e) {
		
		if(reloadButtonResiduo == e.getSource()){
			changeTreeFiltering(imgInput);
			updateImage(this.imgInput);
		}
		else if(e.getSource() == reloadButtonResiduo){
			flagLabel = false;
		}
		else if(e.getSource() == applyButtonGrad){
			if(pruning != null && pruning instanceof PruningBasedMSERWithElongation){
				GenericDialog gd = new GenericDialog("Parameters", this);
				gd.addNumericField("Elongation (0 to 1)", paramElongation.elong, 3);
				gd.addNumericField("Area (min)", paramElongation.areaMin, 0);
				gd.addNumericField("Area (max)", paramElongation.areaMax, 0);
				gd.showDialog();
				if (!gd.wasCanceled()){
					paramElongation.elong = gd.getNextNumber();
					paramElongation.areaMin = (int) gd.getNextNumber();
					paramElongation.areaMax = (int) gd.getNextNumber();
					updateExtractionResidues();
				}
			}
		}
		
		else if(e.getSource() == applyButtonGranulometry){
			
			if(comboResiduo.getSelectedItem().equals("Ultimate leveling by reconstruction")){

				int type = 0;
				ComponentTree tree1 = null;
				if(comboAttributoResiduo.getSelectedItem().equals("Dilation")){
					tree1 = ((ComponentTree) minTree).getClone(); 
					type = UltimateLevelingByReconstruction.DILATION;
				}
				else if(comboAttributoResiduo.getSelectedItem().equals("Opening")){
					tree1 = ((ComponentTree) maxTree).getClone();
					type = UltimateLevelingByReconstruction.OPENING;
				}
				else if(comboAttributoResiduo.getSelectedItem().equals("Closing")){
					tree1 = ((ComponentTree) minTree).getClone(); 
					type = UltimateLevelingByReconstruction.CLOSING;
				}
				else if(comboAttributoResiduo.getSelectedItem().equals("Erosion")){
					tree1 = ((ComponentTree) maxTree).getClone();
					type = UltimateLevelingByReconstruction.EROSION;
				}
				
				UltimateLevelingByReconstruction ulr;
				ulr = new UltimateLevelingByReconstruction(tree1.getInputImage(), tree1, type);
				ulr.enableComputerDistribution(true);
				ulr.computeUAO(attributeValueMaxResiduo.getValue(), 1);
				new Granulometry(ulr.getNodeDistribuition(), tree1);
			}
			else if(comboResiduo.getSelectedItem().equals("Ultimate Attribute opening") || comboResiduo.getSelectedItem().equals("Ultimate Attribute closing")){
				ComponentTree treeCT = ((ComponentTree) tree).getClone();
				UltimateAttributeOpening r = new UltimateAttributeOpening( treeCT );
				r.enableComputerDistribution(true);
				r.computeUAO( getAttributeValue(), getAttributeType(), getPruningSelected(), getFilteringResidues());
				new Granulometry(r.getNodeDistribuition(), treeCT);	
			}
			else if(comboResiduo.getSelectedItem().equals("Max{ ultimate Attribute opening and closing }")){
				
			}
			
			else if(comboResiduo.getSelectedItem().equals("Ultimate grain filter")){
				
			}
			
			
		}
		
		else if(e.getSource() == applyButtonContrast){			
			WindowImages.show(getImageContrastEnhancement( imgInput ), "Contrast Enhancement");
			/*
			int cont = 0;
			int maxIter = Integer.parseInt( JOptionPane.showInputDialog("number of interation") );
			GrayScaleImage imgBin = imgInput;
			do{
				GrayScaleImage imgContrast = getImageContrastEnhancement( imgBin );
				cont += 1;
				imgBin = imgContrast;
			}while(cont < maxIter);
			WindowImages.show(imgBin, "Contrast Enhancement");*/
		}
		else if(e.getSource() == analisysButton){
			if(analisysButton.isSelected()){
				analisysButton.setText("Choose the pixel for analisys..");
				super.setCursor(Cursor.CROSSHAIR_CURSOR);
			}else{
				analisysButton.setText("Residual evolution");
				super.setCursor(Cursor.DEFAULT_CURSOR);
			}
		}
		else if(e.getSource() == applyButtonFilter){
			boolean selected[] = getPruningSelected().getMappingSelectedNodes();
			if(tree instanceof ComponentTree){
				VisualizationComponentTree.getInstance( (ComponentTree) this.tree, selected, null ).setVisible(true);
				
			}else{
				VisualizationTreeOfShape.getInstance( (TreeOfShape) this.tree, selected, null ).setVisible(true);
			}
		}
		else if(chkLabel == e.getSource()){
			chkWS.setSelected(false);
			flagLabel = chkLabel.isSelected();
			updateImage( imgCurrent );
		}
		else if(chkWS == e.getSource()){
			chkLabel.setSelected(false);
			flagLabel = chkWS.isSelected();
			if(flagLabel)
				updateImage( imgCurrent );
			else
				updateImage( imgCurrent );
		}
		else if(comboResiduo == e.getSource()){
		
			if(comboResiduo.getSelectedItem().equals("Ultimate Attribute opening")){
				if(!isValidPrimitiveType()){
					comboAttributoResiduo.setSelectedIndex(1);
				}
				updateExtractionResidues();
			}
			if(comboResiduo.getSelectedItem().equals("Ultimate leveling by reconstruction")){
				if(!isValidPrimitiveType()){
					comboAttributoResiduo.setSelectedIndex(6);
				}
				updateExtractionResidues();
			}
			
			if(comboResiduo.getSelectedItem().equals("Max{ ultimate Attribute opening and closing }")){
				if(!isValidPrimitiveType()){
					comboAttributoResiduo.setSelectedIndex(1);
				}
				updateExtractionResidues();
			}
			else if(comboResiduo.getSelectedItem().equals("Ultimate Attribute closing")){
				if(!isValidPrimitiveType()){
					comboAttributoResiduo.setSelectedIndex(1);
				}
				tree = new ConnectedFilteringByComponentTree(imgInput, adj8, false);
				updateExtractionResidues();
			}
			else if(comboResiduo.getSelectedItem().equals("Ultimate grain filter")){
				if(!isValidPrimitiveType()){
					comboAttributoResiduo.setSelectedIndex(1);
				}
				tree = new ConnectedFilteringByTreeOfShape(imgInput);
				updateExtractionResidues();
			}
			
		}
		else if(comboAttributoResiduo == e.getSource()){
			updateExtractionResidues();
		}
		else if(comboInfoResiduo == e.getSource()){
			updateExtractionResidues();
		}
		else if(comboPruningStrategy == e.getSource()){
			updateExtractionResidues();
		}
		else if(comboFilterRegion == e.getSource()){
			if(comboFilterRegion.getSelectedItem().equals("MSER")){
				paramDeltaOfFilter.setValue(7);
				paramDeltaOfFilter.repaint();
			}
			updateExtractionResidues();
		}
		
		
	}
	
	public void stateChangedExtractionResidues(ChangeEvent e){
		if(attributeValueMaxResiduo == e.getSource()){
			if(chkInteractive.isSelected()){
				updateExtractionResidues();
			}
			else{
				if(!attributeValueMaxResiduo.getValueIsAdjusting())
					updateExtractionResidues();
				else
					getAttributeValue();
			}
		}		
		else if(paramDeltaOfPruningStrategies == e.getSource()){
			if(chkInteractive.isSelected()){
				updateExtractionResidues();
			}
			else{
				if(!paramDeltaOfPruningStrategies.getValueIsAdjusting())
					updateExtractionResidues();
			}
		}
		else if(paramDeltaOfFilter == e.getSource()){
			if(chkInteractive.isSelected()){
				updateExtractionResidues();
			}
			else{
				if(!paramDeltaOfFilter.getValueIsAdjusting())
					updateExtractionResidues();
			}
		}
		
	}
	
	public int getAttributeType(){
		if(comboAttributoResiduo.getSelectedItem().equals("Area")){
			return Attribute.AREA;
		}
		else if(comboAttributoResiduo.getSelectedItem().equals("Altitude")){ //area
			return Attribute.ALTITUDE;
		}
		else if(comboAttributoResiduo.getSelectedItem().equals("Volume")){ //volume
			return Attribute.VOLUME;
		}
		else if(comboAttributoResiduo.getSelectedItem().equals("Height")){ //Height
			return Attribute.HEIGHT;
		}
		else if(comboAttributoResiduo.getSelectedItem().equals("Width")){ //Height
			return Attribute.WIDTH;
		}
		else
			return -1;
		
	}
	public int getAttributeValue(){
		int attributeValue = 0;
		if(comboAttributoResiduo.getSelectedItem().equals("Area")){
			attributeValue = (int) ((attributeValueMaxResiduo.getValue() / 100.0) * imgCurrent.getSize());
			attributeValue = (int) Math.floor(Math.pow(imgCurrent.getSize(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
			
		}
		else if(comboAttributoResiduo.getSelectedItem().equals("Altitude")){ //area
			attributeValue = (int) ((attributeValueMaxResiduo.getValue() / 100.0) * 255);
			attributeValue = (int) Math.floor(Math.pow(255, 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
			
		}
		else if(comboAttributoResiduo.getSelectedItem().equals("Volume")){ //volume
			attributeValue = (int) ((attributeValueMaxResiduo.getValue() / 100.0) * imgCurrent.getSize());
			attributeValue = (int) Math.floor(Math.pow(imgCurrent.getSize(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
			
		}
		else if(comboAttributoResiduo.getSelectedItem().equals("Height")){ //Height
			attributeValue = (int) ((attributeValueMaxResiduo.getValue() / 100.0) * imgCurrent.getHeight());
			attributeValue = (int) Math.floor(Math.pow(imgCurrent.getHeight(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
			
		}
		else if(comboAttributoResiduo.getSelectedItem().equals("Width")){ //Height
			attributeValue = (int) ((attributeValueMaxResiduo.getValue() / 100.0) * imgCurrent.getWidth());
			attributeValue = (int) Math.floor(Math.pow(imgCurrent.getWidth(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
			
		}
		limiarLabelResiduo.setText("value: " + attributeValue);
		return attributeValue;
	}
	
	
	

	public GrayScaleImage getImageContrastEnhancement(GrayScaleImage img){
		
		int attributeValue = getAttributeValue();
		//int attributeValueMin = 0;
		
		//attributeValueMin = attributeValueMinResiduo.getValue();
		MorphologicalTreeFiltering tree = null;
		MorphologicalTreeFiltering maxTree= null;
		MorphologicalTreeFiltering minTree= null;
		if(img == imgInput){
			tree = this.tree;
			maxTree = this.maxTree;
			minTree = this.minTree;
		}else{
			if(comboResiduo.getSelectedItem().equals("Ultimate Attribute opening")){
				tree = new ConnectedFilteringByComponentTree(img, adj8, true);
			}
			if(comboResiduo.getSelectedItem().equals("Max{ ultimate Attribute opening and closing }")){
				maxTree = new ConnectedFilteringByComponentTree(img, adj8, true);
				minTree = new ConnectedFilteringByComponentTree(img, adj8, false);
			}
			else if(comboResiduo.getSelectedItem().equals("Ultimate Attribute closing")){
				tree = new ConnectedFilteringByComponentTree(img, adj8, false);
			}
			else if(comboResiduo.getSelectedItem().equals("Ultimate grain filter")){
				tree = new ConnectedFilteringByTreeOfShape(img);
			}
		}
		
		
		GrayScaleImage resPos = null;
		GrayScaleImage resNeg = null;
		if(comboResiduo.getSelectedItem().equals("Ultimate Attribute opening")){
			UltimateAttributeOpening r = new UltimateAttributeOpening((ComponentTree) tree);
			r.computeUAO( getAttributeValue(), getAttributeType(), getPruningSelected(), getFilteringResidues());
			resPos = r.getResidues();
		}
		else if(comboResiduo.getSelectedItem().equals("Max{ ultimate Attribute opening and closing }")){
			
			this.tree = minTree;
			MappingStrategyOfPruning mappingMin = getPruningSelected();
			boolean filterMin[] = getFilteringResidues();
			
			this.tree = maxTree;
			MappingStrategyOfPruning mappingMax = getPruningSelected();
			boolean filterMax[] = getFilteringResidues();
			
			UltimateAttributeOpenClose uaoAutoDual = new UltimateAttributeOpenClose((ComponentTree)minTree, (ComponentTree)maxTree);
			uaoAutoDual.computeUAO(attributeValue, getAttributeType(), mappingMin, filterMin, mappingMax, filterMax);
			
			resPos = uaoAutoDual.getResiduesPos();
			resNeg = uaoAutoDual.getResiduesNeg();
			
		}
		else if(comboResiduo.getSelectedItem().equals("Ultimate Attribute closing")){
			UltimateAttributeOpening r = new UltimateAttributeOpening((ComponentTree) tree);
			r.computeUAO( attributeValue, getAttributeType(), getPruningSelected(), getFilteringResidues());
			resNeg = r.getResidues();
		}
		else if(comboResiduo.getSelectedItem().equals("Ultimate grain filter")){
			UltimateGrainFilter r = new UltimateGrainFilter((TreeOfShape) tree);
			r.computeUGF( attributeValue, getAttributeType(), getPruningSelected(), getFilteringResidues());
			resPos = r.getResiduesPos();
			resNeg = r.getResiduesNeg();
		}
	
		GrayScaleImage imgOut = img.duplicate();
		/*int tmp=0;
		for(int i=0; i < imgOut.getSize(); i++){
			if(resPos != null )
				tmp = (imgOut.getPixel(i) + resPos.getPixel(i));
			if(resNeg != null )
				tmp = (imgOut.getPixel(i) - resNeg.getPixel(i));
	        	
			if(tmp < 0)
				tmp = 0;
			else if(tmp > 255)
				tmp = 255;
			
			imgOut.setPixel(i, tmp);
		}*/
		if(resPos != null )
			imgOut = ImageAlgebra.add(imgOut, resPos);
		if(resNeg != null )
			imgOut = ImageAlgebra.subtraction(imgOut, resNeg);
		
		return imgOut;
		
	}
	
	public void updateExtractionResidues(){
		int attributeValue = getAttributeValue();
		//int attributeValueMin = 0;
		
		if(!isValidPrimitiveType()){
			JOptionPane.showMessageDialog(this, "Primitive type is invalid!");
			return;
		}
		
		if(comboResiduo.getSelectedItem().equals("Ultimate leveling by reconstruction")){
			int type = 0;
			ComponentTree tree1 = null;
			ComponentTree tree2 = null;
			if(comboAttributoResiduo.getSelectedItem().equals("Dilation")){
				tree1 = (ComponentTree) minTree;
				type = UltimateLevelingByReconstruction.DILATION;
			}
			else if(comboAttributoResiduo.getSelectedItem().equals("Opening")){
				tree1 = (ComponentTree) maxTree;
				type = UltimateLevelingByReconstruction.OPENING;
			}
			else if(comboAttributoResiduo.getSelectedItem().equals("Closing")){
				tree1 = (ComponentTree) minTree;
				type = UltimateLevelingByReconstruction.CLOSING;
			}
			else if(comboAttributoResiduo.getSelectedItem().equals("Erosion")){
				tree1 = (ComponentTree) maxTree;
				type = UltimateLevelingByReconstruction.EROSION;
			}
			else if(comboAttributoResiduo.getSelectedItem().equals("Dilation / Erosion")){
				tree2 = (ComponentTree) maxTree;
				tree1 = (ComponentTree) minTree;
				type = UltimateLevelingByReconstruction.DILATION_EROSION;
			}
			else if(comboAttributoResiduo.getSelectedItem().equals("Opening / Closing")){
				tree2 = (ComponentTree) maxTree;
				tree1 = (ComponentTree) minTree;
				type = UltimateLevelingByReconstruction.OPENING_CLOSING;
			}
			else if(comboAttributoResiduo.getSelectedItem().equals("Median")){
				tree2 = (ComponentTree) maxTree;
				tree1 = (ComponentTree) minTree;
				type = UltimateLevelingByReconstruction.MEDIAN;
			}
			else if(comboAttributoResiduo.getSelectedItem().equals("Gaussian")){
				tree2 = (ComponentTree) maxTree;
				tree1 = (ComponentTree) minTree;
				type = UltimateLevelingByReconstruction.GAUSSIAN;
			}
			UltimateLevelingByReconstruction ulr;
			if(tree1 != null && tree2 != null){
				ulr = new UltimateLevelingByReconstruction(tree1.getInputImage(), tree1, tree2, type);
			}else{
				ulr = new UltimateLevelingByReconstruction(tree1.getInputImage(), tree1, type);	
			}
			
			ulr.computeUAO(attributeValueMaxResiduo.getValue(), 1);
			if(comboInfoResiduo.getSelectedIndex() > 2){
				flagColor = true;
				updateImage(ulr.getAssociateIndexImage());	
			}else{
				flagColor = false;
				updateImage(ulr.getResidues());	
			}
			
		}
		/*else if(comboResiduo.getSelectedItem().equals("Ultimate closing by reconstruction")){
			UltimateLevelingByReconstruction uorec = new UltimateLevelingByReconstruction(minTree.getInputImage(), (ComponentTree)maxTree, UltimateLevelingByReconstruction.CLOSING);
			uorec.computeUAO(attributeValueMaxResiduo.getValue(), 3);
			if(comboInfoResiduo.getSelectedIndex() > 2){
				flagColor = true;
				updateImage(uorec.getAssociateIndexImage());	
			}else{
				flagColor = false;
				updateImage(uorec.getResidues());	
			}
		}*/
		else if(comboResiduo.getSelectedItem().equals("Ultimate Attribute opening")){
			UltimateAttributeOpening r = new UltimateAttributeOpening((ComponentTree) tree);
			r.computeUAO( getAttributeValue(), getAttributeType(), getPruningSelected(), getFilteringResidues());
			if(comboInfoResiduo.getSelectedIndex() > 2){
				flagColor = true;
				updateImage(r.getAssociateIndexImage());	
			}else{
				flagColor = false;
				updateImage(r.getResidues());	
			}
		}
		
		else if(comboResiduo.getSelectedItem().equals("Max{ ultimate Attribute opening and closing }")){
			
			int attributeType = getAttributeType();
			
			tree = minTree;
			MappingStrategyOfPruning mappingMin = getPruningSelected();
			boolean filterMin[] = getFilteringResidues();
			
			tree = maxTree;
			MappingStrategyOfPruning mappingMax = getPruningSelected();
			boolean filterMax[] = getFilteringResidues();
			
			UltimateAttributeOpenClose uaoAutoDual = new UltimateAttributeOpenClose((ComponentTree)minTree, (ComponentTree)maxTree);
			uaoAutoDual.computeUAO(attributeValue, attributeType, mappingMin, filterMin, mappingMax, filterMax);
			if(comboInfoResiduo.getSelectedItem().equals("Residues (+/-)")){
				flagColor = false;
				updateImage(uaoAutoDual.getResidues());		
			}else if(comboInfoResiduo.getSelectedItem().equals("Residues (+)")){
				flagColor = false;
				updateImage(uaoAutoDual.getResiduesPos());	
			}else if(comboInfoResiduo.getSelectedItem().equals("Residues (-)")){
				flagColor = false;
				updateImage(uaoAutoDual.getResiduesNeg());
			}else if(comboInfoResiduo.getSelectedItem().equals("Inf. residues (+/-)")){
				flagColor = true;
				updateImage(uaoAutoDual.getAssociateIndexImage());
			}else if(comboInfoResiduo.getSelectedItem().equals("Inf. residues (+)")){
				flagColor = true;
				updateImage(uaoAutoDual.getAssociateIndexImagePos());
			}else if(comboInfoResiduo.getSelectedItem().equals("Inf. residues (-)")){
				flagColor = true;
				updateImage(uaoAutoDual.getAssociateIndexImageNeg());
			}
			
			
			
		}
		else if(comboResiduo.getSelectedItem().equals("Ultimate Attribute closing")){
			UltimateAttributeOpening r = new UltimateAttributeOpening((ComponentTree) tree);
			r.computeUAO( attributeValue, getAttributeType(), getPruningSelected(), getFilteringResidues());
			if(comboInfoResiduo.getSelectedIndex() > 2){
				flagColor = true;
				updateImage(r.getAssociateIndexImage());	
			}else{
				flagColor = false;
				updateImage(r.getResidues());	
			}
		}
		else if(comboResiduo.getSelectedItem().equals("Ultimate grain filter")){
			UltimateGrainFilter r = new UltimateGrainFilter((TreeOfShape) tree);
			r.computeUGF( attributeValue, getAttributeType(), getPruningSelected(), getFilteringResidues());
			if(comboInfoResiduo.getSelectedItem().equals("Residues (+/-)")){
				flagColor = false;
				updateImage(r.getResidues());
			}else if(comboInfoResiduo.getSelectedItem().equals("Residues (+)")){
				flagColor = false;
				updateImage(r.getResiduesPos());
			}else if(comboInfoResiduo.getSelectedItem().equals("Residues (-)")){
				flagColor = false;
				updateImage(r.getResiduesNeg());
			}else if(comboInfoResiduo.getSelectedItem().equals("Inf. residues (+/-)")){
				flagColor = true;
				updateImage(r.getAssociateImage());
			}else if(comboInfoResiduo.getSelectedItem().equals("Inf. residues (+)")){
				flagColor = true;
				updateImage(r.getAssociateImagePos());
			}else if(comboInfoResiduo.getSelectedItem().equals("Inf. residues (-)")){
				flagColor = true;
				updateImage(r.getAssociateImageNeg());
			}
			
		}
	
	}
	
	public boolean isValidPrimitiveType(){
		if(comboAttributoResiduo.getSelectedItem().equals("--- primitive based on attribute ---") ||
				comboAttributoResiduo.getSelectedItem().equals("--- primitive based on marked ---")){
			return false;
		}
		
		boolean isMarked = false;
		if(comboAttributoResiduo.getSelectedItem().equals("Dilation")  
				|| comboAttributoResiduo.getSelectedItem().equals("Opening")
				|| comboAttributoResiduo.getSelectedItem().equals("Closing")
				|| comboAttributoResiduo.getSelectedItem().equals("Erosion")
				|| comboAttributoResiduo.getSelectedItem().equals("Dilation / Erosion")
				|| comboAttributoResiduo.getSelectedItem().equals("Opening / Closing")
				|| comboAttributoResiduo.getSelectedItem().equals("Median")
				|| comboAttributoResiduo.getSelectedItem().equals("Gaussian")){
				
				isMarked = true;
			}
		if(isMarked && comboResiduo.getSelectedItem().equals("Ultimate leveling by reconstruction")){
			return true;
		}
		else if(!isMarked && !comboResiduo.getSelectedItem().equals("Ultimate leveling by reconstruction")){
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public void windowClosed(WindowEvent e) {
		//imgPlus.getCanvas().removeMouseListener(this);
		this.setVisible(false);	
		this.setEnabled(false);
	}
	@Override
	public void windowOpened(WindowEvent e) { }
	@Override
	public void windowClosing(WindowEvent e) {	
        super.windowClosing(e);
	}
	@Override
	public void windowIconified(WindowEvent e) { }
	@Override
	public void windowDeiconified(WindowEvent e) { }
	@Override
	public void windowActivated(WindowEvent e) { }
	@Override
	public void windowDeactivated(WindowEvent e) { }
	

	
	
	
}
