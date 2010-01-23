package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

public class CentralHandleResponse extends HandleResponse implements IDragResponse {
	private final IEditingOperation editingOperation;
	
	public CentralHandleResponse(IEditingOperation editingOperation){
		this.editingOperation = editingOperation;
	}
	
	@Override
	public void dragContinuing(Point newLocation) {
		this.calculateLocationDelta(newLocation);
		this.editingOperation.moveOngoing(this.getDelta());
	}

	@Override
	public void dragFinished() {
		this.exitDragOngoingState();
		this.editingOperation.moveFinished(this.getDelta());
	}

	@Override
	public void dragStarted(Point newLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(newLocation);
		this.calculateLocationDelta(newLocation);
		this.editingOperation.moveStarted();
	}

	@Override
	public boolean canContinueDrag(Point delta) {
		return true;
	}
}
