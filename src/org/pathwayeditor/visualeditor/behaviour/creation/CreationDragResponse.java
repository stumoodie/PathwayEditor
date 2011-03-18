package org.pathwayeditor.visualeditor.behaviour.creation;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.HandleResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IShapeCreationOperation;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class CreationDragResponse extends HandleResponse {
	private final IShapeCreationOperation shapeCreationOperation;
	private final IShapeTypeInspector shapeTypeInspector;
	private Point lastDelta;

	public CreationDragResponse(IShapeCreationOperation shapeCreationOperation, IShapeTypeInspector shapeTypeInspector) {
		this.shapeCreationOperation = shapeCreationOperation;
		this.shapeTypeInspector = shapeTypeInspector;
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
		this.shapeCreationOperation.setShapeObjectType(shapeTypeInspector.getCurrentShapeType());
		this.shapeCreationOperation.startCreationDrag(startLocation);
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
