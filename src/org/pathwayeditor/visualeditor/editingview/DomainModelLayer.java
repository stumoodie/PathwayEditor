package org.pathwayeditor.visualeditor.editingview;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.rendering.FigureRenderer;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.figure.rendering.IGraphicsEngine;
import org.pathwayeditor.graphicsengine.Java2DGraphicsEngine;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILabelController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.IRootController;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;

public class DomainModelLayer implements IDomainModelLayer {
	private final IViewControllerModel viewControllerStore;
	private final Logger logger = Logger.getLogger(this.getClass());
	
	
	public DomainModelLayer(IViewControllerModel viewControllerStore) {
		this.viewControllerStore = viewControllerStore;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ICanvasDrawer#getVierwControllerStore()
	 */
	@Override
	public IViewControllerModel getViewControllerStore(){
		return this.viewControllerStore;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ICanvasDrawer#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D g2d){
		IGraphicsEngine graphicsEngine = new Java2DGraphicsEngine(g2d);
		Rectangle rectangleBounds = g2d.getClipBounds();
		Envelope updateBound =  new Envelope(rectangleBounds.getX(),rectangleBounds.getY(), rectangleBounds.getWidth(), rectangleBounds.getHeight());;
		if(logger.isDebugEnabled()){
			logger.debug("Refreshing the clipped bounds=" + rectangleBounds + ",modelBounds=" + updateBound);
		}
//		IIntersectionCalculator intersectionCalculator = this.viewControllerStore.getIntersectionCalculator();
//		intersectionCalculator.setComparator(new Comparator<IDrawingPrimitiveController>() {
//			
//			@Override
//			public int compare(IDrawingPrimitiveController o1, IDrawingPrimitiveController o2) {
//				int o1Level = o1.getDrawingElement().getCurrentDrawingElement().getLevel();
//				int o2Level = o2.getDrawingElement().getCurrentDrawingElement().getLevel();
//				int retVal = (o1Level < o2Level) ? -1 : (o1Level > o2Level ? 1 : 0);
//				if(retVal == 0){
//					long o1Idx = o1.getDrawingElement().getCurrentDrawingElement().getUniqueIndex();
//					long o2Idx = o2.getDrawingElement().getCurrentDrawingElement().getUniqueIndex();
//					retVal = o1Idx < o2Idx ? -1 : (o1Idx > o2Idx ? 1 : 0); 
//				}
//				return retVal;
//			}
//		});
//		SortedSet<IDrawingPrimitiveController> controllers = intersectionCalculator.findIntersectingControllerBounds(updateBound);
		SortedSet<IDrawingElementController> controllers = new TreeSet<IDrawingElementController>();
//		SortedSet<IDrawingElementController> controllers = new TreeSet<IDrawingElementController>(new Comparator<IDrawingElementController>() {
//			
//			@Override
//			public int compare(IDrawingElementController o1, IDrawingElementController o2) {
//				int o1Level = o1.getDrawingElement().getLevel();
//				int o2Level = o2.getDrawingElement().getLevel();
//				int retVal = (o1Level < o2Level) ? -1 : (o1Level > o2Level ? 1 : 0);
//				if(retVal == 0){
//					long o1Idx = o1.getDrawingElement().getUniqueIndex();
//					long o2Idx = o2.getDrawingElement().getUniqueIndex();
//					retVal = o1Idx < o2Idx ? -1 : (o1Idx > o2Idx ? 1 : 0); 
//				}
//				return retVal;
//			}
//		});
		Iterator<IDrawingElementController> contIter = this.viewControllerStore.drawingPrimitiveIterator();
		while(contIter.hasNext()){
			IDrawingElementController primCont = contIter.next();
			if(!(primCont instanceof IRootController) && primCont.getDrawnBounds().intersects(updateBound)){
				if(logger.isTraceEnabled()){
					logger.trace("Found intersecting primitive=" + primCont);
				}
				controllers.add(primCont);
			}
		}
		for(IDrawingElementController controller : controllers){
			if(logger.isTraceEnabled()){
				logger.trace("Refreshing controller=" + controller + ", bounds=" + controller.getDrawnBounds());
			}
			if(controller instanceof IShapeController){
				IShapeController shapeNode = (IShapeController)controller;
				IFigureRenderingController figController = shapeNode.getFigureController();
				if(logger.isTraceEnabled()){
					logger.trace("Refreshing node=" + shapeNode + " at bounds=" + shapeNode.getDrawnBounds());
				}
				FigureRenderer drawer = new FigureRenderer(figController.getFigureDefinition());
				drawer.drawFigure(graphicsEngine);
			}
			else if(controller instanceof ILabelController){
				ILabelController labelNode = (ILabelController)controller;
				if(logger.isTraceEnabled()){
					logger.trace("Refreshing node=" + labelNode + " at bounds=" + labelNode.getDrawnBounds());
				}
				IFigureRenderingController figController = labelNode.getFigureController();
				FigureRenderer drawer = new FigureRenderer(figController.getFigureDefinition());
				drawer.drawFigure(graphicsEngine);
			}
			else if(controller instanceof ILinkController){
				ILinkController edge = (ILinkController)controller;
				if(logger.isTraceEnabled()){
					logger.trace("Refreshng edge=" + edge + " at bounds=" + edge.getDrawnBounds());
				}
				LinkDrawer linkDrawer = new LinkDrawer(edge.getLinkDefinition());
				linkDrawer.paint(g2d);
			}
		}
//		intersectionCalculator.setComparator(null);
	}

	@Override
	public LayerType getLayerType() {
		return LayerType.DOMAIN;
	}

}
