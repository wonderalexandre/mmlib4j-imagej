package mmlib4j.imagej.guj;
import ij.gui.Plot;
import ij.util.Tools;

import java.awt.Color;
import java.util.ArrayList;

import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueComponentTree;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueTreeOfShapes;
import mmlib4j.representation.tree.attribute.ComputerMserComponentTree;
import mmlib4j.representation.tree.attribute.ComputerMserTreeOfShapes;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedGradualTransition;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by ImageJ
 */
public class HistogramOfBranch {

	private int indexAttr;
	int x;
	int y;
	
	public HistogramOfBranch (int indexAttr, int x, int y) {
		this.indexAttr = indexAttr;
		this.x = x;
		this.y = y;
	}
	
	public void showScoreMser(NodeCT node, boolean[] selected, Double []scoreBranch) {
		int contStable = 0;
		int contMser = 0;
		for (int i = 0; i < scoreBranch.length; i++) {
			if(scoreBranch[i] != null){
				contStable++;
			}
			if(selected[i] && scoreBranch[i] != null){
				contMser++;
			}
		}
		
		
		double score[] = new double[contStable];
		double indiceScore[] = new double[contStable];
		double indiceMser[] = new double[contMser];
		double scoreMser[] = new double[contMser];
		int contScore = 0;
		int contScoreMser = 0;
		
		for(NodeCT nodePath: node.getPathToRoot()){
			if(scoreBranch[nodePath.getId()] != null){
				indiceScore[contScore] = contScore;
				score[contScore] = scoreBranch[nodePath.getId()];
				contScore++;
			}
			if(selected[nodePath.getId()] && scoreBranch[nodePath.getId()] != null){
				indiceMser[contScoreMser] = contScore;
				scoreMser[contScoreMser] = scoreBranch[nodePath.getId()];
				contScoreMser++;
			}
		}
		/*for (int i = 0; i < scoreBranch.length; i++) {
			if(scoreBranch[i] != null){
				indiceScore[contScore] = i;
				score[contScore] = scoreBranch[i];
				contScore++;
			}
			if(selected[i] && scoreBranch[i] != null){
				indiceMser[contScoreMser] = i;
				scoreMser[contScoreMser] = scoreBranch[i];
				contScoreMser++;
			}
		}*/

		Plot pw = new Plot("Stability function for the nodes of the path (which contains selected node) from leaf to the root.", "nodes stable", "score of nodes stable", indiceScore, score);
		double[] a = Tools.getMinMax(indiceScore);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(score);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(xmin, xmax, ymin, ymax);
		//pw.addPoints(indiceScore, score, Plot.CIRCLE);
		
		pw.setColor(Color.RED);
		pw.addPoints(indiceMser, scoreMser, Plot.CIRCLE);
		//pw.addPoints(indiceMser, scoreMser, Plot.BOX);
		//pw.addPoints(indiceMser, scoreMser, Plot.X);
		
		
		pw.setColor(Color.BLACK);
		pw.addPoints(indiceScore, score, Plot.CROSS);
		
		pw.show();
		
	}

	public void showScoreMser(Attribute []q) {

		 
		int contStable = 0;
		for (int i = 0; i < q.length; i++) {
			if(q[i] != null){
				contStable++;
			}
			
		}
		
		double score[] = new double[contStable];
		double indiceScore[] = new double[contStable];
		int contScore = 0;
		for (int i = 0; i < q.length; i++) {
			if(q[i] != null){
				indiceScore[contScore] = i;
				score[contScore] = q[i].getValue();
				contScore++;
			}
		}

		Plot pw = new Plot("Function of stability", "nodes stable", "score of nodes stable", indiceScore, score);
		double[] a = Tools.getMinMax(indiceScore);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(score);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(xmin, xmax, ymin, ymax);
		pw.addPoints(indiceScore, score, Plot.CIRCLE);
		pw.show();
		
	}
	
