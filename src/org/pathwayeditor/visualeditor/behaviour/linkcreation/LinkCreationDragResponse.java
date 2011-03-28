package org.pathwayeditor.visualeditor.behaviour.linkcreation;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.HandleResponse;
import org.pathwayeditor.visualeditor.behaviour.creation.ILinkCreationDragResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IShapeController;

public class LinkCreationDragResponse extends HandleResponse implements ILinkCreationDragResponse {
	private final ILinkCreationOperation linkCreationOperation;
	private final ILinkTypeInspector shapeTypeInspector;
	private Point lastDelta;
	private boolean canContinue;
	private IDrawingElementController currentNode;

	public LinkCreationDragResponse(ILinkCreationOperation linkCreationOperation, ILinkTypeInspector linkTypeInspector) {
		this.linkCreationOperation = linkCreationOperation;
		this.shapeTypeInspector = linkTypeInspector;
	}

	@Override
	public boolean canContinueDrag(Point delta) {
		return this.canContinue;
	}

	
	
	@Override
	public void dragStarted(Point startLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(startLocation);
		this.lastDelta = this.calculateLocationDelta(startLocation);
		if(this.currentNode instanceof IShapeController){
			this.linkCreationOperation.setLinkObjectType(shapeTypeInspector.getCurrentLinkType());
			this.linkCreationOperation.startCreationDrag((IShapeNode)currentNode.getDrawingElement());
			this.canContinue = true;
		}
		else{
			this.canContinue = false;
		}
	}

	@Override
	public void dragContinuing(Point newLocation) {
		this.lastDelta = this.calculateLocationDelta(newLocation); 
		this.linkCreationOperation.ongoingCreationDrag(this.lastDelta);
	}

	@Override
	public void dragFinished() {
		if(this.canContinue){
			this.linkCreationOperation.finishCreationDrag(lastDelta);
		}
		this.exitDragOngoingState();
	}

	@Override
	public boolean canReparent() {
		return this.linkCreationOperation.canCreationSucceed();
	}

	@Override
	public boolean canOperationSucceed() {
		return this.linkCreationOperation.canCreationSucceed();
	}

	@Override
	public void setCurrentNode(IDrawingElementController drawingPrimitiveController) {
		this.currentNode = drawingPrimitiveController;
	}


}
