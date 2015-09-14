package mmlib4j.imagej.guj;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.process.ByteProcessor;
import ij.util.Tools;

import java.awt.Color;
import java.util.ArrayList;

import mmlib4j.imagej.utils.ImageJAdapter;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class Granulometry  {
    
	
	
	public Granulometry(ArrayList<NodeCT>[] dist, ComponentTree tree){
		patternSpectrumUltimateLeveling(dist, tree);
		//selectedPrimitives(dist, tree);
		//patternSpectrumCC(dist, tree);
		//patternSpectrumResidues(dist, tree);
		//patternSpectrumArea(dist, tree);
	}
	
	

	public void patternSpectrumUltimateLeveling(ArrayList<NodeCT>[] dist, ComponentTree tree){
		
		ImageStack stack = new ImageStack(tree.getInputImage().getWidth(), tree.getInputImage().getHeight());
		ByteProcessor img = ImageJAdapter.toByteProcessor(tree.getInputImage());
		stack.addSlice("primitive: 0", img);
		
		int indexMax = 1;
		for(int i=0; i < dist.length; i++){
			if(dist[i]!=null){
				indexMax++;
			}
		}
		
		float px[] = new float[indexMax];
		float py[] = new float[indexMax];
		for(int i=0; i < tree.getInputImage().getSize(); i++)
			py[0] += tree.getInputImage().getPixel(i);
		
		for(int i=0, index=1; i < dist.length; i++){
			
			if(dist[i]!=null){
				py[index] = py[index-1];
				px[index] = index;
				
				for(NodeCT node: dist[i]){
					if(node.getParent() != null){
						if(tree.isMaxtree()){
							py[index] -= node.getCanonicalPixels().size() * Math.abs(node.getLevel() - node.getParent().getLevel());
						}
						else{
							py[index] += node.getCanonicalPixels().size() * Math.abs(node.getLevel() - node.getParent().getLevel());
						}
						ComponentTree.prunning(tree, node);
					}
				}
				img = ImageJAdapter.toByteProcessor(tree.reconstruction());
				stack.addSlice("primitive: "+index, img);
				index += 1;
			}
			else{
			//	stack.addSlice("primitive: "+i, img);
			}
		}
		
		new ImagePlus("primitives", stack).show();
		
    	Plot pw = new Plot("size distribution","primitives", "[id - ψ_i]", px, py);
    	pw.setSize(1000, 500);
    	double[] a = Tools.getMinMax(px);
        double xmin=a[0], xmax=a[1];
        a = Tools.getMinMax(py);
        double ymin=a[0], ymax= a[1];// Math.max(a[1], tree.getInputImage().getSize()/3);//  a[1];
        pw.setLimits(xmin,xmax,ymin,ymax);
        pw.addPoints(px,py, PlotWindow.CIRCLE);
        
       // pw.addLabel(0.6, 0.2, "Note: xyz");
        
        pw.draw();
        pw.show();
        
        
        

		float psX[] = new float[indexMax-1];
		float psY[] = new float[indexMax-1];
		
		for(int i=0; i < indexMax-1; i++){
			psX[i] = i;
			psY[i] = Math.abs(py[i] - py[i+1]);
			//py[i] = py[i] / img.getSize();
		}
		
		/*MomentHistogram m = new MomentHistogram(psY);
		System.out.println("Momentos:");
		System.out.println(m.calculateMomentToString(1));
		System.out.println(m.calculateMomentToString(2));
		System.out.println(m.calculateMomentToString(3));
		System.out.println(m.calculateMomentToString(4));
		*/
    	Plot ps = new Plot("pattern strectrum","primitives", "density", psX, psY);
    	ps.setSize(1000, 500);
    	a = Tools.getMinMax(psX);
        xmin=a[0];
        xmax=a[1];
        a = Tools.getMinMax(psY);
        ymin=a[0];
        ymax=a[1];
        ps.setLimits(xmin,xmax,ymin,ymax);
        ps.addPoints(psX,psY, PlotWindow.CIRCLE);
        
       // pw.addLabel(0.6, 0.2, "Note: xyz");
        
        ps.draw();
        ps.show();
	}
	
	
	
	public void patternSpectrumUltimateLeveling2(ArrayList<NodeCT>[] dist, ComponentTree tree){
		
		ImageStack stack = new ImageStack(tree.getInputImage().getWidth(), tree.getInputImage().getHeight());
		ByteProcessor img = ImageJAdapter.toByteProcessor(tree.getInputImage());
		stack.addSlice("primitive: 0", img);
		
		int indexMax = 1;
		for(int i=0; i < dist.length; i++){
			if(dist[i]!=null){
				indexMax++;
			}
		}
		
		float pxPS[] = new float[dist.length+1];
		float pyPS[] = new float[dist.length+1];
		
		float px[] = new float[indexMax];
		float py[] = new float[indexMax];
		for(int i=0; i < tree.getInputImage().getSize(); i++)
			py[0] += tree.getInputImage().getPixel(i);
		
		pyPS[0] = py[0];
		
		for(int i=1, index=1; i <= dist.length; i++){
			pyPS[i] = pyPS[i-1];
			pxPS[i] = i;
			
			if(dist[i-1]!=null){
				py[index] = py[index-1];
				px[index] = i;
				
				for(NodeCT node: dist[i-1]){
					if(node.getParent() != null){
						if(tree.isMaxtree()){
							py[index] -= node.getCanonicalPixels().size() * Math.abs(node.getLevel() - node.getParent().getLevel());
							pyPS[i] -= node.getCanonicalPixels().size() * Math.abs(node.getLevel() - node.getParent().getLevel());
						}
						else{
							py[index] += node.getCanonicalPixels().size() * Math.abs(node.getLevel() - node.getParent().getLevel());
							pyPS[i] += node.getCanonicalPixels().size() * Math.abs(node.getLevel() - node.getParent().getLevel());
						}
						ComponentTree.prunning(tree, node);
					}
				}
				img = ImageJAdapter.toByteProcessor(tree.reconstruction());
				stack.addSlice("primitive: "+index, img);
				index += 1;
			}
			else{
			//	stack.addSlice("primitive: "+i, img);
			}
		}
		
		new ImagePlus("primitives", stack).show();
		
    	Plot pw = new Plot("size distribution","primitives", "[id - ψ_i]", pxPS, pyPS);
    	pw.setSize(1000, 500);
    	double[] a = Tools.getMinMax(pxPS);
        double xmin=a[0], xmax=a[1];
        a = Tools.getMinMax(pyPS);
        double ymin=a[0], ymax= a[1];// Math.max(a[1], tree.getInputImage().getSize()/3);//  a[1];
        
        pw.setLimits(xmin,xmax,ymin,ymax);

        pw.setColor(Color.BLACK);
        pw.addPoints(pxPS,pyPS, PlotWindow.LINE);
        pw.draw();
        
        pw.setColor(Color.RED);
		pw.addPoints(px, py, Plot.CIRCLE);
		pw.draw();

        
       // pw.addLabel(0.6, 0.2, "Note: xyz");
        
        //
        pw.show();
        
        
        

        float psX[] = new float[dist.length-1];
		float psY[] = new float[dist.length-1];
		
		for(int i=0; i < psX.length; i++){
			psX[i] = i;
			psY[i] = Math.abs(pyPS[i] - pyPS[i+1]);
		}
		
		
		/*MomentHistogram m = new MomentHistogram(psY);
		System.out.println("Momentos:");
		System.out.println(m.calculateMomentToString(1));
		System.out.println(m.calculateMomentToString(2));
		System.out.println(m.calculateMomentToString(3));
		System.out.println(m.calculateMomentToString(4));
		*/
    	Plot ps = new Plot("pattern strectrum","primitives", "density", psX, psY);
    	ps.setSize(1000, 500);
    	a = Tools.getMinMax(psX);
        xmin=a[0];
        xmax=a[1];
        a = Tools.getMinMax(psY);
        ymin=a[0];
        ymax=a[1];
        ps.setLimits(xmin,xmax,ymin,ymax);
        ps.addPoints(psX,psY, PlotWindow.CIRCLE);
        
       // pw.addLabel(0.6, 0.2, "Note: xyz");
        
        ps.draw();
        ps.show();
	}
	

	
	public void selectedPrimitives(ArrayList<NodeCT>[] dist, ComponentTree tree){
		
		ImageStack stack = new ImageStack(tree.getInputImage().getWidth(), tree.getInputImage().getHeight());
		ByteProcessor img = ImageJAdapter.toByteProcessor(tree.getInputImage());
		stack.addSlice("primitive: 0", img);
		
		
		
		for(int i=0, index=1; i < dist.length; i++){
			if(dist[i]!=null){
				for(NodeCT node: dist[i]){
					if(node.getParent() != null){
						ComponentTree.prunning(tree, node);
					}
				}
				img = ImageJAdapter.toByteProcessor(tree.reconstruction());
				stack.addSlice("primitive: "+index, img);
				index += 1;
			}
		}
		
		new ImagePlus("primitives", stack).show();
	}
	
	
	public void patternSpectrum(ArrayList<NodeCT>[] dist, ComponentTree tree){
		
		ImageStack stack = new ImageStack(tree.getInputImage().getWidth(), tree.getInputImage().getHeight());
		ByteProcessor img = ImageJAdapter.toByteProcessor(tree.getInputImage());
		stack.addSlice("primitive: 0", img);
		
	
		float px[] = new float[dist.length];
		float py[] = new float[dist.length];
		for(int i=0; i < tree.getInputImage().getSize(); i++)
			py[0] += tree.getInputImage().getPixel(i);
		
		for(int i=1; i < dist.length; i++){
			
			py[i] = py[i-1];
			px[i] = i;
			
			if(dist[i]!=null){
				for(NodeCT node: dist[i]){
					if(node.getParent() != null){
						if(tree.isMaxtree())
							py[i] -= node.getCanonicalPixels().size() * Math.abs(node.getLevel() - node.getParent().getLevel());
						else
							py[i] += node.getCanonicalPixels().size() * Math.abs(node.getLevel() - node.getParent().getLevel());
						ComponentTree.prunning(tree, node);
					}
				}
				img = ImageJAdapter.toByteProcessor(tree.reconstruction());
				stack.addSlice("primitive: "+i, img);
			}
			else{
				stack.addSlice("primitive: "+i, img);
			}
		}
		
		new ImagePlus("primitives", stack).show();
		
    	Plot pw = new Plot("size distribution","primitives", "[id - ψ_i]", px, py);
    	pw.setSize(1000, 500);
    	double[] a = Tools.getMinMax(px);
        double xmin=a[0], xmax=a[1];
        a = Tools.getMinMax(py);
        double ymin=a[0], ymax= a[1];// Math.max(a[1], tree.getInputImage().getSize()/3);//  a[1];
        pw.setLimits(xmin,xmax,ymin,ymax);
        pw.addPoints(px,py, PlotWindow.CIRCLE);
        
       // pw.addLabel(0.6, 0.2, "Note: xyz");
        
        pw.draw();
        pw.show();
        
        
        

		float psX[] = new float[dist.length-1];
		float psY[] = new float[dist.length-1];
		
		for(int i=0; i < psX.length; i++){
			psX[i] = i;
			psY[i] = Math.abs(py[i] - py[i+1]);
		}
		
		/*MomentHistogram m = new MomentHistogram(psY);
		System.out.println("Momentos:");
		System.out.println(m.calculateMomentToString(1));
		System.out.println(m.calculateMomentToString(2));
		System.out.println(m.calculateMomentToString(3));
		System.out.println(m.calculateMomentToString(4));
		*/
    	Plot ps = new Plot("pattern strectrum","primitives", "density", psX, psY);
    	ps.setSize(1000, 500);
    	a = Tools.getMinMax(psX);
        xmin=a[0];
        xmax=a[1];
        a = Tools.getMinMax(psY);
        ymin=a[0];
        ymax=a[1];
        ps.setLimits(xmin,xmax,ymin,ymax);
        ps.addPoints(psX,psY, PlotWindow.CIRCLE);
        
       // pw.addLabel(0.6, 0.2, "Note: xyz");
        
        ps.draw();
        ps.show();
	}

	
	

	
	
}
