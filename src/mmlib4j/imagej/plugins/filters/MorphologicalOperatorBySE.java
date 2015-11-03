package mmlib4j.imagej.plugins.filters;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.frame.PlugInFrame;
import ij.process.ByteProcessor;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.filtering.LinearFilters;
import mmlib4j.filtering.MorphologicalOperatorsBasedOnSE;
import mmlib4j.filtering.ToggleMapping;
import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ReconstructionMorphological;
import mmlib4j.segmentation.Labeling;
import mmlib4j.utils.AdjacencyRelation;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class MorphologicalOperatorBySE  extends PlugInFrame implements ActionListener, ChangeListener {
	
	private static final long serialVersionUID = -6046850754334555050L;
	private GrayScaleImage imgInput;
	private GrayScaleImage imgCurrent;
	
	private AdjacencyRelation adj8 = AdjacencyRelation.getCircular(1.5);
	
	private ComponentTree mintree;
	private ComponentTree maxtree;
	private ReconstructionMorphological recMorph;
	private ImagePlus imgPlus;
	
	public MorphologicalOperatorBySE(ImagePlus plus) {
		super("Morphological operators");
		
		this.imgPlus = plus;
		this.imgInput = ImageJAdapter.toGrayScaleImage( (ByteProcessor) plus.getProcessor());
		imgCurrent = imgInput.duplicate();
		this.maxtree = new ComponentTree(imgInput, adj8, true);
		this.mintree = new ComponentTree(imgInput, adj8, false);
		this.recMorph = new ReconstructionMorphological(imgInput, mintree, maxtree);
		
		super.add(createPanelFiltering());
		super.setSize(280, 500);
		super.setLocationRelativeTo( IJ.getInstance() );
		super.setVisible(true);	
		
	}
	
	
	
	public void updateImage(GrayScaleImage img){
		this.imgCurrent= img;
		if(isLabel.isSelected()){
			imgPlus.setProcessor( ImageJAdapter.toColorProcessor( Labeling.labeling(imgCurrent, adj8).randomColor() ) );
		}else{
			imgPlus.setProcessor( ImageJAdapter.toByteProcessor(imgCurrent) );
		}
		
	}
	
	public void windowClosing(WindowEvent e){
		updateImage(imgInput);
		close();
	}
	
	
	public void updateImage(ColorImage img){
		imgPlus.setProcessor( ImageJAdapter.toColorProcessor( img ) );	
	}
	
	@Override
	public void stateChanged(ChangeEvent event) {
		stateChangedFilter(event);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		actionPerformedFilter(event);
	}	

		
/**
 ********************************************************************************	
 ********************************FILTERING***************************************
 ********************************************************************************
 */
	private JComboBox comboSE;
	private JComboBox comboMorphologicalOperator;
	private JButton reloadButtonFilter;
	private JButton applyButtonFilter;
	private JSlider attributeValueFilter;
	private JLabel limiarLabelFilter;
	private JCheckBox isReconstruction;
	private JCheckBox isLabel;
	private JCheckBox chkInteractive;
	
	public JPanel createPanelFiltering( ){
		JPanel appPanelFiltering = new JPanel(new GridLayout(8, 1, 0, 0));
		
		//1
		comboMorphologicalOperator = new JComboBox();
		comboMorphologicalOperator.setBorder(BorderFactory.createTitledBorder("Morphological Operator"));
		comboMorphologicalOperator.addItem("Erosion");
		comboMorphologicalOperator.addItem("Dilation");
		comboMorphologicalOperator.addItem("Opening");
		comboMorphologicalOperator.addItem("Closing");
		comboMorphologicalOperator.addItem("Black tophat");
		comboMorphologicalOperator.addItem("White tophat");
		comboMorphologicalOperator.addItem("ASF closing-opening");
		comboMorphologicalOperator.addItem("ASF opening-closing");
		comboMorphologicalOperator.addItem("Median");
		comboMorphologicalOperator.addItem("Mean");
		comboMorphologicalOperator.addItem("Add (-)");
		comboMorphologicalOperator.addItem("Add (+)");
		comboMorphologicalOperator.addItem("Toggle mapping");
		comboMorphologicalOperator.addItem("Toggle mapping residue");
		comboMorphologicalOperator.addItem("Gradient");
		comboMorphologicalOperator.addItem("Gradient (internal)");
		comboMorphologicalOperator.addItem("Gradient (external)");
		
		comboMorphologicalOperator.addActionListener(this);
		
		//2
		comboSE = new JComboBox();
		comboSE.setBorder(BorderFactory.createTitledBorder("SE type"));
		comboSE.addItem("Square");
		comboSE.addItem("Disk");
		comboSE.addItem("Vertical line");
		comboSE.addItem("Horizontal line");
		comboSE.setSelectedIndex(1);
		
		
		//4
		attributeValueFilter = new JSlider(JSlider.HORIZONTAL, 0,	100, 0);
		attributeValueFilter.setValue(0);
		attributeValueFilter.setBorder(BorderFactory.createTitledBorder("value SE"));
		attributeValueFilter.setMajorTickSpacing(5);
		
		attributeValueFilter.setPaintTicks(true);
		attributeValueFilter.addChangeListener(this);
		

		limiarLabelFilter = new JLabel("value: 0");
		isReconstruction = new JCheckBox("Morphological Reconstruction?");
		isLabel = new JCheckBox("Labeling?");
		chkInteractive = new JCheckBox("Interactive update");
		
		appPanelFiltering.add(comboMorphologicalOperator);
		appPanelFiltering.add(comboSE);
		appPanelFiltering.add(attributeValueFilter);
		appPanelFiltering.add(limiarLabelFilter);
		appPanelFiltering.add(isReconstruction);
		appPanelFiltering.add(isLabel);
		appPanelFiltering.add(chkInteractive);
		
		isReconstruction.addActionListener(this);
		isLabel.addActionListener(this);
		chkInteractive.addActionListener(this);
		
		//5
		reloadButtonFilter = new JButton("Reset");
		applyButtonFilter = new JButton("Apply");
		
		reloadButtonFilter.addActionListener(this);
		applyButtonFilter.addActionListener(this);
		
		
		JPanel panelButtons = new JPanel(new GridLayout(1, 1, 0, 0));
		panelButtons.add(reloadButtonFilter);
		panelButtons.add(applyButtonFilter);
		
		appPanelFiltering.add(panelButtons);
		return appPanelFiltering;
	}

	
	public void initFilter(){
		comboSE.setSelectedIndex(1);
		comboMorphologicalOperator.setSelectedIndex(0);
		attributeValueFilter.setValue(0);
		imgCurrent = imgInput.duplicate();
		updateImage(imgInput);
	}
	
	
	boolean processFinalize = false;
	public void applyFilter(){
		if(processFinalize)
			return;
		processFinalize = true;
		double attributeValue = 0;
		attributeValue = 1 + (attributeValueFilter.getValue() / 2.0);
		AdjacencyRelation adj = null;
		
		if(comboSE.getSelectedItem().equals("Square")){ 
			adj = AdjacencyRelation.getBox((int)attributeValue, (int)attributeValue);
		}
		else if(comboSE.getSelectedItem().equals("Disk")){
			adj = AdjacencyRelation.getCircular(attributeValue);
		}
		else if(comboSE.getSelectedItem().equals("Vertical line")){
			adj = AdjacencyRelation.getBox(1, (int)attributeValue);
		}
		else if(comboSE.getSelectedItem().equals("Horizontal line")){
			adj = AdjacencyRelation.getBox((int)attributeValue, 1);
		}
		
		GrayScaleImage imgOut = null;
		
		if(comboMorphologicalOperator.getSelectedItem().equals("Erosion")){
			imgOut = MorphologicalOperatorsBasedOnSE.erosion(imgInput, adj);

			if(isReconstruction.isSelected())
				imgOut = recMorph.reconstructionByDilation(imgOut);
				//imgOut = maxtree.reconstructionMorphological(imgOut);
		}
		else if(comboMorphologicalOperator.getSelectedItem().equals("Dilation")){
			imgOut = MorphologicalOperatorsBasedOnSE.dilation(imgInput, adj);
			if(isReconstruction.isSelected())
				imgOut = recMorph.reconstructionByErosion(imgOut); 
				//mintree.reconstructionMorphological(imgOut);
		}

		else if(comboMorphologicalOperator.getSelectedItem().equals("Median")){
			imgOut = LinearFilters.median(imgInput, adj);
			
			if(isReconstruction.isSelected())
				imgOut = recMorph.selfReconstruction(imgOut);
		}
		else if(comboMorphologicalOperator.getSelectedItem().equals("Mean")){
			imgOut = LinearFilters.mean(imgInput, adj);
			
			if(isReconstruction.isSelected())
				imgOut = recMorph.selfReconstruction(imgOut);
		}
		else if(comboMorphologicalOperator.getSelectedItem().equals("Add (-)")){
			imgOut = imgInput.duplicate();
			imgOut.add((int)-attributeValue*2);
			if(isReconstruction.isSelected())
				imgOut = recMorph.reconstructionByDilation(imgOut);
		}
		else if(comboMorphologicalOperator.getSelectedItem().equals("Add (+)")){
			imgOut = imgInput.duplicate();
			imgOut.add((int)attributeValue*2);
			
			if(isReconstruction.isSelected())
				imgOut = recMorph.reconstructionByErosion(imgOut);
		}
		
		
		else if(comboMorphologicalOperator.getSelectedItem().equals("ASF closing-opening")){
			SimpleLinkedList<AdjacencyRelation> familyAdj = null;
			if(comboSE.getSelectedItem().equals("Square")){ 
				familyAdj = AdjacencyRelation.getFamilyBox(1, (int)attributeValue, 1, (int)attributeValue, 1, 1);
			}
			else if(comboSE.getSelectedItem().equals("Disk")){
				familyAdj = AdjacencyRelation.getFamilyCircular(0.5, attributeValue, 0.5);
			}
			else if(comboSE.getSelectedItem().equals("Vertical line")){
				familyAdj = AdjacencyRelation.getFamilyVertical(1, (int)attributeValue, 1);
			}
			else if(comboSE.getSelectedItem().equals("Horizontal line")){
				familyAdj = AdjacencyRelation.getFamilyHorizontal(1, (int)attributeValue, 1);
			}
			
			imgOut = MorphologicalOperatorsBasedOnSE.asfCloseOpen(imgInput, familyAdj);
			
			if(isReconstruction.isSelected())
				imgOut = recMorph.selfReconstruction(imgOut);
		}
		else if(comboMorphologicalOperator.getSelectedItem().equals("ASF opening-closing")){
			SimpleLinkedList<AdjacencyRelation> familyAdj = null;
			if(comboSE.getSelectedItem().equals("Square")){ 
				familyAdj = AdjacencyRelation.getFamilyBox(1, (int)attributeValue, 1, (int)attributeValue, 1, 1);
			}
			else if(comboSE.getSelectedItem().equals("Disk")){
				familyAdj = AdjacencyRelation.getFamilyCircular(0.5, attributeValue, 0.5);
			}
			else if(comboSE.getSelectedItem().equals("Vertical line")){
				familyAdj = AdjacencyRelation.getFamilyVertical(1, (int)attributeValue, 1);
			}
			else if(comboSE.getSelectedItem().equals("Horizontal line")){
				familyAdj = AdjacencyRelation.getFamilyHorizontal(1, (int)attributeValue, 1);
			}
			
			imgOut = MorphologicalOperatorsBasedOnSE.asfOpenClose(imgInput, familyAdj);
			
			if(isReconstruction.isSelected())
				imgOut = recMorph.selfReconstruction(imgOut);
		}	
		else if(comboMorphologicalOperator.getSelectedItem().equals("Opening")){
			imgOut = MorphologicalOperatorsBasedOnSE.opening(imgInput, adj);
			
			if(isReconstruction.isSelected()){
				imgOut = recMorph.reconstructionByDilation(imgOut);
			}
		}
		else if(comboMorphologicalOperator.getSelectedItem().equals("Closing")){
			imgOut = MorphologicalOperatorsBasedOnSE.closing(imgInput, adj);
			
			if(isReconstruction.isSelected())
				imgOut = recMorph.reconstructionByErosion(imgOut);
		}
		else if(comboMorphologicalOperator.getSelectedItem().equals("Black tophat")){
			imgOut = MorphologicalOperatorsBasedOnSE.closingTopHat(imgInput, adj);
			
			if(isReconstruction.isSelected())
				imgOut = recMorph.selfReconstruction(imgOut);
		}
		else if(comboMorphologicalOperator.getSelectedItem().equals("White tophat")){
			imgOut = MorphologicalOperatorsBasedOnSE.openingTopHat(imgInput, adj);
			
			if(isReconstruction.isSelected())
				imgOut = recMorph.selfReconstruction(imgOut);
			
		}
		else if(comboMorphologicalOperator.getSelectedItem().equals("Toggle mapping")){
			imgOut = ToggleMapping.toggleMapping(imgInput, adj);
			if(isReconstruction.isSelected())
				imgOut = recMorph.selfReconstruction(imgOut);
			
			
		}
		else if(comboMorphologicalOperator.getSelectedItem().equals("Toggle mapping residue")){
			imgOut = ToggleMapping.toggleMappingResidue(imgInput, adj);
			if(isReconstruction.isSelected())
				imgOut = recMorph.selfReconstruction(imgOut);
		}	
		else if(comboMorphologicalOperator.getSelectedItem().equals("Gradient")){
			imgOut = MorphologicalOperatorsBasedOnSE.gradient(imgInput, adj);
			if(isReconstruction.isSelected())
				imgOut = recMorph.selfReconstruction(imgOut);
		}	
		else if(comboMorphologicalOperator.getSelectedItem().equals("Gradient (internal)")){
			imgOut = MorphologicalOperatorsBasedOnSE.gradientInternal(imgInput, adj);
			if(isReconstruction.isSelected())
				imgOut = recMorph.selfReconstruction(imgOut);
		}
		else if(comboMorphologicalOperator.getSelectedItem().equals("Gradient (external)")){
			imgOut = MorphologicalOperatorsBasedOnSE.gradientExternal(imgInput, adj);
			if(isReconstruction.isSelected())
				imgOut = recMorph.selfReconstruction(imgOut);
		}

		updateImage(imgOut);	
		limiarLabelFilter.setText("value: " + attributeValue);
		processFinalize = false;
	}
	
	public void actionPerformedFilter(ActionEvent event) {
		if(event.getSource() == comboMorphologicalOperator){ //alterando a arvore da filtragem
			changeTreeFiltering();
		}
		else if(event.getSource() == isReconstruction){
			applyFilter();
		}
		else if(event.getSource() == applyButtonFilter){
			super.close();
			/*this.imgInput = imgCurrent.duplicate();
			changeTreeFiltering();
			this.maxtree = new ComponentTree(imgInput, adj8, true);
			this.mintree = new ComponentTree(imgInput, adj8, false);
			*/
			
		}
		else if(event.getSource() == reloadButtonFilter){
			isLabel.setSelected(false);
			attributeValueFilter.setValue(0);
			updateImage(this.imgInput);
		}
		else if(event.getSource() == isLabel){
			if(isLabel.isSelected())
				updateImage( Labeling.labeling(imgCurrent, adj8).randomColor() );
			else
				updateImage( imgCurrent );
		}
		
		
	}
	
	public void changeTreeFiltering(){
		attributeValueFilter.setValue(0);
	}

	public void stateChangedFilter(ChangeEvent event) {
		if(event.getSource() == attributeValueFilter){
			
			if(chkInteractive.isSelected()){
				applyFilter();
			}
			else{
				if(!attributeValueFilter.getValueIsAdjusting())
					applyFilter();
				else{
					double attributeValue = 1 + (attributeValueFilter.getValue() / 2.0);
					limiarLabelFilter.setText("value: " + attributeValue);
				}
					
				
			}
			
			
		}
		
	}

	
}
