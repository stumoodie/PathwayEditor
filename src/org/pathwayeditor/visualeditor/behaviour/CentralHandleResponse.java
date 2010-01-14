package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;

public class CentralHandleResponse implements IDragResponse {
	private final IEditingOperation editingOperation;
	
	public CentralHandleResponse(IEditingOperation editingOperation){
		this.editingOperation = editingOperation;
	}
	
	@Override
	public void dragContinuing(Point delta) {
		this.editingOperation.moveOngoing(delta);
	}

	@Override
	public void dragFinished(Point delta) {
		this.editingOperation.moveFinished(delta);
	}

	@Override
	public void dragStarted() {
		this.editingOperation.moveStarted();
	}

}
