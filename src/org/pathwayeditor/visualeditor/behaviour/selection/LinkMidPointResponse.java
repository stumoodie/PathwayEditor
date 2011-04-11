package org.pathwayeditor.visualeditor.behaviour.selection;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.HandleResponse;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkOperation;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class LinkMidPointResponse extends HandleResponse implements ISelectionDragResponse {
	private final ILinkOperation linkOperation;
	private ISelectionHandle selectionHandle = null;
	private Point lastDelta;
	
	public LinkMidPointResponse(ILinkOperation linkOperation){
		super();
		this.linkOperation = linkOperation;
	}
	
	@Override
	public boolean canContinueDrag(Point delta) {
		return true;
	}

	@Override
	public boolean canOperationSucceed() {
		return true;
	}

	@Override
	public boolean canReparent() {
		return false;
	}

	@Override
	public void dragContinuing(Point newLocation) {
		this.lastDelta = calculateLocationDelta(newLocation);
		this.linkOperation.newBendPointOngoing(selectionHandle, this.lastDelta);
	}

	@Override
	public void dragFinished() {
		this.exitDragOngoingState();
		this.linkOperation.newBendPointFinished(selectionHandle, this.lastDelta);
	}

	@Override
	public void dragStarted(Point startLocation) {
		this.setStartLocation(startLocation);
		this.lastDelta = calculateLocationDelta(startLocation);
		this.linkOperation.newBendPointStarted(selectionHandle);
		this.enterDragOngoingState();
	}

	@Override
	public void setSelectionHandle(ISelectionHandle selectionHandle) {
		this.selectionHandle = selectionHandle;
	}

	@Override
	protected void handleAltSelection(boolean isSelected) {
	}
}
