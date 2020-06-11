package mmlib4j.imagej.guj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.Paint;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import ij.measure.ResultsTable;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.InfoTree;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.ImageBuilder;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class VisualizationMorphologicalTree extends JPanel {

	DelegateTree<NodeLevelSets, Integer> graph;
	VisualizationViewer<NodeLevelSets, Integer> vv;
	TreeLayout<NodeLevelSets, Integer> treeLayout;
	NodeLevelSets root;
	BufferedImage img;
	int typeOfTree;
	private int id = 1;
	
	public VisualizationMorphologicalTree(final MorphologicalTree tree, boolean map1[], boolean map2[]) {
		super.setLayout(new BorderLayout());
		this.root = tree.getRoot();
		this.typeOfTree = tree.getTypeOfTree();
		this.img = ImageBuilder.convertToImage(tree.getInputImage());
		
		graph = new DelegateTree<NodeLevelSets, Integer>();

		graph.setRoot(tree.getRoot());
		createTree(tree.getRoot());
		
		
		
		treeLayout = new TreeLayout<NodeLevelSets, Integer>(graph);
		vv = new VisualizationViewer<NodeLevelSets, Integer>(treeLayout);
		vv.setBackground(Color.white);
		// vv.getRenderContext().setEdgeDrawPaintTransformer(edgeDrawPaintTransformer);
		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
		vv.getRenderContext().setVertexLabelTransformer(new LabelNodes());
		vv.getRenderContext().setVertexFillPaintTransformer(new NodeShape(map1, map2));
		// add a listener for ToolTips
		vv.setVertexToolTipTransformer(new LabelNodes());
		vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
		
		// Container content = getContentPane();

		final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(graphMouse);
		final PopupMenu popup = new PopupMenu("Options");
		final MenuItem menu1 = new MenuItem("Show image connected component");
		final MenuItem menu2 = new MenuItem("Extraction attribute");
		popup.add(menu1);
		popup.add(menu2);
		vv.add(popup);
		
		class GraphMouseListener2  implements GraphMouseListener<NodeLevelSets>, ActionListener{
			NodeLevelSets v;
			ResultsTable rt = new ResultsTable();
			public void graphReleased(NodeLevelSets v, MouseEvent me) {}
			public void graphPressed(NodeLevelSets v, MouseEvent me) {}
			public void graphClicked(NodeLevelSets v, MouseEvent me) {
				this.v = v;	
				if(me.getButton() == MouseEvent.BUTTON3){
					popup.show(vv, me.getX(), me.getY());
				}else{
					WindowImages.show(v.createImageSC(128), "Level: "+ v.getLevel());
				}
			}
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == menu1){
					WindowImages.show(v.createImageSC(128), "Level: "+ v.getLevel());
				}
				else if(e.getSource() == menu2){
					addTable(rt, v);
					rt.show("Attribute");
				}
			}
		}
		GraphMouseListener2 ml = new GraphMouseListener2();
		vv.addGraphMouseListener(ml);
		menu1.addActionListener(ml);
		menu2.addActionListener(ml);
		
		final ScalingControl scaler = new CrossoverScalingControl();

		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1.1f, vv.getCenter());
			}
		});
		JButton minus = new JButton("-");
		minus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1 / 1.1f, vv.getCenter());
			}
		});

		JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
		scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
		scaleGrid.add(plus);
		scaleGrid.add(minus);

		final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);

		super.add(panel, BorderLayout.CENTER);
		super.add(scaleGrid, BorderLayout.SOUTH);

	}
	
	public void addTable(ResultsTable rt, NodeLevelSets v){
		rt.incrementCounter();
		rt.addValue("PIXEL_CANONICAL", v.getCanonicalPixel());
		if(v.hasAttribute(Attribute.AREA)) rt.addValue("AREA", v.getAttributeValue(Attribute.AREA));
		if(v.hasAttribute(Attribute.VOLUME)) rt.addValue("VOLUME", v.getAttributeValue(Attribute.VOLUME));
		if(v.hasAttribute(Attribute.ALTITUDE)) rt.addValue("ALTITUDE", v.getAttributeValue(Attribute.ALTITUDE));
		if(v.hasAttribute(Attribute.HEIGHT)) rt.addValue("HEIGHT", v.getAttributeValue(Attribute.HEIGHT));
		if(v.hasAttribute(Attribute.WIDTH)) rt.addValue("WIDTH", v.getAttributeValue(Attribute.WIDTH));
		if(v.hasAttribute(Attribute.LEVEL)) rt.addValue("LEVEL", v.getAttributeValue(Attribute.LEVEL));
		if(v.hasAttribute(Attribute.VARIANCE_LEVEL)) rt.addValue("VARIANCE_LEVEL", v.getAttributeValue(Attribute.VARIANCE_LEVEL));
		if(v.hasAttribute(Attribute.STD_LEVEL)) rt.addValue("STD_LEVEL", v.getAttributeValue(Attribute.STD_LEVEL));
		if(v.hasAttribute(Attribute.LEVEL_MEAN)) rt.addValue("LEVEL_MEAN", v.getAttributeValue(Attribute.LEVEL_MEAN));
		 
		if(v.hasAttribute(Attribute.BIT_QUADS_PERIMETER)) rt.addValue("BIT_QUADS_PERIMETER", v.getAttributeValue(Attribute.BIT_QUADS_PERIMETER));
		if(v.hasAttribute(Attribute.BIT_QUADS_EULER_NUMBER)) rt.addValue("BIT_QUADS_EULER_NUMBER", v.getAttributeValue(Attribute.BIT_QUADS_EULER_NUMBER));
		if(v.hasAttribute(Attribute.BIT_QUADS_HOLE_NUMBER)) rt.addValue("BIT_QUADS_HOLE_NUMBER", v.getAttributeValue(Attribute.BIT_QUADS_HOLE_NUMBER));
		if(v.hasAttribute(Attribute.BIT_QUADS_PERIMETER_CONTINUOUS)) rt.addValue("BIT_QUADS_PERIMETER_CONTINUOUS", v.getAttributeValue(Attribute.BIT_QUADS_PERIMETER_CONTINUOUS));
		if(v.hasAttribute(Attribute.BIT_QUADS_CIRCULARITY)) rt.addValue("BIT_QUADS_CIRCULARITY", v.getAttributeValue(Attribute.BIT_QUADS_CIRCULARITY));
		if(v.hasAttribute(Attribute.BIT_QUADS_AVERAGE_AREA)) rt.addValue("BIT_QUADS_AVERAGE_AREA", v.getAttributeValue(Attribute.BIT_QUADS_AVERAGE_AREA));
		if(v.hasAttribute(Attribute.BIT_QUADS_PERIMETER)) rt.addValue("BIT_QUADS_PERIMETER", v.getAttributeValue(Attribute.BIT_QUADS_PERIMETER));
		if(v.hasAttribute(Attribute.BIT_QUADS_AVERAGE_LENGTH)) rt.addValue("BIT_QUADS_AVERAGE_LENGTH", v.getAttributeValue(Attribute.BIT_QUADS_AVERAGE_LENGTH));
		if(v.hasAttribute(Attribute.BIT_QUADS_AVERAGE_WIDTH)) rt.addValue("BIT_QUADS_AVERAGE_WIDTH", v.getAttributeValue(Attribute.BIT_QUADS_AVERAGE_WIDTH));
		if(v.hasAttribute(Attribute.BIT_QUADS_AVERAGE_AREA)) rt.addValue("BIT_QUADS_AVERAGE_AREA", v.getAttributeValue(Attribute.BIT_QUADS_AVERAGE_AREA));
		
		//if(v.hasAttribute(Attribute.PERIMETER)) rt.addValue("PERIMETER", v.getAttributeValue(Attribute.PERIMETER));
		if(v.hasAttribute(Attribute.PERIMETER_EXTERNAL)) rt.addValue("PERIMETER_EXTERNAL", v.getAttributeValue(Attribute.PERIMETER_EXTERNAL));
		if(v.hasAttribute(Attribute.CIRCULARITY)) rt.addValue("CIRCULARITY", v.getAttributeValue(Attribute.CIRCULARITY));
		if(v.hasAttribute(Attribute.COMPACTNESS)) rt.addValue("COMPACTNESS", v.getAttributeValue(Attribute.COMPACTNESS));
		if(v.hasAttribute(Attribute.ELONGATION)) rt.addValue("ELONGATION", v.getAttributeValue(Attribute.ELONGATION));
		if(v.hasAttribute(Attribute.RECTANGULARITY)) rt.addValue("RECTANGULARITY", v.getAttributeValue(Attribute.RECTANGULARITY));
		if(v.hasAttribute(Attribute.RATIO_WIDTH_HEIGHT)) rt.addValue("RATIO_WIDTH_HEIGHT", v.getAttributeValue(Attribute.RATIO_WIDTH_HEIGHT));
		if(v.hasAttribute(Attribute.MOMENT_ASPECT_RATIO)) rt.addValue("MOMENT_ASPECT_RATIO", v.getAttributeValue(Attribute.MOMENT_ASPECT_RATIO));
		if(v.hasAttribute(Attribute.MOMENT_COMPACTNESS)) rt.addValue("MOMENT_COMPACTNESS", v.getAttributeValue(Attribute.MOMENT_COMPACTNESS));
		if(v.hasAttribute(Attribute.MOMENT_ECCENTRICITY)) rt.addValue("MOMENT_ECCENTRICITY", v.getAttributeValue(Attribute.MOMENT_ECCENTRICITY));
		if(v.hasAttribute(Attribute.MOMENT_ELONGATION)) rt.addValue("MOMENT_ELONGATION", v.getAttributeValue(Attribute.MOMENT_ELONGATION));
		if(v.hasAttribute(Attribute.MOMENT_LENGTH_MAJOR_AXES)) rt.addValue("MOMENT_LENGTH_MAJOR_AXES", v.getAttributeValue(Attribute.MOMENT_LENGTH_MAJOR_AXES));
		if(v.hasAttribute(Attribute.MOMENT_LENGTH_MINOR_AXES)) rt.addValue("MOMENT_LENGTH_MINOR_AXES", v.getAttributeValue(Attribute.MOMENT_LENGTH_MINOR_AXES));
		if(v.hasAttribute(Attribute.MOMENT_ORIENTATION)) rt.addValue("MOMENT_ORIENTATION", v.getAttributeValue(Attribute.MOMENT_ORIENTATION));
		if(v.hasAttribute(Attribute.MOMENT_OF_INERTIA)) rt.addValue("MOMENT_OF_INERTIA", v.getAttributeValue(Attribute.MOMENT_OF_INERTIA));
		
	}
	
	

	private void createTree(NodeLevelSets node) {
		if (node != this.root) {
			graph.addChild(id, node.getParent(), node);
			id++;
		}
		for (NodeLevelSets son : node.getChildren()) {
			createTree(son);
		}
	}
	
	

	public static JFrame getInstance(MorphologicalTree tree) {
		return getInstance(tree, null, null);
	}
	
	public static JFrame getInstance(MorphologicalTree tree, boolean map1[], boolean map2[]) {
		JFrame frame;
		if (tree.getTypeOfTree() == MorphologicalTree.MAXTREE)
			frame = new JFrame("Component tree  - maxtree");
		else if (tree.getTypeOfTree() == MorphologicalTree.MINTREE)
			frame = new JFrame("Component tree  - mintree");
		else
			frame = new JFrame("Component tree  - tree of shapes");
		Container content = frame.getContentPane();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		loadAttribute(tree);
		content.add(new VisualizationMorphologicalTree(tree, map1, map2));
		frame.pack();
		return frame;
	}

	private static void loadAttribute(final MorphologicalTree tree){
		new Thread(new Runnable() {
			public void run() {
				Attribute.loadAttribute(tree, Attribute.BIT_QUADS_AREA);
				Attribute.loadAttribute(tree, Attribute.MOMENT_CENTRAL_11);
				Attribute.loadAttribute(tree, Attribute.PERIMETER_EXTERNAL);
			}
		}).start();
	}
	
	
	public static void main(String args[]) {
		GrayScaleImage imgInput = ImageBuilder.openGrayImage();
		TreeOfShape tree = new TreeOfShape(imgInput);
		getInstance(tree).setVisible(true);
		
		TreeOfShape tree2 = new TreeOfShape(imgInput,296,63);		
		getInstance(tree2).setVisible(true);
		
		
		//tree.extendedTree();
		
	}

}

