package mmlib4j.imagej.guj;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.graph.Edge;
import mmlib4j.utils.ImageBuilder;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.Caching;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class VisualizationViewerImage<T> extends VisualizationViewer<T, Edge<T>>{
	
	private static final long serialVersionUID = 1L;
	BufferedImage img;
	
	public VisualizationViewerImage(Layout<T, Edge<T>> layout) {
		super(layout);
	}
	
	public void setImage(GrayScaleImage img){
		this.img = ImageBuilder.convertToImage(img);
		
	}
	
	protected void renderGraph(Graphics2D g2d) {
	    if(renderContext.getGraphicsContext() == null) {
	        renderContext.setGraphicsContext(new GraphicsDecorator(g2d));
        } else {
        	renderContext.getGraphicsContext().setDelegate(g2d);
        }
        renderContext.setScreenDevice(this);
	    Layout<T, Edge<T>> layout = model.getGraphLayout();

		g2d.setRenderingHints(renderingHints);
		g2d.drawImage(img, null, 0, 0);
		
		AffineTransform oldXform = g2d.getTransform();
        AffineTransform newXform = new AffineTransform(oldXform);
        newXform.concatenate(renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW).getTransform());
        g2d.setTransform(newXform);


		for(Paintable paintable : preRenderers) {
		    if(paintable.useTransform()) {
		        paintable.paint(g2d);
		    } else {
		        g2d.setTransform(oldXform);
		        paintable.paint(g2d);
                g2d.setTransform(newXform);
		    }
		}
		
        if(layout instanceof Caching) {
        	((Caching)layout).clear();
        }
        renderer.render(renderContext, layout);
        
		for(Paintable paintable : postRenderers) {
		    if(paintable.useTransform()) {
		        paintable.paint(g2d);
		    } else {
		        g2d.setTransform(oldXform);
		        paintable.paint(g2d);
                g2d.setTransform(newXform);
		    }
		}
		g2d.setTransform(oldXform);
	}
	
}