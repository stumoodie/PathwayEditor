package org.pathwayeditor.visualeditor.behaviour;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;

public class ResizeHandleResponse extends HandleResponse {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final INewPositionCalculator newPositionCalculator;
	private final IResizeOperation operation;
	
	public ResizeHandleResponse(INewPositionCalculator newPositionCalculator, IResizeOperation resizeOperation){
		super();
		this.newPositionCalculator = newPositionCalculator;
		this.operation = resizeOperation;
	}
	
	@Override
	public void dragContinuing(Point newLocation) {
		this.calculateLocationDelta(newLocation);
		this.newPositionCalculator.calculateDeltas(this.getDelta());
		if(logger.isTraceEnabled()){
			logger.trace("Drag continuing. newLocation=" + newLocation + ",delta=" + this.getDelta() + ",originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.operation.resizeContinuing(newPositionCalculator.getResizedOrigin(), newPositionCalculator.getResizedDelta());
	}

	@Override
	public void dragFinished() {
		this.exitDragOngoingState();
		if(logger.isTraceEnabled()){
			logger.trace("Drag finished. delta=" + this.getDelta() + ",originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.operation.resizeFinished(newPositionCalculator.getResizedOrigin(), newPositionCalculator.getResizedDelta());
	}

	@Override
	public void dragStarted(Point newLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(newLocation);
		calculateLocationDelta(newLocation);
		this.newPositionCalculator.calculateDeltas(getDelta());
		if(logger.isTraceEnabled()){
			logger.trace("Drag started. newLocation=" + newLocation + ",delta=" + this.getDelta() + ",originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.operation.resizeStarted();
	}

	@Override
	public boolean canContinueDrag(Point newLocation) {
		// The problem is we don;t want to change the state of the class here. So we must undo
		// these changes before the method returns.
		Point originalLocation = this.getLastLocation();
		this.calculateLocationDelta(newLocation);
		this.newPositionCalculator.calculateDeltas(this.getDelta());
		boolean retVal = this.operation.canResize(newPositionCalculator.getResizedOrigin(), newPositionCalculator.getResizedDelta());
		if(logger.isTraceEnabled()){
			logger.trace("Can continue drag? retVal=" + retVal + ",newLocation=" + newLocation + ",delta=" + this.getDelta() + ",originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.calculateLocationDelta(originalLocation);
		this.newPositionCalculator.calculateDeltas(this.getDelta());
		return retVal;
	}
}
