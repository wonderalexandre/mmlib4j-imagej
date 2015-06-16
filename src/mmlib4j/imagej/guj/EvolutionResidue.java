package mmlib4j.imagej.guj;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.process.ByteProcessor;
import ij.util.Tools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class EvolutionResidue {

	NodeCT nodeSelected;
	int x, y;
	public EvolutionResidue(ArrayList<NodeCT>[] dist, ComponentTree tree, int x, int y){
		this.x = x;
		this.y = y;
		nodeSelected = tree.getSC( y * tree.getInputImage().getWidth() + x );
		patternSpectrumUltimateLeveling(dist, tree);
	}
	
	

	public void patternSpectrumUltimateLeveling(ArrayList<NodeCT>[] dist, ComponentTree tree){
		
		ImageStack stack = new ImageStack(tree.getInputImage().getWidth(), tree.getInputImage().getHeight());
		ByteProcessor img = null;//ImageJAdapter.toByteProcessor(tree.getInputImage());
		//stack.addSlice("primitive: 0", img);
		
		int indexMax = 0;
		for(int i=0; i < dist.length; i++){
			if(dist[i]!=null){
				indexMax++;
			}
		}
		
		float px1[] = new float[indexMax];
		float py1[] = new float[indexMax];
		ArrayList<Double> listX = new ArrayList<Double>();
		ArrayList<Double> listY = new ArrayList<Double>();
		int index = 0;
		if(tree.isMaxtree())
			py1[index] = tree.getInputImage().maxValue();
		else
			py1[index] = tree.getInputImage().minValue();
		
		for(int i=0; i < dist.length; i++){
			if(dist[i]!=null){
				
				px1[index] = index;
				if(index != 0)
					py1[index] = py1[index-1];
				
				boolean flag = false;
				for(NodeCT node: dist[i]){
					if(nodeSelected.isComparable(node))
						if(node.getParent() != null){
							flag = true;
							ComponentTree.prunning(tree, node);
						}
				}
				if(flag){
					img = ImageJAdapter.toByteProcessor(tree.reconstruction());
					py1[index] = img.getPixel(x, y);
					
					stack.addSlice("primitive: "+ index + " (residue)", img);
					
					listX.add( (double) px1[index] );
					listY.add( (double) py1[index] );
				}else{
					if(index == 0)
						img = ImageJAdapter.toByteProcessor(tree.reconstruction());
					stack.addSlice("primitive: "+ index, img);
				}
				
				index += 1;
			}
			
		}
		float px[] = Arrays.copyOfRange(px1, 0, index);
		float py[] = Arrays.copyOfRange(py1, 0, index);
		
		new ImagePlus("primitives", stack).show();
		
    	Plot pw = new Plot("size distribution","primitives", "level", px, py);
    	pw.setSize(1000, 500);
    	double[] a = Tools.getMinMax(px);
        double xmin=a[0]-1, xmax=a[1]+1;
        a = Tools.getMinMax(py);
        double ymin=0, ymax= 255;// Math.max(a[1], tree.getInputImage().getSize()/3);//  a[1];
        pw.setLimits(xmin,xmax,ymin,ymax);
        pw.setColor(Color.BLACK);
        pw.draw();
        
        pw.setColor(Color.RED);
        pw.addPoints(listX, listY,PlotWindow.CIRCLE);
        
        
        //pw.addPoints(px,py, PlotWindow.LINE);
        
        //
        
        
        
       // pw.addLabel(0.6, 0.2, "Note: xyz");
        
        
        pw.show();
        
	}
	
	
	
}
