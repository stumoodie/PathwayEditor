package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;

public class NorthEastHandleResponse implements IDragResponse {
	private final IResizeOperation operation;
	
	public NorthEastHandleResponse(IResizeOperation operation){
		this.operation = operation;
	}
	
	@Override
	public void dragContinuing(Point delta) {
		Point originDelta = new Point(0, delta.getY());
		Dimension resizeDelta = new Dimension(delta.getX(), -delta.getY());
		this.operation.resizeContinuing(originDelta, resizeDelta);
	}

	@Override
	public void dragFinished(Point delta) {
		Point originDelta = new Point(0, delta.getY());
		Dimension resizeDelta = new Dimension(delta.getX(), -delta.getY());
		this.operation.resizeFinished(originDelta, resizeDelta);
	}

	@Override
	public void dragStarted() {
		this.operation.resizeStarted();
	}

}
