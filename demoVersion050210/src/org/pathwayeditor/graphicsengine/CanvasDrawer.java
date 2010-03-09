package org.pathwayeditor.graphicsengine;

import java.awt.Graphics2D;
import java.util.Iterator;

import org.pathwayeditor.figure.figuredefn.FigureDrawer;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.figuredefn.IGraphicsEngine;
import org.pathwayeditor.visualeditor.controller.ILabelController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;

public class CanvasDrawer implements ICanvasDrawer {
	private final IViewControllerStore viewControllerStore;
	
	
	public CanvasDrawer(IViewControllerStore viewControllerStore) {
		this.viewControllerStore = viewControllerStore;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ICanvasDrawer#getVierwControllerStore()
	 */
	public IViewControllerStore getViewControllerStore(){
		return this.viewControllerStore;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ICanvasDrawer#paint(java.awt.Graphics2D)
	 */
	public void paint(Graphics2D g2d){
		IGraphicsEngine graphicsEngine = new Java2DGraphicsEngine(g2d);
		Iterator<IShapeController> shapeIter = this.viewControllerStore.shapePrimitiveIterator();
		while(shapeIter.hasNext()){
			IShapeController shapeNode = shapeIter.next();
			IFigureController controller = shapeNode.getFigureController();
			FigureDrawer drawer = new FigureDrawer(controller.getFigureDefinition());
			drawer.drawFigure(graphicsEngine);
		}
		Iterator<ILabelController> labelIter = this.viewControllerStore.labelPrimitiveIterator();
		while(labelIter.hasNext()){
			ILabelController labelNode = labelIter.next();
			IFigureController controller = labelNode.getFigureController();
			FigureDrawer drawer = new FigureDrawer(controller.getFigureDefinition());
			drawer.drawFigure(graphicsEngine);
		}
		Iterator<ILinkController> linkEdgeIter = this.viewControllerStore.linkPrimitiveIterator();
		while(linkEdgeIter.hasNext()){
			ILinkController edge = linkEdgeIter.next();
			LinkDrawer linkDrawer = new LinkDrawer(edge);
			linkDrawer.paint(g2d);
		}
	}
}
