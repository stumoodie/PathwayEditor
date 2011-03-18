package org.pathwayeditor.visualeditor.behaviour;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.editingview.IShapePane;

public class ShapeCreationStateController implements ICreationStateBehaviourController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
	private final CreationMouseListener mouseClickFeedbackListener;
	private boolean activated = false;

	public ShapeCreationStateController(IShapePane pane, IShapeCreationOperation creationOp){
		this.shapePane = pane;
        this.mouseClickFeedbackListener = new CreationMouseListener(this, creationOp);
	}
	

	@Override
	public Point getAdjustedMousePosition(double originalMouseX, double originalMouseY){
		Point retVal = this.shapePane.getPaneBounds().getOrigin();
		retVal = retVal.translate(originalMouseX, originalMouseY);
		if(logger.isTraceEnabled()){
			logger.trace("Adjust position. origX=" + originalMouseX + ",origY=" + originalMouseY + " : adjustedPoint=" + retVal + ", paneBounds=" + shapePane.getPaneBounds());
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


	@Override
	public boolean isActivated() {
		return this.activated ;
	}
}
