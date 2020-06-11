package mmlib4j.imagej.plugins.filters;

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

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.plugin.frame.PlugInFrame;
import ij.process.ByteProcessor;
import mmlib4j.filtering.AttributeFilters;
import mmlib4j.imagej.guj.HistogramOfBranch;
import mmlib4j.imagej.guj.VisualizationComponentTree;
import mmlib4j.imagej.plugins.residual.UltimateLevelings;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.InfoTree;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedAttribute;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedExtinctionValue;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedGradualTransition;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedMSER;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedTBMR;
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
	private MorphologicalTree tree;
	private InfoTree prunedTree;
	
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
		
		tree = new ComponentTree(imgInput, adj8, true);
		
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
		comboAttributoFilter.addItem("Altitude");
		comboAttributoFilter.addItem("Height");
		comboAttributoFilter.addItem("Width");
		comboAttributoFilter.addItem("Level");
		comboAttributoFilter.addItem("Rectangularity");
		comboAttributoFilter.addItem("Ratio with/height");
		comboAttributoFilter.addItem("Variance level");
		comboAttributoFilter.addItem("Average level");
		comboAttributoFilter.addItem("STD level");
		comboAttributoFilter.addItem("Moment - compactness");
		comboAttributoFilter.addItem("Moment - eccentricity");
		comboAttributoFilter.addItem("Moment - elongation");
		comboAttributoFilter.addItem("Moment - lenght major axes");
		comboAttributoFilter.addItem("Moment - lenght minor axes");
		comboAttributoFilter.addItem("Moment - orientation");
		comboAttributoFilter.addItem("Moment - aspect ratio");
		comboAttributoFilter.addItem("Moment of inertia");
		comboAttributoFilter.addItem("Perimeter external");
		comboAttributoFilter.addItem("Circularity");
		comboAttributoFilter.addItem("Compactness");
		comboAttributoFilter.addItem("Elongation");
		comboAttributoFilter.addItem("Sum gradient of contour");
		comboAttributoFilter.addItem("Functional attribute");
		comboAttributoFilter.addItem("Bitquads - perimeter");
		comboAttributoFilter.addItem("Bitquads - euler number");
		comboAttributoFilter.addItem("Bitquads - hole number");
		comboAttributoFilter.addItem("Bitquads - perimeter continuous");
		comboAttributoFilter.addItem("Bitquads - circularity");
		comboAttributoFilter.addItem("Bitquads - average area");
		comboAttributoFilter.addItem("Bitquads - average perimeter");
		comboAttributoFilter.addItem("Bitquads - average lenght");
		comboAttributoFilter.addItem("Bitquads - average width");
		
		
		//3
		comboPruningFilter = new JComboBox();
		comboPruningFilter.setBorder(BorderFactory.createTitledBorder("Pruning strategy"));
		
		comboPruningFilter.addItem("Pruning Min");
		comboPruningFilter.addItem("Pruning Max");
		comboPruningFilter.addItem("Pruning Vertebi");
		
		comboPruningFilter.addItem("Direct rule");
		comboPruningFilter.addItem("Subtractive rule");
		
		comboPruningFilter.addItem("Extinction Value");
		comboPruningFilter.addItem("MSER");
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
		winHist.run( (InfoPrunedTree) prunedTree, getPruningType(), deltaMSER);
		
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
			return UltimateLevelings.PRUNING_EXTINCTION_VALUE;
		}
		else if(comboPruningFilter.getSelectedItem().equals("MSER")){
			return UltimateLevelings.PRUNING_MSER;
		}
		else if(comboPruningFilter.getSelectedItem().equals("TBMR")){
			return UltimateLevelings.PRUNING_TBMR;
		}
		else if(comboPruningFilter.getSelectedItem().equals("Gradual transition")){
			return UltimateLevelings.PRUNING_GRADUAL_TRANSITION;
		}
		else if(comboPruningFilter.getSelectedItem().equals("Pruning Min")){
			return AttributeFilters.PRUNING_MIN;
		}
		else if(comboPruningFilter.getSelectedItem().equals("Pruning Max")){
			return AttributeFilters.PRUNING_MAX;
		}
		else if(comboPruningFilter.getSelectedItem().equals("Pruning Vertebi")){
			return AttributeFilters.PRUNING_VITERBI;
		}
		else if(comboPruningFilter.getSelectedItem().equals("Direct rule")){
			return AttributeFilters.DIRECT_RULE;
		}
		else if(comboPruningFilter.getSelectedItem().equals("Subtractive rule")){
			return AttributeFilters.SUBTRACTIVE_RULE;
		}else
			return AttributeFilters.PRUNING_MIN;
		
	}
		
	
	public int getAttributeType(){
		if(comboAttributoFilter.getSelectedItem().equals("Area")){ //area
			return Attribute.AREA;
		}else if(comboAttributoFilter.getSelectedItem().equals("Volume")){ //volume
			return Attribute.VOLUME;
		}else if(comboAttributoFilter.getSelectedItem().equals("Height")){ //Height
			return Attribute.HEIGHT;
		}else if(comboAttributoFilter.getSelectedItem().equals("Width")){ //Width
			return Attribute.WIDTH;
		}else if(comboAttributoFilter.getSelectedItem().equals("Altitude")){ //Width
			return Attribute.ALTITUDE;
		}else if(comboAttributoFilter.getSelectedItem().equals("Level")){ //Width
			return Attribute.LEVEL;
		}else if(comboAttributoFilter.getSelectedItem().equals("Rectangularity")){ //Width
			return Attribute.RECTANGULARITY;
		}else if(comboAttributoFilter.getSelectedItem().equals("Ratio with/height")){ //Width
			return Attribute.RATIO_WIDTH_HEIGHT;
		}else if(comboAttributoFilter.getSelectedItem().equals("Variance level")){
			return Attribute.VARIANCE_LEVEL;
		}else if(comboAttributoFilter.getSelectedItem().equals("Average level")){
			return Attribute.LEVEL_MEAN;
		}else if(comboAttributoFilter.getSelectedItem().equals("STD level")){ 
			return Attribute.STD_LEVEL;
		}else if(comboAttributoFilter.getSelectedItem().equals("Moment - compactness")){
			return Attribute.MOMENT_COMPACTNESS;
		}else if(comboAttributoFilter.getSelectedItem().equals("Moment - eccentricity")){
			return Attribute.MOMENT_ECCENTRICITY;
		}else if(comboAttributoFilter.getSelectedItem().equals("Moment - elongation")){ 
			return Attribute.MOMENT_ELONGATION;
		}else if(comboAttributoFilter.getSelectedItem().equals("Moment - lenght major axes")){
			return Attribute.MOMENT_LENGTH_MAJOR_AXES;
		}else if(comboAttributoFilter.getSelectedItem().equals("Moment - lenght minor axes")){
			return Attribute.MOMENT_LENGTH_MINOR_AXES;
		}else if(comboAttributoFilter.getSelectedItem().equals("Moment - orientation")){ 
			return Attribute.MOMENT_ORIENTATION;
		}else if(comboAttributoFilter.getSelectedItem().equals("Moment - aspect ratio")){
			return Attribute.MOMENT_ASPECT_RATIO;
		}else if(comboAttributoFilter.getSelectedItem().equals("Moment of inertia")){
			return Attribute.MOMENT_OF_INERTIA;
		}else if(comboAttributoFilter.getSelectedItem().equals("Bitquads - perimeter")){ 
			return Attribute.BIT_QUADS_PERIMETER;
		}else if(comboAttributoFilter.getSelectedItem().equals("Bitquads - euler number")){
			return Attribute.BIT_QUADS_EULER_NUMBER;
		}else if(comboAttributoFilter.getSelectedItem().equals("Bitquads - hole number")){ 
			return Attribute.BIT_QUADS_HOLE_NUMBER;
		}else if(comboAttributoFilter.getSelectedItem().equals("Bitquads - perimeter continuous")){ 
			return Attribute.BIT_QUADS_PERIMETER_CONTINUOUS;
		}else if(comboAttributoFilter.getSelectedItem().equals("Bitquads - circularity")){ 
			return Attribute.BIT_QUADS_CIRCULARITY;
		}else if(comboAttributoFilter.getSelectedItem().equals("Bitquads - average area")){
			return Attribute.BIT_QUADS_AVERAGE_AREA;
		}else if(comboAttributoFilter.getSelectedItem().equals("Bitquads - average perimeter")){ 
			return Attribute.BIT_QUADS_AVERAGE_PERIMETER;
		}else if(comboAttributoFilter.getSelectedItem().equals("Bitquads - average lenght")){ 
			return Attribute.BIT_QUADS_AVERAGE_LENGTH;
		}else if(comboAttributoFilter.getSelectedItem().equals("Bitquads - average width")){ 
			return Attribute.BIT_QUADS_AVERAGE_WIDTH;
		}else if(comboAttributoFilter.getSelectedItem().equals("Functional attribute")){ //Width
			return Attribute.FUNCTIONAL_ATTRIBUTE;
		}
		else if(comboAttributoFilter.getSelectedItem().equals("Perimeter external")){ //Width
			return Attribute.PERIMETER_EXTERNAL;
		}else if(comboAttributoFilter.getSelectedItem().equals("Circularity")){ //Width
			return Attribute.CIRCULARITY;
		}else if(comboAttributoFilter.getSelectedItem().equals("Compactness")){ //Width
			return Attribute.COMPACTNESS;
		}else if(comboAttributoFilter.getSelectedItem().equals("Elongation")){ //Width
			return Attribute.ELONGATION;
		}else if(comboAttributoFilter.getSelectedItem().equals("Sum gradient of contour")){ //Width
			return Attribute.SUM_GRAD_CONTOUR;
		}
		
		
		else if(comboAttributoFilter.getSelectedItem().equals("Length major axes")){ //Width
			return Attribute.MOMENT_LENGTH_MAJOR_AXES;
		}
		else
			return -1;
	}
	
	public double getAttributeValue(){
		
		double max = Attribute.getMaxValue(tree, getAttributeType());
		double attributeValue =  (attributeValueFilter.getValue() / 100.0) * max ;
		attributeValue = Math.pow(max, 1-2) * Math.pow(attributeValue, 2); //funcao potencia
		/*
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
		*/
		return attributeValue;
	}
	
	
	public void applyFilter(boolean process){
		//int typeRec = AttributeFilters.PRUNING_MIN;
		if(!Attribute.hasAttribute(tree, getAttributeType()))
			Attribute.loadAttribute(tree, getAttributeType());
		double attributeValue = getAttributeValue();
		if(process){
			lastAttributeValue = attributeValue;
			lastAttributeType = getAttributeType();
			lastPruning = getPruningType();
			//lastTypeRec = typeRec;
			filteringProcessing();
			
		}
		this.limiarLabelFilter.setText("attribute value: " + attributeValue);
		
	}
	
	private double lastAttributeValue; 
	private int lastAttributeType;
	private int lastPruning;
	private int lastTypeRec;
	 
	public InfoTree processPrunedTree(){
		InfoTree prunedTree = null;
		
		if(lastPruning == UltimateLevelings.PRUNING_EXTINCTION_VALUE){
			prunedTree =  new PruningBasedExtinctionValue(tree, lastAttributeType).getPrunedTree(lastAttributeValue);
		}
		else if(lastPruning == UltimateLevelings.PRUNING_MSER){
			prunedTree = new PruningBasedMSER(tree, Attribute.AREA, deltaMSER).getPrunedTree(lastAttributeValue);
		}
		else if(lastPruning == UltimateLevelings.PRUNING_GRADUAL_TRANSITION){
			prunedTree = new PruningBasedGradualTransition(tree, lastAttributeType, gradualTransition).getPrunedTree(lastAttributeValue);
			//prunedTree = new AttributeFilters(tree).getPrunedTreeByGradualTransition(lastAttributeValue, lastAttributeType, gradualTransition);
		}
		else if(lastPruning == UltimateLevelings.PRUNING_TBMR){
			int tMin = 100;
			int tMax = (int) (tree.getInputImage().getSize() * 0.80);
			prunedTree = new PruningBasedTBMR(tree, tMin, tMax).getPrunedTree(lastAttributeValue);
		}
		else if(lastPruning == AttributeFilters.DIRECT_RULE){
			prunedTree = new AttributeFilters(tree).getInfoMergedTreeByDirectRule(lastAttributeValue, lastAttributeType);
		}
		else if(lastPruning == AttributeFilters.SUBTRACTIVE_RULE){
			prunedTree = new AttributeFilters(tree).getInfoMergedTreeBySubstractiveRule(lastAttributeValue, lastAttributeType);
		}
		else {
			prunedTree = new AttributeFilters(tree).getInfoPrunedTree(lastAttributeValue, lastAttributeType, lastPruning);
		}
		return prunedTree;
	}
	
	public void filteringProcessing(){
		if(tree instanceof ComponentTree){
			ComponentTree ct = (ComponentTree)tree;
			prunedTree = processPrunedTree();
			if(ct.isMaxtree()){
				this.numRegionExtremaLabelFilter.setText("num regional maxima: " + prunedTree.getNumLeaves());
				this.numCCsLabelFilter.setText("num CCs: " + prunedTree.getNumNode() + " (upper set)");
			}else{
				this.numRegionExtremaLabelFilter.setText("num regional minima: " + prunedTree.getNumLeaves());
				this.numCCsLabelFilter.setText("num CCs: " +  prunedTree.getNumNode() + " (lower set)");
			}
			imgCurrent = prunedTree.reconstruction();
			this.numFlatZoneLabelFilter.setText("num flatzone: " + Labeling.getNumFlatzone(imgCurrent, ct.getAdjacency()));
		}
		else{
			prunedTree = processPrunedTree();
			//prunedTree = new AttributeFilters(tree).getInfoPrunedTree(lastAttributeValue, lastAttributeType, AttributeFilters.PRUNING_MIN);
			//prunedTree = tos.getPrunedTree(lastAttributeValue, lastAttributeType, lastPruning);
			this.numRegionExtremaLabelFilter.setText("num regional extrema: " + prunedTree.getNumLeaves());
			this.numCCsLabelFilter.setText("num CCs: " + prunedTree.getNumNode() + " (lower/upper set)");
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
			if(lastPruning == UltimateLevelings.PRUNING_EXTINCTION_VALUE){
				boolean selected[] = new PruningBasedExtinctionValue(tree, lastAttributeType).getMappingSelectedNodes();
				VisualizationComponentTree.getInstance( prunedTree, selected, null ).setVisible(true);
			}
			else if(lastPruning == UltimateLevelings.PRUNING_MSER){
				//boolean selected[] = new MserCT((ComponentTree) tree).getMappingNodesByMSER(deltaMSER, prunedTree);
				PruningBasedMSER mser = new PruningBasedMSER(tree, Attribute.AREA, deltaMSER); 
				boolean selected[] = mser.getMappingSelectedNodes();
				VisualizationComponentTree.getInstance( prunedTree, selected, null ).setVisible(true);
			}
			else if(lastPruning == UltimateLevelings.PRUNING_GRADUAL_TRANSITION){
				PruningBasedGradualTransition gt = new PruningBasedGradualTransition(tree, this.lastAttributeType, gradualTransition);
				boolean selected[] = gt.getMappingSelectedNodes( );
				VisualizationComponentTree.getInstance( prunedTree, selected, null ).setVisible(true);
			}
			else if(lastPruning == UltimateLevelings.PRUNING_TBMR){
				int tMin = 100;
				int tMax = (int) (tree.getInputImage().getSize() * 0.80);
				PruningBasedTBMR tbmr = new PruningBasedTBMR(tree, tMin, tMax);
				boolean selected[] = tbmr.getMappingSelectedNodes();
				VisualizationComponentTree.getInstance( prunedTree, selected, null ).setVisible(true);
			}
			else{
				PruningBasedAttribute gt = new PruningBasedAttribute(tree, this.lastAttributeType, (int)lastAttributeValue);
				boolean selected[] = gt.getMappingSelectedNodes( );
					
				VisualizationComponentTree.getInstance( prunedTree, selected, null ).setVisible(true);
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
			tree = new ComponentTree(imgCurrent, adj8, true);
		else if(comboTreeFilter.getSelectedItem().equals("Attribute Closing"))
			tree = new ComponentTree(imgCurrent, adj8, false);
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
			
			tree = new TreeOfShape(imgCurrent, pInfX, pInfY);
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
