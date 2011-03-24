package org.pathwayeditor.visualeditor.behaviour.linkcreation;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.HandleResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class LinkCreationDragResponse extends HandleResponse {
	private final ILinkCreationOperation linkCreationOperation;
	private final ILinkTypeInspector shapeTypeInspector;
	private Point lastDelta;
	private boolean canContinue;

	public LinkCreationDragResponse(ILinkCreationOperation linkCreationOperation, ILinkTypeInspector linkTypeInspector) {
		this.linkCreationOperation = linkCreationOperation;
		this.shapeTypeInspector = linkTypeInspector;
	}

	@Override
	public boolean canContinueDrag(Point delta) {
		return this.canContinue;
	}

	@Override
	public void dragStarted(ISelectionHandle selectionHandle, Point startLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(startLocation);
		this.lastDelta = this.calculateLocationDelta(startLocation);
		if(selectionHandle != null && selectionHandle.getType().equals(SelectionHandleType.Central) && selectionHandle.getDrawingPrimitiveController() instanceof IShapeController){
			this.linkCreationOperation.setLinkObjectType(shapeTypeInspector.getCurrentLinkType());
			this.linkCreationOperation.startCreationDrag((IShapeNode)selectionHandle.getDrawingPrimitiveController().getDrawingElement());
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

}
