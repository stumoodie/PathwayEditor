package org.pathwayeditor.visualeditor.behaviour;

import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.editingview.IDomainModelLayer;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.editingview.LayerType;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;

public class ShapeCreationStateController implements ICreationStateBehaviourController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private IShapePane shapePane;
	private final CreationMouseListener mouseClickFeedbackListener;
	private boolean activated = false;

	public ShapeCreationStateController(IShapePane pane, IShapeCreationOperation creationOp){
		this.shapePane = pane;
        this.mouseClickFeedbackListener = new CreationMouseListener(this, creationOp);
	}
	

	public Point getAdjustedMousePosition(double originalMouseX, double originalMouseY){
		Point retVal = this.shapePane.getPaneBounds().getOrigin();
		retVal = retVal.translate(originalMouseX, originalMouseY);
		if(logger.isTraceEnabled()){
			logger.trace("Adjust position. origX=" + originalMouseX + ",origY=" + originalMouseY + " : adjustedPoint=" + retVal + ", paneBounds=" + shapePane.getPaneBounds());
		}
		return retVal;  
	}
//	public Point getAdjustedMousePosition(double originalMouseX, double originalMouseY){
//		AffineTransform paneTransform = this.shapePane.getLastUsedTransform();
//		Point retVal = null;
//		if(paneTransform == null){
//			retVal = new Point(originalMouseX, originalMouseY);
//		}
//		else{
//			retVal = new Point((originalMouseX-paneTransform.getTranslateX())/paneTransform.getScaleX(), (originalMouseY-paneTransform.getTranslateY())/paneTransform.getScaleY()); 
//		}
//		return retVal;  
//	}

	@Override
	public void activate(){
        this.shapePane.addMouseListener(this.mouseClickFeedbackListener);
        this.shapePane.addMouseMotionListener(this.mouseClickFeedbackListener);

        this.activated = true;
	}

	@Override
	public void deactivate(){
        this.shapePane.removeMouseListener(this.mouseClickFeedbackListener);
        this.shapePane.removeMouseMotionListener(this.mouseClickFeedbackListener);

        this.activated = false;
	}


	public IDrawingElementController findDrawingElementAt(Point location) {
		IDomainModelLayer domainLayer = this.shapePane.getLayer(LayerType.DOMAIN);
		IIntersectionCalculator intCalc = domainLayer.getViewControllerStore().getIntersectionCalculator();
		intCalc.setFilter(null);
		SortedSet<IDrawingElementController> hits = intCalc.findDrawingPrimitivesAt(new Point(location.getX(), location.getY()));
		IDrawingElementController retVal = null;
		if(!hits.isEmpty()){
			retVal = hits.first();
			if(logger.isTraceEnabled()){
				logger.trace("Found hit at: " + retVal);
			}
		}
		return retVal;
	}

	@Override
	public boolean isActivated() {
		return this.activated ;
	}
}
