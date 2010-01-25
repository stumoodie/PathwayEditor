package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IEditingOperation.ReparentingStateType;

public class CentralHandleResponse extends HandleResponse implements IMoveResponse {
	private final IEditingOperation editingOperation;
	private ReparentingStateType reparentingState = ReparentingStateType.FORBIDDEN;
	
	public CentralHandleResponse(IEditingOperation editingOperation){
		this.editingOperation = editingOperation;
	}
	
	@Override
	public void dragContinuing(Point newLocation) {
		this.calculateLocationDelta(newLocation);
		this.editingOperation.moveOngoing(this.getDelta());
		this.reparentingState = this.editingOperation.getReparentingState();
	}

	@Override
	public void dragFinished() {
		this.exitDragOngoingState();
		this.editingOperation.moveFinished(this.getDelta(), reparentingState);
	}

	@Override
	public void dragStarted(Point newLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(newLocation);
		this.calculateLocationDelta(newLocation);
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
	public boolean canMove() {
		return this.reparentingState.equals(ReparentingStateType.CAN_MOVE);
	}

}
