package mmlib4j.imagej.guj;

import ij.measure.ResultsTable;

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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.ImageBuilder;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class VisualizationTreeOfShape extends JPanel {

    Forest<NodeToS,Integer> graph;
    VisualizationViewer<NodeToS,Integer> vv;
    TreeLayout<NodeToS,Integer> treeLayout;
    TreeOfShape compTree;
    
    public VisualizationTreeOfShape(TreeOfShape tree) {
    	this(tree, null, null);
    }
    
    public VisualizationTreeOfShape(TreeOfShape tree, boolean map1[], boolean map2[]) {
        super.setLayout(new BorderLayout());
    	this.compTree = tree;
    	graph = new DelegateTree<NodeToS,Integer>();
        
        ((DelegateTree)graph).setRoot(tree.getRoot());
        createTree(tree.getRoot());
        
        treeLayout = new TreeLayout<NodeToS,Integer>(graph);
        vv =  new VisualizationViewer<NodeToS,Integer>(treeLayout);
        vv.setBackground(Color.white);
        //vv.getRenderContext().setEdgeDrawPaintTransformer(edgeDrawPaintTransformer);
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
        vv.getRenderContext().setVertexLabelTransformer(new LabelNodesToS());
        vv.getRenderContext().setVertexFillPaintTransformer(new NodeShapeToS(map1, map2));
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new LabelNodesToS());
        vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
        

        //Container content = getContentPane();
        
        
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(graphMouse);
        
        final PopupMenu popup = new PopupMenu("Options");
		final MenuItem menu1 = new MenuItem("Show image connected component");
		final MenuItem menu2 = new MenuItem("Extraction attribute");
		popup.add(menu1);
		popup.add(menu2);
		vv.add(popup);
		class GraphMouseListener2  implements GraphMouseListener<NodeToS>, ActionListener{
			NodeToS v;
			ResultsTable rt = new ResultsTable();
			public void graphReleased(NodeToS v, MouseEvent me) {}
			public void graphPressed(NodeToS v, MouseEvent me) {}
			public void graphClicked(NodeToS v, MouseEvent me) {
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
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });
        
        JPanel scaleGrid = new JPanel(new GridLayout(1,0));
        scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        
        super.add(panel, BorderLayout.CENTER);
        super.add(scaleGrid, BorderLayout.SOUTH);
        
        
    }
    
    public VisualizationTreeOfShape(InfoPrunedTree prunedTree, boolean map1[], boolean map2[]) {
        super.setLayout(new BorderLayout());
    	this.compTree = (TreeOfShape) prunedTree.getTree();
    	graph = new DelegateTree<NodeToS,Integer>();
        
        ((DelegateTree)graph).setRoot(compTree.getRoot());
        createTree(prunedTree.getRoot());
        
        treeLayout = new TreeLayout<NodeToS,Integer>(graph);
        vv =  new VisualizationViewer<NodeToS,Integer>(treeLayout);
        vv.setBackground(Color.white);
        //vv.getRenderContext().setEdgeDrawPaintTransformer(edgeDrawPaintTransformer);
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
        vv.getRenderContext().setVertexLabelTransformer(new LabelNodesToS());
        vv.getRenderContext().setVertexFillPaintTransformer(new NodeShapeToS(map1, map2));
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new LabelNodesToS());
        vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
        

        //Container content = getContentPane();
        
        
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(graphMouse);
        final PopupMenu popup = new PopupMenu("Options");
		final MenuItem menu1 = new MenuItem("Show image connected component");
		final MenuItem menu2 = new MenuItem("Extraction attribute");
		popup.add(menu1);
		popup.add(menu2);
		vv.add(popup);
		class GraphMouseListener2  implements GraphMouseListener<NodeToS>, ActionListener{
			NodeToS v;
			ResultsTable rt = new ResultsTable();
			public void graphReleased(NodeToS v, MouseEvent me) {}
			public void graphPressed(NodeToS v, MouseEvent me) {}
			public void graphClicked(NodeToS v, MouseEvent me) {
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
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });
        
        JPanel scaleGrid = new JPanel(new GridLayout(1,0));
        scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        
        super.add(panel, BorderLayout.CENTER);
        super.add(scaleGrid, BorderLayout.SOUTH);
        
        
    }
    

	public void addTable(ResultsTable rt, NodeToS v){
		rt.incrementCounter();
		rt.addValue("PIXEL_CANONICAL", v.getCanonicalPixel());
		if(v.hasAttribute(Attribute.AREA)) rt.addValue("AREA", v.getAttributeValue(Attribute.AREA));
		if(v.hasAttribute(Attribute.VOLUME)) rt.addValue("VOLUME", v.getAttributeValue(Attribute.VOLUME));
		if(v.hasAttribute(Attribute.ALTITUDE)) rt.addValue("ALTITUDE", v.getAttributeValue(Attribute.ALTITUDE));
		if(v.hasAttribute(Attribute.HEIGHT)) rt.addValue("HEIGHT", v.getAttributeValue(Attribute.HEIGHT));
		if(v.hasAttribute(Attribute.WIDTH)) rt.addValue("WIDTH", v.getAttributeValue(Attribute.WIDTH));
		if(v.hasAttribute(Attribute.LEVEL)) rt.addValue("LEVEL", v.getAttributeValue(Attribute.LEVEL));
		if(v.hasAttribute(Attribute.VARIANCE_LEVEL)) rt.addValue("VARIANCE_LEVEL", v.getAttributeValue(Attribute.VARIANCE_LEVEL));
		if(v.hasAttribute(Attribute.LEVEL_MEAN)) rt.addValue("LEVEL_MEAN", v.getAttributeValue(Attribute.LEVEL_MEAN));
		if(v.hasAttribute(Attribute.NUM_HOLES)) rt.addValue("NUM_HOLES", v.getAttributeValue(Attribute.NUM_HOLES));
		if(v.hasAttribute(Attribute.PERIMETER)) rt.addValue("PERIMETER", v.getAttributeValue(Attribute.PERIMETER));
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
		
	}
    
    /**
     * 
     */
    private int id=0;
    private void createTree(NodeToS node) {
    	if(node != compTree.getRoot()){
    		((DelegateTree)graph).addChild(id++, node.getParent(), node);
    	}
    	for(NodeToS son: node.getChildren()){
    		createTree(son);
    	}   	
    }

    private void createTree(InfoPrunedTree.NodePrunedTree node) {
    	if(node.getInfo() != compTree.getRoot()){
    		((DelegateTree)graph).addChild(id++, node.getInfo().getParent(), node.getInfo());
    	}
    	for(InfoPrunedTree.NodePrunedTree son: node.getChildren()){
    		createTree(son);
    	}   	
    }

    /**
     * a driver for this demo
     */
    public static JFrame getInstance(TreeOfShape tree) {
        return getInstance(tree, null, null);
    }

    public static JFrame getInstance(TreeOfShape tree, boolean map1[], boolean map2[]) {
        JFrame frame = new JFrame("Tree of shape");
        Container content = frame.getContentPane();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        content.add(new VisualizationTreeOfShape(tree, map1, map2));
        frame.pack();
        return frame;
    }
    
	public static JFrame getInstance(InfoPrunedTree prunedTree) {
        JFrame frame = new JFrame("Tree of shape");
        Container content = frame.getContentPane();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        content.add(new VisualizationTreeOfShape(prunedTree, null, null));
        frame.pack();
        return frame;
    }
    

	public static JFrame getInstance(InfoPrunedTree prunedTree, boolean map1[], boolean map2[]) {
		JFrame frame = new JFrame("Tree of shape");
		Container content = frame.getContentPane();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		content.add(new VisualizationTreeOfShape(prunedTree, map1, map2));
		frame.pack();
		return frame;
	}
	
    public static void main(String args[]){
    	GrayScaleImage imgInput = ImageBuilder.openGrayImage();
    	TreeOfShape tree = new TreeOfShape(imgInput);
    	tree.extendedTree();
    	getInstance(tree).setVisible(true);;
    }
    
}

class LabelNodesToS implements Transformer<NodeToS,String> {

    public String transform(NodeToS v) {
        return  String.valueOf(v.getLevel());
    }
 }

class NodeShapeToS implements Transformer<NodeToS, Paint> {
	Transformer<NodeToS, Paint> t1 = new ConstantTransformer(Color.RED);
	Transformer<NodeToS, Paint> t1Leaf = new ConstantTransformer(Color.decode("0X990000"));
	Transformer<NodeToS, Paint> t1Selected = new ConstantTransformer(Color.GREEN);

	Transformer<NodeToS, Paint> t2 = new ConstantTransformer(Color.BLUE);
	Transformer<NodeToS, Paint> t2Leaf = new ConstantTransformer(Color.decode("0X000099"));
	Transformer<NodeToS, Paint> t2Selected = new ConstantTransformer(Color.GREEN);
	
	boolean selected[];
	boolean selected2[];
	
	NodeShapeToS(boolean map1[], boolean map2[]){
		selected = map1;
		selected2 = map2;
	}
	
	public Paint transform(NodeToS node) {
		
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
