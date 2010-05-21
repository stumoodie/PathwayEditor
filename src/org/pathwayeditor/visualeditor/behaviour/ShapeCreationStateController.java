package org.pathwayeditor.visualeditor.behaviour;

import java.awt.geom.AffineTransform;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
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
		AffineTransform paneTransform = this.shapePane.getLastUsedTransform();
		Point retVal = null;
		if(paneTransform == null){
			retVal = new Point(originalMouseX, originalMouseY);
		}
		else{
			retVal = new Point((originalMouseX-paneTransform.getTranslateX())/paneTransform.getScaleX(), (originalMouseY-paneTransform.getTranslateY())/paneTransform.getScaleY()); 
		}
		return retVal;  
	}

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


	public IDrawingPrimitiveController findDrawingElementAt(Point location) {
		IDomainModelLayer domainLayer = this.shapePane.getLayer(LayerType.DOMAIN);
		IIntersectionCalculator intCalc = domainLayer.getViewControllerStore().getIntersectionCalculator();
		intCalc.setFilter(null);
		SortedSet<IDrawingPrimitiveController> hits = intCalc.findDrawingPrimitivesAt(new Point(location.getX(), location.getY()));
		IDrawingPrimitiveController retVal = null;
		if(!hits.isEmpty()){
			retVal = hits.first();
			logger.info("Found hit at: " + retVal);
		}
		return retVal;
	}

	@Override
	public boolean isActivated() {
		return this.activated ;
	}
}
