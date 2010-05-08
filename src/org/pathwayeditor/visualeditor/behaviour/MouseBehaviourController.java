package org.pathwayeditor.visualeditor.behaviour;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.visualeditor.editingview.IShapePane;

public class MouseBehaviourController implements IMouseBehaviourController {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private IMouseStateBehaviourController currentStateController; 
	private final IMouseStateBehaviourController selectionStateController;
	private IShapePane shapePane;

	private boolean activated;
	
	public MouseBehaviourController(IShapePane pane, IOperationFactory opFactory){
		this.shapePane = pane;
		this.selectionStateController = new SelectionStateController(pane, opFactory);
		this.currentStateController = this.selectionStateController;
	}

	@Override
	public void activate() {
		this.currentStateController.activate();
		this.activated = true;
	}

	@Override
	public boolean isActivated(){
		return this.activated;
	}
	
	@Override
	public void deactivate() {
		this.currentStateController.deactivate();
		this.activated = false;
	}

//	@Override
//	public IDrawingPrimitiveController findDrawingElementAt(Point location) {
//		return this.currentStateController.findDrawingElementAt(location);
//	}
//
//	@Override
//	public Point getAdjustedMousePosition(double x, double y) {
//		return this.currentStateController.getAdjustedMousePosition(x, y);
//	}
//
//	@Override
//	public IDragResponse getDragResponse(SelectionHandleType type) {
//		return this.getDragResponse(type);
//	}
//
//	@Override
//	public IMouseFeedbackResponse getMouseFeedbackResponse(SelectionHandleType type) {
//		return this.currentStateController.getMouseFeedbackResponse(type);
//	}
//
//	@Override
//	public ISelectionRecord getSelectionRecord() {
//		return this.currentStateController.getSelectionRecord();
//	}

	@Override
	public void setLinkCreationMode(ILinkObjectType linkType) {
		if(logger.isDebugEnabled()){
			logger.debug("In link creation mode for object type: " + linkType.getName());
		}
	}

	@Override
	public void setSelectionMode() {
		if(this.currentStateController != this.selectionStateController){
			if(this.isActivated()){
				this.currentStateController.deactivate();
			}
			this.currentStateController = this.selectionStateController;
			if(this.isActivated()){
				this.currentStateController.activate();
			}
			logger.debug("Setting selection controller state");
		}
	}

	@Override
	public void setShapeCreationMode(IShapeObjectType shapeType) {
		if(logger.isDebugEnabled()){
			logger.debug("In link creation mode for object type: " + shapeType.getName());
		}
	}

	@Override
	public void updateView() {
		this.shapePane.updateView();
//		this.currentStateController.updateView();
	}
	

}
