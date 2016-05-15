package mmlib4j.representation.tree.tos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
//-Xmx1024M -Xms1024M


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class CorrespondenceTree extends JPanel{


	DelegateForest<NodeLevelSets,String> graph;
    VisualizationViewer<NodeLevelSets,String> vv;
    TreeLayout<NodeLevelSets,String> treeLayout;
    
    ComponentTree mintree;
	ComponentTree maxtree;
	TreeOfShape tos;
	AdjacencyRelation adj4 = AdjacencyRelation.getCircular(1);
	AdjacencyRelation adj8 = AdjacencyRelation.getCircular(1.5);
	GrayScaleImage img;
	int cont = 0;
	
    public void addToS(TreeOfShape tos){
    	DelegateTree<NodeLevelSets,String> tree = new DelegateTree<NodeLevelSets,String>(); 
    	tree.setRoot(tos.getRoot());
    	createTree(tos.getRoot(), tree, "ToS_");
    	  
        graph.addTree(tree);
        
        graph.setRoot(tos.getRoot());
    }
    
    public void addCT(ComponentTree ct){
    	String id = "maxtree_";
    	if(ct.isMaxtree())
    		maxtree = ct;
    	else{
    		mintree = ct;
    		id = "mintree_";
    	}
    	
    	DelegateTree<NodeLevelSets,String> tree = new DelegateTree<NodeLevelSets,String>(); 
    	tree.setRoot(ct.getRoot());
    	createTree(ct.getRoot(), tree, id);
        
        graph.addTree(tree);
    }
    
    public CorrespondenceTree(GrayScaleImage img) {
    	super.setLayout(new BorderLayout());
    	this.graph = new DelegateForest<NodeLevelSets,String>();
    	this.img = img;
    	
		this.mintree = new ComponentTree(img, adj8, false);
		//mintree.extendedTree();
		addCT(mintree);
		
		
		this.tos = new TreeOfShape(img);
		//tos.extendedTree();
		addToS(tos);
    	
    	this.maxtree = new ComponentTree(img, adj8, true);
    	//maxtree.extendedTree();
    	addCT(maxtree);	
	
    	createLayout();
    }
    
    public void createLayout(){

        treeLayout = new TreeLayout<NodeLevelSets,String>(graph, 70, 70);
        
        vv =  new VisualizationViewer<NodeLevelSets,String>(treeLayout);
        vv.setBackground(Color.white);
        vv.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<String, Paint>() {
			public Paint transform(String id) {
				if(id.startsWith("Merge_"))
					return Color.GREEN;
				else
					return Color.BLACK;
			}
		});
        
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
        vv.getRenderContext().setVertexLabelTransformer(new LabelNodesTree());
        vv.getRenderContext().setVertexFillPaintTransformer(new NodeShapeTree());
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new LabelNodesTree());
        vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
        

        //Container content = getContentPane();
        
        
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        vv.setGraphMouse(graphMouse);
        vv.addGraphMouseListener(new GraphMouseListener<NodeLevelSets>() {
			public void graphReleased(NodeLevelSets v, MouseEvent me) {}
			public void graphPressed(NodeLevelSets v, MouseEvent me) {}
			public void graphClicked(NodeLevelSets v, MouseEvent me) {
				if(v instanceof NodeCT){
					NodeCT no = (NodeCT) v;
					if(no.isNodeMaxtree()){
						WindowImages.show(no.createImage(), "Upper CC (Maxtree)  - level: " + no.getLevel() + " - SC: " + !no.isClone());
						for(NodeCT nodeCor: getNodeCorrespondece(no, mintree, -1)){
			    			graph.addEdge("Merge_"+ (cont++), no, nodeCor);
			    		}
					}
					else{
						WindowImages.show(no.createImage(),"Lower CC (Mintree)  - level: " + no.getLevel() + " - SC: " + !no.isClone());
						//no.getParent().showNode("Lower CC (Mintree)  - level: " + no.getParent().getLevel() + " - SC: " + !no.getParent().isClone());
					/*	for(NodeCT nodeCor: getNodeCorrespondece(no, maxtree, -1)){
			    			graph.addEdge("Merge__"+ (cont++), no, nodeCor);
			    		}
						for(NodeCT nodeCor: getNodeCorrespondece(no, maxtree, no.getParent().getLevel())){
			    			graph.addEdge("Merge__"+ (cont++), no.getParent(), nodeCor);
			    		}
						*/
						
						for(NodeCT n: no.getAdjacencyNodes()){
							WindowImages.show(n.createImage(),"Adjcentes - level: " + n.getLevel() + " - SC: " + !n.isClone());
						}
						//for(NodeCT node: no.getSubtree())
						//	graph.removeVertex(node, false);
						
						//no.flagPruning = true;
						//mintree.prunning(no);
						//CorrespondenceTree.getFrame( mintree.reconstruction() ).setVisible(true);
						
						
						
					}
				
				}else{
					NodeToS no = (NodeToS) v;
					if(no.isNodeMaxtree())
						WindowImages.show(no.createImage(),"Tree of shapes  - level: " + no.getLevel() + " - SC: " + !no.isClone() + " - type: Upper (Maxtree)");
					else
						WindowImages.show(no.createImage(),"Tree of shapes  - level: " + no.getLevel() + " - SC: " + !no.isClone() + " - type: Lower (Mintree)");
				}
			}
		});
		
		
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
    
    
    /**
     * 
     */
    private void createTree(NodeLevelSets node, DelegateTree<NodeLevelSets,String> tree, String id) {
    	if(node instanceof NodeToS){
    		NodeToS nodeToS = (NodeToS) node;
    		if(node != tree.getRoot()){
        		tree.addChild(id + nodeToS.getId(), nodeToS.getParent(), node);
        	}
        	for(NodeToS son: nodeToS.getChildren()){
        		createTree(son, tree, id);
        	}   	
    	}else{
    		NodeCT nodeCT = (NodeCT) node;
    		if(node != tree.getRoot()){
        		tree.addChild(id+ nodeCT.getId(), nodeCT.getParent(), node);
        	}
        	for(NodeCT son: nodeCT.getChildren()){
        		createTree(son, tree, id);
        	}
    	}
    	
    	
    }

    public void correspondenceMinMax(){
    	//boolean flag[] = new boolean[maxtree.getNumNode()];
    	int cont=0;
    	for(NodeCT node: mintree.getListNodes()){
    		//for(Integer pixel: node.getPixels()){
    			//if(! flag[maxtree.getSC(pixel).getId()]){
    				//flag[maxtree.getSC(pixel).getId()] = true;
    				//graph.addEdge("Merge_"+ cont++, node, maxtree.getSC(pixel));
    			//}
    		//}
    		for(NodeCT nodeCor: getNodeCorrespondece(node, maxtree, -1)){
    			graph.addEdge("Merge_"+ cont++, node, nodeCor);
    		}
    	}
    }
    
    public LinkedList<NodeCT> getNodeCorrespondece(NodeCT node, ComponentTree tree, int level){
    	LinkedList<NodeCT> list = new LinkedList<NodeCT>();
    	boolean flag[] = new boolean[tree.getNumNode()];
    	for(Integer p: node.getCanonicalPixels()){
    		if(level == -1){
    			if(! flag[tree.getSC(p).getId()]){
    				flag[tree.getSC(p).getId()] = true;
    				list.add(tree.getSC(p));
    			}
    		}
    		else{
    			for(int q: tree.getAdjacency().getAdjacencyPixels(tree.getInputImage(), p)){
    				if(tree.getSC(q).getLevel() == level && !flag[tree.getSC(q).getId()]){
    					list.add(tree.getSC(q));
    				//	flag[tree.getSC(q).getId()] = true;
    				}
    						
    			}
    			
    		}
    	}
    	return list;
    }
