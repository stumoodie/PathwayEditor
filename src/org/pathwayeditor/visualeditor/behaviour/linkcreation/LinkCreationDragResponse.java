package org.pathwayeditor.visualeditor.behaviour.linkcreation;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.HandleResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class LinkCreationDragResponse extends HandleResponse {
	private final ILinkCreationOperation shapeCreationOperation;
	private final ILinkTypeInspector shapeTypeInspector;
	private Point lastDelta;

	public LinkCreationDragResponse(ILinkCreationOperation linkCreationOperation, ILinkTypeInspector linkTypeInspector) {
		this.shapeCreationOperation = linkCreationOperation;
		this.shapeTypeInspector = linkTypeInspector;
	}

	@Override
	public boolean canContinueDrag(Point delta) {
		return true;
	}

	@Override
	public void dragStarted(ISelectionHandle selectionHandle, Point startLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(startLocation);
		this.lastDelta = this.calculateLocationDelta(startLocation);
		this.shapeCreationOperation.setLinkObjectType(shapeTypeInspector.getCurrentLinkType());
//		this.shapeCreationOperation.startCreationDrag(startLocation);
	}

	@Override
	public void dragContinuing(Point newLocation) {
		this.lastDelta = this.calculateLocationDelta(newLocation); 
		this.shapeCreationOperation.ongoingCreationDrag(this.lastDelta);
	}

	@Override
	public void dragFinished() {
		this.shapeCreationOperation.finishCreationDrag(lastDelta);
		this.exitDragOngoingState();
	}

	@Override
	public boolean canReparent() {
		return this.shapeCreationOperation.canCreationSucceed();
	}

	@Override
	public boolean canOperationSucceed() {
		return this.shapeCreationOperation.canCreationSucceed();
	}

}
