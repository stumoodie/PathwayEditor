package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IEditingOperation.ReparentingStateType;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class CentralHandleResponse extends HandleResponse {
	private final IEditingOperation editingOperation;
	private ReparentingStateType reparentingState = ReparentingStateType.FORBIDDEN;
	private Point lastDelta;
	
	public CentralHandleResponse(IEditingOperation editingOperation){
		this.editingOperation = editingOperation;
	}
	
	@Override
	public void dragContinuing(Point newLocation) {
		this.lastDelta = this.calculateLocationDelta(newLocation);
		this.editingOperation.moveOngoing(this.lastDelta);
		this.reparentingState = this.editingOperation.getReparentingState(this.lastDelta);
	}

	@Override
	public void dragFinished() {
		this.exitDragOngoingState();
		this.editingOperation.moveFinished(this.lastDelta, reparentingState);
	}

	@Override
	public void dragStarted(ISelectionHandle selectionHandle, Point newLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(newLocation);
		this.lastDelta = this.calculateLocationDelta(newLocation);
		this.reparentingState = ReparentingStateType.FORBIDDEN;
		this.editingOperation.moveStarted();
	}

	@Override
	public boolean canContinueDrag(Point delta) {
		return true;
	}

	@Override
	public boolean canReparent() {
		return this.reparentingState.equals(ReparentingStateType.CAN_REPARENT);
	}

	@Override
	public boolean canOperationSucceed() {
		return this.reparentingState.equals(ReparentingStateType.CAN_MOVE);
	}

}
