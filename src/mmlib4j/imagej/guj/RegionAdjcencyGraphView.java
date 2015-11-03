package mmlib4j.imagej.guj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.TransformerUtils;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.graph.Edge;
import mmlib4j.representation.graph.Graph;
import mmlib4j.representation.graph.rag.ComputerWeightRAG;
import mmlib4j.representation.graph.rag.RAGVertex;
import mmlib4j.representation.graph.rag.RegionAdjcencyGraph;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

public class RegionAdjcencyGraphView extends RegionAdjcencyGraph implements Graph<Integer> {
 
	
	protected RegionAdjcencyGraphView(GrayScaleImage img, GrayScaleImage labels, ComputerWeightRAG<RegionAdjcencyGraph> compWeight) {
		super(img, labels, compWeight);
	}

	public void draw(){
		 UndirectedSparseGraph<RAGVertex, Edge<RAGVertex>> graph = new UndirectedSparseGraph<RAGVertex, Edge<RAGVertex>>();
		 Map<RAGVertex, Point2D> map = new HashMap<RAGVertex, Point2D>();
		 for(RAGVertex v: this.vertices){
			 graph.addVertex(v);
			 
			 int x = v.getPixelRep() % img.getWidth(); 
			 int y = v.getPixelRep() / img.getWidth();
			 
			 map.put(v, new Point2D.Double(x, y));
			 
		 }
		 for(Edge<Integer> e: edges){
			 Edge<RAGVertex> edge = new Edge<RAGVertex>(getVertexByIndex(e.getVertex1()), getVertexByIndex(e.getVertex2()), e.getWeight()); 
			 graph.addEdge(edge, edge.getVertex1(), edge.getVertex2());
		 }
		 Transformer<RAGVertex, Point2D> vertexLocations = TransformerUtils.mapTransformer(map);
		 
		 
		 StaticLayout<RAGVertex, Edge<RAGVertex>> graphLayout = new StaticLayout<RAGVertex, Edge<RAGVertex>>(graph, vertexLocations, new Dimension(img.getWidth(), img.getHeight()));
		 //FRLayout<RAGVertex, Edge<RAGVertex> > graphLayout = new FRLayout<RAGVertex, Edge<RAGVertex>>(graph, new Dimension(img.getWidth(), img.getHeight()));
		 final VisualizationViewerImage<RAGVertex> vv =  new VisualizationViewerImage<RAGVertex>(graphLayout);
		 vv.setImage(img);
		 //vv.setBackground(Color.white);
		 vv.setForeground(Color.GREEN); 
		 vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
		 vv.getRenderContext().setVertexLabelTransformer(new Transformer<RAGVertex,String>() {
			    public String transform(RAGVertex v) {
			        return  String.valueOf(v.getLevel());
			    }
		 });
		 vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Edge<RAGVertex>, String>() {
			 public String transform(Edge<RAGVertex> e) {
				return String.valueOf(e.getWeight());
			 }
		});
		 vv.getRenderContext().setEdgeFontTransformer(new Transformer<Edge<RAGVertex>, Font>() {
			public Font transform(Edge<RAGVertex> arg0) {
				return new Font(Font.SANS_SERIF, Font.BOLD, 9);
			}			 
		});
		 vv.getRenderContext().setVertexFontTransformer(new Transformer<RAGVertex, Font>() {
				public Font transform(RAGVertex arg0) {
					return new Font(Font.SANS_SERIF, Font.BOLD, 11);
				}			 
			});		 
		 vv.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<Edge<RAGVertex>, Paint>() {
			public Paint transform(Edge<RAGVertex> arg0) {
				return Color.RED;
			}			 
		});
		vv.getRenderContext().setVertexShapeTransformer(new Transformer<RAGVertex, Shape>() {
			public Shape transform(RAGVertex arg0) {
				return new Ellipse2D.Double(-3,-3,7,7);
			}
		});
		 
		 // vv.getRenderContext().setVertexFillPaintTransformer(new NodeShape());
		 // add a listener for ToolTips
		 vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));

		 
		 
		 final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		 graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
		 vv.setGraphMouse(graphMouse);
		 	
		 
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
	     JFrame frame = new JFrame("RAG");   
	     frame.setSize(img.getWidth(), img.getHeight());
	     frame.setLayout(new BorderLayout());
	     frame.add(panel, BorderLayout.CENTER);
		 frame.add(scaleGrid, BorderLayout.SOUTH); 
		 frame.setVisible(true);
	}
	

	public static void main(String args[]){
		GrayScaleImage img = ImageBuilder.openGrayImage(ImageBuilder.windowOpenFile());
	//	img = ImageUtils.reduceDepth(img, 64);
		AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5);
		//RegionAdjcencyGraph rag = RegionAdjcencyGraph.getRAGByBasinsWatershed(img);
		RegionAdjcencyGraphView rag2 = (RegionAdjcencyGraphView) RegionAdjcencyGraphView.getRAGByFlatzone(img);
		rag2.draw();
		//IGrayScaleImage imgErosion = rag.erosion();
		//WindowImages.show(new IImage[]{img, imgErosion});
		
		
	}
}
