package org.pathwayeditor.visualeditor.behaviour;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class MarqueeSelectionHandleResponse extends HandleResponse {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IMarqueeOperation operation;
	private Point originDelta;
	private Dimension sizeDelta;
	
	public MarqueeSelectionHandleResponse(IMarqueeOperation resizeOperation){
		super();
		this.operation = resizeOperation;
	}
	
	private void calculateBounds(Point currentLocation){
		double originX = 0.0;
		double originY = 0.0;
		Point delta = this.calculateLocationDelta(currentLocation);
		double sizeX = delta.getX();
		double sizeY = delta.getY();
		if(delta.getX() < 0.0){
			originX = delta.getX();
			sizeX = -delta.getX();
		}
		if(delta.getY() < 0.0){
			originY = delta.getY();
			sizeY = -delta.getY();
		}
		this.originDelta = new Point(originX, originY);
		this.sizeDelta = new Dimension(sizeX, sizeY);
	}
	
	@Override
	public void dragContinuing(Point newLocation) {
		calculateBounds(newLocation);
		if(logger.isTraceEnabled()){
			logger.trace("Drag continuing. newLocation=" + newLocation + ",originDelta=" + this.originDelta + ", sizeDelta=" + this.sizeDelta);
		}
		this.operation.selectionContinuing(this.originDelta, this.sizeDelta);
	}

	@Override
	public void dragFinished() {
		this.exitDragOngoingState();
		if(logger.isTraceEnabled()){
			logger.trace("Drag finished. originDelta=" + this.originDelta + ", sizeDelta=" + this.sizeDelta);
		}
		this.operation.selectionFinished(this.originDelta, this.sizeDelta);
	}

	@Override
	public void dragStarted(ISelectionHandle selectionHandle, Point newLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(newLocation);
		calculateBounds(newLocation);
		if(logger.isTraceEnabled()){
			logger.trace("Drag started. newLocation=" + newLocation);
		}
		this.operation.selectionStarted(newLocation);
	}

	@Override
	public boolean canContinueDrag(Point newLocation) {
		return true;
	}

	@Override
	public boolean canReparent() {
		return false;
	}

	@Override
	public boolean canOperationSucceed() {
		return true;
	}

}