	public void run(InfoPrunedTree prunedTree, int typePruning, int delta) {
		ArrayList<Float> listPx = new ArrayList<Float>();
		ArrayList<Float> listPy = new ArrayList<Float>();

		ArrayList<Float> listPxSelected = null;
		ArrayList<Float> listPySelected = null;
		
		MorphologicalTreeFiltering treeIn = prunedTree.getTree();
		if(treeIn instanceof ComponentTree){	
			ComponentTree tree = (ComponentTree) treeIn;
			
			if (typePruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE){
				ComputerExtinctionValueComponentTree ev = new ComputerExtinctionValueComponentTree(tree);
				boolean selected[] = ev.getExtinctionValueNodeCT(indexAttr, prunedTree);
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}else if(typePruning == MorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserComponentTree mser = new ComputerMserComponentTree(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta, prunedTree); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				showScoreMser(mser.getAttributeStability());
				showScoreMser(node, selected, mser.getScoreOfBranch(node));
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}
			else if(typePruning == MorphologicalTreeFiltering.PRUNING_GRADUAL_TRANSITION){
				PruningBasedGradualTransition gt = new PruningBasedGradualTransition(treeIn, prunedTree.getAttributeType(), delta);
				boolean selected[] = gt.getMappingSelectedNodes(prunedTree); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
			}
			else{
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));	
				}
			}
			
			
		}else{
			TreeOfShape tree = (TreeOfShape) treeIn;
			
			if (typePruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE){
				ComputerExtinctionValueTreeOfShapes ev = new ComputerExtinctionValueTreeOfShapes(tree);
				boolean selected[] = ev.getExtinctionValueNode(indexAttr, prunedTree);
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				
				
			}
			else if(typePruning == MorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserTreeOfShapes mser = new ComputerMserTreeOfShapes(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta, prunedTree); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
			}
			else{
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
				}
				
			}
		}
		
		float vPx[] = new float[listPx.size()];
		float vPy[] = new float[listPy.size()];
		for (int i = 0; i < listPx.size(); i++) {
			vPx[i] = listPx.get(i);
			vPy[i] = listPy.get(i);
		}

		Plot pw = new Plot("Histogram", "level", "attribute", vPx, vPy);
		double[] a = Tools.getMinMax(vPx);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(vPy);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(xmin-5, xmax+5, ymin-5, ymax-5);
		
		
		if(listPxSelected != null){
			float vPxEV[] = new float[listPxSelected.size()];
			float vPyEV[] = new float[listPySelected.size()];
			for (int i = 0; i < listPxSelected.size(); i++) {
				vPxEV[i] = listPxSelected.get(i);
				vPyEV[i] = listPySelected.get(i);
				
			}
			pw.setColor(Color.RED);
			pw.addPoints(vPxEV, vPyEV, Plot.CIRCLE);
			pw.addPoints(vPxEV, vPyEV, Plot.BOX);
			pw.addPoints(vPxEV, vPyEV, Plot.X);
		}
		pw.setColor(Color.BLACK);
		pw.addPoints(vPx, vPy, Plot.CROSS);
		pw.show();
	}
	

	public void run(MorphologicalTreeFiltering treeIn, int typePruning, int delta) {
		ArrayList<Float> listPx = new ArrayList<Float>();
		ArrayList<Float> listPy = new ArrayList<Float>();

		ArrayList<Float> listPxSelected = null;
		ArrayList<Float> listPySelected = null;
		
		
		if(treeIn instanceof ComponentTree){	
			ComponentTree tree = (ComponentTree) treeIn;
			
			if (typePruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE){
				ComputerExtinctionValueComponentTree ev = new ComputerExtinctionValueComponentTree(tree);
				boolean selected[] = ev.getExtinctionValueNodeCT(indexAttr);
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}else if(typePruning == MorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserComponentTree mser = new ComputerMserComponentTree(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				//showScoreMser(mser.getScore());
				showScoreMser(node, selected, mser.getScoreOfBranch(node));
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}
			else if(typePruning == MorphologicalTreeFiltering.PRUNING_GRADUAL_TRANSITION){
				PruningBasedGradualTransition gt = new PruningBasedGradualTransition(treeIn, indexAttr, delta);
				boolean selected[] = gt.getMappingSelectedNodes(); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();	
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
			}
			else{
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
					}else{
						node = node.getParent();
					}
				}
			}
			
			
		}else{
			TreeOfShape tree = (TreeOfShape) treeIn;
			
			if (typePruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE){
				ComputerExtinctionValueTreeOfShapes ev = new ComputerExtinctionValueTreeOfShapes(tree);
				boolean selected[] = ev.getExtinctionValueNode(indexAttr, delta);
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				
				
			}
			else if(typePruning == MorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserTreeOfShapes mser = new ComputerMserTreeOfShapes(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
			}
			else{
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();	
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
					}else
						node = node.getParent();
				}
				
			}
		}
		
		float vPx[] = new float[listPx.size()];
		float vPy[] = new float[listPy.size()];
		for (int i = 0; i < listPx.size(); i++) {
			vPx[i] = listPx.get(i);
			vPy[i] = listPy.get(i);
		}

		Plot pw = new Plot("Residual evolution for the pixel (" + this.x + ", " + this.y + ")", "attribute value", "level", vPy, vPx);
		double[] a = Tools.getMinMax(vPx);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(vPy);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(ymin-5, ymax-5, xmin-5, xmax+5);
		
		
		if(listPxSelected != null){
			float vPxEV[] = new float[listPxSelected.size()];
			float vPyEV[] = new float[listPySelected.size()];
			for (int i = 0; i < listPxSelected.size(); i++) {
				vPxEV[i] = listPxSelected.get(i);
				vPyEV[i] = listPySelected.get(i);
				
			}
			pw.setColor(Color.RED);
			pw.addPoints(vPyEV, vPxEV, Plot.CIRCLE);
			pw.addPoints(vPyEV, vPxEV, Plot.BOX);
			pw.addPoints(vPyEV, vPxEV, Plot.X);
		}
		pw.setColor(Color.BLACK);
		pw.addPoints(vPy, vPx, Plot.CROSS);
		pw.show();
	}
	

	public void run(MorphologicalTreeFiltering treeIn, boolean selected[]) {
		ArrayList<Float> listPx = new ArrayList<Float>();
		ArrayList<Float> listPy = new ArrayList<Float>();

		ArrayList<Float> listPxSelected = null;
		ArrayList<Float> listPySelected = null;
		
		
		if(treeIn instanceof ComponentTree){	
			ComponentTree tree = (ComponentTree) treeIn;
			boolean selected2[] = new boolean[tree.getNumNode()];
			ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
			NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
			selected2[node.hashCode()] = true;
			listPx.add(new Float(node.getLevel()));
			listPy.add(getAttribute(node));
			while (node.getParent() != null) {
				if(getAttribute(node) != getAttribute(node.getParent())){
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
				}else{
					node = node.getParent();
				}
			}
				
			listPxSelected = new ArrayList<Float>();
			listPySelected = new ArrayList<Float>();
			for(NodeCT n: listEVPath){
				listPxSelected.add(new Float(n.getLevel()));
				listPySelected.add(getAttribute(n));
			}
			//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
		}else{
			TreeOfShape tree = (TreeOfShape) treeIn;
			boolean selected2[] = new boolean[tree.getNumNode()];
			ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
			NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
			selected2[node.hashCode()] = true;
			listPx.add(new Float(node.getLevel()));
			listPy.add(getAttribute(node));
			while (node.getParent() != null) {
				if(getAttribute(node) != getAttribute(node.getParent())){
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
				}else{
					node = node.getParent();
				}
					
			}
				
			listPxSelected = new ArrayList<Float>();
			listPySelected = new ArrayList<Float>();
			for(NodeToS n: listEVPath){
				listPxSelected.add(new Float(n.getLevel()));
				listPySelected.add(getAttribute(n));
			}
				
			
		}
		
		float vPx[] = new float[listPx.size()];
		float vPy[] = new float[listPy.size()];
		for (int i = 0; i < listPx.size(); i++) {
			vPx[i] = listPx.get(i);
			vPy[i] = listPy.get(i);
		}

		Plot pw = new Plot("Attribute profile for the pixel (" + this.x + ", " + this.y + ")", "attribute value", "level", vPy, vPx);
		double[] a = Tools.getMinMax(vPx);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(vPy);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(ymin-5, ymax-5, xmin-5, xmax+5);
		
		
		if(listPxSelected != null){
			float vPxEV[] = new float[listPxSelected.size()];
			float vPyEV[] = new float[listPySelected.size()];
			for (int i = 0; i < listPxSelected.size(); i++) {
				vPxEV[i] = listPxSelected.get(i);
				vPyEV[i] = listPySelected.get(i);
				
			}
			pw.setColor(Color.RED);
			pw.addPoints(vPyEV, vPxEV, Plot.CIRCLE);
			pw.addPoints(vPyEV, vPxEV, Plot.BOX);
			pw.addPoints(vPyEV, vPxEV, Plot.X);
		}
		pw.setColor(Color.BLACK);
		pw.addPoints(vPy, vPx, Plot.CROSS);
		pw.show();
	}
	

	public void runPrimitivesFamily(MorphologicalTreeFiltering treeIn, int typePruning, int delta) {
		ArrayList<Float> listPx = new ArrayList<Float>();
		ArrayList<Float> listPy = new ArrayList<Float>();

		ArrayList<Float> listPxSelected = null;
		ArrayList<Float> listPySelected = null;
		
		
		if(treeIn instanceof ComponentTree){	
			ComponentTree tree = (ComponentTree) treeIn;
			
			if (typePruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE){
				ComputerExtinctionValueComponentTree ev = new ComputerExtinctionValueComponentTree(tree);
				boolean selected[] = ev.getExtinctionValueNodeCT(indexAttr);
				boolean selected2[] = new boolean[tree.getNumNode()];
				
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(contNodes[n.getId()]);
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}else if(typePruning == MorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserComponentTree mser = new ComputerMserComponentTree(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				//showScoreMser(mser.getScore());
				showScoreMser(node, selected, mser.getScoreOfBranch(node));
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(contNodes[n.getId()]);
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}
			else if(typePruning == MorphologicalTreeFiltering.PRUNING_GRADUAL_TRANSITION){
				PruningBasedGradualTransition gt = new PruningBasedGradualTransition(treeIn, indexAttr, delta);
				boolean selected[] = gt.getMappingSelectedNodes(); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();	
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(contNodes[n.getId()]);
				}
			}
			else{
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
					}else{
						node = node.getParent();
					}
				}
			}
			
			
		}else{
			TreeOfShape tree = (TreeOfShape) treeIn;
			
			if (typePruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE){
				ComputerExtinctionValueTreeOfShapes ev = new ComputerExtinctionValueTreeOfShapes(tree);
				boolean selected[] = ev.getExtinctionValueNode(indexAttr, delta);
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(contNodes[n.getId()]);
				}
				
				
			}
			else if(typePruning == MorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserTreeOfShapes mser = new ComputerMserTreeOfShapes(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(contNodes[n.getId()]);
				}
			}
			else{
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();	
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
					}else
						node = node.getParent();
				}
				
			}
		}
		
		float vPx[] = new float[listPx.size()];
		float vPy[] = new float[listPy.size()];
		for (int i = 0; i < listPx.size(); i++) {
			vPx[i] = listPx.get(i);
			vPy[i] = listPy.get(i);
		}

		Plot pw = new Plot("Residual evolution for the pixel (" + this.x + ", " + this.y + ")", "primitives family (index)", "level", vPy, vPx);
		double[] a = Tools.getMinMax(vPx);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(vPy);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(ymin-5, ymax-5, xmin-5, xmax+5);
		
		
		if(listPxSelected != null){
			float vPxEV[] = new float[listPxSelected.size()];
			float vPyEV[] = new float[listPySelected.size()];
			for (int i = 0; i < listPxSelected.size(); i++) {
				vPxEV[i] = listPxSelected.get(i);
				vPyEV[i] = listPySelected.get(i);
				
			}
			pw.setColor(Color.RED);
			pw.addPoints(vPyEV, vPxEV, Plot.CIRCLE);
			pw.addPoints(vPyEV, vPxEV, Plot.BOX);
			pw.addPoints(vPyEV, vPxEV, Plot.X);
		}
		pw.setColor(Color.BLACK);
		pw.addPoints(vPy, vPx, Plot.CROSS);
		pw.show();
	}
	
	
	public boolean isFound(ArrayList<NodeCT> list, NodeCT node){
		for(NodeCT n: list){
			if(node.getId() == n.getId())
				return true;
		}
		return false;
	}

	public float getAttribute(NodeCT node){
		if(indexAttr == Attribute.AREA)
			return node.getArea();
		else if(indexAttr == Attribute.VOLUME)
			return (float)node.getAttributeValue(Attribute.VOLUME);
		else if(indexAttr == Attribute.HEIGHT)
			return (float)node.getAttributeValue(Attribute.HEIGHT);
		else if(indexAttr == Attribute.WIDTH)
			return (float)node.getAttributeValue(Attribute.WIDTH);
		else if(indexAttr == Attribute.ALTITUDE)
			return (float)node.getAttributeValue(Attribute.ALTITUDE);
		return -1;
	}
	

	public float getAttribute(NodeToS node){
		if(indexAttr == 0)
			return node.getArea();
		else if(indexAttr == 1)
			return (int) node.getAttributeValue(Attribute.VOLUME);
		else if(indexAttr == 2)
			return (int) node.getAttributeValue(Attribute.HEIGHT);
		else
			return (int) node.getAttributeValue(Attribute.WIDTH);
	}
}
