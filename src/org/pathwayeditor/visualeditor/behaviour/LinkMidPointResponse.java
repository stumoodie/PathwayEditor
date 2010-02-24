package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class LinkMidPointResponse extends HandleResponse {
	private final ILinkOperation linkOperation;
	private ISelectionHandle selectionHandle = null;
	private Point lastLocation;
	
	public LinkMidPointResponse(ILinkOperation linkOperation){
		super();
		this.linkOperation = linkOperation;
	}
	
	@Override
	public boolean canContinueDrag(Point delta) {
		return true;
	}

	@Override
	public boolean canMove() {
		return true;
	}

	@Override
	public boolean canReparent() {
		return false;
	}

	@Override
	public void dragContinuing(Point newLocation) {
		this.linkOperation.newBendPointOngoing(selectionHandle, newLocation);
		this.lastLocation = newLocation;
	}

	@Override
	public void dragFinished() {
		this.exitDragOngoingState();
		this.linkOperation.newBendPointFinished(selectionHandle, this.lastLocation);
	}

	@Override
	public void dragStarted(ISelectionHandle selectionHandle, Point startLocation) {
		this.lastLocation = startLocation;
		this.selectionHandle = selectionHandle;
		this.linkOperation.newBendPointStarted(selectionHandle);
		this.enterDragOngoingState();
	}
}