/*
    public void pruning(NodeCT node){
    	if(node.isNodeMaxtree()){
    		NodeCT parent = node.getParent();
    		LinkedList<NodeCT> listCorParent = getNodeCorrespondece(parent, mintree, -1);
    		LinkedList<NodeCT> listCor = getNodeCorrespondece(node, mintree, -1);
    		
    		for(NodeCT no: listCor){
    			for(int p: node.getPixels()){
    				no.getPixels().remove(p);
    			}
    		}
    	}
    }*/
    
    public void correspondence(){
    	boolean flag[] = new boolean[tos.getNumNode()];
    	for(int p=0; p < img.getSize(); p++){
			if(tos.getSC(p).isNodeMaxtree){
				if(! flag[tos.getSC(p).getId()]){
					flag[tos.getSC(p).getId()] = true;
					graph.addEdge("Merge_"+ tos.getSC(p).getId(), maxtree.getSC(p), tos.getSC(p));
				}
			}else{
				if(! flag[tos.getSC(p).getId()]){
					flag[tos.getSC(p).getId()] = true;
					graph.addEdge("Merge_"+ tos.getSC(p).getId(), mintree.getSC(p), tos.getSC(p));
				}
			}
			/*
			if(tos.getSC(p).isNodeMaxtree){
				if(maxtree.getSC(p).getLevel() == tos.getSC(p).getLevel()){
					if(maxtree.getSC(p).getPixels().size() == tos.getSC(p).getPixels().size()){
						if(maxtree.getSC(p).getArea() < mintree.getSC(p).getArea()){
							
						}else{
							System.out.println("Ops..2 ===>>");		
						}
						
					}else{
								
					}
				}else{
					System.out.println("*******Ops..1");	
				}
				
			}else{
				if(mintree.getSC(p).getLevel() == tos.getSC(p).getLevel()){
					if(mintree.getSC(p).getPixels().size() == tos.getSC(p).getPixels().size()){
						if(mintree.getSC(p).getArea() < maxtree.getSC(p).getArea()){
							
						}else{
							System.out.println("--------------Ops..2 ===>>");	
						}
					}else{
								
					}
				}else{
					System.out.println("******-----------Ops..1");	
				}
					
					
					
			}*/
		}
		
    }
    
    public static JFrame getFrame(GrayScaleImage img){
    	WindowImages.show(img);
    	CorrespondenceTree jTree = new CorrespondenceTree(img);		
		JFrame frame = new JFrame("Trees");
		Container content = frame.getContentPane();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		content.add(jTree);
		frame.pack();
		return frame;
    }
	
	public static void main(String[] args) {
		GrayScaleImage imgInput = ImageBuilder.openGrayImage();
		
		
		CorrespondenceTree jTree = new CorrespondenceTree(imgInput);
		jTree.correspondence();
		
		JFrame frame = new JFrame("Trees");
		Container content = frame.getContentPane();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		content.add(jTree);
		frame.pack();
		frame.setVisible(true);
		
		/*
		VisualizationComponentTree.getInstance(mintree).setVisible(true);
		VisualizationComponentTree.getInstance(maxtree).setVisible(true);
		VisualizationTreeOfShape.getInstance(tos).setVisible(true);;
		*/
		
		///Funcao<Integer, Integer, Integer> f = (a, b) -> { return (a * b); };
		//System.out.println(f.aplicar(10, 2));
		
	}

}


