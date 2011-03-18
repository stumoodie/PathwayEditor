package org.pathwayeditor.visualeditor.behaviour.selection;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.HandleResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.IResizeOperation;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

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
		Point delta = this.calculateLocationDelta(newLocation);
		this.newPositionCalculator.calculateDeltas(delta);
		if(logger.isTraceEnabled()){
			logger.trace("Drag continuing. newLocation=" + newLocation + ",delta=" + delta + ",originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.operation.resizeContinuing(newPositionCalculator.getResizedOrigin(), newPositionCalculator.getResizedDelta());
	}

	@Override
	public void dragFinished() {
		this.exitDragOngoingState();
		if(logger.isTraceEnabled()){
			logger.trace("Drag finished. originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.operation.resizeFinished(newPositionCalculator.getResizedOrigin(), newPositionCalculator.getResizedDelta());
	}

	@Override
	public void dragStarted(ISelectionHandle selectionHandle, Point newLocation) {
		this.enterDragOngoingState();
		this.setStartLocation(newLocation);
		Point delta = calculateLocationDelta(newLocation);
		this.newPositionCalculator.calculateDeltas(delta);
		if(logger.isTraceEnabled()){
			logger.trace("Drag started. newLocation=" + newLocation + ",delta=" + delta + ",originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.operation.resizeStarted();
	}

	@Override
	public boolean canContinueDrag(Point newLocation) {
		// The problem is we don;t want to change the state of the class here. So we must undo
		// these changes before the method returns.
		Point lastDelta = this.newPositionCalculator.getLastDelta(); 
		Point delta = this.calculateLocationDelta(newLocation);
		this.newPositionCalculator.calculateDeltas(delta);
		boolean retVal = this.operation.canResize(newPositionCalculator.getResizedOrigin(), newPositionCalculator.getResizedDelta());
		if(logger.isTraceEnabled()){
			logger.trace("Can continue drag? retVal=" + retVal + ",newLocation=" + newLocation + ",delta=" + delta + ",originDelta="
					+ newPositionCalculator.getResizedOrigin() + ",resizeDelta=" + this.newPositionCalculator.getResizedDelta());
		}
		this.newPositionCalculator.calculateDeltas(lastDelta);
		return retVal;
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