class LabelNodes implements Transformer<NodeLevelSets, String> {
	InfoTree prunedTree = null;
	public LabelNodes(){}
	public LabelNodes(InfoTree prunedTree){
		this.prunedTree = prunedTree;
	}
	
	public String transform(NodeLevelSets v) {
		//if(true)
		//	return String.valueOf(v.getLevel());
		//if(prunedTree == null)
			return String.valueOf(v.getAttributeValue(Attribute.AREA));		
		//else
		//	return String.valueOf(v.getAttribute(prunedTree.getAttributeType()).getValue() );
			//return String.valueOf(v.getLevel() + " : " + v.getAttributeValue(prunedTree.getAttributeType()));
			
	}
}

class NodeShape implements Transformer<NodeLevelSets, Paint> {
	
	boolean selected[];
	boolean selected2[];
	
	NodeShape(){ }
	
	NodeShape(boolean map[]){
		selected = map;
	}
	
	NodeShape(boolean map1[], boolean map2[]){
		selected = map1;
		selected2 = map2;
	}
	
	Transformer<NodeLevelSets, Paint> t1 = new ConstantTransformer(Color.RED);
	Transformer<NodeLevelSets, Paint> t1Leaf = new ConstantTransformer(Color.decode("0X990000"));
	Transformer<NodeLevelSets, Paint> t1Selected = new ConstantTransformer(Color.GREEN);

	Transformer<NodeLevelSets, Paint> t2 = new ConstantTransformer(Color.BLUE);
	Transformer<NodeLevelSets, Paint> t2Leaf = new ConstantTransformer(Color.decode("0X000099"));
	Transformer<NodeLevelSets, Paint> t2Selected = new ConstantTransformer(Color.GREEN);

	public Paint transform(NodeLevelSets node) {
		if (node.isNodeMaxtree()) {
			if (selected != null && selected[node.hashCode()]) {
				return t1Selected.transform(node);

			} 
			else if (selected2 != null && selected2[node.hashCode()]) {
				return t1Leaf.transform(node);
			} 
			else {
				if (!node.isClone())
					return t1Leaf.transform(node);
				else
					return t1.transform(node);
			}
		} else {
			if (selected != null && selected[node.hashCode()]) {
				return t2Selected.transform(node);
			} 
			else if (selected2 != null && selected2[node.hashCode()]) {
				return t2Leaf.transform(node);
			} 
			else {
				if (!node.isClone())
					return t2Leaf.transform(node);
				else
					return t2.transform(node);
			}
		}
	}

}