/////////////////////////////////////////////////////////////////////////////////////////

class LabelNodesTree implements Transformer<NodeLevelSets,String> {

    public String transform(NodeLevelSets v) {
    	if(v instanceof NodeCT)
    		return  String.valueOf(((NodeCT) v).getLevel());
    	else
    		return  String.valueOf(((NodeToS) v).getLevel());
    }
 }

class NodeShapeTree implements Transformer<NodeLevelSets, Paint> {
	//vermelho = mintree
	Transformer<NodeLevelSets, Paint> t2 = new ConstantTransformer(Color.RED);
	Transformer<NodeLevelSets, Paint> t2Leaf = new ConstantTransformer(Color.decode("0X990000"));
	
	//azul = maxtree
	Transformer<NodeLevelSets, Paint> t1 = new ConstantTransformer(Color.BLUE);
	Transformer<NodeLevelSets, Paint> t1Leaf = new ConstantTransformer(Color.decode("0X000099"));
	
	
	public Paint transform(NodeLevelSets nodeTmp) {
		if(nodeTmp instanceof NodeCT){
			NodeCT node = (NodeCT) nodeTmp;
			if(node.isNodeMaxtree()){
				if(!node.isClone())
					return t1Leaf.transform(node);
				else
					return t1.transform(node);
			}else{
				if(node.flagPruning)
					return t2Leaf.transform(node);
				if(!node.isClone())
					return t2Leaf.transform(node);
				else
					return t2.transform(node);
			}
		}else{
			NodeToS node = (NodeToS) nodeTmp;
			if(node.isNodeMaxtree()){
				if(!node.isClone())
					return t1Leaf.transform(node);
				else
					return t1.transform(node);
			}else{
				if(!node.isClone())
					return t2Leaf.transform(node);
				else
					return t2.transform(node);
			}
		}
		
	}
}
