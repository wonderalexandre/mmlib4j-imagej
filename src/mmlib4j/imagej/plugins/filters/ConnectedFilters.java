package mmlib4j.imagej.plugins.filters;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.plugin.frame.PlugInFrame;
import ij.process.ByteProcessor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
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
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mmlib4j.imagej.guj.HistogramOfBranch;
import mmlib4j.imagej.guj.VisualizationComponentTree;
import mmlib4j.imagej.guj.VisualizationTreeOfShape;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueComponentTree;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueTreeOfShapes;
import mmlib4j.representation.tree.attribute.ComputerMserTreeOfShapes;
import mmlib4j.representation.tree.attribute.ComputerTbmrComponentTree;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedAttribute;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedGradualTransition;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.segmentation.Labeling;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageAlgebra;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class ConnectedFilters extends PlugInFrame implements MouseListener, ActionListener, ChangeListener, WindowListener {
	
	private static final long serialVersionUID = 2712059893881609948L;
	
	private GrayScaleImage imgInput;
	private GrayScaleImage imgCurrent;
	private MorphologicalTreeFiltering tree;
	private InfoPrunedTree prunedTree;
	
	private AdjacencyRelation adj8 = AdjacencyRelation.getCircular(1.5);
	private ImagePlus imgPlus;
	private int pInfX=-1;
	private int pInfY=-1;
	private int deltaMSER;
	private int gradualTransition;
	
	private JComboBox comboAttributoFilter;
	private JComboBox comboTreeFilter;
	private JComboBox comboPruningFilter;
	private JButton reloadButtonFilter;
	private JButton applyButtonFilter;
	private JCheckBox chkLabeling;
	private JSlider attributeValueFilter;
	private JCheckBox chkInteractive;
	private JCheckBox chkResidues;
	private JLabel limiarLabelFilter;
	private JLabel numCCsLabelFilter;
	private JLabel numFlatZoneLabelFilter;
	private JLabel numRegionExtremaLabelFilter;
	
	public ConnectedFilters(ImagePlus plus) {
		super("MMorph4J: Connected Filters");
		super.setSize(270, 460);
		this.imgPlus = plus;
		this.imgInput = imgCurrent = ImageJAdapter.toGrayScaleImage( (ByteProcessor) plus.getProcessor());
		
		tree = new ConnectedFilteringByComponentTree(imgInput, adj8, true);
		
		super.setLayout(new BorderLayout());
		super.add( createPanelFiltering( ) );
		super.setVisible(true);	
		super.addWindowListener(this);
		
		imgPlus.getCanvas().addMouseListener(this);
		
		 WindowManager.addWindow(this);
		 GUI.center(this);
		 setVisible(true);
	        
		 applyFilter(true);
	}
	
	public JPanel createPanelFiltering( ){
		JPanel appPanelFiltering = new JPanel(new GridLayout(8, 1, 0, 0));
		
		//1
		comboTreeFilter = new JComboBox();
		comboTreeFilter.setBorder(BorderFactory.createTitledBorder("Operator"));
		comboTreeFilter.addItem("Attribute Opening");
		comboTreeFilter.addItem("Attribute Closing");
		comboTreeFilter.addItem("Grain Filter");
		comboTreeFilter.addActionListener(this);
		
		//2
		comboAttributoFilter = new JComboBox();
		comboAttributoFilter.setBorder(BorderFactory.createTitledBorder("Attribute type"));
		comboAttributoFilter.addItem("Area");
		comboAttributoFilter.addItem("Volume");
		//comboAttributoFilter.addItem("Altitude");
		comboAttributoFilter.addItem("Height");
		comboAttributoFilter.addItem("Width");
		//comboAttributoFilter.addItem("Length major axes");
		
		//3
		comboPruningFilter = new JComboBox();
		comboPruningFilter.setBorder(BorderFactory.createTitledBorder("Pruning strategy"));
		comboPruningFilter.addItem("Extinction Value");
		comboPruningFilter.addItem("MSER");
		comboPruningFilter.addItem("Pruning");
		comboPruningFilter.addItem("TBMR");
		comboPruningFilter.addItem("Gradual transition");
		comboPruningFilter.setSelectedIndex(2);
		
		
		//4
		attributeValueFilter = new JSlider(JSlider.HORIZONTAL, 0,	100, 0);
		attributeValueFilter.setValue(0);
		attributeValueFilter.setBorder(BorderFactory.createTitledBorder("Attribute value"));
		attributeValueFilter.setMajorTickSpacing(25);
		attributeValueFilter.setMinorTickSpacing(1);
		attributeValueFilter.setPaintTicks(true);
		attributeValueFilter.addChangeListener(this);
		
		
		//5 e 6
		limiarLabelFilter = new JLabel("attribute value: 0");
		numCCsLabelFilter = new JLabel("num CCs: ");
		numRegionExtremaLabelFilter = new JLabel("num regional minima: ");
		numFlatZoneLabelFilter = new JLabel("num flatzone: ");
		
		JPanel panelLabels1 = new JPanel(new GridLayout(2, 1, 0, 0));
		panelLabels1.setBorder(BorderFactory.createTitledBorder(""));
		panelLabels1.add(limiarLabelFilter);
		panelLabels1.add(numCCsLabelFilter);
		
		JPanel panelLabels2 = new JPanel(new GridLayout(2, 1, 0, 0));
		panelLabels2.setBorder(BorderFactory.createTitledBorder(""));
		panelLabels2.add(numRegionExtremaLabelFilter);
		panelLabels2.add(numFlatZoneLabelFilter);
		
		chkInteractive = new JCheckBox("Interactive update");
		chkLabeling = new JCheckBox("Labeling");
		chkResidues = new JCheckBox("Top-hat");
		
		JPanel panelChk = new JPanel(new GridLayout(3, 1, 0, 0));
		panelChk.setBorder(BorderFactory.createTitledBorder(""));
		panelChk.add(chkInteractive);
		panelChk.add(chkLabeling);
		panelChk.add(chkResidues);
		
		appPanelFiltering.add(comboTreeFilter);
		appPanelFiltering.add(comboAttributoFilter);
		appPanelFiltering.add(comboPruningFilter);
		appPanelFiltering.add(attributeValueFilter);
		appPanelFiltering.add(panelLabels1);
		appPanelFiltering.add(panelLabels2);
		appPanelFiltering.add(panelChk);
		
		//5
		reloadButtonFilter = new JButton("Reload");
		applyButtonFilter = new JButton("Visualization tree");
		
		reloadButtonFilter.addActionListener(this);
		applyButtonFilter.addActionListener(this);
		
		
		JPanel panelButtons = new JPanel(new GridLayout(1, 2, 0, 0));
		panelButtons.add(reloadButtonFilter);
		panelButtons.add(applyButtonFilter);
		
		comboPruningFilter.addActionListener(this);
		chkInteractive.addActionListener(this);
		chkLabeling.addActionListener(this);
		chkResidues.addActionListener(this);
		appPanelFiltering.add(panelButtons);
		return appPanelFiltering;
	}
	
	
	public void updateImage(GrayScaleImage img){
		if(chkLabeling.isSelected()){
			imgPlus.setProcessor( ImageJAdapter.toColorProcessor(Labeling.labeling(img, adj8).randomColor()) );
			//imgPlus.getCanvas().getGraphics().drawImage(ImageBuilder.convertToImage(img), 0, 0, null);
		}
		else{
			imgPlus.setProcessor( ImageJAdapter.toByteProcessor(img) );
			//imgPlus.getCanvas().getGraphics().drawImage(ImageBuilder.convertToImage(img), 0, 0, null);
		}
	}
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		int mousex = e.getX();
		int mousey = e.getY();
		HistogramOfBranch winHist = new HistogramOfBranch(getAttributeType(), mousex, mousey);
		winHist.run(prunedTree, getPruningType(), deltaMSER);
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {	}
	@Override
	public void mouseExited(MouseEvent e) {	}
	@Override
	public void mousePressed(MouseEvent e) {	}
	@Override
	public void mouseReleased(MouseEvent e) {	}

	
	public int getPruningType(){
		if(comboPruningFilter.getSelectedItem().equals("Extinction Value")){
			return MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE;
		}
		else if(comboPruningFilter.getSelectedItem().equals("MSER")){
			return MorphologicalTreeFiltering.PRUNING_MSER;
		}
		else if(comboPruningFilter.getSelectedItem().equals("TBMR")){
			return MorphologicalTreeFiltering.PRUNING_TBMR;
		}
		else if(comboPruningFilter.getSelectedItem().equals("Gradual transition")){
			return MorphologicalTreeFiltering.PRUNING_GRADUAL_TRANSITION;
		}
		else {
			return MorphologicalTreeFiltering.PRUNING;
		}
		
	}
	
	
	
	public int getAttributeType(){
		if(comboAttributoFilter.getSelectedItem().equals("Area")){ //area
			return Attribute.AREA;
		}
		else if(comboAttributoFilter.getSelectedItem().equals("Volume")){ //volume
			return Attribute.VOLUME;
		}
		else if(comboAttributoFilter.getSelectedItem().equals("Height")){ //Height
			return Attribute.HEIGHT;
		}
		else if(comboAttributoFilter.getSelectedItem().equals("Width")){ //Width
			return Attribute.WIDTH;
		}
		else if(comboAttributoFilter.getSelectedItem().equals("Altitude")){ //Width
			return Attribute.ALTITUDE;
		}
		else if(comboAttributoFilter.getSelectedItem().equals("Length major axes")){ //Width
			return Attribute.MOMENT_LENGTH_MAJOR_AXES;
		}
		else
			return -1;
	}
	
	public int getAttributeValue(){
		int attributeValue = 0;
		if(comboAttributoFilter.getSelectedItem().equals("Area")){ //area
			attributeValue = (int) ((attributeValueFilter.getValue() / 100.0) * imgCurrent.getSize());
			attributeValue = (int) Math.floor(Math.pow(imgCurrent.getSize(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
		}
		else if(comboAttributoFilter.getSelectedItem().equals("Volume")){ //volume
			attributeValue = (int) ((attributeValueFilter.getValue() / 100.0) * imgCurrent.getSize() * 10);
			attributeValue = (int) Math.floor(Math.pow(imgCurrent.getSize(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
		}
		else if(comboAttributoFilter.getSelectedItem().equals("Height")){ //Height
			attributeValue = (int) ((attributeValueFilter.getValue() / 100.0) * imgCurrent.getHeight());
			attributeValue = (int) Math.floor(Math.pow(imgCurrent.getHeight(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
		}
		else if(comboAttributoFilter.getSelectedItem().equals("Width")){ //Width
			attributeValue = (int) ((attributeValueFilter.getValue() / 100.0) * imgCurrent.getWidth());
			attributeValue = (int) Math.floor(Math.pow(imgCurrent.getWidth(), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
		}
		else if(comboAttributoFilter.getSelectedItem().equals("Altitude")){ //Width
			attributeValue = (int) ((attributeValueFilter.getValue() / 100.0) * Math.max(imgCurrent.getWidth(), imgCurrent.getHeight()));
			attributeValue = (int) Math.floor(Math.pow(Math.max(imgCurrent.getWidth(), imgCurrent.getHeight()), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
		}
		else if(comboAttributoFilter.getSelectedItem().equals("Length major axes")){ //Width
			attributeValue = (int) ((attributeValueFilter.getValue() / 100.0) * Math.max(imgCurrent.getWidth(), imgCurrent.getHeight()));
			attributeValue = (int) Math.floor(Math.pow(Math.max(imgCurrent.getWidth(), imgCurrent.getHeight()), 1-2) * Math.pow(attributeValue, 2)); //funcao potencia
		}
		
		return attributeValue;
	}
	
	
	public void applyFilter(boolean process){
		int typeRec = MorphologicalTreeFiltering.RULE_DIRECT;
		int attributeValue = getAttributeValue();
		if(process){
			lastAttributeValue = attributeValue;
			lastAttributeType = getAttributeType();
			lastPruning = getPruningType();
			lastTypeRec = typeRec;
			filteringProcessing();
			
		}
		this.limiarLabelFilter.setText("attribute value: " + attributeValue);
		
	}
	
	private int lastAttributeValue; 
	private int lastAttributeType;
	private int lastPruning;
	private int lastTypeRec;
	 
	public InfoPrunedTree processPrunedTree(){
		InfoPrunedTree prunedTree = null;
		ConnectedFilteringByComponentTree ct = (ConnectedFilteringByComponentTree)tree;
		if(lastPruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE){
			prunedTree = ct.getPrunedTreeByExtinctionValue(lastAttributeValue, lastAttributeType);
		}
		else if(lastPruning == MorphologicalTreeFiltering.PRUNING_MSER){
			prunedTree = ct.getPrunedTreeByMSER(lastAttributeValue, lastAttributeType, deltaMSER);
		}
		else if(lastPruning == MorphologicalTreeFiltering.PRUNING_GRADUAL_TRANSITION){
			prunedTree = ct.getPrunedTreeByGradualTransition(lastAttributeValue, lastAttributeType, gradualTransition);
		}
		else if(lastPruning == MorphologicalTreeFiltering.PRUNING_TBMR){
			int tMin = 100;
			int tMax = (int) (tree.getInputImage().getSize() * 0.80);
			prunedTree = ct.getPrunedTreeByTBMR(lastAttributeValue, lastAttributeType, tMin, tMax);
		}
		else {
			prunedTree = ct.getPrunedTree(lastAttributeValue, lastAttributeType);
		}
		return prunedTree;
	}
	
	public void filteringProcessing(){
		if(tree instanceof ConnectedFilteringByComponentTree){
			ConnectedFilteringByComponentTree ct = (ConnectedFilteringByComponentTree)tree;
			prunedTree = processPrunedTree();
			if(ct.isMaxtree()){
				this.numRegionExtremaLabelFilter.setText("num regional maxima: " + prunedTree.getNumLeaves());
				this.numCCsLabelFilter.setText("num CCs: " + prunedTree.getNunNode() + " (upper set)");
			}else{
				this.numRegionExtremaLabelFilter.setText("num regional minima: " + prunedTree.getNumLeaves());
				this.numCCsLabelFilter.setText("num CCs: " +  prunedTree.getNunNode() + " (lower set)");
			}
			imgCurrent = prunedTree.reconstruction();
			this.numFlatZoneLabelFilter.setText("num flatzone: " + Labeling.getNumFlatzone(imgCurrent, ct.getAdjacency()));
		}
		else{
			ConnectedFilteringByTreeOfShape tos = (ConnectedFilteringByTreeOfShape) tree;
			prunedTree = tos.getPrunedTree(lastAttributeValue, lastAttributeType, lastPruning);
			this.numRegionExtremaLabelFilter.setText("num regional extrema: " + prunedTree.getNumLeaves());
			this.numCCsLabelFilter.setText("num CCs: " + prunedTree.getNunNode() + " (lower/upper set)");
			imgCurrent = prunedTree.reconstruction();
			this.numFlatZoneLabelFilter.setText("num flatzone: " + Labeling.getNumFlatzone(imgCurrent, AdjacencyRelation.getAdjacency8()));
		}
		if(chkResidues.isSelected())
			updateImage( ImageAlgebra.subtractionAbs(imgInput, imgCurrent) );
		else
			updateImage(imgCurrent);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == comboTreeFilter){ //alterando a arvore da filtragem
			changeTreeFiltering();
		}
		else if(event.getSource() == applyButtonFilter){
			if(tree instanceof ComponentTree){
				if(lastPruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE){
					boolean selected[] = new ComputerExtinctionValueComponentTree((ComponentTree) tree).getExtinctionValueNodeCT(lastAttributeType, prunedTree);
					VisualizationComponentTree.getInstance( prunedTree, selected, null ).setVisible(true);
				}
				else if(lastPruning == MorphologicalTreeFiltering.PRUNING_MSER){
					//boolean selected[] = new MserCT((ComponentTree) tree).getMappingNodesByMSER(deltaMSER, prunedTree);
					ComputerTbmrComponentTree tbmr = new ComputerTbmrComponentTree((ComponentTree)tree); 
					boolean selected[] = tbmr.getSelectedNode(100, 9999999);
					VisualizationComponentTree.getInstance( prunedTree, selected, null ).setVisible(true);
				}
				else if(lastPruning == MorphologicalTreeFiltering.PRUNING_GRADUAL_TRANSITION){
					PruningBasedGradualTransition gt = new PruningBasedGradualTransition(tree, this.lastAttributeType, gradualTransition);
					boolean selected[] = gt.getMappingSelectedNodes( );
					VisualizationComponentTree.getInstance( prunedTree, selected, null ).setVisible(true);
				}
				else if(lastPruning == MorphologicalTreeFiltering.PRUNING_TBMR){
					ComputerTbmrComponentTree tbmr = new ComputerTbmrComponentTree((ComponentTree)tree);
					int tMin = 100;
					int tMax = (int) (tree.getInputImage().getSize() * 0.80);
					boolean selected[] = tbmr.getSelectedNode(tMin, tMax);
					VisualizationComponentTree.getInstance( prunedTree, selected, null ).setVisible(true);
				}
				else{
					PruningBasedAttribute gt = new PruningBasedAttribute(tree, this.lastAttributeType);
					boolean selected[] = gt.getMappingSelectedNodes( );
					VisualizationComponentTree.getInstance( prunedTree, selected, null ).setVisible(true);
				}
				
			}
			else{
				if(lastPruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE){
					boolean selected[] = new ComputerExtinctionValueTreeOfShapes((TreeOfShape) tree).getExtinctionValueNode(lastAttributeType, prunedTree);
					VisualizationTreeOfShape.getInstance( prunedTree, selected, null ).setVisible(true);
				}
				else if(lastPruning == MorphologicalTreeFiltering.PRUNING_MSER){
					boolean selected[] = new ComputerMserTreeOfShapes((TreeOfShape) tree).getMappingNodesByMSER(deltaMSER, prunedTree);
					VisualizationTreeOfShape.getInstance( prunedTree, selected, null ).setVisible(true);
				}
				else{
					VisualizationTreeOfShape.getInstance( prunedTree ).setVisible(true);
				}

				
			}
		}
		else if(event.getSource() == reloadButtonFilter){
			chkLabeling.setSelected(false);
			attributeValueFilter.setValue(0);
			updateImage(this.imgInput);
			changeTreeFiltering();
		}
		else if(event.getSource() == chkResidues){
			if(chkResidues.isSelected())
				updateImage( ImageAlgebra.subtractionAbs(imgInput, imgCurrent) );
			else
				updateImage( imgCurrent );
		}
		else if(event.getSource() == chkLabeling){
			updateImage( imgCurrent );
		}
		else if(event.getSource() == comboPruningFilter){
			if(comboPruningFilter.getSelectedItem().equals("MSER")){
				 GenericDialog gd = new GenericDialog("MSER");
				 gd.addMessage("Maximally Stable Extremal Regions");
				 gd.addNumericField("Thresholding (delta)", 10, 0);
				 gd.showDialog();
				 deltaMSER = 10;
				 if (!gd.wasCanceled()){
					 deltaMSER = (int) gd.getNextNumber();
				 }	
			}
			else if(comboPruningFilter.getSelectedItem().equals("Gradual transition")){
				 GenericDialog gd = new GenericDialog("Gradual transition");
				 gd.addMessage("Gradual transition");
				 gd.addNumericField("Thresholding (delta)", 5, 0);
				 gd.showDialog();
				 gradualTransition = 5;
				 if (!gd.wasCanceled()){
					 gradualTransition = (int) gd.getNextNumber();
				 }	
			}
			applyFilter(true);
		}
		
	}
	
	public void changeTreeFiltering(){
		if(comboTreeFilter.getSelectedItem().equals("Attribute Opening"))
			tree = new ConnectedFilteringByComponentTree(imgCurrent, adj8, true);
		else if(comboTreeFilter.getSelectedItem().equals("Attribute Closing"))
			tree = new ConnectedFilteringByComponentTree(imgCurrent, adj8, false);
		else if(comboTreeFilter.getSelectedItem().equals("Grain Filter")){
			 GenericDialog gd = new GenericDialog("Tree of shape");
			 gd.addNumericField("point infinity (axis x)", -1, 0);
			 gd.addNumericField("point infinity (axis y)", -1, 0);
			 gd.showDialog();
			 
			 pInfX = pInfY = -1;
			 if (!gd.wasCanceled()){
				 pInfX = (int) gd.getNextNumber();
				 pInfY = (int) gd.getNextNumber();
			 }
			
			tree = new ConnectedFilteringByTreeOfShape(imgCurrent, pInfX, pInfY);
		}
		
		applyFilter(true);
		attributeValueFilter.setValue(0);
	}

	
	@Override
	public void stateChanged(ChangeEvent event) {
		if(event.getSource() == attributeValueFilter){
			if(chkInteractive.isSelected()){
				applyFilter(attributeValueFilter.getValueIsAdjusting());
			}
			else{
				applyFilter(!attributeValueFilter.getValueIsAdjusting());
			}
			
		}
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		imgPlus.getCanvas().removeMouseListener(this);
		this.setVisible(false);		
		this.setEnabled(false);
	}

	
	@Override
	public void windowOpened(WindowEvent e) {	}
	@Override
	public void windowClosing(WindowEvent e) {	
        super.windowClosing(e);
	}
	@Override
	public void windowIconified(WindowEvent e) {	}
	@Override
	public void windowDeiconified(WindowEvent e) {	}
	@Override
	public void windowActivated(WindowEvent e) {	}
	@Override
	public void windowDeactivated(WindowEvent e) {	}
		
}
