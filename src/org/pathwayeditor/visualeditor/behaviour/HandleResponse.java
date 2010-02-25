package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;


public abstract class HandleResponse implements IDragResponse {
	private boolean altSelected = false;
	private boolean cmdSelected = false;
	private boolean shiftSelected = false;
	private boolean isDragOngoing = false;
	private Point startLocation;
//	private Point delta; 
//	private Point lastLocation;
	
	protected HandleResponse(){
	}
	
//	protected final Point getLastLocation(){
//		return this.lastLocation;
//	}
	
	protected final Point getStartLocation(){
		return this.startLocation;
	}
	
	protected final void setStartLocation(Point startLocation){
		this.startLocation  = startLocation;
//		this.lastLocation = startLocation;
//		this.delta = Point.ORIGIN;
	}
	
	protected final Point calculateLocationDelta(Point newLocation){
		return this.startLocation.difference(newLocation);
	}
	
//	protected final Point getDelta(){
//		return this.delta;
//	}
	
	protected final boolean isAltSelected() {
		return altSelected;
	}

	protected final boolean isCmdSelected() {
		return cmdSelected;
	}

	protected final boolean isShiftSelected() {
		return shiftSelected;
	}
	
	protected final void enterDragOngoingState(){
		this.isDragOngoing = true;
	}
	
	protected final void exitDragOngoingState(){
		this.isDragOngoing = false;
	}
	
	@Override
	public final boolean isDragOngoing(){
		return this.isDragOngoing;
	}

	@Override
	public final void altSelected(boolean isSelected) {
		this.altSelected = isSelected;
	}

	@Override
	public final void cmdSelected(boolean isSelected) {
		this.cmdSelected = isSelected;
	}

	@Override
	public final void shiftSelected(boolean isSelected) {
		this.shiftSelected = isSelected;
	}
}
