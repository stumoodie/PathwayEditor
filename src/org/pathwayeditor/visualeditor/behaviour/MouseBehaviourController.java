package org.pathwayeditor.visualeditor.behaviour;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.visualeditor.editingview.IShapePane;

public class MouseBehaviourController implements IMouseBehaviourController {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private IMouseStateBehaviourController currentStateController; 
	private final ISelectionStateBehaviourController selectionStateController;
	private final ICreationStateBehaviourController creationStateController;
	private final IShapeCreationOperation shapeCreationOp;

	private boolean activated;
	
	public MouseBehaviourController(IShapePane pane, IOperationFactory opFactory){
		this.selectionStateController = new SelectionStateController(pane, opFactory);
		this.shapeCreationOp = opFactory.getShapeCreationOperation();
		this.creationStateController = new ShapeCreationStateController(pane, this.shapeCreationOp);
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
		if(this.currentStateController.equals(this.shapeCreationOp)){
			this.shapeCreationOp.setShapeObjectType(shapeType);
		}
		else{
			if(this.isActivated()){
				this.currentStateController.deactivate();
			}
			this.shapeCreationOp.setShapeObjectType(shapeType);
			this.currentStateController = this.creationStateController;
			if(this.isActivated()){
				this.currentStateController.activate();
			}
		}
	}

}
